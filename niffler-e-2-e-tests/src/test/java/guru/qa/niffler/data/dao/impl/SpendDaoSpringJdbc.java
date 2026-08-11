package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.SpendDao;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.mapper.SpendEntityRowMapper;
import guru.qa.niffler.data.tpl.DataSources;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SpendDaoSpringJdbc implements SpendDao {

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
    public SpendEntity updateSpend(SpendEntity spend) {
        template.update(
                "UPDATE spend SET username = ?, spend_date = ?, currency = ?, amount = ?, description = ?, category_id = ?" +
                        " WHERE id = ?",
                spend.getUsername(),
                spend.getSpendDate(),
                spend.getCurrency().name(),
                spend.getAmount(),
                spend.getDescription(),
                spend.getCategory().getId()
        );
        return spend;
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
    public List<SpendEntity> findAllByUsername(String username) {
        return template.query(
                """
                        SELECT sp.id, sp.username, sp.currency, sp.spend_date, sp.amount, sp.description,
                        sp.category_id, c.name, c.username AS category_username, c.archived
                        FROM spend AS sp
                        JOIN category AS c ON sp.category_id = c.id
                        WHERE sp.username = ?
                        """,
                SpendEntityRowMapper.INSTANCE,
                username
        );
    }

    @Override
    public List<SpendEntity> findAll() {
        return template.query(
                """
                        SELECT sp.id, sp.username, sp.currency, sp.spend_date, sp.amount, sp.description,
                        sp.category_id, c.name, c.username AS category_username, c.archived
                        FROM spend AS sp
                        JOIN category AS c ON sp.category_id = c.id
                        """,
                SpendEntityRowMapper.INSTANCE
        );
    }

    @Override
    public void deleteSpend(SpendEntity spend) {
        template.update("DELETE FROM spend WHERE id = ?", spend.getId());
    }
}
