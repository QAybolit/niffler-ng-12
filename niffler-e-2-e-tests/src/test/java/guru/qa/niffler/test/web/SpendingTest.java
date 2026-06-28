package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.Spending;
import guru.qa.niffler.jupiter.annotation.meta.WebTest;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.page.LoginPage;
import org.junit.jupiter.api.Test;

@WebTest
public class SpendingTest {

    private static final Config CFG = Config.getInstance();

    @Spending(
            username = "Dina",
            category = "Покупки",
            amount = 50000
    )
    @Test
    void mainPageShouldBeDisplayedAfterSuccessLogin(SpendJson spendJson) {
        final String newDescription = "Покупки в Авивпарке";

        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .successLogin("Dina", "12345")
                .checkThatPageLoaded()
                .editSpending(spendJson.description())
                .setNewSpendingDescription(newDescription)
                .save()
                .checkThatTableContainsSpending(newDescription);
    }
}
