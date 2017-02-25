import org.eclipse.jgit.api.Git as JGit
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar

buildscript {
    val kotlinVersion = "1.0.6"
    extra["kotlinVersion"] = kotlinVersion
    repositories {
        jcenter()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.eclipse.jgit:org.eclipse.jgit:4.6.0.201612231935-r")
    }
}

apply {
    plugin("idea")
    plugin("java")
    plugin("kotlin")
    plugin("maven-publish")
}

repositories {
    jcenter()
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

val kotlinVersion: String by extra
dependencies {
    compile(gradleApi())
    compile("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    testCompile("junit:junit:4.12")
}

val git = Git(project)
val sourceSets = the<JavaPluginConvention>().sourceSets

/**
 * Add a task for source Jar
 */
val sourceJar = task<Jar>("sourceJar") {
    val main by sourceSets
    from(main.allSource)
}

/**
 * Remove 'repository' directory from Git
 */
task("removeRepository") {
    doLast {
        git.rm("repository")
        git.commit("repository", message = "Remove repository")
    }
}

/**
 * Configuration for maven-publish plugin
 */
configure<PublishingExtension> {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.kamatama41"
            artifactId = "gradle-embulk-plugin"

            val java by components
            from(java)

            // Add source jar to publication
            artifact(mapOf(
                    "source" to sourceJar,
                    "classifier" to "sources"
            ))
        }
    }
    repositories {
        maven { setUrl("file://${File(rootDir, "repository").absolutePath}") }
    }
}

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
}
