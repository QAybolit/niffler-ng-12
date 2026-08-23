package guru.qa.niffler.jupiter.extension;

import guru.qa.niffler.jupiter.annotation.Category;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.service.SpendClient;
import guru.qa.niffler.service.SpendDbClient;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static guru.qa.niffler.jupiter.extension.TestMethodContextExtension.context;
import static guru.qa.niffler.utils.RandomDataUtils.randomCategoryName;

public class CategoryExtension implements
        BeforeEachCallback,
        AfterTestExecutionCallback,
        ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(CategoryExtension.class);

    private final SpendClient spendClient = new SpendDbClient();

    @Override
    public void beforeEach(ExtensionContext context) {
        AnnotationSupport.findAnnotation(
                context.getRequiredTestMethod(),
                User.class
        ).ifPresent(userAnno -> {
                    if (userAnno.categories().length > 0) {
                        Optional<UserJson> createdUser = UserExtension.createdUser();
                        final String username = createdUser.isPresent()
                                ? createdUser.get().username()
                                : userAnno.username();

                        List<CategoryJson> createdCategories = new ArrayList<>();
                        for (Category categoryAnno : userAnno.categories()) {
                            CategoryJson newCategory = spendClient.createCategory(
                                    new CategoryJson(
                                            null,
                                            randomCategoryName(),
                                            username,
                                            categoryAnno.archived()
                                    )
                            );
                            createdCategories.add(newCategory);
                        }

                        if (createdUser.isPresent()) {
                            createdUser.get().testData().categories().addAll(createdCategories);
                        } else {
                            context.getStore(NAMESPACE).put(
                                    context.getUniqueId(),
                                    createdCategories.toArray(CategoryJson[]::new)
                            );
                        }
                    }
                }
        );
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws
            ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(CategoryJson[].class);
    }

    @Override
    public CategoryJson[] resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws
            ParameterResolutionException {
        return createdCategory().orElseThrow();
    }

    public static Optional<CategoryJson[]> createdCategory() {
        final ExtensionContext methodContext = context();
        return Optional.ofNullable(methodContext.getStore(NAMESPACE)
                .get(methodContext.getUniqueId(), CategoryJson[].class));
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        Optional<UserJson> createdUser = UserExtension.createdUser();

        List<CategoryJson> categories;

        if (createdUser.isPresent()) {
            categories = createdUser.get().testData().categories();
        } else {
            Optional<CategoryJson[]> categoriesArray = createdCategory();
            categories = categoriesArray.map(Arrays::asList).orElse(Collections.emptyList());
        }

        for (CategoryJson category : categories) {
            spendClient.deleteCategory(String.valueOf(category.id()));
        }
    }
}
