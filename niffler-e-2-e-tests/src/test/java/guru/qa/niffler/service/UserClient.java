package guru.qa.niffler.service;

import guru.qa.niffler.model.UserJson;

import java.util.List;
import java.util.Optional;

public interface UserClient {

    UserJson createUser(String username, String password);

    Optional<UserJson> findUserById(String id);

    Optional<UserJson> findUserByUsername(String username);

    void createIncomeInvitation(UserJson targetUser, int count);

    void createOutcomeInvitation(UserJson targetUser, int count);

    void createFriends(UserJson targetUser, int count);

    List<UserJson> findAllUsers();

    void deleteUser(UserJson user);
}
