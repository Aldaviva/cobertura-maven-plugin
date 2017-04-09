package com.aldaviva.coverage;

import java.io.File;
import java.util.List;
import net.sourceforge.cobertura.check.CoverageResultEntry;
import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.Cobertura;
import net.sourceforge.cobertura.reporting.CoverageThresholdsReport;
import net.sourceforge.cobertura.reporting.Report;
import net.sourceforge.cobertura.reporting.ReportName;
import net.sourceforge.cobertura.util.FileLocker;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.springframework.util.FileSystemUtils;

/**
 * This class runs after your tests are finishes and checks to see if they executed enough lines of your code under test.
 * If not, the build fails.
 */
@Mojo(name = "check", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class CheckMojo extends AbstractCoberturaMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        FileLocker.class.getName(); //force classloader to load FileLocker before shutdown hook is called, avoiding a NoClassDefFoundException

        final Arguments args = getCoberturaArguments();
        final Cobertura cobertura = new Cobertura(args).calculateCoverage().checkThresholds()/*.saveProjectData()*/;
        final Report report = cobertura.report();

        final boolean isLineCoveragePassing = isCoveragePassing(report, "Line");
        final boolean isBranchCoveragePassing = isCoveragePassing(report, "Branch");

        restoreUninstrumentedClassFiles();

        if (!isLineCoveragePassing || !isBranchCoveragePassing) {
            throw new MojoFailureException("Coverage checks not met");
        }
    }

    private boolean isCoveragePassing(final Report report, final String coverageType) {
        final CoverageResultEntry coverage = getProjectCoverage(report, coverageType.toUpperCase());
        if (coverage != null && !Double.isNaN(coverage.getCurrentCoverage())) {
            getLog().info(coverageType + " coverage: " + renderPercentage(coverage.getCurrentCoverage()) + " actual, "
                    + renderPercentage(coverage.getExpectedCoverage()) + " required.");

            return !coverage.isBelowExpectedCoverage();
        } else {
            return true;
        }
    }

    private static String renderPercentage(double percentage) {
        return String.format("%.1f%%", percentage * 100.0);
    }

    /**
     * @param coberturaReport the result of calling {@link Cobertura#report()}
     * @param coverageType one of {@code BRANCH} or {@code LINE}
     * @return double in the range [0,1] where 0 represents no coverage and 1 represents full coverage. If {@code coverageType}
     * is unknown, will return 0.
     */
    private CoverageResultEntry getProjectCoverage(final Report coberturaReport, final String coverageType) {
        final List<CoverageResultEntry> coverages = ((CoverageThresholdsReport) coberturaReport
                .getByName(ReportName.THRESHOLDS_REPORT)).getCoverageResultEntries();

        for (final CoverageResultEntry coverage : coverages) {
            if ("PROJECT".equals(String.valueOf(coverage.getCoverageLevel())) && coverageType.equals(String.valueOf(coverage.getCoverageType()))) {
                if (coverage.getCurrentCoverage() != Double.NaN) {
                    // for some reason, cobertura only measures and reports branch or line coverage if you give it a threshold > 0 for branches or lines, respectively
                    // this means you can't measure coverage without enforcing it
                    return coverage;
                } else {
                    // user did not specify a threshold for this check, so Cobertura didn't measure it
                    return null;
                }
            }
        }

        // Cobertura report didn't contain PROJECT result for some reason. Not aware of any situation that would cause this.
        return null;
    }

    /**
     * Restores the backup created by {@link InstrumentMojo#backupUninstrumentedClasses()}.
     */
    private void restoreUninstrumentedClassFiles() {
        final File classesDir = new File(project.getBuild().getOutputDirectory());
        final File uninstrumentedClassesDir = new File(project.getBuild().getDirectory(), UNINSTRUMENTED_CLASSES_DIR);
        FileSystemUtils.deleteRecursively(classesDir);
        uninstrumentedClassesDir.renameTo(classesDir);
    }
}
