package guru.qa.niffler.data.dao;

import guru.qa.niffler.data.entity.userdata.UserEntity;

public interface FriendshipDao {

    void addIncomeInvitation(UserEntity requester, UserEntity addressee);

    void addOutcomeInvitation(UserEntity requester, UserEntity addressee);

    void addFriend(UserEntity requester, UserEntity addressee);
}
