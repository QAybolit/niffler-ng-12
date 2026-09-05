package guru.qa.niffler.service;

import guru.qa.niffler.model.UserJson;

import java.util.List;
import java.util.Optional;

public interface UserClient {

    UserJson createUser(String username, String password);

    Optional<UserJson> findUserById(String id);

    Optional<UserJson> findUserByUsername(String username);

    List<UserJson> createIncomeInvitation(UserJson targetUser, int count);

    List<UserJson> createOutcomeInvitation(UserJson targetUser, int count);

    List<UserJson> createFriends(UserJson targetUser, int count);

    List<UserJson> findAllUsers();

    void deleteUser(UserJson user);
}
