package reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.invoke.MethodHandles.lookup;

/**
 * Singleton manager for Extent Reports.
 *
 * <p>Handles report initialization, test creation, and HTML generation.
 * Thread-safe for parallel test execution.</p>
 *
 * <h2>Usage:</h2>
 * <pre>
 * // Create test (typically done by listener)
 * ExtentTest test = ExtentReportManager.createTest("testName", "description");
 *
 * // Get current test for logging
 * ExtentTest test = ExtentReportManager.getTest();
 * test.info("Log message");
 *
 * // Flush reports (done at end of suite)
 * ExtentReportManager.flushReports();
 * </pre>
 */
public class ExtentReportManager {

    private static final Logger log = LoggerFactory.getLogger(lookup().lookupClass());

    private static ExtentReports extent;
    private static final String REPORT_DIR = System.getProperty("user.dir") + "/reports";
    private static String reportPath;

    // ThreadLocal for parallel test support
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    private ExtentReportManager() {
        // Singleton
    }

    /**
     * Get or create ExtentReports instance.
     */
    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            createInstance();
        }
        return extent;
    }

    /**
     * Create and configure ExtentReports.
     */
    private static void createInstance() {
        // Create reports directory
        File reportDir = new File(REPORT_DIR);
        if (!reportDir.exists()) {
            reportDir.mkdirs();
        }

        // Generate timestamped report name
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        reportPath = REPORT_DIR + "/API_Test_Report_" + timestamp + ".html";

        // Configure Spark Reporter
        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setTheme(Theme.STANDARD);
        spark.config().setDocumentTitle("REST API Test Report");
        spark.config().setReportName("RestAssured API Tests");
        spark.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");
        spark.config().setEncoding("UTF-8");

        // Create ExtentReports instance
        extent = new ExtentReports();
        extent.attachReporter(spark);

        // Add system info
        extent.setSystemInfo("Project", "API Testing Portfolio");
        extent.setSystemInfo("Framework", "RestAssured + TestNG");
        extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        extent.setSystemInfo("OS", System.getProperty("os.name"));

        log.info("ExtentReports initialized: {}", reportPath);
    }

    /**
     * Create a new test in the report.
     */
    public static ExtentTest createTest(String testName) {
        ExtentTest test = getInstance().createTest(testName);
        extentTest.set(test);
        return test;
    }

    /**
     * Create a new test with description.
     */
    public static ExtentTest createTest(String testName, String description) {
        ExtentTest test = getInstance().createTest(testName, description);
        extentTest.set(test);
        return test;
    }

    /**
     * Get current test for this thread.
     */
    public static ExtentTest getTest() {
        return extentTest.get();
    }

    /**
     * Flush reports to disk.
     */
    public static synchronized void flushReports() {
        if (extent != null) {
            extent.flush();
            log.info("Report generated: {}", reportPath);
        }
    }

    /**
     * Get report file path.
     */
    public static String getReportPath() {
        return reportPath;
    }

    /**
     * Remove test from thread local (cleanup).
     */
    public static void removeTest() {
        extentTest.remove();
    }

    /**
     * Reset manager (for testing).
     */
    public static synchronized void reset() {
        if (extent != null) {
            extent.flush();
        }
        extent = null;
        extentTest.remove();
    }
}
