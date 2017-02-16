package com.aldaviva.coverage;

import java.io.File;
import java.util.Set;
import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.ArgumentsBuilder;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class AbstractCoberturaMojo extends AbstractMojo {

    @Parameter(property = "cobertura.exclusions")
    protected Set<String> exclusions;

    @Parameter(property = "project", readonly = true)
    protected MavenProject project;

    @Parameter(property = "cobertura.minLineCoveredRatio", defaultValue = "0")
    protected double minLineCoveredRatio;

    @Parameter(property = "cobertura.minBranchCoveredRatio", defaultValue = "0")
    protected double minBranchCoveredRatio;

    protected Arguments getCoberturaArguments() {
//        File destinationDirectory = new File(project.getBuild().getDirectory(), "instrumented");
//        destinationDirectory.mkdirs();

        ArgumentsBuilder argsBuilder = new ArgumentsBuilder()
                .setBaseDirectory(project.getBasedir().getAbsolutePath())
                //.setDataFile(new File(project.getBasedir(), "cobertura.ser").getAbsolutePath())
                .setDestinationDirectory(project.getBuild().getOutputDirectory())
                .setTotalLineCoverageThreshold(minLineCoveredRatio)
                .setTotalBranchCoverageThreshold(minBranchCoveredRatio)
                .addFileToInstrument(new File(project.getBuild().getDirectory(), "uninstrumented-classes").getAbsolutePath());

        Arguments args = argsBuilder.build();

        getLog().info("baseDir = " + args.getBaseDirectory());
        getLog().info("dataFile = " + args.getDataFile());
        getLog().info("destDir = " + args.getDestinationDirectory());
        getLog().info("instrumenting = " + args.getFilesToInstrument().iterator().next().getAbsolutePath());

        return args;
    }
}
