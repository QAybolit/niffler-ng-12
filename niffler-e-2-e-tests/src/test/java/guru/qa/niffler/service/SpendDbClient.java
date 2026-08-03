package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
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
import java.sql.Connection;

import static guru.qa.niffler.data.Databases.transaction;

public class SpendDbClient implements SpendClient {

    private static final Config CFG = Config.getInstance();

    @Override
    public SpendJson createSpend(SpendJson spend) {
        return transaction(connection -> {
                    SpendEntity spendEntity = SpendEntity.fromJson(spend);
                    if (spendEntity.getCategory().getId() == null) {
                        CategoryEntity categoryEntity = new CategoryDaoJdbc(connection)
                                .create(spendEntity.getCategory());
                        spendEntity.setCategory(categoryEntity);
                    }
                    return SpendJson.fromEntity(
                            new SpendDaoJdbc(connection).create(spendEntity)
                    );
                },
                CFG.spendJdbcUrl(),
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    @Override
    public SpendJson editSpend(SpendJson spend) {
        return transaction(connection -> {
                    SpendEntity spendEntity = SpendEntity.fromJson(spend);
                    if (spendEntity.getCategory().getId() == null) {
                        CategoryEntity categoryEntity = new CategoryDaoJdbc(connection)
                                .create(spendEntity.getCategory());
                        spendEntity.setCategory(categoryEntity);
                    }
                    return SpendJson.fromEntity(new SpendDaoJdbc(connection)
                            .updateSpend(spendEntity));
                },
                CFG.spendJdbcUrl(),
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    public Optional<SpendJson> getSpend(String id) {
        return transaction(connection -> {
                    Optional<SpendEntity> spendEntity = new SpendDaoJdbc(connection)
                            .findSpendById(UUID.fromString(id));
                    return spendEntity.map(SpendJson::fromEntity);
                },
                CFG.spendJdbcUrl(),
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    public List<SpendJson> allSpends(String username) {
        return transaction(connection -> {
                    return new SpendDaoJdbc(connection).findAllByUsername(username).stream()
                            .map(SpendJson::fromEntity)
                            .toList();
                },
                CFG.spendJdbcUrl(),
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    public void deleteSpend(String id) {
        transaction(connection -> {
                    SpendDao spendDao = new SpendDaoJdbc(connection);
                    Optional<SpendEntity> spendEntity = spendDao.findSpendById(UUID.fromString(id));
                    if (spendEntity.isPresent()) {
                        spendDao.deleteSpend(spendEntity.get());
                    } else {
                        throw new RuntimeException("Can't delete spend with id " + id);
                    }
                },
                CFG.spendJdbcUrl(),
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    @Override
    public CategoryJson createCategory(CategoryJson category) {
        return transaction(connection -> {
                    CategoryEntity categoryEntity = CategoryEntity.fromJson(category);
                    return CategoryJson.fromEntity(new CategoryDaoJdbc(connection)
                            .create(categoryEntity));
                },
                CFG.spendJdbcUrl(),
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    @Override
    public CategoryJson updateCategory(CategoryJson category) {
        return transaction(connection -> {
                    CategoryEntity categoryEntity = CategoryEntity.fromJson(category);
                    return CategoryJson.fromEntity(new CategoryDaoJdbc(connection)
                            .updateCategory(categoryEntity));
                },
                CFG.spendJdbcUrl(),
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    @Override
    public Optional<CategoryJson> findCategoryByNameAndUsername(String categoryName, String username) {
        return transaction(connection -> {
                    Optional<CategoryEntity> categoryEntity = new CategoryDaoJdbc(connection)
                            .findCategoryByUsernameAndCategoryName(username, categoryName);
                    return categoryEntity.map(CategoryJson::fromEntity);
                },
                CFG.spendJdbcUrl(),
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    public List<CategoryJson> allCategory(String username) {
        return transaction(connection -> {
                    return new CategoryDaoJdbc(connection).findAllByUsername(username).stream()
                            .map(CategoryJson::fromEntity)
                            .toList();
                },
                CFG.spendJdbcUrl(),
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    public void deleteCategory(String id) {
        transaction(connection -> {
                    CategoryDao categoryDao = new CategoryDaoJdbc(connection);
                    Optional<CategoryEntity> categoryEntity = categoryDao.findCategoryById(UUID.fromString(id));
                    if (categoryEntity.isPresent()) {
                        categoryDao.deleteCategory(categoryEntity.get());
                    } else {
                        throw new RuntimeException("Can't delete category with id " + id);
                    }
                },
                CFG.spendJdbcUrl(),
                Connection.TRANSACTION_READ_COMMITTED
        );
    }
}
