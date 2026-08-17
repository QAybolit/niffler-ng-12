package guru.qa.niffler.data.extractor;

import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.model.CurrencyValues;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpendEntityResultSetExtractor implements ResultSetExtractor<SpendEntity> {

    @Override
    public SpendEntity extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<UUID, SpendEntity> spendMap = new ConcurrentHashMap<>();
        UUID spendId = null;
        while (rs.next()) {
            spendId = rs.getObject("id", UUID.class);
            SpendEntity spend = spendMap.computeIfAbsent(spendId, id -> {
                try {
                    SpendEntity se = new SpendEntity();
                    se.setId(rs.getObject("id", UUID.class));
                    se.setUsername(rs.getString("username"));
                    se.setCurrency(CurrencyValues.valueOf(rs.getString("currency")));
                    se.setSpendDate(rs.getDate("spend_date"));
                    se.setAmount(rs.getDouble("amount"));
                    se.setDescription(rs.getString("description"));

                    return se;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            CategoryEntity category = new CategoryEntity();
            category.setId(rs.getObject("category_id", UUID.class));
            category.setName(rs.getString("name"));
            category.setUsername(rs.getString("category_username"));
            category.setArchived(rs.getBoolean("archived"));

            spend.setCategory(category);
        }
        return spendMap.get(spendId);
    }
}
