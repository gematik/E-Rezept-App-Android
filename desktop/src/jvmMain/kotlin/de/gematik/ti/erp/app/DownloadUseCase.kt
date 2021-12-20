package de.gematik.ti.erp.app

import de.gematik.ti.erp.app.communication.repository.CommunicationRepository
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

class DownloadUseCase(
    private val prescriptionRepository: PrescriptionRepository,
    private val communicationRepository: CommunicationRepository
) {
    suspend fun update() =
        supervisorScope {
            awaitAll(
                async {
                    prescriptionRepository.download()
                },
                async {
                    communicationRepository.download()
                }
            ).find { it.isFailure } ?: Result.success(Unit)
        }
}
