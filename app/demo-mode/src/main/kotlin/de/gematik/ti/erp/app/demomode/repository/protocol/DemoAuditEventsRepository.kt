package de.gematik.ti.erp.app.demomode.repository.protocol

import de.gematik.ti.erp.app.demomode.datasource.DemoModeDataSource
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.protocol.model.AuditEventData
import de.gematik.ti.erp.app.protocol.repository.AuditEventsRepository
import java.util.UUID

class DemoAuditEventsRepository(private val dataSource: DemoModeDataSource) : AuditEventsRepository {
    override suspend fun downloadAuditEvents(
        profileId: ProfileIdentifier,
        count: Int?,
        offset: Int?
    ): Result<AuditEventData.AuditEventMappingResult> {
        val mappingResult = AuditEventData.AuditEventMappingResult(
            bundleId = UUID.randomUUID().toString(),
            bundleResultCount = 3,
            auditEvents = dataSource.auditEvents.value
        )
        return Result.success(mappingResult)
    }

}