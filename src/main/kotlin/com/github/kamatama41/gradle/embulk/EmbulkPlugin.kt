package com.github.kamatama41.gradle.embulk

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Jar

class EmbulkPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(JavaPlugin::class.java)
        project.configurations.maybeCreate("provided")

        classpathTask(project)
        clearTask(project)
    }

    fun classpathTask(project: Project) {
        project.tasks.create("classpath", Copy::class.java) { task ->
            project.afterEvaluate {
                val jar = project.tasks.findByName("jar") as Jar
                val runtime = project.configurations.findByName("runtime")
                val provided = project.configurations.findByName("provided")

                task.dependsOn(jar)
                task.group = GROUP_NAME

                task.doFirst { project.file(CLASSPATH_DIR).deleteRecursively() }

                task.from(runtime - provided + project.files(jar.archivePath))
                task.into(CLASSPATH_DIR)
            }
        }
    }

    fun clearTask(project: Project) {
        project.tasks.create("clear") { task ->
            project.afterEvaluate {
                project.delete(CLASSPATH_DIR, "${project.name}.gemspec")
            }
        }
    }

    companion object {
        val GROUP_NAME = "embulk"
        val CLASSPATH_DIR = "classpath"
    }
}
