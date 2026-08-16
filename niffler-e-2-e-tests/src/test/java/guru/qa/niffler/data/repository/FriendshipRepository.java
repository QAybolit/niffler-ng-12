package guru.qa.niffler.data.repository;

import guru.qa.niffler.data.entity.userdata.UserEntity;

public interface FriendshipRepository {

    void addIncomeInvitation(UserEntity requester, UserEntity addressee);

    void addOutcomeInvitation(UserEntity requester, UserEntity addressee);

    void addFriend(UserEntity requester, UserEntity addressee);
}
