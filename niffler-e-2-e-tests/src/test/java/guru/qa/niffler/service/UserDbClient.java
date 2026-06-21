package guru.qa.niffler.service;

import guru.qa.niffler.data.dao.UserdataUserDao;
import guru.qa.niffler.data.dao.impl.UserdataUserDaoJdbc;
import guru.qa.niffler.data.entity.userdata.UserEntity;
import guru.qa.niffler.model.UserJson;

import java.util.Optional;
import java.util.UUID;

public class UserDbClient implements UserClient {

    private final UserdataUserDao userdataUserDao = new UserdataUserDaoJdbc();

    @Override
    public UserJson createUser(UserJson userJson) {
        UserEntity userEntity = UserEntity.fromJson(userJson);
        return UserJson.fromEntity(userdataUserDao.create(userEntity));
    }

    @Override
    public Optional<UserJson> findUserById(String id) {
        Optional<UserEntity> userEntity = userdataUserDao.findById(UUID.fromString(id));
        return userEntity.map(UserJson::fromEntity);
    }

    @Override
    public Optional<UserJson> findUserByUsername(String username) {
        Optional<UserEntity> userEntity = userdataUserDao.findByUsername(username);
        return userEntity.map(UserJson::fromEntity);
    }

    @Override
    public void deleteUser(UserJson user) {
        userdataUserDao.delete(UserEntity.fromJson(user));
    }
}
