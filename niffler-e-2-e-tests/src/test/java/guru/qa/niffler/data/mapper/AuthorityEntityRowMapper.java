package guru.qa.niffler.data.mapper;

import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.Authority;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class AuthorityEntityRowMapper implements RowMapper<AuthorityEntity> {

    public static final AuthorityEntityRowMapper INSTANCE = new AuthorityEntityRowMapper();

    private AuthorityEntityRowMapper() {
    }

    @Override
    public AuthorityEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        AuthorityEntity result = new AuthorityEntity();
        result.setId(rs.getObject("id", UUID.class));
        result.setAuthority(Authority.valueOf(rs.getString("authority")));

        AuthUserEntity user = new AuthUserEntity();
        user.setId(rs.getObject("user_id", UUID.class));
        user.setUsername(rs.getString("username"));
        user.setEnabled(rs.getBoolean("enabled"));
        user.setAccountNonLocked(rs.getBoolean("account_non_locked"));
        user.setAccountNonExpired(rs.getBoolean("account_non_expired"));
        user.setCredentialsNonExpired(rs.getBoolean("credentials_non_expired"));

        result.setUser(user);
        return result;
    }
}
