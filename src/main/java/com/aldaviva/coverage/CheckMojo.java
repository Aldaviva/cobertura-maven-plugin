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

@Mojo(name = "check", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class CheckMojo extends AbstractCoberturaMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        FileLocker.class.getName(); //force classloader to load FileLocker before shutdown hook is called, avoiding a NoClassDefFoundException
        getLog().info("checking to make sure minimum line covered ratio is at least " + (minLineCoveredRatio * 100) + "%");
        Arguments args = getCoberturaArguments();
        Cobertura cobertura = new Cobertura(args).calculateCoverage().checkThresholds().saveProjectData();
        Report report = cobertura.report();
        List<CoverageResultEntry> coverages = ((CoverageThresholdsReport) report.getByName(ReportName.THRESHOLDS_REPORT)).getCoverageResultEntries();
        boolean isLineCoveragePassing = true;
        boolean isBranchCoveragePassing = true;
        for (CoverageResultEntry coverage : coverages) {
            //getLog().info(coverage.getName() + " (" + coverage.getCoverageType() + " " + coverage.getCoverageLevel() + "): " + coverage.getCurrentCoverage() + "/" + coverage.getExpectedCoverage());
            if ("PROJECT".equals(String.valueOf(coverage.getCoverageLevel()))) {
                if ("BRANCH".equals(String.valueOf(coverage.getCoverageType()))) {
                    getLog().info("branch coverage: "+coverage.getCurrentCoverage()*100 +"% (minimum "+coverage.getExpectedCoverage()*100+"% required)");
                    isBranchCoveragePassing = !coverage.isBelowExpectedCoverage();
                } else if ("LINE".equals(String.valueOf(coverage.getCoverageType()))) {
                    getLog().info("line coverage: "+coverage.getCurrentCoverage()*100 +"% (minimum "+coverage.getExpectedCoverage()*100+"% required)");
                    isLineCoveragePassing = !coverage.isBelowExpectedCoverage();
                }
            }
        }

        // restore uninstrumented class files
        File classesDir = new File(project.getBuild().getOutputDirectory());
        File uninstrumentedClassesDir = new File(project.getBuild().getDirectory(), "uninstrumented-classes");
        FileSystemUtils.deleteRecursively(classesDir);
        uninstrumentedClassesDir.renameTo(classesDir);

        if(!isLineCoveragePassing){
            getLog().error("Line coverage check not met.");
            throw new MojoFailureException("Line coverage check not met");
        } else if(!isBranchCoveragePassing){
            getLog().error("Branch coverage check not met.");
            throw new MojoFailureException("Branch coverage check not met");
        }
    }
}
