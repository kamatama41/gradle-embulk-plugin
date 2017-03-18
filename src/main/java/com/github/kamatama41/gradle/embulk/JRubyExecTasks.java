package com.github.kamatama41.gradle.embulk;

import com.github.jrubygradle.JRubyExec;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

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
                task.jrubyArgs("-rrubygems/gem_runner", "-eGem::GemRunner.new.run(ARGV)", "push");
                task.setScript(String.format("pkg/%s-%s.gem", project.getName(), project.getVersion()));

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
                task.jrubyArgs("-rrubygems/gem_runner", "-eGem::GemRunner.new.run(ARGV)", "build");
                task.setScript(project.getName() + ".gemspec");

                task.doLast(new Action<Task>() {
                    @Override
                    public void execute(Task task) {
                        String gemFile = String.format("%s-%s.gem", project.getName(), project.getVersion());
                        boolean renamed = project.file(gemFile).renameTo(project.file("pkg/" + gemFile));
                        if(!renamed) {
                            project.getLogger().warn("File was not renamed.");
                        }
                    }
                });

                JRubyExec.updateJRubyDependencies(project);
            }
        });
    }
}
