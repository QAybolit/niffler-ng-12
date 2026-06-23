package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.AuthAuthorityDao;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.Authority;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.UUID;

public class AuthAuthorityDaoJdbc implements AuthAuthorityDao {

    private static final Config CFG = Config.getInstance();

    private final Connection connection;

    public AuthAuthorityDaoJdbc(Connection connection) {
        this.connection = connection;
    }

    @Override
    public AuthorityEntity create(AuthorityEntity authority) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO authority (user_id, authority)" +
                        " VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            ps.setObject(1, authority.getUser().getId());
            ps.setString(2, authority.getAuthority().name());

            ps.executeUpdate();

            final UUID generatedKey;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    generatedKey = rs.getObject("id", UUID.class);
                } else {
                    throw new SQLException("Can't find id in ResultSet");
                }
            }
            authority.setId(generatedKey);
            return authority;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<AuthorityEntity> findUserByById(UUID id) {
        try (PreparedStatement ps = connection.prepareStatement(
                """
                        SELECT at.id, at.authority, at.user_id, us.username, us.enabled, us.account_non_expired, us.account_non_locked, us.credentials_non_expired
                        FROM authority AS at
                        JOIN user AS us ON at.user_id = us.id
                        WHERE id = ?
                        """
        )) {
            ps.setObject(1, id);
            ps.execute();

            try (ResultSet rs = ps.getResultSet()) {
                if (rs.next()) {
                    AuthorityEntity ae = new AuthorityEntity();
                    ae.setId(rs.getObject("id", UUID.class));
                    ae.setAuthority(Authority.valueOf(rs.getString("authority")));

                    AuthUserEntity user = new AuthUserEntity();
                    user.setId(rs.getObject("user_id", UUID.class));
                    user.setUsername(rs.getString("username"));
                    user.setEnabled(rs.getBoolean("enabled"));
                    user.setAccountNonLocked(rs.getBoolean("account_non_locked"));
                    user.setAccountNonExpired(rs.getBoolean("account_non_expired"));
                    user.setCredentialsNonExpired(rs.getBoolean("credentials_non_expired"));

                    ae.setUser(user);
                    return Optional.of(ae);
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAuthority(UUID id) {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM authority WHERE id = ?"
        )) {
            ps.setObject(1, id);
            int result = ps.executeUpdate();

            if (result <= 0) {
                throw new SQLException("Can't find authority id in table 'authority'");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
