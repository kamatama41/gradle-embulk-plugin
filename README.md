[![CircleCI](https://circleci.com/gh/kamatama41/gradle-embulk-plugin.svg?style=svg)](https://circleci.com/gh/kamatama41/gradle-embulk-plugin)

# gradle-plugin-embulk
This Gradle plugin provides you with some tasks to help your [Embulk](http://www.embulk.org) plugin development.

## Requirement
- Gradle 3.2.1 or later

## Getting started
Add the snippets to your build.gradle

```gradle
buildscript {
    repositories {
        jcenter()
        maven { url 'http://kamatama41.github.com/maven-repository/repository' }
    }
    dependencies {
        classpath "com.github.kamatama41:gradle-embulk-plugin:<latest-version>"
    }
}

apply plugin: "com.github.kamatama41.embulk"

embulk {
    version = "0.8.18"
    category = "file-input"
    name = "xlsx"
    authors = ["A User"]
    email = "a.user@example.com"
    homepage = "https://github.com/a.user/embulk-input-xlsx"
}
```

So that you can generate your plugin template with the command `(./gradlew|gradle) newPlugin`

```
% gradle newPlugin
Starting a Gradle Daemon (subsequent builds will be faster)
:embulkSetup
Setting Embulk version to 0.8.18
:embulk_new_java-file-input_xlsx
2017-03-21 04:12:05.020 +0900: Embulk v0.8.18
Creating embulk-input-xlsx/
  Creating embulk-input-xlsx/README.md
  Creating embulk-input-xlsx/LICENSE.txt
  Creating embulk-input-xlsx/.gitignore
  Creating embulk-input-xlsx/gradle/wrapper/gradle-wrapper.jar
  Creating embulk-input-xlsx/gradle/wrapper/gradle-wrapper.properties
  Creating embulk-input-xlsx/gradlew.bat
  Creating embulk-input-xlsx/gradlew
  Creating embulk-input-xlsx/config/checkstyle/checkstyle.xml
  Creating embulk-input-xlsx/config/checkstyle/default.xml
  Creating embulk-input-xlsx/build.gradle
  Creating embulk-input-xlsx/lib/embulk/input/xlsx.rb
  Creating embulk-input-xlsx/src/main/java/org/embulk/input/xlsx/XlsxFileInputPlugin.java
  Creating embulk-input-xlsx/src/test/java/org/embulk/input/xlsx/TestXlsxFileInputPlugin.java

Plugin template is successfully generated.
Next steps:

  $ cd embulk-input-xlsx
  $ ./gradlew package

:newPlugin

BUILD SUCCESSFUL

Total time: 13.166 secs
```

## Dependencies
This plugin automatically adds the following dependency into your project.
- `org.embulk:embulk-core:<version>`

## Tasks
- `newPlugin`: Generate a new plugin template
- `classpath`: Copy jar files to classpath directory
- `gemspec`: Generate a gemspec file for this plugin
- `gem`: Generate a gem file
- `package`: Generate a package, which is needed to run this plugin locally 
- `checkstyle`: Run a Checkstyle process
- `gemPush`: Push gem file to rubygems.org
- `embulkSetup`: Install a version of Embulk, which you specified by `version`
- `embulk_#{command}`: Run an embulk command ([details](#embulk_command))

### embulk_${command}
You can run an embulk command with a gradle task `embulk_*`, which can be added command arguments with `_`.
Also, this plugin automatically sets some arguments when executing a command such as `run` or `guess`,
so you don't have to specify yaml path and package path by default.

#### Examples
- `(./gradlew|gradle) embulk_--version`
  - This is equivalent to `embulk --version`

- `(./gradlew|gradle) embulk_run`
  - This is equivalent to `embulk run config.yml -L <project_root>`

- `(./gradlew|gradle) embulk_guess`
  - This is equivalent to `embulk guess config.yml -o output.yml -L <project_root>`

If you want to change `config.yml` or `output.yml` to other file name, you can do it with `configYaml` or `outputYaml` option such as the following

```gradle
embulk {
    ....
    configYaml = "myconfig.yml"
    outputYaml = "myoutput.yml"
}
```
