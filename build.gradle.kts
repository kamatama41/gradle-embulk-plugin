import org.ajoberstar.gradle.git.publish.GitPublishExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar

buildscript {
    val kotlinVersion = "1.0.6"
    extra["kotlinVersion"] = kotlinVersion
    repositories {
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.ajoberstar:gradle-git-publish:0.1.1-rc.1")
    }
}

apply {
    plugin("idea")
    plugin("java")
    plugin("kotlin")
    plugin("maven-publish")
    plugin("org.ajoberstar.git-publish")
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

/**
 * Add a task for source Jar
 */
val sourceSets = the<JavaPluginConvention>().sourceSets
val sourceJar = task<Jar>("sourceJar") {
    val main by sourceSets
    from(main.allSource)
    classifier = "sources"
}

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

val publish by tasks
val gitPublishReset by tasks
publish.dependsOn(gitPublishReset)

val gitPublishCommit by tasks
gitPublishCommit.dependsOn(publish)
