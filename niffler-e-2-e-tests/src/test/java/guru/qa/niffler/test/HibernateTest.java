package guru.qa.niffler.test;

import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.service.UserDbClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class HibernateTest {

    @ValueSource(strings = {
            "Valentin-12"
    })
    @ParameterizedTest
    public void createUserTest(String username) {
        UserDbClient userDbClient = new UserDbClient();
        UserJson user = userDbClient.createUserHibernate(
                username,
                "12345"
        );
        userDbClient.addOutcomeInvitation(user, 1);
        userDbClient.addIncomeInvitation(user, 1);
    }
}
