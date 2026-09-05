package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.mapper.CategoryEntityRowMapper;
import guru.qa.niffler.data.mapper.SpendEntityRowMapper;
import guru.qa.niffler.data.repository.SpendRepository;
import guru.qa.niffler.data.tpl.DataSources;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;
import java.util.UUID;

public class SpendRepositorySpringJdbc implements SpendRepository {

    private final JdbcTemplate template = new JdbcTemplate(DataSources.dataSource(CFG.spendJdbcUrl()));
    private static final Config CFG = Config.getInstance();

    @Override
    public SpendEntity create(SpendEntity spend) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(con -> {
                    PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO spend (username, spend_date, currency, amount, description, category_id)" +
                                    " VALUES (?, ?, ?, ?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS
                    );
                    ps.setString(1, spend.getUsername());
                    ps.setObject(2, spend.getSpendDate());
                    ps.setString(3, spend.getCurrency().name());
                    ps.setDouble(4, spend.getAmount());
                    ps.setString(5, spend.getDescription());
                    ps.setObject(6, spend.getCategory().getId());
                    return ps;
                }, keyHolder
        );
        final UUID generatedKey = keyHolder.getKeyAs(UUID.class);
        spend.setId(generatedKey);
        return spend;
    }

    @Override
    public SpendEntity update(SpendEntity spend) {
        template.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE spend SET username = ?, spend_date = ?, currency = ?, amount = ?, description = ?, category_id = ? WHERE id = ?"
            );
            ps.setString(1, spend.getUsername());
            ps.setObject(2, spend.getSpendDate());
            ps.setString(3, spend.getCurrency().name());
            ps.setDouble(4, spend.getAmount());
            ps.setString(5, spend.getDescription());
            ps.setObject(6, spend.getCategory().getId());
            return ps;
        });
        return spend;
    }

    @Override
    public CategoryEntity createCategory(CategoryEntity category) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO category (name, username, archived) VALUES (?, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, category.getName());
            ps.setString(2, category.getUsername());
            ps.setBoolean(3, category.isArchived());
            return ps;
        }, keyHolder);

        final UUID generatedKey = keyHolder.getKeyAs(UUID.class);
        category.setId(generatedKey);

        return category;
    }

    @Override
    public Optional<CategoryEntity> findCategoryById(UUID id) {
        return Optional.ofNullable(
                template.queryForObject(
                        "SELECT * FROM category WHERE id = ?",
                        CategoryEntityRowMapper.INSTANCE,
                        id
                )
        );
    }

    @Override
    public Optional<CategoryEntity> findCategoryByUsernameAndCategoryName(String username, String categoryName) {
        return Optional.ofNullable(
                template.queryForObject(
                        "SELECT * FROM category c WHERE username = ? AND name = ?",
                        CategoryEntityRowMapper.INSTANCE,
                        username,
                        categoryName
                )
        );
    }

    @Override
    public Optional<SpendEntity> findSpendById(UUID id) {
        return Optional.ofNullable(
                template.queryForObject(
                        """
                                SELECT sp.id, sp.username, sp.currency, sp.spend_date, sp.amount, sp.description,
                                sp.category_id, c.name, c.username AS category_username, c.archived
                                FROM spend AS sp
                                JOIN category AS c ON sp.category_id = c.id
                                WHERE sp.id = ?
                                """,
                        SpendEntityRowMapper.INSTANCE,
                        id
                )
        );
    }

    @Override
    public Optional<SpendEntity> findSpendByUsernameAndSpendDescription(String username, String description) {
        return Optional.ofNullable(
                template.queryForObject(
                        """
                                SELECT sp.id, sp.username, sp.currency, sp.spend_date, sp.amount, sp.description,
                                sp.category_id, c.name, c.username AS category_username, c.archived
                                FROM spend AS sp
                                JOIN category AS c ON sp.category_id = c.id
                                WHERE sp.username = ? AND sp.description = ?
                                """,
                        SpendEntityRowMapper.INSTANCE,
                        username,
                        description
                )
        );
    }

    @Override
    public void remove(SpendEntity spend) {
        template.update("DELETE FROM spend WHERE id = ?", spend.getId());
    }

    @Override
    public void removeCategory(CategoryEntity category) {
        template.update("DELETE FROM category WHERE id = ?", category.getId());
    }
}
