package com.github.kamatama41.gradle.embulk

import com.google.common.base.CaseFormat
import org.gradle.api.Project

fun String.toUpperCamel(): String = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, this)

inline fun <reified T : Any> Project.the(): T =
        convention.findPlugin(T::class.java) ?: convention.getByType(T::class.java)!!
