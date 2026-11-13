package com.eyther.lumbridge.features.insights.data.export

expect object PlatformExport {
    fun shareCsv(filename: String, content: String)
}
