package guru.qa.niffler.service;

import guru.qa.niffler.model.UserJson;

import java.util.Optional;

public interface UserClient {

    UserJson createUser(UserJson userJson);

    Optional<UserJson> findUserById(String id);

    Optional<UserJson> findUserByUsername(String username);

    void deleteUser(UserJson user);
}
