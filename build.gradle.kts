import com.github.kamatama41.gradle.gitrelease.GitReleaseExtension

buildscript {
    val kotlinVersion = "1.1.1"
    extra["kotlinVersion"] = kotlinVersion
    repositories {
        jcenter()
        maven { setUrl("http://kamatama41.github.com/maven-repository/repository") }
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.github.kamatama41:gradle-git-release-plugin:0.1.0")
    }
}

apply {
    plugin("idea")
    plugin("kotlin")
    plugin("com.github.kamatama41.git-release")
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

configure<GitReleaseExtension> {
    groupId = "com.github.kamatama41"
    artifactId = "gradle-embulk-plugin"
    repoUri = "git@github.com:kamatama41/maven-repository.git"
    repoDir = file("${System.getProperty("user.home")}/gh-maven-repository")
}
