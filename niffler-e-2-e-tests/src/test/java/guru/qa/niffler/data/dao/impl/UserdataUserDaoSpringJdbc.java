package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.UserdataUserDao;
import guru.qa.niffler.data.entity.userdata.UserEntity;
import guru.qa.niffler.data.mapper.UserEntityRowMapper;
import guru.qa.niffler.data.tpl.DataSources;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserdataUserDaoSpringJdbc implements UserdataUserDao {

    private final JdbcTemplate template = new JdbcTemplate(DataSources.dataSource(CFG.userdataJdbcUrl()));
    private static final Config CFG = Config.getInstance();

    @Override
    public UserEntity create(UserEntity user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO \"user\" (username, currency, firstname, surname, photo, photo_small, full_name)" +
                            " VALUES (?, ?, ?, ?, ?, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getCurrency().name());
            ps.setString(3, user.getFirstname());
            ps.setString(4, user.getSurname());
            ps.setBytes(5, user.getPhoto());
            ps.setBytes(6, user.getPhotoSmall());
            ps.setString(7, user.getFullname());
            return ps;
        }, keyHolder);

        final UUID generatedKey = (UUID) keyHolder.getKeys().get("id");
        user.setId(generatedKey);

        return user;
    }

    @Override
    public Optional<UserEntity> findById(UUID id) {
        return Optional.ofNullable(
                template.queryForObject(
                        "SELECT * FROM \"user\" WHERE id = ?",
                        UserEntityRowMapper.INSTANCE,
                        id
                )
        );
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        return Optional.ofNullable(
                template.queryForObject(
                        "SELECT * FROM \"user\" WHERE username = ?",
                        UserEntityRowMapper.INSTANCE,
                        username
                )
        );
    }

    @Override
    public List<UserEntity> findAll() {
        return template.query(
                "SELECT * FROM \"user\"",
                UserEntityRowMapper.INSTANCE
        );
    }

    @Override
    public void delete(UserEntity user) {
        template.update("DELETE FROM \"user\" WHERE id = ?", user.getId());
    }
}
