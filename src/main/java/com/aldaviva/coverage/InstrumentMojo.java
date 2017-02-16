package com.aldaviva.coverage;

import java.io.File;
import java.io.IOException;
import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.Cobertura;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.springframework.util.FileSystemUtils;

@Mojo(name = "instrument", defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES, threadSafe = true)
public class InstrumentMojo extends AbstractCoberturaMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("instrumenting project with exclusions:");
        for (final String exclusion : exclusions) {
            getLog().info("  " + exclusion);
        }

        Arguments coberturaArguments = getCoberturaArguments();
        coberturaArguments.getDestinationDirectory().mkdirs();
        coberturaArguments.getDataFile().delete();

        File classesDir = new File(project.getBuild().getOutputDirectory());
        File uninstrumentedClassesDir = new File(project.getBuild().getDirectory(), "uninstrumented-classes");
        uninstrumentedClassesDir.mkdirs();

        // make a backup of the uninstrumented class files to restore during check goal
        try {
            FileSystemUtils.copyRecursively(classesDir, uninstrumentedClassesDir);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to make backups of class files before instrumenting", e);
        }

        try {
            //System.setProperty("net.sourceforge.cobertura.datafile", coberturaArguments.getDataFile().getAbsolutePath());
            new Cobertura(coberturaArguments).instrumentCode().saveProjectData();
        } catch (Throwable throwable) {
            throw new MojoExecutionException("Cobertura instrumentation failed", throwable);
        }
    }
}
