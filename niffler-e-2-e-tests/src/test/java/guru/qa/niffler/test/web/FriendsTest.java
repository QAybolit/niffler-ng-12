package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.annotation.meta.WebTest;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.page.LoginPage;
import org.junit.jupiter.api.Test;


@WebTest
public class FriendsTest {

    private static final Config CFG = Config.getInstance();

    @User(
            friends = 1
    )
    @Test
    public void friendsShouldBePresentInFriendsTableTest(UserJson user) {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .successLogin(user.username(),  user.testData().password())
                .checkThatPageLoaded()
                .friendsPage()
                .checkExistingFriends(user.testData().friendsUsernames());
    }
}
