package com.github.kamatama41.gradle.embulk

import org.gradle.api.Project

open class EmbulkExtension(project: Project) {
    lateinit var category: String
    lateinit var name: String
    var authors: Array<String> = emptyArray()
    var summary: String? = null
    var description: String? = null
    var email: String? = null
    var licenses: Array<String> = arrayOf("MIT")
    var homepage: String = ""
}
