package guru.qa.niffler.data.extractor;

import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.Authority;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthUserEntityResultSetExtractor implements ResultSetExtractor<AuthUserEntity> {

    @Override
    public AuthUserEntity extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<UUID, AuthUserEntity> userMap = new ConcurrentHashMap<>();
        UUID userId = null;
        while (rs.next()) {
            userId = rs.getObject("id", UUID.class);
            AuthUserEntity user = userMap.computeIfAbsent(userId, id -> {
                try {
                    AuthUserEntity authUser = new AuthUserEntity();
                    authUser.setId(rs.getObject("user_id", UUID.class));
                    authUser.setUsername(rs.getString("username"));
                    authUser.setEnabled(rs.getBoolean("enabled"));
                    authUser.setAccountNonLocked(rs.getBoolean("account_non_locked"));
                    authUser.setAccountNonExpired(rs.getBoolean("account_non_expired"));
                    authUser.setCredentialsNonExpired(rs.getBoolean("credentials_non_expired"));

                    return authUser;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            AuthorityEntity authority = new AuthorityEntity();
            authority.setId(rs.getObject("authority_id", UUID.class));
            authority.setAuthority(Authority.valueOf(rs.getString("authority")));
            user.getAuthorities().add(authority);
        }
        return userMap.get(userId);
    }
}
