package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.FriendshipDao;
import guru.qa.niffler.data.entity.userdata.FriendshipStatus;
import guru.qa.niffler.data.entity.userdata.UserEntity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import static guru.qa.niffler.data.tpl.Connections.holder;

public class FriendshipDaoJdbc implements FriendshipDao {

    private static final Config CFG = Config.getInstance();

    @Override
    public void addIncomeInvitation(UserEntity requester, UserEntity addressee) {
        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(
                "INSERT INTO friendship (requester_id, addressee_id, status, created_date)" +
                        " VALUES (?, ?, ?, ?)"
        )) {
            ps.setObject(1, requester.getId());
            ps.setObject(2, addressee.getId());
            ps.setString(3, FriendshipStatus.PENDING.name());
            ps.setObject(4, new java.sql.Date(new Date().getTime()));

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addOutcomeInvitation(UserEntity requester, UserEntity addressee) {
        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(
                "INSERT INTO friendship (requester_id, addressee_id, status, created_date)" +
                        " VALUES (?, ?, ?, ?)"
        )) {
            ps.setObject(1, requester.getId());
            ps.setObject(2, addressee.getId());
            ps.setString(3, FriendshipStatus.PENDING.name());
            ps.setObject(4, new java.sql.Date(new Date().getTime()));

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addFriend(UserEntity requester, UserEntity addressee) {
        try (PreparedStatement checkPs = holder(CFG.spendJdbcUrl()).connection().prepareStatement(
                "SELECT COUNT(*) AS total FROM friendship" +
                        " WHERE requester_id = ? AND addressee_id = ? OR requester_id = ? AND addressee_id = ?");
             PreparedStatement friendPs = holder(CFG.spendJdbcUrl()).connection().prepareStatement(
                     "UPDATE friendship SET status = ?" +
                             "  WHERE requester_id = ? AND addressee_id = ? OR requester_id = ? AND addressee_id = ?"
             )) {
            checkPs.setObject(1, requester.getId());
            checkPs.setObject(2, addressee.getId());
            checkPs.setObject(3, addressee.getId());
            checkPs.setObject(4, requester.getId());
            checkPs.execute();

            try (ResultSet rs = checkPs.getResultSet()) {
                int count = rs.getInt("total");
                if (count != 2) {
                    throw new RuntimeException("Both Income Invitation and Outcome Invitation must be present");
                }
            }

            friendPs.setObject(1, FriendshipStatus.ACCEPTED.name());
            friendPs.setObject(2, requester.getId());
            friendPs.setObject(3, addressee.getId());
            friendPs.setObject(4, addressee.getId());
            friendPs.setObject(5, requester.getId());

            friendPs.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
