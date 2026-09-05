package guru.qa.niffler.service;

import com.github.javafaker.Faker;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.Authority;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.entity.userdata.UserEntity;
import guru.qa.niffler.data.repository.AuthUserRepository;
import guru.qa.niffler.data.repository.UserdataUserRepository;
import guru.qa.niffler.data.repository.impl.AuthUserRepositoryHibernate;
import guru.qa.niffler.data.repository.impl.UserdataUserRepositoryHibernate;
import guru.qa.niffler.data.tpl.XaTransactionTemplate;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.UserJson;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserDbClient implements UserClient {

    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private Faker faker = new Faker();

    private final AuthUserRepository authUserRepository = new AuthUserRepositoryHibernate();
    private final UserdataUserRepository userdataUserRepository = new UserdataUserRepositoryHibernate();

    private final XaTransactionTemplate xaTxTemplate = new XaTransactionTemplate(
            CFG.authJdbcUrl(),
            CFG.userdataJdbcUrl()
    );

    @Override
    public UserJson createUser(String username, String password) {
        return xaTxTemplate.execute(() -> {
            AuthUserEntity authUser = getAuthUserEntity(username, password);
            authUserRepository.create(authUser);
            return UserJson.fromEntity(
                    userdataUserRepository.create(userEntity(username))
            );
        });
    }

    @Override
    public Optional<UserJson> findUserById(String id) {
        return xaTxTemplate.execute(() -> {
                    Optional<UserEntity> userEntity = userdataUserRepository.findById(UUID.fromString(id));
                    return userEntity.map(UserJson::fromEntity);
                }
        );
    }

    @Override
    public Optional<UserJson> findUserByUsername(String username) {
        return xaTxTemplate.execute(() -> {
                    Optional<UserEntity> userEntity = userdataUserRepository.findByUsername(username);
                    return userEntity.map(UserJson::fromEntity);
                }
        );
    }

    @Override
    public List<UserJson> createIncomeInvitation(UserJson targetUser, int count) {
        List<UserJson> users = new ArrayList<>();

        if (count > 0) {
            UserEntity targetEntity = userdataUserRepository.findById(targetUser.id()).orElseThrow();

            for (int i = 0; i < count; i++) {
                xaTxTemplate.execute(() -> {
                    String username = faker.name().username();
                    AuthUserEntity authUser = getAuthUserEntity(username, "12345");
                    authUserRepository.create(authUser);
                    UserEntity addressee = userdataUserRepository.create(userEntity(username));
                    userdataUserRepository.sendInvitation(addressee, targetEntity);
                    users.add(UserJson.fromEntity(addressee));
                    return null;
                });
            }
        }
        return users;
    }

    @Override
    public List<UserJson> createOutcomeInvitation(UserJson targetUser, int count) {
        List<UserJson> users = new ArrayList<>();

        if (count > 0) {
            UserEntity targetEntity = userdataUserRepository.findById(targetUser.id()).orElseThrow();

            for (int i = 0; i < count; i++) {
                xaTxTemplate.execute(() -> {
                    String username = faker.name().username();
                    AuthUserEntity authUser = getAuthUserEntity(username, "12345");
                    authUserRepository.create(authUser);
                    UserEntity addressee = userdataUserRepository.create(userEntity(username));
                    userdataUserRepository.sendInvitation(targetEntity, addressee);
                    users.add(UserJson.fromEntity(addressee));
                    return null;
                });
            }
        }
        return users;
    }

    @Override
    public List<UserJson> createFriends(UserJson targetUser, int count) {
        List<UserJson> users = new ArrayList<>();

        if (count > 0) {
            UserEntity targetEntity = userdataUserRepository.findById(targetUser.id()).orElseThrow();

            for (int i = 0; i < count; i++) {
                xaTxTemplate.execute(() -> {
                    String username = faker.name().username();
                    AuthUserEntity authUser = getAuthUserEntity(username, "12345");
                    authUserRepository.create(authUser);
                    UserEntity addressee = userdataUserRepository.create(userEntity(username));
                    userdataUserRepository.addFriend(targetEntity, addressee);
                    users.add(UserJson.fromEntity(addressee));
                    return null;
                });
            }
        }
        return users;
    }

    @Override
    public List<UserJson> findAllUsers() {
        return xaTxTemplate.execute(() -> {
                    List<UserEntity> userEntities = userdataUserRepository.findAll();
                    return userEntities.stream()
                            .map(UserJson::fromEntity)
                            .toList();
                }
        );
    }

    @Override
    public void deleteUser(UserJson user) {
        xaTxTemplate.execute(() -> {
            Optional<UserEntity> userEntity = userdataUserRepository.findById(user.id());
            if (userEntity.isPresent()) {
                userdataUserRepository.remove(userEntity.get());
            } else {
                throw new RuntimeException("Can't delete category with id " + user.id());
            }
        });
    }

    private UserEntity userEntity(String username) {
        UserEntity ue = new UserEntity();
        ue.setUsername(username);
        ue.setCurrency(CurrencyValues.RUB);
        return ue;
    }

    private static AuthUserEntity getAuthUserEntity(String username, String password) {
        AuthUserEntity authUser = new AuthUserEntity();
        authUser.setUsername(username);
        authUser.setPassword(passwordEncoder.encode(password));
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
}
