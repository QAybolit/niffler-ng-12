package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.AuthAuthorityDao;
import guru.qa.niffler.data.dao.AuthUserDao;
import guru.qa.niffler.data.dao.UserdataUserDao;
import guru.qa.niffler.data.dao.impl.AuthAuthorityDaoJdbc;
import guru.qa.niffler.data.dao.impl.AuthUserDaoJdbc;
import guru.qa.niffler.data.dao.impl.UserdataUserDaoJdbc;
import guru.qa.niffler.data.dao.impl.UserdataUserDaoSpringJdbc;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.Authority;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.entity.userdata.UserEntity;
import guru.qa.niffler.data.tpl.DataSources;
import guru.qa.niffler.data.tpl.JdbcTransactionTemplate;
import guru.qa.niffler.data.tpl.XaTransactionTemplate;
import guru.qa.niffler.model.UserJson;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class UserDbClient implements UserClient {

    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final AuthUserDao authUserDao = new AuthUserDaoJdbc();
    private final AuthAuthorityDao authAuthorityDao = new AuthAuthorityDaoJdbc();
    private final UserdataUserDao userdataUserDao = new UserdataUserDaoJdbc();

    private final TransactionTemplate transactionTemplate = new TransactionTemplate(
            new JdbcTransactionManager(
                    DataSources.dataSource(CFG.authJdbcUrl())
            )
    );

    private final JdbcTransactionTemplate jdbcTxTemplate = new JdbcTransactionTemplate(CFG.userdataJdbcUrl());

    private final XaTransactionTemplate xaTxTemplate = new XaTransactionTemplate(
            CFG.authJdbcUrl(),
            CFG.userdataJdbcUrl()
    );

    public UserJson createUserSpringJdbc(UserJson user) {
        return xaTxTemplate.execute(() -> {
            AuthUserEntity authUser = new AuthUserEntity();
            authUser.setUsername(user.username());
            authUser.setPassword(passwordEncoder.encode("12345"));
            authUser.setEnabled(true);
            authUser.setAccountNonExpired(true);
            authUser.setAccountNonLocked(true);
            authUser.setCredentialsNonExpired(true);

            AuthUserEntity createAuthUser = authUserDao.create(authUser);
            AuthorityEntity[] authorities = Arrays.stream(Authority.values())
                    .map(e -> {
                                AuthorityEntity ae = new AuthorityEntity();
                                ae.setUser(createAuthUser);
                                ae.setAuthority(e);
                                return ae;
                            }
                    ).toArray(AuthorityEntity[]::new);
            authAuthorityDao.create(authorities);

            return UserJson.fromEntity(
                    new UserdataUserDaoSpringJdbc()
                            .create(UserEntity.fromJson(user))
            );
        });

    }

    @Override
    public UserJson createUser(UserJson userJson) {
        return jdbcTxTemplate.execute(() -> {
                    UserEntity userEntity = UserEntity.fromJson(userJson);
                    return UserJson.fromEntity(userdataUserDao.create(userEntity));
                },
                Connection.TRANSACTION_READ_COMMITTED
        );

    }

    @Override
    public Optional<UserJson> findUserById(String id) {
        return jdbcTxTemplate.execute(() -> {
                    Optional<UserEntity> userEntity = userdataUserDao.findById(UUID.fromString(id));
                    return userEntity.map(UserJson::fromEntity);
                },
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    @Override
    public Optional<UserJson> findUserByUsername(String username) {
        return jdbcTxTemplate.execute(() -> {
                    Optional<UserEntity> userEntity = userdataUserDao.findByUsername(username);
                    return userEntity.map(UserJson::fromEntity);
                },
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    @Override
    public void deleteUser(UserJson user) {
        jdbcTxTemplate.execute(
                () -> userdataUserDao.delete(UserEntity.fromJson(user)),
                Connection.TRANSACTION_READ_COMMITTED
        );
    }
}
