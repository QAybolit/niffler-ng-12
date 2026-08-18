package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.jpa.EntityManagers;
import guru.qa.niffler.data.repository.SpendRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.Optional;
import java.util.UUID;

public class SpendRepositoryHibernate implements SpendRepository {

    private static final Config CFG = Config.getInstance();
    private final EntityManager entityManager = EntityManagers.em(CFG.spendJdbcUrl());

    @Override
    public SpendEntity create(SpendEntity spend) {
        entityManager.joinTransaction();
        entityManager.persist(spend);
        return spend;
    }

    @Override
    public SpendEntity update(SpendEntity spend) {
        entityManager.joinTransaction();
        return entityManager.merge(spend);
    }

    @Override
    public CategoryEntity createCategory(CategoryEntity category) {
        entityManager.joinTransaction();
        entityManager.persist(category);
        return category;
    }

    @Override
    public Optional<CategoryEntity> findCategoryById(UUID id) {
        return Optional.ofNullable(entityManager.find(CategoryEntity.class, id));
    }

    @Override
    public Optional<CategoryEntity> findCategoryByUsernameAndSpendName(String username, String spendName) {
        try {
            CategoryEntity category = entityManager.createQuery(
                            "SELECT с FROM CategoryEntity c JOIN SpendEntity s ON s.category.id = с" +
                                    " WHERE c.username = :username AND s.description = :spendName",
                            CategoryEntity.class
                    ).setParameter("username", username)
                    .setParameter("spendName", spendName)
                    .getSingleResult();

            return Optional.of(category);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<SpendEntity> findSpendById(UUID id) {
        return Optional.ofNullable(entityManager.find(SpendEntity.class, id));
    }

    @Override
    public Optional<SpendEntity> findSpendByUsernameAndSpendDescription(String username, String description) {
        try {
            SpendEntity spend = entityManager.createQuery(
                            "SELECT s FROM SpendEntity s JOIN SpendEntity s WHERE s.username = :username AND s.description = :description",
                            SpendEntity.class
                    ).setParameter("username", username)
                    .setParameter("description", description)
                    .getSingleResult();

            return Optional.of(spend);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public void remove(SpendEntity spend) {
        entityManager.joinTransaction();
        entityManager.remove(spend);
    }

    @Override
    public void removeCategory(CategoryEntity category) {
        entityManager.joinTransaction();
        entityManager.remove(category);
    }
}
