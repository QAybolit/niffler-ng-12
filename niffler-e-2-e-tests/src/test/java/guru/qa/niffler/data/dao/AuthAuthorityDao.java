package guru.qa.niffler.data.dao;

import guru.qa.niffler.data.entity.auth.AuthorityEntity;

import java.util.Optional;
import java.util.UUID;

public interface AuthAuthorityDao {

    AuthorityEntity create(AuthorityEntity authority);

    Optional<AuthorityEntity> findUserByById(UUID id);

    void deleteAuthority(UUID id);
}
