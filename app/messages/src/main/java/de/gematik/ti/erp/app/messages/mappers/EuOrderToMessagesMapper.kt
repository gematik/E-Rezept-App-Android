/*
 * Copyright (Change Date see Readme), gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */
package de.gematik.ti.erp.app.messages.mappers

import android.content.Context
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.datetime.ErpTimeFormatter
import de.gematik.ti.erp.app.eurezept.mapper.countryCodeToFlag
import de.gematik.ti.erp.app.eurezept.model.EuAccessCode
import de.gematik.ti.erp.app.eurezept.model.EuEventType
import de.gematik.ti.erp.app.eurezept.model.EuOrder
import de.gematik.ti.erp.app.eurezept.model.EuTaskEvent
import de.gematik.ti.erp.app.messages.ui.model.EuOrderMessageUiModel
import de.gematik.ti.erp.app.utils.letNotNull
import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Locale

/**
 * Mapper that transforms EU prescription order data and events into UI-ready message models.
 *
 * This mapper handles the complex logic of converting raw EU order events into a timeline of messages
 * that can be displayed in the UI. It groups related events that occur within 1 second of each other,
 * determines button visibility based on access code validity and recreation status, and formats
 * timestamps and descriptions appropriately.
 *
 * ## Key Responsibilities:
 * - Converts [EuOrder] and [EuTaskEvent] data into [EuOrderMessageUiModel] instances
 * - Groups events that occur within 1 second (e.g., multiple prescriptions added at once)
 * - Determines if action buttons (Show Code, Revoke Code) should be displayed based on:
 *   - Access code validity (not expired)
 *   - Whether the code has been recreated (only most recent recreation shows buttons)
 *   - Whether the code has been revoked
 * - Formats timestamps and country information for display
 * - Adds timeline flags (isFirst, isLast) for proper UI rendering
 *
 * ## Supported Event Types:
 * - **ACCESS_CODE_CREATED**: Initial access code generation
 * - **ACCESS_CODE_RECREATED**: Access code regenerated (previous codes become inactive)
 * - **TASK_ADDED**: Prescription added to the order
 * - **TASK_REMOVED**: Prescription removed from the order
 * - **TASK_REDEEMED**: Prescription redeemed at pharmacy
 *
 * ## Usage Example:
 * ```kotlin
 * val mapper = EuOrderToMessagesMapper(context, erpTimeFormatter)
 *
 * // Map an order to UI messages
 * val messages = mapper.map(
 *     order = euOrder,
 *     threadEvents = emptyList(), // or specific events to filter
 *     mappedTaskIdsToNames = { taskIds ->
 *         // Resolve task IDs to prescription names
 *         taskIds.map { taskId -> prescriptionRepository.getNameForTask(taskId) }
 *     }
 * )
 *
 * // Result: List of EuOrderMessageUiModel sorted by timestamp (newest first)
 * // Each message contains:
 * // - Formatted date/time
 * // - Prescription names
 * // - Country code and flag emoji
 * // - Whether to show action buttons
 * // - Timeline position (isFirst, isLast)
 * ```
 *
 * ## Event Grouping Logic:
 * Events of the same type that occur within 1 second are merged into a single message.
 * Example: Adding 3 prescriptions in quick succession shows as one "Prescriptions Added" message.
 *
 * ## Button Visibility Rules:
 * - **ACCESS_CODE_CREATED**: Shows buttons if no recreation exists AND code is valid
 * - **ACCESS_CODE_RECREATED**: Only the LATEST recreation shows buttons (if code is valid)
 * - Other event types never show buttons
 *
 * @param context Android context for accessing string resources
 * @param erpTimeFormatter Formatter for converting timestamps to localized date/time strings
 */
