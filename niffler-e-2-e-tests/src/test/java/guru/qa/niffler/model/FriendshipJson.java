package guru.qa.niffler.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import guru.qa.niffler.data.entity.userdata.FriendshipEntity;
import guru.qa.niffler.data.entity.userdata.FriendshipStatus;

import java.util.Date;

public record FriendshipJson(
        @JsonProperty("requester_id")
        UserJson requester,
        @JsonProperty("addressee_id")
        UserJson addressee,
        @JsonProperty("created_date")
        Date createdDate,
        @JsonProperty("status")
        FriendshipStatus status
) {

    public static FriendshipJson fromEntity(FriendshipEntity entity) {
        return new FriendshipJson(
                UserJson.fromEntity(entity.getRequester()),
                UserJson.fromEntity(entity.getAddressee()),
                entity.getCreatedDate(),
                entity.getStatus()
        );
    }
}
