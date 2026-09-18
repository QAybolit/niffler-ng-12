package guru.qa.niffler.page;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class EditSpendingPage {
    private final SelenideElement amountInput = $("#amount");
    private final SelenideElement descriptionInput = $("#description");
    private final SelenideElement saveBtn = $("#save");

    public EditSpendingPage setNewSpendingDescription(String description) {
        descriptionInput.clear();
        descriptionInput.val(description);
        return this;
    }

    public EditSpendingPage setNewSpendingAmount(String amount) {
        amountInput.clear();
        amountInput.val(amount);
        return this;
    }

    public MainPage save() {
        saveBtn.click();
        return new MainPage();
    }
}
