package guru.qa.niffler.test;

import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.service.SpendDbClient;
import guru.qa.niffler.service.UserDbClient;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class JdbcTest {

    @Test
    public void createSpendTest() {
        System.out.println("Start createSpendTest");
        SpendDbClient spendDbClient = new SpendDbClient();
        SpendJson spendJson = spendDbClient.createSpend(
                new SpendJson(
                        null,
                        new Date(),
                        new CategoryJson(
                                null,
                                "test-name-1345",
                                "Dina",
                                false
                        ),
                        CurrencyValues.RUB,
                        1000.0,
                        "test description",
                        "Dina"
                )
        );
        assertEquals(1000.0, spendJson.amount());
        assertEquals("test description", spendJson.description());
        assertEquals("test-name-1345", spendJson.category().name());
    }

    @Test
    public void failSpendCreationTest() {
        System.out.println("Start failSpendCreationTest");
        SpendDbClient spendDbClient = new SpendDbClient();
        try {
            SpendJson spendJson = spendDbClient.createSpend(
                    new SpendJson(
                            null,
                            new Date(),
                            new CategoryJson(
                                    null,
                                    "test-name-12",
                                    "Dina",
                                    false
                            ),
                            CurrencyValues.RUB,
                            1000.0,
                            "test description",
                            null
                    )
            );

        } catch (RuntimeException e) {
            System.out.println("SPEND ERROR ===> " + e.getMessage());

            Optional<CategoryJson> category = spendDbClient.findCategoryByNameAndUsername("test-name-12", "Dina");
            assertFalse(category.isPresent(), "Category with name 'test-name-12' exists in DB");
        }
    }

    @Test
    public void createCategoryTest() {
        System.out.println("Start createCategoryTest");
        SpendDbClient spendDbClient = new SpendDbClient();
        CategoryJson categoryJson = spendDbClient.createCategory(
                new CategoryJson(
                        null,
                        "category-test-4",
                        "Dina",
                        false
                )
        );
        System.out.println("CATEGORY JSON ===> " + categoryJson);
    }

    @Test
    public void findAllCategoriesByUsernameTest() {
        System.out.println("Start findAllCategoriesByUsernameTest");
        SpendDbClient spendDbClient = new SpendDbClient();
        List<CategoryJson> list = spendDbClient.allCategory("Dina");
        System.out.println("CATEGORY LIST ===> " + list);
    }

    @Test
    public void deleteCategoryTest() {
        System.out.println("Start deleteCategoryTest");
        SpendDbClient spendDbClient = new SpendDbClient();
        spendDbClient.deleteCategory("65f5a29e-6d58-11f1-95bf-0242ac110004");
    }

    @Test
    public void updateCategoryTest() {
        System.out.println("Start updateCategoryTest");
        SpendDbClient spendDbClient = new SpendDbClient();
        spendDbClient.updateCategory(
                new CategoryJson(
                        UUID.fromString("60666d5e-6d58-11f1-8d79-0242ac110004"),
                        "category-test-100",
                        "Dina",
                        false
                )
        );
    }

    @Test
    public void findAllSpendsByIdTest() {
        System.out.println("Start findAllSpendsByIdTest");
        SpendDbClient spendDbClient = new SpendDbClient();
        List<SpendJson> list = spendDbClient.allSpends("Dina");
        System.out.println("SPEND LIST ===> " + list);
    }

    @Test
    public void findSpendByIdTest() {
        System.out.println("Start findSpendByIdTest");
        SpendDbClient spendDbClient = new SpendDbClient();
        Optional<SpendJson> spendJson = spendDbClient.getSpend("d7e258e6-6cec-11f1-96b9-0242ac110004");
        if (spendJson.isPresent()) {
            System.out.println("SPEND JSON ===> " + spendJson.get());
        } else {
            System.out.println("SPEND NOT FOUND");
        }
    }

    @Test
    public void deleteSpendTest() {
        System.out.println("Start deleteSpendTest");
        SpendDbClient spendDbClient = new SpendDbClient();
        spendDbClient.deleteSpend("d7e258e6-6cec-11f1-96b9-0242ac110004");
    }

    @Test
    public void createUserTest() {
        UserDbClient userDbClient = new UserDbClient();
        UserJson userJson = userDbClient.createUser(
                new UserJson(
                        null,
                        "Bob",
                        "Robert",
                        "Smith",
                        "Robert Smith",
                        CurrencyValues.RUB,
                        null,
                        null
                )
        );

        System.out.println("USER JSON ===> " + userJson);
    }

    @Test
    public void failUserCreationTest() {
        UserDbClient userDbClient = new UserDbClient();
        try {
            UserJson userJson = userDbClient.createUser(
                    new UserJson(
                            null,
                            "Mary",
                            null,
                            "Smith",
                            "Mary Smith",
                            CurrencyValues.RUB,
                            null,
                            null
                    )
            );
        } catch (RuntimeException e) {
            Optional<UserJson> userJson =  userDbClient.findUserByUsername("Harry");
            assertFalse(userJson.isPresent(), "User with username 'Harry' exists in DB");
        }
    }

    @Test
    public void findUserByIdTest() {
        UserDbClient userDbClient = new UserDbClient();
        Optional<UserJson> json = userDbClient.findUserById("b115cb28-6d5d-11f1-9ef1-0242ac110004");
        if (json.isPresent()) {
            System.out.println("USER JSON ===> " + json.get());
        } else {
            System.out.println("USER NOT FOUND");
        }
    }

    @Test
    public void findUserByUsernameTest() {
        UserDbClient userDbClient = new UserDbClient();
        Optional<UserJson> json = userDbClient.findUserByUsername("Alex");
        if (json.isPresent()) {
            System.out.println("USER JSON ===> " + json.get());
        } else {
            System.out.println("USER NOT FOUND");
        }
    }

    @Test
    public void deleteUserTest() {
        UserDbClient userDbClient = new UserDbClient();
        Optional<UserJson> user = userDbClient.findUserById("b115cb28-6d5d-11f1-9ef1-0242ac110004");
        if (user.isPresent()) {
            userDbClient.deleteUser(user.get());
        } else {
            System.out.println("USER NOT FOUND");
        }
    }
}
