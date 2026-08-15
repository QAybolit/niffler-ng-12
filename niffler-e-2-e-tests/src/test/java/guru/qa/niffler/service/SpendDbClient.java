package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.CategoryDao;
import guru.qa.niffler.data.dao.SpendDao;
import guru.qa.niffler.data.dao.impl.CategoryDaoJdbc;
import guru.qa.niffler.data.dao.impl.SpendDaoJdbc;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.tpl.JdbcTransactionTemplate;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.sql.Connection;

public class SpendDbClient implements SpendClient {

    private static final Config CFG = Config.getInstance();

    private final CategoryDao categoryDao = new CategoryDaoJdbc();
    private final SpendDao spendDao = new SpendDaoJdbc();
    private final JdbcTransactionTemplate jdbcTxTemplate = new JdbcTransactionTemplate(CFG.spendJdbcUrl());

    @Override
    public SpendJson createSpend(SpendJson spend) {
        return jdbcTxTemplate.execute(() -> {
                    SpendEntity spendEntity = SpendEntity.fromJson(spend);
                    if (spendEntity.getCategory().getId() == null) {
                        CategoryEntity categoryEntity = categoryDao
                                .findCategoryByUsernameAndCategoryName(
                                        spendEntity.getUsername(),
                                        spendEntity.getCategory().getName())
                                .orElseGet(() -> categoryDao.create(spendEntity.getCategory()));
                        spendEntity.setCategory(categoryEntity);
                    }
                    return SpendJson.fromEntity(
                            spendDao.create(spendEntity)
                    );
                },
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    @Override
    public SpendJson editSpend(SpendJson spend) {
        return jdbcTxTemplate.execute(() -> {
                    SpendEntity spendEntity = SpendEntity.fromJson(spend);
                    if (spendEntity.getCategory().getId() == null) {
                        CategoryEntity categoryEntity = categoryDao
                                .findCategoryByUsernameAndCategoryName(
                                        spendEntity.getUsername(),
                                        spendEntity.getCategory().getName())
                                .orElseGet(() -> categoryDao.create(spendEntity.getCategory()));
                        spendEntity.setCategory(categoryEntity);
                    }
                    return SpendJson.fromEntity(spendDao.updateSpend(spendEntity));
                },
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    public Optional<SpendJson> getSpend(String id) {
        return jdbcTxTemplate.execute(() -> {
                    Optional<SpendEntity> spendEntity = spendDao.findSpendById(UUID.fromString(id));
                    return spendEntity.map(SpendJson::fromEntity);
                },
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    public List<SpendJson> allSpends(String username) {
        return jdbcTxTemplate.execute(
                () -> spendDao.findAllByUsername(username).stream()
                        .map(SpendJson::fromEntity)
                        .toList(),
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    public void deleteSpend(String id) {
        jdbcTxTemplate.execute(() -> {
                    Optional<SpendEntity> spendEntity = spendDao.findSpendById(UUID.fromString(id));
                    if (spendEntity.isPresent()) {
                        spendDao.deleteSpend(spendEntity.get());
                    } else {
                        throw new RuntimeException("Can't delete spend with id " + id);
                    }
                },
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    @Override
    public CategoryJson createCategory(CategoryJson category) {
        return jdbcTxTemplate.execute(() -> {
                    CategoryEntity categoryEntity = CategoryEntity.fromJson(category);
                    return CategoryJson.fromEntity(categoryDao.create(categoryEntity));
                },
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    @Override
    public CategoryJson updateCategory(CategoryJson category) {
        return jdbcTxTemplate.execute(() -> {
                    CategoryEntity categoryEntity = CategoryEntity.fromJson(category);
                    return CategoryJson.fromEntity(categoryDao.updateCategory(categoryEntity));
                },
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    @Override
    public Optional<CategoryJson> findCategoryByNameAndUsername(String categoryName, String username) {
        return jdbcTxTemplate.execute(() -> {
                    Optional<CategoryEntity> categoryEntity = categoryDao
                            .findCategoryByUsernameAndCategoryName(username, categoryName);
                    return categoryEntity.map(CategoryJson::fromEntity);
                },
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    public List<CategoryJson> allCategory(String username) {
        return jdbcTxTemplate.execute(() -> {
                    return categoryDao.findAllByUsername(username).stream()
                            .map(CategoryJson::fromEntity)
                            .toList();
                },
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    public void deleteCategory(String id) {
        jdbcTxTemplate.execute(() -> {
                    Optional<CategoryEntity> categoryEntity = categoryDao.findCategoryById(UUID.fromString(id));
                    if (categoryEntity.isPresent()) {
                        categoryDao.deleteCategory(categoryEntity.get());
                    } else {
                        throw new RuntimeException("Can't delete category with id " + id);
                    }
                },
                Connection.TRANSACTION_READ_COMMITTED
        );
    }
}
