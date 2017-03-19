package com.github.kamatama41.gradle.embulk

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class EmbulkPluginTest {
    @Rule @JvmField
    val testProjectDir = TemporaryFolder()
    val projectDir by lazy { testProjectDir.root!! }
    val buildDir by lazy { "${projectDir.absolutePath}/build/embulk" }

    @Before
    fun setUp() {
        setupProject()
    }

    @Test
    fun classpath() {
        build("classpath")
        // Check generated a Jar file
        assertTrue(File("$projectDir/classpath/embulk-input-xlsx-0.1.0.jar").exists())
    }

    @Test
    fun gemspec() {
        build("gemspec")
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
    }

    @Test
    fun checkstyle() {
        // Generated plugin Java code has checkstyle error by default, so it will fail.
        buildAndFail("checkstyle")

        // Check generated checkstyle.xml and default.xml to build dir.
        assertTrue(File("$buildDir/config/checkstyle/checkstyle.xml").exists())
        assertTrue(File("$buildDir/config/checkstyle/default.xml").exists())
    }

    //////////////////////////////////////////////////////////////
    // Helper functions
    //////////////////////////////////////////////////////////////

    private fun setupProject() {
        projectFile("build.gradle").writeText("""
            plugins { id "com.github.kamatama41.embulk" }

            version = "0.1.0"

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
        projectFile("settings.gradle").writeText("""rootProject.name = 'embulk-input-xlsx'""")

        // Generate new plugin codes
        build("newPlugin")
        assertTrue(File("$projectDir", "README.md").exists())
        assertTrue(File("$projectDir", "LICENSE.txt").exists())
        assertTrue(File("$projectDir", ".gitignore").exists())
        assertTrue(File("$projectDir", "lib/embulk/input/xlsx.rb").exists())
        assertTrue(File("$projectDir", "src/main/java/org/embulk/input/xlsx/XlsxFileInputPlugin.java").exists())
        assertFalse(File("$projectDir", "embulk-input-xlsx").exists())
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
