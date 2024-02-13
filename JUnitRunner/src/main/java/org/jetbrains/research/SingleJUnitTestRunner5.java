package org.jetbrains.research;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.util.Arrays;
import java.util.List;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

public class SingleJUnitTestRunner5 {
  public static void main(String... args) throws ClassNotFoundException {
    String[] classAndMethod = args[0].split("#");
    LauncherDiscoveryRequest request =
        LauncherDiscoveryRequestBuilder.request()
            .selectors(selectMethod(Class.forName(classAndMethod[0]).getClassLoader(), classAndMethod[1]))
            .build();

    Launcher launcher = LauncherFactory.create();
    SummaryGeneratingListener listener = new SummaryGeneratingListener();

    launcher.registerTestExecutionListeners(listener);
    launcher.execute(request);

    TestExecutionSummary result = listener.getSummary();
    List<TestExecutionSummary.Failure> failures = result.getFailures();
    for (TestExecutionSummary.Failure failure : failures) {
      System.err.println(Arrays.toString(failure.getException().getStackTrace()) + "\n ===");
    }
    System.exit(result.getTestsFailedCount() == 0 ? 0 : 1);

  }
}