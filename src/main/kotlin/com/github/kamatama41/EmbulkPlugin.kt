package com.github.kamatama41

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class EmbulkPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.create("embulk", EmbulkTask::class.java)
    }

    open class EmbulkTask: DefaultTask()
}
