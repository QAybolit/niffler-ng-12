package guru.qa.niffler.service;

import guru.qa.niffler.data.dao.CategoryDao;
import guru.qa.niffler.data.dao.SpendDao;
import guru.qa.niffler.data.dao.impl.CategoryDaoJdbc;
import guru.qa.niffler.data.dao.impl.SpendDaoJdbc;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SpendDbClient implements SpendClient {

    private final SpendDao spendDao = new SpendDaoJdbc();
    private final CategoryDao categoryDao = new CategoryDaoJdbc();

    @Override
    public SpendJson createSpend(SpendJson spend) {
        SpendEntity spendEntity = SpendEntity.fromJson(spend);
        if (spendEntity.getCategory().getId() == null) {
            CategoryEntity categoryEntity = categoryDao.create(spendEntity.getCategory());
            spendEntity.setCategory(categoryEntity);
        }
        return SpendJson.fromEntity(spendDao.create(spendEntity));
    }

    @Override
    public SpendJson editSpend(SpendJson spend) {
        SpendEntity spendEntity = SpendEntity.fromJson(spend);
        if (spendEntity.getCategory().getId() == null) {
            CategoryEntity categoryEntity = categoryDao.create(spendEntity.getCategory());
            spendEntity.setCategory(categoryEntity);
        }
        return SpendJson.fromEntity(spendDao.updateSpend(spendEntity));
    }

    public Optional<SpendJson> getSpend(String id) {
        Optional<SpendEntity> spendEntity = spendDao.findSpendById(UUID.fromString(id));
        return spendEntity.map(SpendJson::fromEntity);
    }

    public List<SpendJson> allSpends(String username) {
        return spendDao.findAllByUsername(username).stream()
                .map(SpendJson::fromEntity)
                .toList();
    }

    public void deleteSpend(String id) {
        Optional<SpendEntity> spendEntity = spendDao.findSpendById(UUID.fromString(id));
        if (spendEntity.isPresent()) {
            spendDao.deleteSpend(spendEntity.get());
        } else {
            throw new RuntimeException("Can't delete spend with id " + id);
        }
    }

    @Override
    public CategoryJson createCategory(CategoryJson category) {
        CategoryEntity categoryEntity = CategoryEntity.fromJson(category);
        return CategoryJson.fromEntity(categoryDao.create(categoryEntity));
    }

    @Override
    public CategoryJson updateCategory(CategoryJson category) {
        CategoryEntity categoryEntity = CategoryEntity.fromJson(category);
        return CategoryJson.fromEntity(categoryDao.updateCategory(categoryEntity));
    }

    @Override
    public Optional<CategoryJson> findCategoryByNameAndUsername(String categoryName, String username) {
        Optional<CategoryEntity> categoryEntity = categoryDao.findCategoryByUsernameAndCategoryName(username, categoryName);
        return categoryEntity.map(CategoryJson::fromEntity);
    }

    public List<CategoryJson> allCategory(String username) {
        return categoryDao.findAllByUsername(username).stream()
                .map(CategoryJson::fromEntity)
                .toList();
    }

    public void deleteCategory(String id) {
        Optional<CategoryEntity> categoryEntity = categoryDao.findCategoryById(UUID.fromString(id));
        if (categoryEntity.isPresent()) {
            categoryDao.deleteCategory(categoryEntity.get());
        } else {
            throw new RuntimeException("Can't delete category with id " + id);
        }
    }
}
