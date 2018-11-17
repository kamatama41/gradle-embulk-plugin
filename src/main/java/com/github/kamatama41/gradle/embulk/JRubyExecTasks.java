package com.github.kamatama41.gradle.embulk;

import com.github.jrubygradle.JRubyExec;
import org.gradle.api.Project;

import java.io.File;

/**
 * Why is this class needed?
 * => For some reason, com.github.jrubygradle.JRubyExec cannot be loaded on Kotlin
 */
public class JRubyExecTasks {
    static void gemPushTask(Project project, final EmbulkExtension extension) {
        final JRubyExec task = project.getTasks().create("gemPush", JRubyExec.class);
        task.dependsOn("gem");
        task.setGroup(EmbulkPlugin.getGroupName());

        project.afterEvaluate(evaluatedProject -> {
            task.setJrubyVersion(extension.getJrubyVersion());
            task.jrubyArgs("-S");
            task.script("gem");
            task.scriptArgs("push", evaluatedProject.file(String.format("pkg/%s-%s.gem", evaluatedProject.getName(), evaluatedProject.getVersion())));

            JRubyExec.updateJRubyDependencies(evaluatedProject);
        });
    }

    static void gemTask(final Project project, final EmbulkExtension extension) {
        final JRubyExec task = project.getTasks().create("gem", JRubyExec.class);
        task.dependsOn("gemspec", "classpath");
        task.setGroup(EmbulkPlugin.getGroupName());

        project.afterEvaluate(evaluatedProject -> {
            task.setJrubyVersion(extension.getJrubyVersion());
            task.jrubyArgs("-S");
            task.script("gem");
            task.scriptArgs("build", evaluatedProject.file(evaluatedProject.getName() + ".gemspec"));

            task.doLast(ignored -> {
                String gemFile = String.format("%s-%s.gem", evaluatedProject.getName(), evaluatedProject.getVersion());
                File dst = evaluatedProject.file("pkg/" + gemFile);
                if(!dst.getParentFile().exists()) {
                    if(!dst.getParentFile().mkdirs()) {
                        throw new IllegalStateException("Prent dir was not made.");
                    }
                }
                if(!evaluatedProject.file(gemFile).renameTo(evaluatedProject.file("pkg/" + gemFile))) {
                    throw new IllegalStateException("File was not renamed.");
                }
            });

            JRubyExec.updateJRubyDependencies(evaluatedProject);
        });
    }
}
