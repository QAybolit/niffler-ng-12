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

public class JdbcTest {

    @Test
    public void createSpendTest() {
        SpendDbClient spendDbClient = new SpendDbClient();
        SpendJson spendJson = spendDbClient.createSpend(
                new SpendJson(
                        null,
                        new Date(),
                        new CategoryJson(
                                null,
                                "test-name-10",
                                "Dina",
                                false
                        ),
                        CurrencyValues.RUB,
                        1000.0,
                        "test description",
                        "Dina"
                )
        );
        System.out.println("JSON ===> " + spendJson);
    }

    @Test
    public void createCategoryTest() {
        SpendDbClient spendDbClient = new SpendDbClient();
        CategoryJson categoryJson = spendDbClient.createCategory(
                new CategoryJson(
                        null,
                        "category-test-2",
                        "Dina",
                        false
                )
        );
        System.out.println("JSON ===> " + categoryJson);
    }

    @Test
    public void findAllCategoriesByUsernameTest() {
        SpendDbClient spendDbClient = new SpendDbClient();
        List<CategoryJson> list = spendDbClient.allCategory("Dina");
        System.out.println("JSON ===> " + list);
    }

    @Test
    public void deleteCategoryTest() {
        SpendDbClient spendDbClient = new SpendDbClient();
        spendDbClient.deleteCategory("65f5a29e-6d58-11f1-95bf-0242ac110004");
    }

    @Test
    public void updateCategoryTest() {
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
        SpendDbClient spendDbClient = new SpendDbClient();
        List<SpendJson> list = spendDbClient.allSpends("Dina");
        System.out.println("JSON ===> " + list);
    }

    @Test
    public void findSpendByIdTest() {
        SpendDbClient spendDbClient = new SpendDbClient();
        Optional<SpendJson> spendJson = spendDbClient.getSpend("d7e258e6-6cec-11f1-96b9-0242ac110004");
        if (spendJson.isPresent()) {
            System.out.println("JSON ===> " + spendJson.get());
        } else {
            System.out.println("JSON NOT FOUND");
        }
    }

    @Test
    public void deleteSpendTest() {
        SpendDbClient spendDbClient = new SpendDbClient();
        spendDbClient.deleteSpend("d7e258e6-6cec-11f1-96b9-0242ac110004");
    }

    @Test
    public void createUserTest() {
        UserDbClient userDbClient = new UserDbClient();
        UserJson userJson = userDbClient.createUser(
                new UserJson(
                        null,
                        "Alex",
                        "Alex",
                        "Smith",
                        "Alex Smith",
                        CurrencyValues.RUB,
                        null,
                        null
                )
        );

        System.out.println("JSON ===> " + userJson);
    }

    @Test
    public void findUserByIdTest() {
        UserDbClient userDbClient = new UserDbClient();
        Optional<UserJson> json = userDbClient.findUserById("b115cb28-6d5d-11f1-9ef1-0242ac110004");
        if (json.isPresent()) {
            System.out.println("JSON ===> " + json.get());
        } else {
            System.out.println("JSON NOT FOUND");
        }
    }

    @Test
    public void findUserByUsernameTest() {
        UserDbClient userDbClient = new UserDbClient();
        Optional<UserJson> json = userDbClient.findUserByUsername("Alex");
        if (json.isPresent()) {
            System.out.println("JSON ===> " + json.get());
        } else {
            System.out.println("JSON NOT FOUND");
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
