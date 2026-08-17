package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.AuthAuthorityDao;
import guru.qa.niffler.data.dao.AuthUserDao;
import guru.qa.niffler.data.dao.UserdataUserDao;
import guru.qa.niffler.data.dao.impl.AuthAuthorityDaoJdbc;
import guru.qa.niffler.data.dao.impl.AuthAuthorityDaoSpringJdbc;
import guru.qa.niffler.data.dao.impl.AuthUserDaoJdbc;
import guru.qa.niffler.data.dao.impl.AuthUserDaoSpringJdbc;
import guru.qa.niffler.data.dao.impl.UserdataUserDaoJdbc;
import guru.qa.niffler.data.dao.impl.UserdataUserDaoSpringJdbc;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.Authority;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.entity.userdata.UserEntity;
import guru.qa.niffler.data.repository.AuthUserRepository;
import guru.qa.niffler.data.repository.UserdataUserRepository;
import guru.qa.niffler.data.repository.impl.AuthUserRepositoryHibernate;
import guru.qa.niffler.data.repository.impl.AuthUserRepositoryJdbc;
import guru.qa.niffler.data.repository.impl.UserdataUserRepositoryHibernate;
import guru.qa.niffler.data.tpl.DataSources;
import guru.qa.niffler.data.tpl.JdbcTransactionTemplate;
import guru.qa.niffler.data.tpl.XaTransactionTemplate;
import guru.qa.niffler.model.UserJson;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserDbClient implements UserClient {

    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    // JDBC
    private final AuthUserDao authUserDaoJdbc = new AuthUserDaoJdbc();
    private final AuthAuthorityDao authAuthorityDaoJdbc = new AuthAuthorityDaoJdbc();
    private final UserdataUserDao userdataUserDaoJdbc = new UserdataUserDaoJdbc();

    // Spring JDBC
    private final AuthUserDao authUserDaoSpringJdbc = new AuthUserDaoSpringJdbc();
    private final AuthAuthorityDao authAuthorityDaoSpringJdbc = new AuthAuthorityDaoSpringJdbc();
    private final UserdataUserDao userdataUserDaoSpringJdbc = new UserdataUserDaoSpringJdbc();

    // JDBC Repository
    private final AuthUserRepository authUserRepositoryJdbc = new AuthUserRepositoryJdbc();

    // Hibernate
    private final AuthUserRepository authUserRepositoryHibernate = new AuthUserRepositoryHibernate();
    private final UserdataUserRepository userdataUserRepositoryHibernate = new UserdataUserRepositoryHibernate();


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

    private final TransactionTemplate txTemplate = new TransactionTemplate(
            new ChainedTransactionManager(
                    new JdbcTransactionManager(DataSources.dataSource(CFG.authJdbcUrl())),
                    new JdbcTransactionManager(DataSources.dataSource(CFG.userdataJdbcUrl()))
            )
    );

    public UserJson createUserHibernate(UserJson user) {
        return xaTxTemplate.execute(() -> {
            AuthUserEntity authUser = getAuthUserEntity(user);
            authUserRepositoryHibernate.create(authUser);
            return UserJson.fromEntity(
                    userdataUserRepositoryHibernate
                            .create(UserEntity.fromJson(user))
            );
        });
    }

    public void addIncomeInvitation(UserJson target, int count) {
        if (count > 0) {
            UserEntity targetEntity = userdataUserRepositoryHibernate.findById(target.id()).orElseThrow();

            for (int i = 1; i <= count; i++) {
                xaTxTemplate.execute(() -> {
                    // TODO доделать
                    AuthUserEntity authUser = getAuthUserEntity(target);
                    authUserRepositoryHibernate.create(authUser);
                    return UserJson.fromEntity(
                            userdataUserRepositoryHibernate
                                    .create(UserEntity.fromJson(target))
                    );
                });
            }

        }
    }

    @NotNull
    private static AuthUserEntity getAuthUserEntity(UserJson user) {
        AuthUserEntity authUser = new AuthUserEntity();
        authUser.setUsername(user.username());
        authUser.setPassword(passwordEncoder.encode("12345"));
        authUser.setEnabled(true);
        authUser.setAccountNonExpired(true);
        authUser.setAccountNonLocked(true);
        authUser.setCredentialsNonExpired(true);
        authUser.setAuthorities(
                Arrays.stream(Authority.values())
                        .map(e -> {
                                    AuthorityEntity ae = new AuthorityEntity();
                                    ae.setUser(authUser);
                                    ae.setAuthority(e);
                                    return ae;
                                }
                        ).toList()
        );
        return authUser;
    }


    // ============= JDBC без транзакций =============
    @Override
    public UserJson createUser(UserJson userJson) {
        UserEntity userEntity = UserEntity.fromJson(userJson);
        return UserJson.fromEntity(userdataUserDaoJdbc.create(userEntity));
    }

    // ============= JDBC с транзакциями =============
    public UserJson createUserTxJdbc(UserJson userJson) {
        return jdbcTxTemplate.execute(() -> {
                    UserEntity userEntity = UserEntity.fromJson(userJson);
                    return UserJson.fromEntity(userdataUserDaoJdbc.create(userEntity));
                },
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    // ============= SpringJDBC без транзакций =============
    public UserJson createUserSpringJdbc(UserJson user) {
        AuthUserEntity authUser = new AuthUserEntity();
        authUser.setUsername(user.username());
        authUser.setPassword(passwordEncoder.encode("12345"));
        authUser.setEnabled(true);
        authUser.setAccountNonExpired(true);
        authUser.setAccountNonLocked(true);
        authUser.setCredentialsNonExpired(true);

        AuthUserEntity createAuthUser = authUserDaoSpringJdbc.create(authUser);
        AuthorityEntity[] authorities = Arrays.stream(Authority.values())
                .map(e -> {
                            AuthorityEntity ae = new AuthorityEntity();
                            ae.setUser(createAuthUser);
                            ae.setAuthority(e);
                            return ae;
                        }
                ).toArray(AuthorityEntity[]::new);
        authAuthorityDaoSpringJdbc.create(authorities);

        return UserJson.fromEntity(
                new UserdataUserDaoSpringJdbc()
                        .create(UserEntity.fromJson(user))
        );
    }

    // ============= SpringJDBC с транзакциями =============
    public UserJson createUserTxSpringJdbc(UserJson user) {
        return xaTxTemplate.execute(() -> {
            AuthUserEntity authUser = new AuthUserEntity();
            authUser.setUsername(user.username());
            authUser.setPassword(passwordEncoder.encode("12345"));
            authUser.setEnabled(true);
            authUser.setAccountNonExpired(true);
            authUser.setAccountNonLocked(true);
            authUser.setCredentialsNonExpired(true);

            AuthUserEntity createAuthUser = authUserDaoSpringJdbc.create(authUser);
            AuthorityEntity[] authorities = Arrays.stream(Authority.values())
                    .map(e -> {
                                AuthorityEntity ae = new AuthorityEntity();
                                ae.setUser(createAuthUser);
                                ae.setAuthority(e);
                                return ae;
                            }
                    ).toArray(AuthorityEntity[]::new);
            authAuthorityDaoSpringJdbc.create(authorities);

            return UserJson.fromEntity(
                    new UserdataUserDaoSpringJdbc()
                            .create(UserEntity.fromJson(user))
            );
        });

    }

    // ============= JDBC Repository с транзакциями =============
    public UserJson createUserTxJdbcRepository(UserJson user) {
        return xaTxTemplate.execute(() -> {
            AuthUserEntity authUser = new AuthUserEntity();
            authUser.setUsername(user.username());
            authUser.setPassword(passwordEncoder.encode("12345"));
            authUser.setEnabled(true);
            authUser.setAccountNonExpired(true);
            authUser.setAccountNonLocked(true);
            authUser.setCredentialsNonExpired(true);
            authUser.setAuthorities(
                    Arrays.stream(Authority.values())
                            .map(e -> {
                                        AuthorityEntity ae = new AuthorityEntity();
                                        ae.setUser(authUser);
                                        ae.setAuthority(e);
                                        return ae;
                                    }
                            ).toList()
            );

            authUserRepositoryJdbc.create(authUser);

            return UserJson.fromEntity(
                    new UserdataUserDaoSpringJdbc()
                            .create(UserEntity.fromJson(user))
            );
        });

    }

    // ============= SpringJDBC с транзакциями =============
    public UserJson createUserChainTxSpringJdbc(UserJson user) {
        return txTemplate.execute(status -> {
            AuthUserEntity authUser = new AuthUserEntity();
            authUser.setUsername(user.username());
            authUser.setPassword(passwordEncoder.encode("12345"));
            authUser.setEnabled(true);
            authUser.setAccountNonExpired(true);
            authUser.setAccountNonLocked(true);
            authUser.setCredentialsNonExpired(true);

            AuthUserEntity createAuthUser = authUserDaoSpringJdbc.create(authUser);
            AuthorityEntity[] authorities = Arrays.stream(Authority.values())
                    .map(e -> {
                                AuthorityEntity ae = new AuthorityEntity();
                                ae.setUser(createAuthUser);
                                ae.setAuthority(e);
                                return ae;
                            }
                    ).toArray(AuthorityEntity[]::new);
            authAuthorityDaoSpringJdbc.create(authorities);

            return UserJson.fromEntity(
                    new UserdataUserDaoSpringJdbc()
                            .create(UserEntity.fromJson(user))
            );
        });

    }

    // ============= JDBC без транзакций =============
    @Override
    public Optional<UserJson> findUserById(String id) {
        Optional<UserEntity> userEntity = userdataUserDaoJdbc.findById(UUID.fromString(id));
        return userEntity.map(UserJson::fromEntity);
    }

    // ============= JDBC с транзакциями =============
    public Optional<UserJson> findUserByIdTxJdbc(String id) {
        return jdbcTxTemplate.execute(() -> {
                    Optional<UserEntity> userEntity = userdataUserDaoJdbc.findById(UUID.fromString(id));
                    return userEntity.map(UserJson::fromEntity);
                },
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    // ============= SpringJDBC без транзакций =============
    public Optional<UserJson> findUserByIdSpringJdbc(String id) {
        Optional<UserEntity> userEntity = userdataUserDaoSpringJdbc.findById(UUID.fromString(id));
        return userEntity.map(UserJson::fromEntity);
    }

    // ============= SpringJDBC с транзакциями =============
    public Optional<UserJson> findUserByIdTxSpringJdbc(String id) {
        return xaTxTemplate.execute(() -> {
                    Optional<UserEntity> userEntity = userdataUserDaoSpringJdbc.findById(UUID.fromString(id));
                    return userEntity.map(UserJson::fromEntity);
                }
        );
    }

    // ============= JDBC без транзакций =============
    @Override
    public Optional<UserJson> findUserByUsername(String username) {
        Optional<UserEntity> userEntity = userdataUserDaoJdbc.findByUsername(username);
        return userEntity.map(UserJson::fromEntity);
    }

    // ============= JDBC с транзакциями =============
    public Optional<UserJson> findUserByUsernameTxJdbc(String username) {
        return jdbcTxTemplate.execute(() -> {
                    Optional<UserEntity> userEntity = userdataUserDaoJdbc.findByUsername(username);
                    return userEntity.map(UserJson::fromEntity);
                },
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    // ============= SpringJDBC без транзакций =============
    public Optional<UserJson> findUserByUsernameSpringJdbc(String username) {
        Optional<UserEntity> userEntity = userdataUserDaoSpringJdbc.findByUsername(username);
        return userEntity.map(UserJson::fromEntity);
    }

    // ============= SpringJDBC с транзакциями =============
    public Optional<UserJson> findUserByUsernameTxSpringJdbc(String username) {
        return xaTxTemplate.execute(() -> {
                    Optional<UserEntity> userEntity = userdataUserDaoSpringJdbc.findByUsername(username);
                    return userEntity.map(UserJson::fromEntity);
                }
        );
    }

    // ============= JDBC без транзакций =============
    @Override
    public void deleteUser(UserJson user) {
        userdataUserDaoJdbc.delete(UserEntity.fromJson(user));
    }

    // ============= JDBC с транзакциями =============
    public void deleteUserTxJdbc(UserJson user) {
        jdbcTxTemplate.execute(
                () -> userdataUserDaoJdbc.delete(UserEntity.fromJson(user)),
                Connection.TRANSACTION_READ_COMMITTED
        );
    }

    // ============= SpringJDBC без транзакций =============
    public void deleteUserSpringJdbc(UserJson user) {
        userdataUserDaoSpringJdbc.delete(UserEntity.fromJson(user));
    }

    // ============= SpringJDBC с транзакциями =============
    public void deleteUserTxSpringJdbc(UserJson user) {
        xaTxTemplate.execute(() -> userdataUserDaoSpringJdbc.delete(UserEntity.fromJson(user)));
    }

    public List<AuthUserEntity> findAll() {
        return authUserDaoJdbc.findAll();
    }
}
