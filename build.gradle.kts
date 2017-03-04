import net.researchgate.release.GitAdapter
import net.researchgate.release.ReleaseExtension
import org.ajoberstar.gradle.git.publish.GitPublishExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar

buildscript {
    val kotlinVersion = "1.1.0"
    extra["kotlinVersion"] = kotlinVersion
    repositories {
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.ajoberstar:gradle-git-publish:0.1.1-rc.1")
        classpath("net.researchgate:gradle-release:2.5.0")
    }
}

apply {
    plugin("idea")
    plugin("java")
    plugin("kotlin")
    plugin("maven-publish")
    plugin("org.ajoberstar.git-publish")
    plugin("net.researchgate.release")
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
    compile("org.eclipse.jgit:org.eclipse.jgit:4.3.1.201605051710-r")
    compile("com.google.guava:guava:20.0")
    compile("com.github.jruby-gradle:jruby-gradle-plugin:0.1.5")
    testCompile("junit:junit:4.12")
}

val artifactId = "gradle-embulk-plugin"

///////////////////////////////////////////////
// Task configurations
///////////////////////////////////////////////

// maven-publish
val publish by tasks
// git-publish
val gitPublishReset by tasks
val gitPublishCommit by tasks
val gitPublishPush by tasks
// release
val afterReleaseBuild by tasks

/**
 * Define task dependencies
 */
publish.dependsOn(gitPublishReset)
gitPublishCommit.dependsOn(publish)
afterReleaseBuild.dependsOn(gitPublishPush)

/**
 * Add a task for source Jar
 */
val sourceSets = the<JavaPluginConvention>().sourceSets
val sourceJar = task<Jar>("sourceJar") {
    val main by sourceSets
    from(main.allSource)
    classifier = "sources"
}

///////////////////////////////////////////////
// Extension configurations
///////////////////////////////////////////////

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.kamatama41"
            println(artifactId)
            artifactId = artifactId

            val java by components
            from(java)

            // Add source jar to publication
            artifact(sourceJar)
        }
    }
    repositories {
        val gitPublish: GitPublishExtension by extensions
        maven { setUrl("file://${file("${gitPublish.repoDir}/repository").absolutePath}") }
    }
}

configure<GitPublishExtension> {
    repoUri = "git@github.com:kamatama41/maven-repository.git"
    branch = artifactId

    preserve { include("**") } // All files are kept in repository
    contents { from(repoDir); include("*") } // No copy
}

configure<ReleaseExtension> {
    @Suppress("UNCHECKED_CAST")
    val config = GitAdapter(project, getProperty("attributes") as Map<String, Any>)
            .createNewConfig() as GitAdapter.GitConfig
    config.requireBranch = "release"

    setProperty("git", config)
}
