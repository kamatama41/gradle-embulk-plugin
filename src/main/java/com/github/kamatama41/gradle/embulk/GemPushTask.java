package com.github.kamatama41.gradle.embulk;

import com.github.jrubygradle.JRubyExec;
import org.gradle.api.Action;
import org.gradle.api.Project;

/**
 * Why is only this Java class?
 * => For some reason, com.github.jrubygradle.JRubyExec cannot be loaded on Kotlin
 */
public class GemPushTask {
    static void add(Project project, EmbulkExtension extension) {
        final JRubyExec task = project.getTasks().create("gemPush", JRubyExec.class);
        task.setJrubyVersion(extension.getJrubyVersion());
        task.setGroup(EmbulkPlugin.getGroupName());
        task.jrubyArgs("-rrubygems/gem_runner", "-eGem::GemRunner.new.run(ARGV)", "push");

        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                task.setScript(String.format("pkg/%s-%s.gem", project.getName(), project.getVersion()));
            }
        });
    }
}
