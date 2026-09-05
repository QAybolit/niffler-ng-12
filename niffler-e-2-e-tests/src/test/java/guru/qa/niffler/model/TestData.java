package guru.qa.niffler.model;

import java.util.ArrayList;
import java.util.List;

public record TestData(String password,
                       List<CategoryJson> categories,
                       List<SpendJson> spendings,
                       List<UserJson> incomeInvitations,
                       List<UserJson> outcomeInvitations,
                       List<UserJson> friends) {

    public TestData(String password) {
        this(password, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public TestData(String password, List<UserJson> incomeInvitations, List<UserJson> outcomeInvitations, List<UserJson> friends) {
        this(password, new ArrayList<>(), new ArrayList<>(), incomeInvitations, outcomeInvitations, friends);
    }

    public TestData(String password, List<CategoryJson> categories, List<SpendJson> spendings, List<UserJson> incomeInvitations, List<UserJson> outcomeInvitations, List<UserJson> friends) {
        this.password = password;
        this.categories = categories;
        this.spendings = spendings;
        this.incomeInvitations = incomeInvitations;
        this.outcomeInvitations = outcomeInvitations;
        this.friends = friends;
    }

    public String[] friendsUsernames() {
        return extractUsernames(friends);
    }

    public String[] incomeInvitationsUsernames() {
        return extractUsernames(incomeInvitations);
    }

    public String[] outcomeInvitationsUsernames() {
        return extractUsernames(outcomeInvitations);
    }

    private String[] extractUsernames(List<UserJson> users) {
        return users.stream().map(UserJson::username).toArray(String[]::new);
    }
}
