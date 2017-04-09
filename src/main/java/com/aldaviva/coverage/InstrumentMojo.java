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

/**
 * This class modified your compiled .class files on disk by adding tons of Cobertura instructions. This allows Cobertura
 * to measure which instructions are executed when you run your tests. This is a different strategy than JaCoCo, which
 * registers an agent with the JVM.
 */
@Mojo(name = "instrument", defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES, threadSafe = true)
public class InstrumentMojo extends AbstractCoberturaMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        final Arguments coberturaArguments = getCoberturaArguments();
        coberturaArguments.getDestinationDirectory().mkdirs();
        coberturaArguments.getDataFile().delete();

        // uninstrumented class files will be restored during check goal
        backupUninstrumentedClasses();

        instrument(coberturaArguments);
    }

    /**
     * Before adding the instrumentation instructions to the class files, make a backup of the class files to be restored
     * after testing is complete. This way your generated artifact won't have instrumentation instructions in it.
     */
    private void backupUninstrumentedClasses() throws MojoExecutionException {
        final File classesDir = new File(project.getBuild().getOutputDirectory());
        final File uninstrumentedClassesDir = new File(project.getBuild().getDirectory(), UNINSTRUMENTED_CLASSES_DIR);

        try {
            uninstrumentedClassesDir.mkdirs();
            FileSystemUtils.copyRecursively(classesDir, uninstrumentedClassesDir);
        } catch (final IOException e) {
            throw new MojoExecutionException("Failed to make backups of class files before instrumenting", e);
        }
    }

    private void instrument(final Arguments coberturaArguments) throws MojoExecutionException {
        try {
            new Cobertura(coberturaArguments).instrumentCode();
        } catch (final Throwable throwable) {
            throw new MojoExecutionException("Cobertura instrumentation failed", throwable);
        }
    }
}
