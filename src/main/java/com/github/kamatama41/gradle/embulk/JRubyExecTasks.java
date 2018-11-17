package com.github.kamatama41.gradle.embulk;

import com.github.jrubygradle.JRubyExec;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

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

        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                task.setJrubyVersion(extension.getJrubyVersion());
                task.jrubyArgs("-S");
                task.script("gem");
                task.scriptArgs("push", project.file(String.format("pkg/%s-%s.gem", project.getName(), project.getVersion())));

                JRubyExec.updateJRubyDependencies(project);
            }
        });
    }

    static void gemTask(final Project project, final EmbulkExtension extension) {
        final JRubyExec task = project.getTasks().create("gem", JRubyExec.class);
        task.dependsOn("gemspec", "classpath");
        task.setGroup(EmbulkPlugin.getGroupName());

        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(final Project project) {
                task.setJrubyVersion(extension.getJrubyVersion());
                task.jrubyArgs("-S");
                task.script("gem");
                task.scriptArgs("build", project.file(project.getName() + ".gemspec"));

                task.doLast(new Action<Task>() {
                    @Override
                    public void execute(Task task) {
                        String gemFile = String.format("%s-%s.gem", project.getName(), project.getVersion());
                        File dst = project.file("pkg/" + gemFile);
                        if(!dst.getParentFile().exists()) {
                            if(!dst.getParentFile().mkdirs()) {
                                throw new IllegalStateException("Prent dir was not made.");
                            }
                        }
                        if(!project.file(gemFile).renameTo(project.file("pkg/" + gemFile))) {
                            throw new IllegalStateException("File was not renamed.");
                        }
                    }
                });

                JRubyExec.updateJRubyDependencies(project);
            }
        });
    }
}
