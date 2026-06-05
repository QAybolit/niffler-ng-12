package guru.qa.niffler.jupiter.extension;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class BeforeAllExample implements BeforeAllCallback {

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    System.out.println("### BeforeAllExample!");
  }
}
