package guru.qa.niffler.config;

public interface Config {

    static Config getInstance() {
        return LocalConfig.INSTANCE;
    }

    String frontUrl();

    String authUrl();

    String authJdbcUrl();

    String userdataJdbcUrl();

    String spendUrl();

    String spendJdbcUrl();

    String currencyJdbcUrl();

    String githubUrl();

    String dbUsername();

    String dbPassword();
}