class EuOrderToMessagesMapper(
    private val context: Context,
    private val erpTimeFormatter: ErpTimeFormatter
) {

    /**
     * Extension function to format an [Instant] as a localized date and time string.
     *
     * Converts the instant to both date and time components using [ErpTimeFormatter],
     * then combines them using the "orders_timestamp" string resource format.
     *
     * @return Formatted string like "27.11.2025, 14:30" or null if formatting fails
     *
     * Example:
     * ```kotlin
     * val instant = Clock.System.now()
     * val formatted = instant.dateTimeString() // "27.11.2025, 14:30"
     * ```
     */
    private fun Instant.dateTimeString(): String? {
        val date = erpTimeFormatter.date(this)
        val time = erpTimeFormatter.time(this)

        return letNotNull(date, time) { dateFormatted, timeFormatted ->
            context.getString(R.string.orders_timestamp, dateFormatted, timeFormatted)
        }
    }

    /**
     * Maps an [EuOrder] and its events into a list of UI-ready message models.
     *
     * This is the main entry point for converting order data into displayable messages.
     * It performs several operations:
     * 1. Filters and sorts events by timestamp
     * 2. Identifies the latest recreation event (if any) for button visibility logic
     * 3. Groups events of the same type that occur within 1 second
     * 4. Maps each event group to the appropriate [EuOrderMessageUiModel] subtype
     * 5. Sorts all messages by timestamp (newest first)
     * 6. Adds timeline flags (isFirst, isLast) for UI rendering
     *
     * ## Parameters:
     * @param order The EU prescription order containing access code and event data. If null, returns empty list.
     * @param threadEvents Optional list of specific events to display. If empty, uses all events from the order.
     * @param mappedTaskIdsToNames Lambda function that resolves task IDs to prescription names.
     *                             Receives a list of task IDs and should return their corresponding display names.
     *
     * @return List of [EuOrderMessageUiModel] sorted by timestamp (newest first), or empty list if:
     *         - order is null
     *         - an error occurs during mapping
     *
     * ## Usage Example 1: Basic usage with all order events
     * ```kotlin
     * val messages = mapper.map(
     *     order = euOrder,
     *     threadEvents = emptyList(),
     *     mappedTaskIdsToNames = { taskIds ->
     *         taskIds.map { taskId ->
     *             prescriptionRepository.getTaskById(taskId)?.medicationName ?: "Unknown"
     *         }
     *     }
     * )
     * ```
     *
     * ## Usage Example 2: Filtering specific events in a date range
     * ```kotlin
     * val filteredEvents = euOrder.events.filter { event ->
     *     event.createdAt >= startDate && event.createdAt <= endDate
     * }
     *
     * val messages = mapper.map(
     *     order = euOrder,
     *     threadEvents = filteredEvents,
     *     mappedTaskIdsToNames = { taskIds ->
     *         // Batch fetch names from repository
     *         prescriptionRepository.getTaskNames(taskIds)
     *     }
     * )
     * ```
     *
     * ## Usage Example 3: In a Use Case
     * ```kotlin
     * class GetEuOrderMessagesUseCase(
     *     private val repository: EuRepository,
     *     private val mapper: EuOrderToMessagesMapper
     * ) {
     *     suspend fun invoke(orderId: String): List<EuOrderMessageUiModel> {
     *         val order = repository.getOrder(orderId) ?: return emptyList()
     *
     *         return mapper.map(
     *             order = order,
     *             mappedTaskIdsToNames = { taskIds ->
     *                 repository.getPrescriptionNames(taskIds)
     *             }
     *         )
     *     }
     * }
     * ```
     *
     * ## Result Structure:
     * The returned list contains different message types based on event types:
     * - [EuOrderMessageUiModel.AccessCodeCreated] - Initial code generation
     * - [EuOrderMessageUiModel.AccessCodeRecreated] - Code regenerated
     * - [EuOrderMessageUiModel.TaskAdded] - Prescription(s) added
     * - [EuOrderMessageUiModel.TaskRemoved] - Prescription(s) removed
     * - [EuOrderMessageUiModel.TaskRedeemed] - Prescription(s) redeemed
     *
     * Each message includes:
     * - Formatted timestamp string
     * - List of prescription names
     * - Country code and flag emoji
     * - Button visibility flags
     * - Timeline position (isFirst/isLast)
     * - Unread status
     * - Revoked status (for access codes)
     */
    fun map(
        order: EuOrder?,
        threadEvents: List<EuTaskEvent> = emptyList(),
        mappedTaskIdsToNames: (List<String>) -> List<String>
    ): List<EuOrderMessageUiModel> {
        order ?: return emptyList()
        try {
            val events = (threadEvents.ifEmpty { order.events }).sortedBy { it.createdAt }

            val latestRecreationEventId = events
                .lastOrNull {
                    it.type == EuEventType.ACCESS_CODE_RECREATED
                }
                ?.id

            val groupedCreatedUiModels = mergeGroupedEvents(
                events = events,
                eventType = EuEventType.ACCESS_CODE_CREATED
            ) { representative, underlyingEventIds, mergedTaskIds ->
                mapCreated(
                    order = order,
                    event = representative,
                    underlyingEventIds = underlyingEventIds,
                    latestRecreationEventId = latestRecreationEventId,
                    mergedTaskIds = mergedTaskIds,
                    prescriptionNamesOverride = mappedTaskIdsToNames(mergedTaskIds)
                )
            }

            val groupedRecreatedUiModels = mergeGroupedEvents(
                events = events,
                eventType = EuEventType.ACCESS_CODE_RECREATED
            ) { representative, underlyingEventIds, mergedTaskIds ->
                mapRecreated(
                    order = order,
                    event = representative,
                    underlyingEventIds = underlyingEventIds,
                    latestRecreationEventId = latestRecreationEventId,
                    mergedTaskIds = mergedTaskIds,
                    prescriptionNamesOverride = mappedTaskIdsToNames(mergedTaskIds)
                )
            }

            val groupedAddedUiModels = mergeGroupedEvents(
                events = events,
                eventType = EuEventType.TASK_ADDED
            ) { representative, underlyingEventIds, mergedTaskIds ->
                mapTaskAdded(
                    order = order,
                    event = representative,
                    underlyingEventIds = underlyingEventIds,
                    mergedTaskIds = mergedTaskIds,
                    prescriptionNamesOverride = mappedTaskIdsToNames(mergedTaskIds)
                )
            }

            val groupedRemovedUiModels = mergeGroupedEvents(
                events = events,
                eventType = EuEventType.TASK_REMOVED
            ) { representative, underlyingEventIds, mergedTaskIds ->
                mapTaskRemoved(
                    order = order,
                    event = representative,
                    underlyingEventIds = underlyingEventIds,
                    mergedTaskIds = mergedTaskIds,
                    prescriptionNamesOverride = mappedTaskIdsToNames(mergedTaskIds)
                )
            }

            val groupedRedeemedUiModels = mergeGroupedEvents(
                events = events,
                eventType = EuEventType.TASK_REDEEMED
            ) { representative, underlyingEventIds, mergedTaskIds ->
                mapTaskRedeemed(
                    order = order,
                    event = representative,
                    underlyingEventIds = underlyingEventIds,
                    mergedTaskIds = mergedTaskIds,
                    prescriptionNamesOverride = mappedTaskIdsToNames(mergedTaskIds)
                )
            }

            val allUiModels = (
                groupedCreatedUiModels +
                    groupedRecreatedUiModels +
                    groupedAddedUiModels +
                    groupedRemovedUiModels +
                    groupedRedeemedUiModels
                )
                .sortedByDescending { it.timestamp }
                .withCorrectTimelineFlags()

            return allUiModels
        } catch (e: Exception) {
            Napier.e("Error mapping order to messages", e)
            return emptyList()
        }
    }

    // --------------------------------------------------------
    // CREATED
    // --------------------------------------------------------
    /**
     * Maps an ACCESS_CODE_CREATED event to a UI model.
     *
     * This handles the initial access code generation event. Button visibility is determined by:
     * - No recreation event exists after this (user should use the most recent code)
     * - Access code is still valid (not expired)
     *
     * @param order The EU order containing access code information
     * @param event The ACCESS_CODE_CREATED event to map
     * @param latestRecreationEventId ID of the most recent recreation event, or null if none exists
     * @param mergedTaskIds List of task IDs that were part of this event (after grouping)
     * @param prescriptionNamesOverride Display names for the prescriptions
     *
     * @return [EuOrderMessageUiModel.AccessCodeCreated] with appropriate button visibility and description
     *
     * Example output:
     * ```
     * Title: "Code created for France 🇫🇷"
     * Description: "You can use this code in France until 14:30"
     * showButtons: true (if no recreation and code valid)
     * isRevoked: true (if access code is missing)
     * ```
     */
    private fun mapCreated(
        order: EuOrder,
        event: EuTaskEvent,
        latestRecreationEventId: String?,
        mergedTaskIds: List<String>,
        prescriptionNamesOverride: List<String>,
        underlyingEventIds: List<String>
    ): EuOrderMessageUiModel {
        val accessCode = order.euAccessCode

        // --- Condition A: no recreation happened after this event ---
        val noRecreation = latestRecreationEventId == null

        // --- Condition B: access code exists AND valid ---
        val accessCodeValid = accessCode?.isValid() ?: false

        // --- Condition C: access code missing (still show button, but with "revoked" text) ---
        val accessCodeMissing = accessCode == null

        // --- Final button logic ---
        val showButtons = noRecreation && accessCodeValid

        val countryFlag = countryCodeToFlag(order.countryCode)
        val countryName = countryCodeToName(order.countryCode)

        val title = context.getString(R.string.eu_messages_code_created_message_title, countryName)

        return EuOrderMessageUiModel.AccessCodeCreated(
            id = event.id,
            underlyingEventIds = underlyingEventIds,
            orderId = order.orderId,
            accessCode = order.euAccessCode?.accessCode ?: "",
            dateTimeString = event.createdAt.dateTimeString() ?: "",
            timestamp = event.createdAt,
            isFirst = true,
            isLast = false,
            showButtons = showButtons,
            taskIds = mergedTaskIds,
            prescriptionNames = prescriptionNamesOverride,
            countryCode = order.countryCode,
            isUnread = event.isUnread,
            isRevoked = accessCodeMissing && noRecreation,
            title = title,
            flagEmoji = countryFlag,
            description = order.getDescriptionText(
                isNotTheMostRecentRecreationEvent = latestRecreationEventId != null,
                accessCodeMissing = accessCodeMissing
            )
        )
    }

    // --------------------------------------------------------
    // RE-CREATED
    // --------------------------------------------------------
    /**
     * Maps an ACCESS_CODE_RECREATED event to a UI model.
     *
     * This handles when an access code is regenerated (e.g., user requested a new code for the same order).
     * Only the MOST RECENT recreation event will show action buttons, as older recreations are superseded.
     *
     * Button visibility logic:
     * - Only shows buttons if this is the latest recreation event AND code is valid
     * - Previous recreation events never show buttons (they've been superseded)
     *
     * @param order The EU order containing access code information
     * @param event The ACCESS_CODE_RECREATED event to map
     * @param latestRecreationEventId ID of the most recent recreation event (should match this event for buttons)
     * @param mergedTaskIds List of task IDs that were part of this event (after grouping)
     * @param prescriptionNamesOverride Display names for the prescriptions, or null to use empty list
     *
     * @return [EuOrderMessageUiModel.AccessCodeRecreated] with appropriate button visibility
     *
     * Example scenario:
     * ```
     * Timeline:
     * 1. ACCESS_CODE_CREATED (10:00) - showButtons: false (superseded by recreation)
     * 2. ACCESS_CODE_RECREATED (11:00) - showButtons: false (superseded by newer recreation)
     * 3. ACCESS_CODE_RECREATED (12:00) - showButtons: true (most recent, if valid)
     * ```
     */
    private fun mapRecreated(
        order: EuOrder,
        event: EuTaskEvent,
        latestRecreationEventId: String?,
        mergedTaskIds: List<String>,
        prescriptionNamesOverride: List<String>? = null,
        underlyingEventIds: List<String>
    ): EuOrderMessageUiModel {
        val isMostRecent = (event.id == latestRecreationEventId)

        val accessCode = order.euAccessCode

        // --- Condition A: access code exists AND valid ---
        val accessCodeValid = accessCode?.isValid() ?: false

        // --- Condition B: access code missing (still show button, but with "revoked" text) ---
        val accessCodeMissing = accessCode == null

        // --- Final button logic ---
        val showButtons = isMostRecent && accessCodeValid

        return EuOrderMessageUiModel.AccessCodeRecreated(
            id = event.id,
            underlyingEventIds = underlyingEventIds,
            orderId = order.orderId,
            accessCode = order.euAccessCode?.accessCode ?: "",
            dateTimeString = event.createdAt.dateTimeString() ?: "",
            timestamp = event.createdAt,
            showButtons = showButtons, // only last recreation shows buttons
            taskIds = mergedTaskIds,
            prescriptionNames = prescriptionNamesOverride ?: emptyList(),
            countryCode = order.countryCode,
            isUnread = event.isUnread,
            isRevoked = accessCodeMissing && isMostRecent,
            title = context.getString(R.string.eu_messages_code_created_again_message_title),
            description = order.getDescriptionText(
                isNotTheMostRecentRecreationEvent = !isMostRecent,
                accessCodeMissing = accessCodeMissing
            )
        )
    }

    // --------------------------------------------------------
    // TASK REDEEMED
    // --------------------------------------------------------
    /**
     * Maps a TASK_REDEEMED event to a UI model.
     *
     * This indicates that one or more prescriptions have been redeemed at a pharmacy in the target country.
     * No action buttons are shown for this event type as the redemption has already occurred.
     *
     * @param order The EU order containing order metadata (country code, access code)
     * @param event The TASK_REDEEMED event to map
     * @param mergedTaskIds List of task IDs that were redeemed (after grouping events within 1 second)
     * @param prescriptionNamesOverride Display names for the redeemed prescriptions, or null to use empty list
     *
     * @return [EuOrderMessageUiModel.TaskRedeemed] with showButtons set to false
     *
     * Example output:
     * ```
     * Title: "" (no title for redeemed events)
     * Description: "Your prescription was used by a pharmacy in FR"
     * showButtons: false (no action needed)
     * prescriptionNames: ["Ibuprofen 600mg", "Amoxicillin 500mg"]
     * ```
     */
    private fun mapTaskRedeemed(
        order: EuOrder,
        event: EuTaskEvent,
        mergedTaskIds: List<String>,
        prescriptionNamesOverride: List<String>? = null,
        underlyingEventIds: List<String>
    ): EuOrderMessageUiModel {
        val description = context.getString(
            R.string.eu_messages_code_used_by_pharmacy_message_body,
            order.countryCode
        )

        return EuOrderMessageUiModel.TaskRedeemed(
            id = event.id,
            underlyingEventIds = underlyingEventIds,
            orderId = order.orderId,
            accessCode = order.euAccessCode?.accessCode ?: "",
            dateTimeString = event.createdAt.dateTimeString() ?: "",
            timestamp = event.createdAt,
            showButtons = false,
            taskIds = mergedTaskIds,
            prescriptionNames = prescriptionNamesOverride ?: emptyList(),
            countryCode = order.countryCode,
            isUnread = event.isUnread,
            title = "",
            description = description
        )
    }

    // --------------------------------------------------------
    // TASK ADDED
    // --------------------------------------------------------
    /**
     * Maps a TASK_ADDED event to a UI model.
     *
     * This indicates that one or more prescriptions were added to the EU order.
     * No action buttons are shown for this informational event.
     * Multiple prescriptions added within 1 second are grouped into a single message.
     *
     * @param order The EU order containing order metadata
     * @param event The TASK_ADDED event to map
     * @param mergedTaskIds List of task IDs that were added (after grouping events within 1 second)
     * @param prescriptionNamesOverride Display names for the added prescriptions, or null to use empty list
     *
     * @return [EuOrderMessageUiModel.TaskAdded] with showButtons set to false
     *
     * Example output:
     * ```
     * Title: "Prescription added"
     * Description: "A prescription was added to your order"
     * showButtons: false
     * prescriptionNames: ["Aspirin 100mg", "Vitamin D3"]
     * ```
     */
    private fun mapTaskAdded(
        order: EuOrder,
        event: EuTaskEvent,
        mergedTaskIds: List<String>,
        prescriptionNamesOverride: List<String>? = null,
        underlyingEventIds: List<String>
    ): EuOrderMessageUiModel {
        return EuOrderMessageUiModel.TaskAdded(
            id = event.id,
            underlyingEventIds = underlyingEventIds,
            orderId = order.orderId,
            accessCode = order.euAccessCode?.accessCode ?: "",
            dateTimeString = event.createdAt.dateTimeString() ?: "",
            timestamp = event.createdAt,
            showButtons = false,
            taskIds = mergedTaskIds,
            prescriptionNames = prescriptionNamesOverride ?: emptyList(),
            countryCode = order.countryCode,
            isUnread = event.isUnread,
            title = context.getString(R.string.eu_messages_prescription_added_title),
            description = context.getString(R.string.eu_messages_prescription_added_body)
        )
    }

    // --------------------------------------------------------
    // TASK REMOVED
    // --------------------------------------------------------
    /**
     * Maps a TASK_REMOVED event to a UI model.
     *
     * This indicates that one or more prescriptions were removed from the EU order.
     * No action buttons are shown for this informational event.
     * Multiple prescriptions removed within 1 second are grouped into a single message.
     *
     * @param order The EU order containing order metadata
     * @param event The TASK_REMOVED event to map
     * @param mergedTaskIds List of task IDs that were removed (after grouping events within 1 second)
     * @param prescriptionNamesOverride Display names for the removed prescriptions, or null to use empty list
     *
     * @return [EuOrderMessageUiModel.TaskRemoved] with showButtons set to false
     *
     * Example output:
     * ```
     * Title: "Prescription removed"
     * Description: "A prescription was removed from your order"
     * showButtons: false
     * prescriptionNames: ["Ibuprofen 600mg"]
     * ```
     */
    private fun mapTaskRemoved(
        order: EuOrder,
        event: EuTaskEvent,
        mergedTaskIds: List<String>,
        prescriptionNamesOverride: List<String>? = null,
        underlyingEventIds: List<String>
    ): EuOrderMessageUiModel {
        return EuOrderMessageUiModel.TaskRemoved(
            id = event.id,
            underlyingEventIds = underlyingEventIds,
            orderId = order.orderId,
            accessCode = order.euAccessCode?.accessCode ?: "",
            dateTimeString = event.createdAt.dateTimeString() ?: "",
            timestamp = event.createdAt,
            showButtons = false,
            taskIds = mergedTaskIds,
            prescriptionNames = prescriptionNamesOverride ?: emptyList(),
            countryCode = order.countryCode,
            isUnread = event.isUnread,
            title = context.getString(R.string.eu_messages_prescription_removed_title),
            description = context.getString(R.string.eu_messages_prescription_removed_body)
        )
    }

    // --------------------------------------------------------
    // Helpers
    // --------------------------------------------------------

    /**
     * Groups events of the same type that occur within 1 second and maps them to UI models.
     *
     * This is a key function for consolidating rapid-fire events (e.g., adding multiple prescriptions
     * in quick succession) into a single UI message. It prevents UI clutter when multiple related
     * actions happen nearly simultaneously.
     *
     * ## Algorithm:
     * 1. Filter events to only include the specified [eventType]
     * 2. Sort events by timestamp (oldest first)
     * 3. Group consecutive events that occur within 1000ms (1 second) of each other
     * 4. For each group, merge all task IDs and use the latest event as representative
     * 5. Map each group to a UI model using the provided [onMap] function
     *
     * @param events All events from the order
     * @param eventType The specific event type to filter and group (e.g., TASK_ADDED)
     * @param onMap Mapping function that converts a representative event and merged task IDs to a UI model
     *
     * @return List of UI models, one per event group (empty if no events of this type exist)
     *
     * ## Example Scenario:
     * ```
     * Input events (all TASK_ADDED):
     * - Event A: taskId="123", timestamp=10:00:00.000
     * - Event B: taskId="456", timestamp=10:00:00.500  (0.5s after A)
     * - Event C: taskId="789", timestamp=10:00:00.900  (0.4s after B)
     * - Event D: taskId="111", timestamp=10:00:02.000  (1.1s after C)
     *
     * Result: 2 groups
     * - Group 1: Events A, B, C → mergedTaskIds=["123","456","789"], representative=Event C
     * - Group 2: Event D → mergedTaskIds=["111"], representative=Event D
     *
     * Each group becomes one UI message with multiple prescription names.
     * ```
     *
     * ## Usage:
     * ```kotlin
     * val addedMessages = mergeGroupedEvents(
     *     events = order.events,
     *     eventType = EuEventType.TASK_ADDED
     * ) { representativeEvent, mergedTaskIds ->
     *     mapTaskAdded(order, representativeEvent, mergedTaskIds, prescriptionNames)
     * }
     * ```
     */
    private fun mergeGroupedEvents(
        events: List<EuTaskEvent>,
        eventType: EuEventType,
        onMap: (EuTaskEvent, List<String>, List<String>) -> EuOrderMessageUiModel
    ): List<EuOrderMessageUiModel> {
        val filtered = events.filter { it.type == eventType }
        if (filtered.isEmpty()) return emptyList()

        val sorted = filtered.sortedBy { it.createdAt }

        val result = mutableListOf<MutableList<EuTaskEvent>>()
        var current = mutableListOf(sorted.first())
        result += current

        for (i in 1 until sorted.size) {
            val prev = sorted[i - 1].createdAt
            val now = sorted[i].createdAt

            val diffMs = now.toEpochMilliseconds() - prev.toEpochMilliseconds()

            @Suppress("MagicNumber")
            if (diffMs <= 1000) { // <= 1 second
                current += sorted[i]
            } else {
                current = mutableListOf(sorted[i])
                result += current
            }
        }

        // 3) Convert each group to a single UiMessage
        return result.map { group ->
            val mergedTaskIds = group.map { it.taskId }.distinct()
            val representative = group.last() // Latest event in the group
            val underlyingEventIds = group.map { it.id }.toSet().filter { it != representative.id } // All other events in the group
            onMap(representative, underlyingEventIds, mergedTaskIds)
        }
    }

    /**
     * Formats an [Instant] as a 24-hour time string (HH:mm format).
     *
     * Converts the instant to the current system's default time zone and extracts
     * the hour and minute components, zero-padded to 2 digits.
     *
     * @param instant The timestamp to format, or null
     * @return Formatted time string like "14:30" or "09:05", or empty string if instant is null
     *
     * Example:
     * ```kotlin
     * val validUntil = Clock.System.now() + 2.hours
     * val timeString = formatAccessTime(validUntil) // "16:30"
     *
     * val description = "You can use this code until $timeString"
     * ```
     */
    private fun formatAccessTime(instant: Instant?): String {
        if (instant == null) return ""

        val zone = TimeZone.currentSystemDefault()
        val localDateTime = instant.toLocalDateTime(zone)

        // HH:mm -> zero-padded 24h time
        return "%02d:%02d".format(localDateTime.hour, localDateTime.minute)
    }

    /**
     * Adds correct timeline position flags (isFirst, isLast) to each message in the list.
     *
     * This extension function marks the first message with isFirst=true and the last message
     * with isLast=true. These flags are used by the UI to determine how to render timeline
     * connectors between messages (e.g., filled vs unfilled timeline dots).
     *
     * The function handles all message types and creates new copies with updated flags,
     * preserving all other properties.
     *
     * @receiver List of [EuOrderMessageUiModel] messages to process
     * @return New list with the same messages but with corrected isFirst/isLast flags
     *
     * Example:
     * ```kotlin
     * val messages = listOf(messageA, messageB, messageC)
     * val withFlags = messages.withCorrectTimelineFlags()
     * // Result:
     * // messageA -> isFirst=true, isLast=false
     * // messageB -> isFirst=false, isLast=false
     * // messageC -> isFirst=false, isLast=true
     * ```
     *
     * Usage in UI:
     * ```kotlin
     * MessageTimeline(
     *     drawFilledTop = !message.isFirst,
     *     drawFilledBottom = !message.isLast,
     *     // ... other properties
     * )
     * ```
     */
    private fun List<EuOrderMessageUiModel>.withCorrectTimelineFlags(): List<EuOrderMessageUiModel> =
        this.mapIndexed { index, msg ->
            val isFirst = index == 0
            val isLast = index == this.lastIndex

            when (msg) {
                is EuOrderMessageUiModel.AccessCodeCreated ->
                    msg.copy(isFirst = isFirst, isLast = isLast)

                is EuOrderMessageUiModel.AccessCodeRecreated ->
                    msg.copy(isFirst = isFirst, isLast = isLast)

                is EuOrderMessageUiModel.TaskAdded ->
                    msg.copy(isFirst = isFirst, isLast = isLast)

                is EuOrderMessageUiModel.TaskRemoved ->
                    msg.copy(isFirst = isFirst, isLast = isLast)

                is EuOrderMessageUiModel.TaskRedeemed ->
                    msg.copy(isFirst = isFirst, isLast = isLast)
            }
        }

    /**
     * Generates the appropriate description text for an access code creation/recreation event.
     *
     * This extension function determines which description to show based on the state of the access code:
     * - If a newer recreation exists: Shows "new code generated" message
     * - If access code is missing/revoked: Shows "code revoked" message
     * - Otherwise: Shows standard "code valid until" message with expiry time
     *
     * The logic handles different scenarios in order of priority:
     * 1. Check if this is NOT the most recent recreation (superseded by newer code)
     * 2. Check if access code is missing/revoked
     * 3. Default to showing validity information
     *
     * @receiver [EuOrder] The EU order to get description for
     * @param isNotTheMostRecentRecreationEvent True if a newer ACCESS_CODE_RECREATED event exists after this one
     * @param accessCodeMissing True if the order's euAccessCode is null (revoked)
     *
     * @return Localized description string appropriate for the access code state
     *
     * Example outputs:
     * ```
     * // Scenario 1: Superseded by newer code
     * isNotTheMostRecentRecreationEvent = true
     * → "A new code has been generated for France. Please use the latest code."
     *
     * // Scenario 2: Code revoked
     * accessCodeMissing = true
     * → "This code for France has been revoked and can no longer be used in France."
     *
     * // Scenario 3: Active code
     * isNotTheMostRecentRecreationEvent = false, accessCodeMissing = false
     * → "You can use this code in France until 14:30"
     * ```
     */
    private fun EuOrder.getDescriptionText(
        isNotTheMostRecentRecreationEvent: Boolean,
        accessCodeMissing: Boolean
    ): String {
        val countryName = countryCodeToName(countryCode)

        return when {
            isNotTheMostRecentRecreationEvent ->
                context.getString(R.string.eu_messages_code_created_but_new_generated_message_body, countryName)

            accessCodeMissing -> context.getString(
                R.string.eu_messages_code_revoked_message_body,
                countryName,
                countryName
            )

            else -> context.getString(
                R.string.eu_messages_code_created_message_body,
                countryName,
                countryName,
                formatAccessTime(euAccessCode?.validUntil)
            )
        }
    }

    /**
     * Checks if an EU access code is still valid (not expired).
     *
     * Compares the access code's validUntil timestamp against the current time (or a provided time).
     * An access code is considered valid if its validUntil time is greater than or equal to the current time.
     *
     * @receiver [EuAccessCode] The access code to check validity for
     * @param now The current time to check against. Defaults to the current system time.
     * @return true if the access code is still valid (not expired), false if expired
     *
     * Example:
     * ```kotlin
     * val accessCode = order.euAccessCode
     * if (accessCode.isValid()) {
     *     // Show "Show Code" and "Revoke Code" buttons
     * } else {
     *     // Code expired, don't show action buttons
     * }
     *
     * // Check validity at a specific time
     * val futureTime = Clock.System.now() + 1.hours
     * val willBeValidInFuture = accessCode.isValid(futureTime)
     * ```
     */
    private fun EuAccessCode.isValid(now: Instant = Clock.System.now()): Boolean = validUntil >= now

    /**
     * Converts an ISO country code to a localized country name.
     *
     * Uses the Java Locale API to get the display name for a country code in the specified locale.
     * This ensures that country names are shown in the user's preferred language.
     *
     * @param countryCode ISO 3166-1 alpha-2 country code (e.g., "FR", "DE", "IT")
     * @param locale The locale to use for the country name. Defaults to the system default locale.
     * @return Localized country name (e.g., "France", "Germany", "Italy" in English locale)
     *
     * Example:
     * ```kotlin
     * // Using default system locale (e.g., English)
     * val name1 = countryCodeToName("FR") // "France"
     * val name2 = countryCodeToName("DE") // "Germany"
     * val name3 = countryCodeToName("IT") // "Italy"
     *
     * // Using a specific locale (e.g., German)
     * val germanLocale = Locale.GERMAN
     * val name4 = countryCodeToName("FR", germanLocale) // "Frankreich"
     * val name5 = countryCodeToName("DE", germanLocale) // "Deutschland"
     * ```
     */
    private fun countryCodeToName(countryCode: String, locale: Locale = Locale.getDefault()): String {
        return Locale("", countryCode).getDisplayCountry(locale)
    }
}
