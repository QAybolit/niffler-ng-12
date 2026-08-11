package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.AuthUserDao;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.mapper.AuthUserEntityRowMapper;
import guru.qa.niffler.data.tpl.DataSources;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AuthUserDaoSpringJdbc implements AuthUserDao {

    private static final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private final JdbcTemplate template = new JdbcTemplate(DataSources.dataSource(CFG.authJdbcUrl()));
    private static final Config CFG = Config.getInstance();

    @Override
    public AuthUserEntity create(AuthUserEntity user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO \\\"user\\\" (username, password, enabled, account_non_expired, account_non_locked, credentials_non_expired)" +
                            " VALUES (?, ?, ?, ?, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, user.getUsername());
            ps.setString(2, passwordEncoder.encode(user.getPassword()));
            ps.setBoolean(3, user.getEnabled());
            ps.setBoolean(4, user.getAccountNonExpired());
            ps.setBoolean(5, user.getAccountNonLocked());
            ps.setBoolean(6, user.getCredentialsNonExpired());

            return ps;
        }, keyHolder);

        final UUID generatedKey = (UUID) keyHolder.getKeys().get("id");
        user.setId(generatedKey);

        return user;
    }

    @Override
    public Optional<AuthUserEntity> findUserByById(UUID id) {
        return Optional.ofNullable(
                template.queryForObject(
                        "SELECT * FROM \"user\" WHERE id = ?",
                        AuthUserEntityRowMapper.INSTANCE,
                        id
                )
        );
    }

    @Override
    public List<AuthUserEntity> findAll() {
        List<AuthUserEntity> users = template.query(
                "SELECT * FROM \"user\"",
                AuthUserEntityRowMapper.INSTANCE
        );
        return users;
    }

    @Override
    public void deleteUserById(UUID id) {
        template.update("DELETE FROM \"user\" WHERE id = ?", id);
    }
}
