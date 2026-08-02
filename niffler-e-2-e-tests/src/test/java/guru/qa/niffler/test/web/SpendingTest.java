package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.Category;
import guru.qa.niffler.jupiter.annotation.Spending;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.annotation.meta.WebTest;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.page.LoginPage;
import org.junit.jupiter.api.Test;

import static guru.qa.niffler.utils.RandomDataUtils.randomSentence;

@WebTest
public class SpendingTest {

    private static final Config CFG = Config.getInstance();

    @User(
            username = "Dina",
            categories = @Category(
                    archived = false
            ),
            spendings = @Spending(
                    category = "Подарки",
                    description = "Покупки в Авиапарке",
                    amount = 10000
            )
    )
    @Test
    public void updateSpendingDescription(SpendJson spendJson) {
        final String newDescription = randomSentence(4);

        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .successLogin(spendJson.username(), "12345")
                .checkThatPageLoaded()
                .editSpending(spendJson.description())
                .setNewSpendingDescription(newDescription)
                .save()
                .checkThatTableContainsSpending(newDescription);
    }

    @User(
            username = "Dina",
            spendings = @Spending(
                    category = "Покупки",
                    description = "Овощи на рынке",
                    amount = 2000,
                    currency = CurrencyValues.RUB
            )
    )
    @Test
    public void updateSpendingAmount(SpendJson spendJson) {
        final String amount = "33333";

        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .successLogin(spendJson.username(), "12345")
                .checkThatPageLoaded()
                .editSpending(spendJson.description())
                .setNewSpendingAmount(amount)
                .save()
                .checkThatTableContainsSpending(amount);
    }
}
