package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.ImportSummary
import com.learnwitme.core.domain.model.PackageManifest
import com.learnwitme.core.domain.service.PackageImporter

class ImportPackageWorkflow(
    private val importer: PackageImporter
) {
    suspend fun execute(manifest: PackageManifest): ImportSummary {
        return importer.import(manifest)
    }
}
