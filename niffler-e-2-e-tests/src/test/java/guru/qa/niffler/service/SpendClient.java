package guru.qa.niffler.service;

import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;

import java.util.Optional;

public interface SpendClient {

    SpendJson createSpend(SpendJson spend);

    SpendJson updateSpend(SpendJson spend);

    CategoryJson createCategory(CategoryJson category);

    Optional<CategoryJson> findCategoryById(String id);

    Optional<CategoryJson> findCategoryByNameAndUsername(String categoryName, String username);

    Optional<SpendJson> findSpendById(String id);

    Optional<SpendJson> findSpendByUsernameAndSpendDescription(String username, String description);

    void deleteSpend(String id);

    void deleteCategory(String id);
}
