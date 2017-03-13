[![CircleCI](https://circleci.com/gh/kamatama41/gradle-embulk-plugin.svg?style=svg)](https://circleci.com/gh/kamatama41/gradle-embulk-plugin)

# gradle-plugin-embulk
Add tasks for [Embulk](http://www.embulk.org) plugin for Java.

## Installation and configuration
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
    embulkVersion = "0.8.18"
    category = "filter"
    name = "myfilter"
    homepage = "https://github.com/someuser/embulk-filter-myfilter"
}
```

## Tasks
- `classpath`: Copy jar files to classpath directory
- `gemspec`: Generate a gemspec file for this plugin
- `gem`: Generate a gem file
- `package`: Generate a package to run this plugin locally 
- `checkstyle`: Check code style
- `gemPush`: Push gem file to rubygems.org
- `embulkSetup`: Install embulk binary
- `embulk_#{command}`: Run embulk command

### embulk_${command}
You can run a embulk command with the task and specify any command argument with `_`.
And this plugin implicitly set some config arguments when running commands such as `run` or `guess` so you don't need to specify config yaml path and project package path by default.

#### Examples
- `./gradlew embulk_--version`
  - This is equivalent to `embulk --version`

- `./gradlew embulk_run`
  - This is equivalent to `embulk run config.yml -L <project_root>`

- `./gradlew embulk_guess`
  - This is equivalent to `embulk guess config.yml -o output.yml -L <project_root>`

If you want to configure yaml file name, you can do it with the params `configYaml` and `outputYaml` such as 

```gradle
embulk {
    ....
    configYaml = "myconfig.yml"
    outputYaml = "myoutput.yml"
}
```
