package com.github.kamatama41.gradle.embulk

import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.api.Git as JGit
import java.io.File

class Git private constructor(val git: JGit) {

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

    companion object {
        fun new(gitDir: File): Git =
                Git(JGit(FileRepositoryBuilder().readEnvironment().findGitDir(gitDir).build()))

        fun init(gitDir: File): Git =
                Git(JGit.init().setDirectory(gitDir).call())
    }
}
