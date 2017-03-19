package com.github.kamatama41.gradle.embulk

import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.junit.RepositoryTestCase
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
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
    fun classpath() {
        build("classpath")
        // Check generated a Jar file
        assertTrue(File("$projectDir/classpath/embulk-filter-myfilter-0.1.0.jar").exists())
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
        testProjectDir = createWorkRepository()
        createFile("build.gradle").writeText("""
            plugins { id "com.github.kamatama41.embulk" }

            version = "0.1.0"

            embulk {
                version = "0.8.18"
                category = "filter"
                name = "myfilter"
                homepage = "https://github.com/someuser/embulk-filter-myfilter"
            }
        """)
        createFile("settings.gradle").writeText("""rootProject.name = 'embulk-filter-myfilter'""")

        // Generate new plugin codes
        build("newPlugin")
        assertTrue(File("$projectDir", "README.md").exists())
        assertTrue(File("$projectDir", "LICENSE.txt").exists())
        assertTrue(File("$projectDir", ".gitignore").exists())
        assertTrue(File("$projectDir", "lib/embulk/filter/myfilter.rb").exists())
        assertTrue(File("$projectDir", "src/main/java/org/embulk/filter/myfilter/MyFilterFilterPlugin.java").exists())
    }

    private fun build(vararg args: String): BuildResult = newRunner(*args, "--stacktrace").build()
    private fun buildAndFail(vararg args: String): BuildResult = newRunner(*args, "--stacktrace").buildAndFail()

    private fun newRunner(vararg args: String): GradleRunner = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(*args)
            .withDebug(true)
            .withPluginClasspath()

    private fun createFile(name: String) = File(projectDir, name)
}
