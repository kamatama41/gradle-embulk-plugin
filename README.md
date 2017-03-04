[![CircleCI](https://circleci.com/gh/kamatama41/gradle-embulk-plugin.svg?style=svg)](https://circleci.com/gh/kamatama41/gradle-embulk-plugin)

# gradle-plugin-embulk
Add tasks for [Embulk](http://www.embulk.org) plugin.

## Installation
Add the snippets to your build.gradle
```gradle
buildscript {
    repositories {
        maven { url 'http://kamatama41.github.com/maven-repository/repository' }
    }
    dependencies {
        classpath "com.github.kamatama41:gradle-embulk-plugin:0.1.0"
    }
}
apply plugin: "com.github.kamatama41.embulk"
```

## Tasks
TODO: Details

- classpath
- gemspec
- gem
- package
- checkstyle
- gemPush
