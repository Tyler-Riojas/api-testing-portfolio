package reporting;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import static java.lang.invoke.MethodHandles.lookup;

/**
 * TestNG listener for Extent Reports integration.
 *
 * <p>Automatically captures test results and generates HTML reports.
 * Register in testng.xml or via @Listeners annotation.</p>
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Creates test entries with descriptions</li>
 *   <li>Captures pass/fail/skip status</li>
 *   <li>Records execution time</li>
 *   <li>Logs failure stack traces</li>
 * </ul>
 */
public class ExtentTestListener implements ITestListener {

    private static final Logger log = LoggerFactory.getLogger(lookup().lookupClass());

    @Override
    public void onStart(ITestContext context) {
        log.info("========================================");
        log.info("Test Suite Started: {}", context.getName());
        log.info("========================================");

        // Initialize ExtentReports
        ExtentReportManager.getInstance();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();

        log.info("Starting test: {}", testName);

        // Create test in report
        ExtentTest test;
        if (description != null && !description.isEmpty()) {
            test = ExtentReportManager.createTest(testName, description);
        } else {
            test = ExtentReportManager.createTest(testName);
        }

        // Add groups as categories
        String[] groups = result.getMethod().getGroups();
        if (groups.length > 0) {
            test.assignCategory(groups);
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        log.info("PASSED: {}", testName);

        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.pass(MarkupHelper.createLabel("TEST PASSED", ExtentColor.GREEN));
            test.info("Execution time: " + getExecutionTime(result) + " seconds");
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        Throwable error = result.getThrowable();

        log.error("FAILED: {}", testName);
        if (error != null) {
            log.error("Reason: {}", error.getMessage());
        }

        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.fail(MarkupHelper.createLabel("TEST FAILED", ExtentColor.RED));
            test.info("Execution time: " + getExecutionTime(result) + " seconds");

            if (error != null) {
                test.fail(error);
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        Throwable error = result.getThrowable();

        log.warn("SKIPPED: {}", testName);

        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.skip(MarkupHelper.createLabel("TEST SKIPPED", ExtentColor.ORANGE));

            if (error != null) {
                test.skip(error);
            }
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("========================================");
        log.info("Test Suite Finished: {}", context.getName());
        log.info("Passed: {}", context.getPassedTests().size());
        log.info("Failed: {}", context.getFailedTests().size());
        log.info("Skipped: {}", context.getSkippedTests().size());
        log.info("========================================");

        // Flush reports
        ExtentReportManager.flushReports();
        log.info("Report: {}", ExtentReportManager.getReportPath());
    }

    /**
     * Calculate test execution time in seconds.
     */
    private String getExecutionTime(ITestResult result) {
        long duration = result.getEndMillis() - result.getStartMillis();
        return String.format("%.2f", duration / 1000.0);
    }
}
