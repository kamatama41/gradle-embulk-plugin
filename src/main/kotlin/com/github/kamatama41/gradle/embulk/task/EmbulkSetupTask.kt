package com.github.kamatama41.gradle.embulk.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileOutputStream
import java.net.URL

open class EmbulkSetupTask : DefaultTask() {
    lateinit var embulkVersion: String
    lateinit var binFile: File

    @TaskAction
    fun exec() {
        if (!binFile.exists()) {
            logger.debug("Setting Embulk version to $embulkVersion")
            val url = URL("https://dl.bintray.com/embulk/maven/embulk-$embulkVersion.jar")
            url.openStream().use { input ->
                if (!binFile.parentFile.exists()) {
                    binFile.parentFile.mkdirs()
                }
                binFile.createNewFile()
                binFile.setExecutable(true, true)
                FileOutputStream(binFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}
