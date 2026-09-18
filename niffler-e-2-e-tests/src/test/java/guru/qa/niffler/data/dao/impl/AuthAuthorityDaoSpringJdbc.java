package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.data.dao.AuthAuthorityDao;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.mapper.AuthorityEntityRowMapper;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AuthAuthorityDaoSpringJdbc implements AuthAuthorityDao {

    private final DataSource dataSource;
    private final JdbcTemplate template;

    public AuthAuthorityDaoSpringJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
        this.template = new JdbcTemplate(dataSource);
    }

    @Override
    public void create(AuthorityEntity... authorities) {
        template.batchUpdate(
                "INSERT INTO authority (user_id, authority) VALUES (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setObject(1, authorities[i].getUser().getId());
                        ps.setString(2, authorities[i].getAuthority().name());
                    }

                    @Override
                    public int getBatchSize() {
                        return authorities.length;
                    }
                }
        );
    }

    @Override
    public Optional<AuthorityEntity> findAuthorityByById(UUID id) {
        return Optional.ofNullable(
                template.queryForObject(
                        """
                                SELECT at.id, at.authority, at.user_id, us.username, us.enabled, us.account_non_expired, us.account_non_locked, us.credentials_non_expired
                                FROM authority AS at
                                JOIN user AS us ON at.user_id = us.id
                                WHERE at.id = ?
                                """,
                        AuthorityEntityRowMapper.INSTANCE,
                        id
                )
        );
    }

    @Override
    public List<AuthorityEntity> findAll() {
        return template.query(
                """
                        SELECT at.id, at.authority, at.user_id, us.username, us.enabled, us.account_non_expired, us.account_non_locked, us.credentials_non_expired
                        FROM authority AS at
                        JOIN user AS us ON at.user_id = us.id
                        """,
                AuthorityEntityRowMapper.INSTANCE
        );
    }

    @Override
    public void deleteAuthority(UUID id) {
        template.update(
                "DELETE FROM authority WHERE id = ?",
                id
        );
    }
}
