buildscript {
    val kotlinVersion = "1.0.6"
    extra["kotlinVersion"] = kotlinVersion
    repositories {
        jcenter()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

apply {
    plugin("idea")
    plugin("kotlin")
}

repositories {
    jcenter()
}

val kotlinVersion: String by extra
dependencies {
    compile(gradleApi())
    compile("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    testCompile("junit:junit:4.12")
}
