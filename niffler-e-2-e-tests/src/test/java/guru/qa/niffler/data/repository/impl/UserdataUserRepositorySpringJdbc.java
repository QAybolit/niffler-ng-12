package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.userdata.FriendshipStatus;
import guru.qa.niffler.data.entity.userdata.UserEntity;
import guru.qa.niffler.data.mapper.UserEntityRowMapper;
import guru.qa.niffler.data.repository.UserdataUserRepository;
import guru.qa.niffler.data.tpl.DataSources;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserdataUserRepositorySpringJdbc implements UserdataUserRepository {

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
    public UserEntity update(UserEntity user) {
        template.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE \"user\" SET username = ?, currency = ?, firstname = ?, surname = ?, photo = ?, photo_small = ?, full_name = ?" +
                            " WHERE id = ?"
            );
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getCurrency().name());
            ps.setString(3, user.getFirstname());
            ps.setString(4, user.getSurname());
            ps.setBytes(5, user.getPhoto());
            ps.setBytes(6, user.getPhotoSmall());
            ps.setString(7, user.getFullname());
            return ps;
        });

        return user;
    }

    @Override
    public void sendInvitation(UserEntity requester, UserEntity addressee) {
        template.update(con -> {
                    PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO friendship (requester_id, addressee_id, status, created_date) VALUES (?, ?, ?, ?)"
                    );
                    ps.setObject(1, requester.getId());
                    ps.setObject(2, addressee.getId());
                    ps.setString(3, FriendshipStatus.PENDING.name());
                    ps.setObject(4, new java.sql.Date(new Date().getTime()));
                    return ps;
                }
        );
    }

    @Override
    public void addFriend(UserEntity requester, UserEntity addressee) {
        List<UserEntity> users = template.query(
                "SELECT * FROM \"user\" WHERE requester_id = ? AND addressee_id = ? OR requester_id = ? AND addressee_id = ?",
                UserEntityRowMapper.INSTANCE
        );

        if (users.size() != 2) {
            throw new RuntimeException("Both Income Invitation and Outcome Invitation must be present");
        }

        template.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE friendship SET status = ?" +
                            " WHERE requester_id = ? AND addressee_id = ? OR requester_id = ? AND addressee_id = ?"
            );
            ps.setObject(1, FriendshipStatus.ACCEPTED.name());
            ps.setObject(2, requester.getId());
            ps.setObject(3, addressee.getId());
            ps.setObject(4, addressee.getId());
            ps.setObject(5, requester.getId());

            return ps;
        });
    }

    @Override
    public List<UserEntity> findAll() {
        return template.query(
                "SELECT * FROM \"user\"",
                UserEntityRowMapper.INSTANCE
        );
    }

    @Override
    public void remove(UserEntity user) {
        template.update("DELETE FROM \"user\" WHERE id = ?", user.getId());
    }
}
