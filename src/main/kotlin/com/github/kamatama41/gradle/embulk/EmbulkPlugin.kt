package com.github.kamatama41.gradle.embulk

import com.github.jrubygradle.JRubyPlugin
import org.apache.tools.ant.DirectoryScanner
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Rule
import org.gradle.api.internal.artifacts.dsl.DefaultRepositoryHandler
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.plugins.quality.CheckstylePlugin
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.bundling.Jar
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class EmbulkPlugin : Plugin<Project> {
    lateinit var project: Project
    lateinit var extension: EmbulkExtension
    val extensionName = "embulk"
    val classpathDir = "classpath"
    val gemspecFile: File by lazy { project.file("${project.name}.gemspec") }

    companion object {
        @JvmStatic
        val groupName = "embulk"
    }

    override fun apply(project: Project) {
        this.project = project
        this.extension = project.extensions.create(extensionName, EmbulkExtension::class.java, project)
        project.plugins.apply(JavaPlugin::class.java)
        project.plugins.apply(JRubyPlugin::class.java)
        project.plugins.apply(CheckstylePlugin::class.java)
        project.configurations.maybeCreate("provided")

        newPluginTask()
        classpathTask()
        gemspecTask()
        gemTask()
        packageTask()
        gemPushTask()
        cleanTask()
        checkstyleTask()
        setupEmbulkTask()
        embulkExecTask()
        embulkDependencies()
    }

    fun newPluginTask() {
        project.tasks.create("newPlugin", Copy::class.java) { task ->
            task.group = groupName
            project.afterEvaluate {
                task.dependsOn("embulk_new_java-${extension.category}_${extension.name}")

                val pluginName = "embulk-${extension.embulkCategory}-${extension.name}"
                task.from(pluginName) { spec ->
                    DirectoryScanner.removeDefaultExclude("**/.gitignore")
                    spec.include("lib/**", "src/**", "README.md", "LICENSE.txt", ".gitignore")
                }
                task.into(".")

                task.doLast {
                    project.file("gradle.properties").writeText("version=0.1.0")
                    project.delete(pluginName)
                }
            }
        }
    }

    fun classpathTask() {
        project.tasks.create("classpath", Copy::class.java) { task ->
            project.afterEvaluate {
                val jar = project.tasks.findByName("jar") as Jar
                val runtime = checkNotNull(project.configurations.findByName("runtime"))
                val provided = checkNotNull(project.configurations.findByName("provided"))

                task.dependsOn(jar)
                task.group = groupName

                task.doFirst { project.file(classpathDir).deleteRecursively() }

                task.from(runtime - provided + project.files(jar.archivePath))
                task.into(classpathDir)
            }
        }
    }

    fun gemPushTask() {
        JRubyExecTasks.gemPushTask(project, extension)
    }

    fun gemspecTask() {
        project.tasks.create("gemspec") { task ->
            project.afterEvaluate {
                task.group = groupName

                task.inputs.file("build.gradle")
                task.outputs.file(gemspecFile)

                task.doLast {
                    gemspecFile.writeText("""
                        |Gem::Specification.new do |spec|
                        |  spec.name          = "${project.name}"
                        |  spec.version       = "${project.version}"
                        |  spec.authors       = ["${extension.authors.joinToString("\", \"")}"]
                        |  spec.summary       = %[${generateSummary()}]
                        |  spec.description   = %[${generateDescription()}]
                        |  spec.email         = ["${extension.email}"]
                        |  spec.licenses      = ["${extension.licenses.joinToString()}"]
                        |  spec.homepage      = "${extension.homepage}"
                        |
                        |  spec.files         = `git ls-files`.split("\n") + Dir["classpath/*.jar"]
                        |  spec.test_files    = spec.files.grep(%r"^(test|spec)/")
                        |  spec.require_paths = ["lib"]
                        |
                        |  #spec.add_dependency 'YOUR_GEM_DEPENDENCY', ['~> YOUR_GEM_DEPENDENCY_VERSION']
                        |  spec.add_development_dependency 'bundler', ['~> 1.0']
                        |  spec.add_development_dependency 'rake', ['>= 10.0']
                        |end
                    """.trimMargin())
                }
            }
        }
    }

    fun packageTask() {
        project.tasks.create("package") { task ->
            task.group = groupName
            task.dependsOn("gemspec", "classpath")
            task.doLast {
                println("> Build succeeded.")
                println("> You can run embulk with '-L ${project.file(".").absolutePath}' argument.")
            }
        }
    }

    fun gemTask() {
        JRubyExecTasks.gemTask(project, extension)
    }

    fun cleanTask() {
        project.tasks.findByName("clean")?.apply {
            doLast { project.delete(classpathDir, gemspecFile) }
        }
    }

    fun checkstyleTask() {
        project.afterEvaluate {
            val config = extension.checkstyleConfig ?: getConfigFromResources("checkstyle/checkstyle.xml")
            val defaultConfig = extension.checkstyleDefaultConfig ?: getConfigFromResources("checkstyle/default.xml")

            project.extensions.configure(CheckstyleExtension::class.java) { checkstyle ->
                checkstyle.configFile = config
                checkstyle.toolVersion = extension.checkstyleVersion
            }

            val checkstyleMain = project.tasks.findByName("checkstyleMain") as Checkstyle
            checkstyleMain.configFile = defaultConfig
            checkstyleMain.ignoreFailures = extension.checkstyleIgnoreFailures

            val checkstyleTest = project.tasks.findByName("checkstyleTest") as Checkstyle
            checkstyleTest.configFile = defaultConfig
            checkstyleTest.ignoreFailures = extension.checkstyleIgnoreFailures

            val sourceSets = project.the<JavaPluginConvention>().sourceSets
            val main = checkNotNull(sourceSets.findByName("main"))
            val test = checkNotNull(sourceSets.findByName("test"))
            project.tasks.create("checkstyle", Checkstyle::class.java) { task ->
                task.classpath = main.output + test.output
                task.setSource(main.allSource + test.allSource)
            }
        }
    }

    fun embulkDependencies() {
        project.afterEvaluate {
            val jcenter = project.repositories.findByName(DefaultRepositoryHandler.DEFAULT_BINTRAY_JCENTER_REPO_NAME)
            if (jcenter == null) {
                project.repositories.add(project.repositories.jcenter())
            }
            project.dependencies.add("compile", "org.embulk:embulk-core:${extension.version}")
            project.dependencies.add("provided", "org.embulk:embulk-core:${extension.version}")
        }
    }

    fun setupEmbulkTask() {
        this.project.tasks.create("embulkSetup") { task ->
            task.group = groupName

            task.doLast {
                val binFile = extension.binFile
                val embulkVersion = extension.version
                if (!binFile.exists()) {
                    println("Setting Embulk version to $embulkVersion")

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
    }

    fun embulkExecTask() {
        project.tasks.addRule(EmbulkExecRule(project, extension))
    }

    private fun generateSummary(): String
            = extension.summary ?: "${extension.displayName} ${extension.displayCategory} plugin for Embulk"

    private fun generateDescription(): String {
        return extension.description ?: when (extension.category) {
            "input" -> "Loads records from ${extension.name}."
            "file-input" -> "Reads files stored on ${extension.name}."
            "parser" -> "Parses ${extension.name} files read by other file input plugins."
            "decoder" -> "Decodes ${extension.name}}-encoded files read by other file input plugins."
            "output" -> "Dumps records to ${extension.name}."
            "file-output" -> "Stores files on ${extension.name}."
            "formatter" -> "Formats ${extension.name} files for other file output plugins."
            "encoder" -> "Encodes files using ${extension.name} for other file output plugins."
            "filter" -> extension.name
            else -> extension.name
        }
    }

    private fun getConfigFromResources(path: String): File {
        val tmpConfig = project.file("${project.buildDir.absolutePath}/embulk/config/$path")
        if (!tmpConfig.parentFile.exists()) {
            tmpConfig.parentFile.mkdirs()
        }
        tmpConfig.delete()
        tmpConfig.createNewFile()

        this.javaClass.classLoader.getResourceAsStream("config/$path").use { input ->
            FileOutputStream(tmpConfig).use { output ->
                input.copyTo(output)
            }
        }

        return tmpConfig
    }

    class EmbulkExecRule(val project: Project, val extension: EmbulkExtension) : Rule {
        override fun getDescription() = """Pattern: "embulk_<command>": Executes an Embulk command."""

        override fun apply(taskName: String) {
            if (taskName.startsWith("embulk_")) {
                project.tasks.create(taskName, JavaExec::class.java) { task ->
                    task.dependsOn("embulkSetup")
                    task.main = "-jar"

                    val token = taskName.split("_".toRegex()).drop(1).toMutableList() // remove first 'embulk'
                    val args = mutableListOf(extension.binFile.absolutePath)

                    val command = token.removeAt(0)
                    args.add(command)
                    if (listOf("run", "cleanup", "preview", "guess").contains(command)) {
                        task.dependsOn("package")
                        args.add(extension.configYaml)

                        if (command == "guess") {
                            args.add("-o")
                            args.add(extension.outputYaml)
                        }
                        args.add("-L")
                        args.add(project.rootDir.absolutePath)
                    }
                    args.addAll(token)

                    task.args(args)
                }
            }
        }
    }
}
