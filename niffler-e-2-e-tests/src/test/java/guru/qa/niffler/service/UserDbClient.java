package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.impl.UserdataUserDaoJdbc;
import guru.qa.niffler.data.entity.userdata.UserEntity;
import guru.qa.niffler.model.UserJson;

import java.sql.Connection;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.Databases.transaction;

public class UserDbClient implements UserClient {

    private static final Config CFG = Config.getInstance();

    @Override
    public UserJson createUser(UserJson userJson) {
        return transaction(connection -> {
                    UserEntity userEntity = UserEntity.fromJson(userJson);
                    return UserJson.fromEntity(new UserdataUserDaoJdbc(connection)
                            .create(userEntity));
                },
                CFG.userdataJdbcUrl(),
                Connection.TRANSACTION_READ_COMMITTED
        );

    }

    @Override
    public Optional<UserJson> findUserById(String id) {
        return transaction(connection -> {
                    Optional<UserEntity> userEntity = new UserdataUserDaoJdbc(connection)
                            .findById(UUID.fromString(id));
                    return userEntity.map(UserJson::fromEntity);
                },
                CFG.userdataJdbcUrl(),
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    @Override
    public Optional<UserJson> findUserByUsername(String username) {
        return transaction(connection -> {
                    Optional<UserEntity> userEntity = new UserdataUserDaoJdbc(connection)
                            .findByUsername(username);
                    return userEntity.map(UserJson::fromEntity);
                },
                CFG.userdataJdbcUrl(),
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    @Override
    public void deleteUser(UserJson user) {
        transaction(connection -> {
                    new UserdataUserDaoJdbc(connection).delete(UserEntity.fromJson(user));
                },
                CFG.userdataJdbcUrl(),
                Connection.TRANSACTION_READ_COMMITTED
        );
    }
}
