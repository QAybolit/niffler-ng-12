package guru.qa.niffler.jupiter.extension;

import guru.qa.niffler.jupiter.annotation.Spending;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.service.SpendClient;
import guru.qa.niffler.service.SpendDbClient;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static guru.qa.niffler.jupiter.extension.TestMethodContextExtension.context;

public class SpendingExtension implements BeforeEachCallback, ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(SpendingExtension.class);

    private final SpendClient spendClient = new SpendDbClient();

    @Override
    public void beforeEach(ExtensionContext context) {
        AnnotationSupport.findAnnotation(
                context.getRequiredTestMethod(),
                User.class
        ).ifPresent(
                userAnno -> {
                    SpendJson created = null;
                    if (userAnno.spendings().length > 0) {
                        Optional<UserJson> createdUser = UserExtension.createdUser();
                        final String username = createdUser.isPresent()
                                ? createdUser.get().username()
                                : userAnno.username();

                        List<SpendJson> createdSpendings = new ArrayList<>();
                        for (Spending spendingAnno : userAnno.spendings()) {
                            SpendJson newSpending = spendClient.createSpend(
                                    new SpendJson(
                                            null,
                                            new Date(),
                                            new CategoryJson(
                                                    null,
                                                    spendingAnno.category(),
                                                    userAnno.username(),
                                                    userAnno.categories()[0].archived()
                                            ),
                                            spendingAnno.currency(),
                                            spendingAnno.amount(),
                                            spendingAnno.description()[0],
                                            userAnno.username()
                                    )
                            );
                            createdSpendings.add(newSpending);
                        }
                        if (createdUser.isPresent()) {
                            createdUser.get().testData().spendings().addAll(createdSpendings);
                        } else {
                            context.getStore(NAMESPACE).put(
                                    context.getUniqueId(),
                                    createdSpendings.toArray(SpendJson[]::new)
                            );
                        }
                    }
                }
        );
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(SpendJson[].class);
    }

    @Override
    public SpendJson[] resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return createdSpending().orElseThrow();
    }

    public static Optional<SpendJson[]> createdSpending() {
        final ExtensionContext methodContext = context();
        return Optional.ofNullable(methodContext.getStore(NAMESPACE)
                .get(methodContext.getUniqueId(), SpendJson[].class));
    }
}
