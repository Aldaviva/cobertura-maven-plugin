package com.aldaviva.coverage;

import java.io.File;
import java.util.Set;
import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.ArgumentsBuilder;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.PathTool;

public abstract class AbstractCoberturaMojo extends AbstractMojo {

    protected static final String UNINSTRUMENTED_CLASSES_DIR = "uninstrumented-classes";

    @Parameter(property = "cobertura.exclusions")
    protected Set<String> exclusions;

    @Parameter(property = "project", readonly = true)
    protected MavenProject project;

    @Parameter(property = "cobertura.minLineCoveredRatio", defaultValue = "0.00")
    protected double minLineCoveredRatio;

    @Parameter(property = "cobertura.minBranchCoveredRatio", defaultValue = "0.00")
    protected double minBranchCoveredRatio;

    protected Arguments getCoberturaArguments() {
        final File classesDir = new File(project.getBuild().getDirectory(), "classes");

        ArgumentsBuilder argsBuilder = new ArgumentsBuilder()
                .setBaseDirectory(project.getBasedir().getAbsolutePath())
                .setDestinationDirectory(project.getBuild().getOutputDirectory())
                .setTotalLineCoverageThreshold(minLineCoveredRatio)
                .setTotalBranchCoverageThreshold(minBranchCoveredRatio)
                .addFileToInstrument(classesDir.getAbsolutePath());

        if (exclusions != null && !exclusions.isEmpty()) {
            // you need to explicitly include something before any exclusions work
            argsBuilder = argsBuilder.addIncludeClassesRegex(".*");

            for (final String exclusion : exclusions) {
                final String targetClassesPathRelativeToProjectDir = PathTool.getRelativeFilePath(project.getBasedir().toString(), project.getBuild().getOutputDirectory());
//                getLog().info("targetClassesPathRelativeToProjectDir = " + targetClassesPathRelativeToProjectDir);
                final String regex = antPathToRegex(exclusion, "/" + targetClassesPathRelativeToProjectDir + "/");
//                getLog().info("adding exclusion regex for \"" + exclusion + "\": \"" + regex + "\"");
                argsBuilder = argsBuilder.addExcludeClassesRegex(regex);
            }
        }

        final Arguments args = argsBuilder.build();

//        getLog().info("baseDir = " + args.getBaseDirectory());
//        getLog().info("dataFile = " + args.getDataFile());
//        getLog().info("destDir = " + args.getDestinationDirectory());
//        getLog().info("instrumenting = " + args.getFilesToInstrument().iterator().next().getAbsolutePath());

        return args;
    }

    private String antPathToRegex(final String antPath, final String classesDirectory) {
        return classesDirectory.replaceAll("\\\\|/", "\\\\.")
                + (antPath.replaceFirst("\\.class$", "")
                .replaceAll("\\\\|/", "\\\\.")
                .replaceAll("\\*\\*", "__MULTI_PATH__")
                .replaceAll("\\*", "__MULTI_WILDCARD__")
                .replaceAll("\\?", "__SINGLE_WILDCARD__")
                .replaceAll("__MULTI_PATH__", ".*?")
                .replaceAll("__MULTI_WILDCARD__", "[^.]+")
                .replaceAll("__SINGLE_WILDCARD__", "[^.]"));

    }
}
