package guru.qa.niffler.test;

import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.service.SpendClient;
import guru.qa.niffler.service.SpendDbClient;
import guru.qa.niffler.service.UserClient;
import guru.qa.niffler.service.UserDbClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class HibernateTest {

    // =========== UserDbClient ===========

    @ValueSource(strings = {
            "Valentin-88",
            "Valentin-89"
    })
    @ParameterizedTest
    public void createUserTest(String username) {
        UserClient userDbClient = new UserDbClient();
        UserJson user = userDbClient.createUser(
                username,
                "12345"
        );
        userDbClient.createIncomeInvitation(user, 1);
        userDbClient.createOutcomeInvitation(user, 1);
    }

    @Test
    public void findUserByIdTest() {
        UserClient userDbClient = new UserDbClient();
        Optional<UserJson> user = userDbClient.findUserById("49b2ace9-2550-4b43-a8d7-4f027f66b9eb");
        Assertions.assertTrue(user.isPresent());
    }

    @Test
    public void findUserByUsernameTest() {
        UserClient userDbClient = new UserDbClient();
        Optional<UserJson> user = userDbClient.findUserByUsername("Valentin-1");
        Assertions.assertTrue(user.isPresent());
    }

    @Test
    public void createFriendsTest() {
        UserClient userDbClient = new UserDbClient();
        UserJson user = userDbClient.createUser(
                "Valentin-144",
                "12345"
        );
        userDbClient.createFriends(user, 1);
    }

    @Test
    public void findAllUsersTest() {
        UserClient userDbClient = new UserDbClient();
        List<UserJson> users = userDbClient.findAllUsers();
        System.out.println(users);
    }

    @Test
    public void deleteUserTest() {
        UserClient userDbClient = new UserDbClient();
        Optional<UserJson> user = userDbClient.findUserByUsername("9159c9c9-e6b3-4754-a766-e131ca95bd28");
        if (user.isPresent()) {
            userDbClient.deleteUser(user.get());
        }
    }

    // =========== SpendDbClient ===========

    @Test
    public void createSpendTest() {
        SpendClient spendDbClient = new SpendDbClient();
        SpendJson spend = spendDbClient.createSpend(
                new SpendJson(
                        null,
                        new Date(),
                        new CategoryJson(
                                null,
                                "test-1243",
                                "Dina",
                                false
                        ),
                        CurrencyValues.RUB,
                        11000.0,
                        "test description 14322",
                        "Dina"
                )
        );
        System.out.println("SPEND ===> " + spend.toString());
    }

    @Test
    public void updateSpendTest() {
        SpendClient spendDbClient = new SpendDbClient();
        SpendJson spend = spendDbClient.updateSpend(
                new SpendJson(
                        null,
                        new Date(),
                        new CategoryJson(
                                null,
                                "test-142",
                                "Dina",
                                false
                        ),
                        CurrencyValues.RUB,
                        2000.0,
                        "test description 211",
                        "Dina"
                )
        );
        System.out.println("SPEND ===> " + spend.toString());
    }

    @Test
    public void findSpendByIdTest() {
        SpendClient spendDbClient = new SpendDbClient();
        Optional<SpendJson> spend = spendDbClient.findSpendById("e8bf3d8e-8e73-11f1-8c49-0242ac110002");
        Assertions.assertTrue(spend.isPresent());
        System.out.println("SPEND ===> " + spend.toString());
    }

    @Test
    public void findSpendByUsernameAndDescriptionTest() {
        SpendClient spendDbClient = new SpendDbClient();
        Optional<SpendJson> spend = spendDbClient.findSpendByUsernameAndSpendDescription("Dina", "Покупки в Авиапарке");
        Assertions.assertTrue(spend.isPresent());
    }

    @Test
    public void deleteSpendTest() {
        SpendClient spendDbClient = new SpendDbClient();
        spendDbClient.deleteSpend("484fc3fe-98dd-11f1-98df-0242ac110002");
    }

    @Test
    public void createCategoryTest() {
        SpendClient spendDbClient = new SpendDbClient();
        CategoryJson categoryJson = spendDbClient.createCategory(
                new CategoryJson(
                        null,
                        "category-3422",
                        "Dina",
                        false
                )
        );
        System.out.println("CATEGORY JSON ===> " + categoryJson.toString());
    }

    @Test
    public void findCategoryByIdTest() {
        SpendClient spendDbClient = new SpendDbClient();
        Optional<CategoryJson> categoryJson = spendDbClient.findCategoryById("60666d5e-6d58-11f1-8d79-0242ac110004");
        Assertions.assertTrue(categoryJson.isPresent());
    }

    @Test
    public void findCategoryByNameAndUsernameTest() {
        SpendClient spendDbClient = new SpendDbClient();
        Optional<CategoryJson> categoryJson = spendDbClient.findCategoryByNameAndUsername("category-test-4", "Dina");
        Assertions.assertTrue(categoryJson.isPresent());
    }

    @Test
    public void deleteCategoryTest() {
        SpendClient spendDbClient = new SpendDbClient();
        spendDbClient.deleteCategory("b46612f4-8e7a-11f1-a837-0242ac110002");
    }
}
