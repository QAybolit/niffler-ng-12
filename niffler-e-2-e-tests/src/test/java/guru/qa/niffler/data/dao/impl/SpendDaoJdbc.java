package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.Databases;
import guru.qa.niffler.data.dao.SpendDao;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.model.CurrencyValues;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SpendDaoJdbc implements SpendDao {

    private static final Config CFG = Config.getInstance();

    @Override
    public SpendEntity create(SpendEntity spend) {
        try (Connection connection = Databases.connect(CFG.spendJdbcUrl())) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO spend (username, spend_date, currency, amount, description, category_id)" +
                            " VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            )) {
                ps.setString(1, spend.getUsername());
                ps.setObject(2, spend.getSpendDate());
                ps.setString(3, spend.getCurrency().name());
                ps.setDouble(4, spend.getAmount());
                ps.setString(5, spend.getDescription());
                ps.setObject(6, spend.getCategory().getId());

                ps.executeUpdate();

                final UUID generatedKey;
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedKey = rs.getObject("id", UUID.class);
                    } else {
                        throw new SQLException("Can't find id in ResultSet");
                    }
                }
                spend.setId(generatedKey);
                return spend;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SpendEntity updateSpend(SpendEntity spend) {
        try (Connection connection = Databases.connect(CFG.spendJdbcUrl())) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE spend SET username = ?, spend_date = ?, currency = ?, amount = ?, description = ?, category_id = ?" +
                            " WHERE id = ?"
            )) {
                ps.setString(1, spend.getUsername());
                ps.setObject(2, spend.getSpendDate());
                ps.setString(3, spend.getCurrency().name());
                ps.setDouble(4, spend.getAmount());
                ps.setString(5, spend.getDescription());
                ps.setObject(6, spend.getCategory().getId());
                ps.setObject(7, spend.getId());

                int result = ps.executeUpdate();

                if (result <= 0) {
                    throw new SQLException("Can't find spend id in table 'spend'");
                }
                return spend;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<SpendEntity> findSpendById(UUID id) {
        try (Connection connection = Databases.connect(CFG.spendJdbcUrl())) {
            try (PreparedStatement ps = connection.prepareStatement(
                    """
                            SELECT sp.id, sp.username, sp.currency, sp.spend_date, sp.amount, sp.description,
                            sp.category_id, c.name, c.username AS category_username, c.archived
                            FROM spend AS sp
                            JOIN category AS c ON sp.category_id = c.id
                            WHERE sp.id = ?
                            """
            )) {
                ps.setObject(1, id);
                ps.execute();

                try (ResultSet rs = ps.getResultSet()) {
                    if (rs.next()) {
                        SpendEntity se = new SpendEntity();
                        se.setId(rs.getObject("id", UUID.class));
                        se.setUsername(rs.getString("username"));
                        se.setCurrency(CurrencyValues.valueOf(rs.getString("currency")));
                        se.setSpendDate(rs.getDate("spend_date"));
                        se.setAmount(rs.getDouble("amount"));
                        se.setDescription(rs.getString("description"));

                        CategoryEntity ce = new CategoryEntity();
                        ce.setId(rs.getObject("category_id", UUID.class));
                        ce.setName(rs.getString("name"));
                        ce.setUsername(rs.getString("category_username"));
                        ce.setArchived(rs.getBoolean("archived"));

                        se.setCategory(ce);
                        return Optional.of(se);
                    } else {
                        return Optional.empty();
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<SpendEntity> findAllByUsername(String username) {
        List<SpendEntity> result = new ArrayList<>();

        try (Connection connection = Databases.connect(CFG.spendJdbcUrl())) {
            try (PreparedStatement ps = connection.prepareStatement(
                    """
                            SELECT sp.id, sp.username, sp.currency, sp.spend_date, sp.amount, sp.description,
                            sp.category_id, c.name, c.username AS category_username, c.archived
                            FROM spend AS sp
                            JOIN category AS c ON sp.category_id = c.id
                            WHERE sp.username = ?
                            """
            )) {
                ps.setString(1, username);
                ps.execute();

                try (ResultSet rs = ps.getResultSet()) {
                    while (rs.next()) {
                        SpendEntity se = new SpendEntity();
                        se.setId(rs.getObject("id", UUID.class));
                        se.setUsername(rs.getString("username"));
                        se.setCurrency(CurrencyValues.valueOf(rs.getString("currency")));
                        se.setSpendDate(rs.getDate("spend_date"));
                        se.setAmount(rs.getDouble("amount"));
                        se.setDescription(rs.getString("description"));

                        CategoryEntity ce = new CategoryEntity();
                        ce.setId(rs.getObject("category_id", UUID.class));
                        ce.setName(rs.getString("name"));
                        ce.setUsername(rs.getString("category_username"));
                        ce.setArchived(rs.getBoolean("archived"));

                        se.setCategory(ce);

                        result.add(se);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public void deleteSpend(SpendEntity spend) {
        try (Connection connection = Databases.connect(CFG.spendJdbcUrl())) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM spend WHERE id = ?"
            )) {
                ps.setObject(1, spend.getId());
                int result = ps.executeUpdate();

                if (result <= 0) {
                    throw new SQLException("Can't find spend id in table 'spend'");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
