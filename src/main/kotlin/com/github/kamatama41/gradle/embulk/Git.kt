package com.github.kamatama41.gradle.embulk

import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.api.Git as JGit
import org.gradle.api.Project

class Git(project: Project) {
    val git = JGit(FileRepositoryBuilder()
            .readEnvironment()
            .findGitDir(project.rootProject.rootDir)
            .build()
    )

    fun add(vararg patterns: String) {
        val cmd = git.add()
        if (patterns.isEmpty()) {
            cmd.isUpdate = true
        } else {
            patterns.forEach { cmd.addFilepattern(it) }
        }
        cmd.call()
    }

    fun rm(vararg patterns: String, cached: Boolean = false) {
        val cmd = git.rm()
        cmd.setCached(cached)
        patterns.forEach { cmd.addFilepattern(it) }
        cmd.call()
    }

    fun commit(vararg paths: String, message: String = "Commit by Gradle") {
        val commit = git.commit()
        commit.message = message
        paths.forEach { commit.setOnly(it) }
        commit.call()
    }

    fun config(): Config = git.repository.config
}
