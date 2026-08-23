package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.repository.SpendRepository;
import guru.qa.niffler.data.repository.impl.SpendRepositoryHibernate;
import guru.qa.niffler.data.tpl.XaTransactionTemplate;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;

import java.util.Optional;
import java.util.UUID;

public class SpendDbClient implements SpendClient {

    private static final Config CFG = Config.getInstance();

    private final SpendRepository spendRepository = new SpendRepositoryHibernate();
    private final XaTransactionTemplate xaTxTemplate = new XaTransactionTemplate(
            CFG.spendJdbcUrl()
    );

    @Override
    public SpendJson createSpend(SpendJson spend) {
        return xaTxTemplate.execute(() -> {
                    SpendEntity spendEntity = SpendEntity.fromJson(spend);
                    if (spendEntity.getCategory().getId() == null) {
                        CategoryEntity categoryEntity = spendRepository.findCategoryByUsernameAndCategoryName(
                                spendEntity.getUsername(),
                                spendEntity.getCategory().getName()
                        ).orElseGet(() -> spendRepository.createCategory(spendEntity.getCategory()));

                        spendEntity.setCategory(categoryEntity);
                    }
                    return SpendJson.fromEntity(
                            spendRepository.create(spendEntity)
                    );
                }
        );
    }

    @Override
    public SpendJson updateSpend(SpendJson spend) {
        return xaTxTemplate.execute(() -> {
                    SpendEntity spendEntity = SpendEntity.fromJson(spend);
                    if (spendEntity.getCategory().getId() == null) {
                        CategoryEntity categoryEntity = spendRepository.findCategoryByUsernameAndCategoryName(
                                spendEntity.getCategory().getUsername(),
                                spendEntity.getDescription()
                        ).orElseGet(() -> spendRepository.createCategory(spendEntity.getCategory()));

                        spendEntity.setCategory(categoryEntity);
                    }
                    return SpendJson.fromEntity(
                            spendRepository.update(spendEntity)
                    );
                }
        );
    }

    public Optional<SpendJson> findSpendById(String id) {
        return xaTxTemplate.execute(() -> {
                    Optional<SpendEntity> spendEntity = spendRepository.findSpendById(UUID.fromString(id));
                    return spendEntity.map(SpendJson::fromEntity);
                }
        );
    }

    @Override
    public Optional<SpendJson> findSpendByUsernameAndSpendDescription(String username, String description) {
        return xaTxTemplate.execute(() -> {
                    Optional<SpendEntity> spendEntity = spendRepository.findSpendByUsernameAndSpendDescription(username, description);
                    return spendEntity.map(SpendJson::fromEntity);
                }
        );
    }

    @Override
    public void deleteSpend(String id) {
        xaTxTemplate.execute(() -> {
                    Optional<SpendEntity> spendEntity = spendRepository.findSpendById(UUID.fromString(id));
                    if (spendEntity.isPresent()) {
                        spendRepository.remove(spendEntity.get());
                    } else {
                        throw new RuntimeException("Can't delete spend with id " + id);
                    }
                }
        );
    }

    @Override
    public CategoryJson createCategory(CategoryJson category) {
        return xaTxTemplate.execute(() -> {
                    CategoryEntity categoryEntity = CategoryEntity.fromJson(category);
                    return CategoryJson.fromEntity(spendRepository.createCategory(categoryEntity));
                }
        );
    }

    @Override
    public Optional<CategoryJson> findCategoryById(String id) {
        return xaTxTemplate.execute(() -> {
                    Optional<CategoryEntity> category = spendRepository.findCategoryById(UUID.fromString(id));
                    return category.map(CategoryJson::fromEntity);
                }
        );
    }

    @Override
    public Optional<CategoryJson> findCategoryByNameAndUsername(String categoryName, String username) {
        return xaTxTemplate.execute(() -> {
                    Optional<CategoryEntity> categoryEntity = spendRepository
                            .findCategoryByUsernameAndCategoryName(username, categoryName);
                    return categoryEntity.map(CategoryJson::fromEntity);
                }
        );
    }

    public void deleteCategory(String id) {
        xaTxTemplate.execute(() -> {
                    Optional<CategoryEntity> categoryEntity = spendRepository.findCategoryById(UUID.fromString(id));
                    if (categoryEntity.isPresent()) {
                        spendRepository.removeCategory(categoryEntity.get());
                    }
                }
        );
    }
}
