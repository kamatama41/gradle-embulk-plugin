package com.github.kamatama41.gradle.embulk

import com.google.common.base.CaseFormat

fun String.toUpperCamel(): String = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, this)
