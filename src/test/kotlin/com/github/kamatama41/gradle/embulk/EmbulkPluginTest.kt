package com.github.kamatama41.gradle.embulk

import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.junit.RepositoryTestCase
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.containsString
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class EmbulkPluginTest : RepositoryTestCase() {
    lateinit var testProjectDir: FileRepository
    val projectDir by lazy { File("${testProjectDir.directory}/..") }
    val buildDir by lazy { "${projectDir.absolutePath}/build/embulk" }

    override fun setUp() {
        super.setUp()
        setupProject()
    }

    @Test
    fun newPluginTask() {
        build("newPlugin")
        assertTrue(File("$projectDir", "README.md").exists())
        assertTrue(File("$projectDir", "LICENSE.txt").exists())
        assertTrue(File("$projectDir", ".gitignore").exists())
        assertTrue(File("$projectDir", "lib/embulk/input/xlsx.rb").exists())
        assertTrue(File("$projectDir", "src/main/java/org/embulk/input/xlsx/XlsxFileInputPlugin.java").exists())
        assertTrue(File("$projectDir", "gradle.properties").exists())
        assertFalse(File("$projectDir", "embulk-input-xlsx").exists())
    }

    @Test
    fun gemTask() {
        build("newPlugin")
        build("gem")
        // Check a Jar file was generated
        assertTrue(File("$projectDir/classpath/embulk-input-xlsx-0.1.0.jar").exists())

        // Check a content of generated gemspec file
        assertThat(projectFile("embulk-input-xlsx.gemspec").readText(), `is`("""
            |Gem::Specification.new do |spec|
            |  spec.name          = "embulk-input-xlsx"
            |  spec.version       = "0.1.0"
            |  spec.authors       = ["Author 1", "Author 2"]
            |  spec.summary       = %[Xlsx file input plugin for Embulk]
            |  spec.description   = %[Reads files stored on xlsx.]
            |  spec.email         = ["auser@example.com"]
            |  spec.licenses      = ["MIT"]
            |  spec.homepage      = "https://github.com/someuser/embulk-input-xlsx"
            |
            |  spec.files         = `git ls-files`.split("\n") + Dir["classpath/*.jar"]
            |  spec.test_files    = spec.files.grep(%r"^(test|spec)/")
            |  spec.require_paths = ["lib"]
            |
            |  #spec.add_dependency 'YOUR_GEM_DEPENDENCY', ['~> YOUR_GEM_DEPENDENCY_VERSION']
            |  spec.add_development_dependency 'bundler', ['~> 1.0']
            |  spec.add_development_dependency 'rake', ['>= 10.0']
            |end
        """.trimMargin()))

        // Check a Gem file was generated
        assertTrue(File("$projectDir/pkg/embulk-input-xlsx-0.1.0.gem").exists())
    }

    @Test
    fun runEmbulkTask() {
        projectFile("config.yml").writeText("""
            |in:
            |  type: xlsx
            |  option1: 1
            |  option2: opt2
            |  option3: opt3
            |  parser:
            |    type: csv
            |    columns: []
            |out: {type: stdout}
        """.trimMargin())

        // Run will fail because of UnsupportedOperationException
        val result = buildAndFail("newPlugin", "embulk_run")
        assertThat(result.output, containsString("Caused by: java.lang.UnsupportedOperationException: XlsxFileInputPlugin.open method is not implemented yet"))
    }

    @Test
    fun checkstyleTask() {
        // Generated plugin Java code has checkstyle error by default, so it will fail.
        buildAndFail("newPlugin", "checkstyle")

        // Check generated checkstyle.xml and default.xml to build dir.
        assertTrue(File("$buildDir/config/checkstyle/checkstyle.xml").exists())
        assertTrue(File("$buildDir/config/checkstyle/default.xml").exists())
    }

    //////////////////////////////////////////////////////////////
    // Helper functions
    //////////////////////////////////////////////////////////////

    private fun setupProject() {
        testProjectDir = createWorkRepository()
        projectFile("build.gradle").writeText("""
            plugins { id "com.github.kamatama41.embulk" }

            embulk {
                version = "0.8.18"
                category = "file-input"
                name = "xlsx"
                authors = ["Author 1", "Author 2"]
                email = "auser@example.com"
                homepage = "https://github.com/someuser/embulk-input-xlsx"
                workDir = file("${File("./.gradle/embulk").absolutePath}")
            }
        """)
        projectFile("settings.gradle").writeText("""rootProject.name='embulk-input-xlsx'""")
    }

    private fun build(vararg args: String): BuildResult {
        val result = newRunner(*args, "--stacktrace").build()
        println("===========================================")
        println(result.output)
        println("===========================================")
        return result
    }

    private fun buildAndFail(vararg args: String): BuildResult {
        val result = newRunner(*args, "--stacktrace").buildAndFail()
        println("===========================================")
        println(result.output)
        println("===========================================")
        return result
    }

    private fun newRunner(vararg args: String): GradleRunner = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(*args)
            .withDebug(true)
            .withPluginClasspath()

    private fun projectFile(name: String) = File(projectDir, name)
}
