package com.adobe.creative;

import com.adobe.creative.models.CampaignBrief;
import com.adobe.creative.models.GenerationResult;
import com.adobe.creative.services.BriefParser;
import com.adobe.creative.services.CreativeGenerator;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@Command(
    name = "creative-automation",
    mixinStandardHelpOptions = true,
    version = "Creative Automation Pipeline 1.0.0",
    description = "GenAI-powered creative automation pipeline for social ad campaigns"
)
public class CreativeAutomationPipeline implements Callable<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(CreativeAutomationPipeline.class);

    @Option(
        names = {"-b", "--brief"},
        description = "Path to campaign brief file (JSON or YAML)",
        required = false
    )
    private String briefPath;

    @Option(
        names = {"-o", "--output"},
        description = "Output directory for generated assets"
    )
    private String outputPath;

    @Option(
        names = {"-v", "--verbose"},
        description = "Enable verbose logging"
    )
    private boolean verbose;

    @Option(
        names = {"-l", "--locale"},
        description = "Generate assets for specific locale (e.g., en-US, de-DE). If not specified, generates for all localizations."
    )
    private String locale;

    @Override
    public Integer call() throws Exception {
        printBanner();

        // Set log level
        if (verbose) {
            System.setProperty("LOG_LEVEL", "DEBUG");
        }

        try {
            // Determine brief file path
            if (briefPath == null || briefPath.isEmpty()) {
                // Try default paths
                String[] defaultPaths = {
                    "examples/campaign-brief.json",
                    "examples/campaign-brief.yaml",
                    "campaign-brief.json"
                };

                for (String defaultPath : defaultPaths) {
                    if (new File(defaultPath).exists()) {
                        briefPath = defaultPath;
                        logger.info("Using default brief file: {}", briefPath);
                        break;
                    }
                }

                if (briefPath == null) {
                    logger.error("No campaign brief file specified. Use --brief flag or provide a default file.");
                    logger.info("Example: java -jar creative-automation.jar --brief examples/campaign-brief.json");
                    return 1;
                }
            }

            // Verify brief file exists
            if (!new File(briefPath).exists()) {
                logger.error("Brief file not found: {}", briefPath);
                return 1;
            }

            // Parse campaign brief
            logger.info("Parsing campaign brief...");
            BriefParser parser = new BriefParser();
            CampaignBrief brief = parser.parseBrief(briefPath);

            System.out.println("Campaign ID:      " + brief.getCampaignId());
            System.out.println("Products:         " + brief.getProducts().size());
            System.out.println("Target Region:    " + brief.getTargetRegion());
            System.out.println("Target Market:    " + brief.getTargetMarket());
            System.out.println("Target Audience:  " + brief.getTargetAudience());
            System.out.println("Campaign Message: " + brief.getCampaignMessage());
            if (brief.getLocalizations() != null && !brief.getLocalizations().isEmpty()) {
                System.out.println("Localizations:    " + String.join(", ", brief.getLocalizations().keySet()));
            }
            if (locale != null && !locale.isEmpty()) {
                System.out.println("Selected Locale:  " + locale);
            }
            System.out.println();

            // Note: Output directory customization can be done via application.properties
            if (outputPath != null && !outputPath.isEmpty()) {
                logger.warn("Custom output path specified via CLI, but this is currently read from application.properties");
                logger.info("To change output directory, modify output.base.path in application.properties");
            }

            // Generate creative assets
            logger.info("Initializing creative generator...");
            CreativeGenerator generator = new CreativeGenerator();

            logger.info("Starting asset generation...");
            System.out.println("This may take a few minutes depending on the number of assets...");
            System.out.println();

            GenerationResult result;
            if (locale != null && !locale.isEmpty()) {
                result = generator.generateCampaignAssetsForLocale(brief, locale);
            } else {
                result = generator.generateCampaignAssets(brief);
            }

            // Generate and display report
            String report = generator.generateReport(result);
            System.out.println(report);

            // Save report to file
            String outputBase = com.adobe.creative.utils.ConfigManager.getProperty("output.base.path", "assets/output");
            String reportPath = Paths.get(
                outputBase,
                brief.getCampaignId(),
                "generation-report.txt"
            ).toString();

            Files.createDirectories(Paths.get(reportPath).getParent());
            Files.writeString(Paths.get(reportPath), report);
            logger.info("Report saved to: {}", reportPath);

            // Exit with appropriate code
            if (result.isSuccess()) {
                logger.info("Asset generation completed successfully!");
                return 0;
            } else {
                logger.warn("Asset generation completed with errors. Check the report for details.");
                return 1;
            }

        } catch (Exception e) {
            logger.error("Fatal error: {}", e.getMessage(), e);
            System.err.println("\n❌ Asset generation failed. Check logs for details.\n");
            return 1;
        }
    }

    private void printBanner() {
        System.out.println();
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║   Creative Automation Pipeline for Social Ad Campaigns   ║");
        System.out.println("║                  Powered by GenAI                         ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new CreativeAutomationPipeline()).execute(args);
        System.exit(exitCode);
    }
}
