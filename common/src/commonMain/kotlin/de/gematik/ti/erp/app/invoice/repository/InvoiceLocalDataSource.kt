/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.invoice.repository

import de.gematik.ti.erp.app.db.entities.v1.AddressEntityV1
import de.gematik.ti.erp.app.db.entities.v1.ProfileEntityV1
import de.gematik.ti.erp.app.db.entities.v1.invoice.ChargeableItemV1
import de.gematik.ti.erp.app.db.entities.v1.invoice.DescriptionTypeV1
import de.gematik.ti.erp.app.db.entities.v1.invoice.InvoiceEntityV1
import de.gematik.ti.erp.app.db.entities.v1.invoice.PKVInvoiceEntityV1
import de.gematik.ti.erp.app.db.entities.v1.invoice.PriceComponentV1
import de.gematik.ti.erp.app.db.entities.v1.task.AccidentTypeV1
import de.gematik.ti.erp.app.db.entities.v1.task.CoverageTypeV1
import de.gematik.ti.erp.app.db.entities.v1.task.IngredientEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.InsuranceInformationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationCategoryV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationRequestEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MultiplePrescriptionInfoEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.OrganizationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.PatientEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.PractitionerEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.QuantityEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.RatioEntityV1
import de.gematik.ti.erp.app.db.queryFirst
import de.gematik.ti.erp.app.db.toInstant
import de.gematik.ti.erp.app.db.toRealmInstant
import de.gematik.ti.erp.app.db.tryWrite
import de.gematik.ti.erp.app.fhir.model.AccidentType
import de.gematik.ti.erp.app.fhir.model.MedicationCategory
import de.gematik.ti.erp.app.fhir.model.extractBinary
import de.gematik.ti.erp.app.fhir.model.extractInvoiceBundle
import de.gematik.ti.erp.app.fhir.model.extractInvoiceKBVAndErpPrBundle
import de.gematik.ti.erp.app.fhir.model.extractKBVBundle
import de.gematik.ti.erp.app.invoice.model.InvoiceData
import de.gematik.ti.erp.app.prescription.model.Quantity
import de.gematik.ti.erp.app.prescription.model.Ratio
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.repository.toMedication
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import io.github.aakira.napier.Napier
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.query.Sort
import io.realm.kotlin.query.max
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.serialization.json.JsonElement
@Suppress("SpreadOperator")
class InvoiceLocalDataSource(
    private val realm: Realm
) {

    fun latestInvoiceModifiedTimestamp(profileId: ProfileIdentifier): Flow<Instant?> =
        realm.query<PKVInvoiceEntityV1>("parent.id = $0", profileId)
            .max<RealmInstant>("timestamp")
            .asFlow()
            .map {
                it?.toInstant()
            }

    private val mutex = Mutex()

    @Suppress("LongMethod", "CyclomaticComplexMethod")
    suspend fun saveInvoice(profileId: ProfileIdentifier, bundle: JsonElement) = mutex.withLock {
        realm.tryWrite {
            queryFirst<ProfileEntityV1>("id = $0", profileId)?.let { profile ->

                lateinit var invoiceEntity: PKVInvoiceEntityV1

                extractInvoiceKBVAndErpPrBundle(
                    bundle,
                    process = { taskId, accessCode, invoiceBundle, kbvBundle, erpPrBundle ->
                        extractInvoiceBundle(
                            invoiceBundle,
                            processDispense = { whenHandedOver ->
                                whenHandedOver
                            },
                            processPharmacyAddress = { line, postalCode, city ->
                                AddressEntityV1().apply {
                                    this.line1 = line?.getOrNull(0) ?: ""
                                    this.line2 = line?.getOrNull(1) ?: ""
                                    this.postalCode = postalCode ?: ""
                                    this.city = city ?: ""
                                }
                            },
                            processPharmacy = { name, address, _, iknr, _, _ ->
                                OrganizationEntityV1().apply {
                                    this.name = name
                                    this.address = address
                                    this.uniqueIdentifier = iknr
                                }
                            },
                            processInvoice = { totalAdditionalFee, totalBruttoAmount, currency,
                                items, additionalItems, additionalInformation ->
                                InvoiceEntityV1().apply {
                                    this.totalAdditionalFee = totalAdditionalFee
                                    this.totalBruttoAmount = totalBruttoAmount
                                    this.currency = currency
                                    items.forEach { item ->
                                        this.chargeableItems.add(
                                            applyChargeableItem(item)
                                        )
                                    }
                                    additionalItems.forEach { item ->
                                        this.additionalDispenseItems.add(
                                            applyChargeableItem(item)
                                        )
                                    }
                                    additionalInformation.forEach {
                                        this.additionalInformation.add(it)
                                    }
                                }
                            },
                            save = { _, timeStamp, pharmacy, invoice, whenHandedOver ->
                                invoiceEntity = queryFirst<PKVInvoiceEntityV1>("taskId = $0", taskId) ?: run {
                                    copyToRealm(PKVInvoiceEntityV1()).also {
                                        profile.invoices += it
                                    }
                                }

                                val kbvBinary = extractBinary(kbvBundle) ?: byteArrayOf() // Verordnung
                                val invoiceBinary = extractBinary(invoiceBundle) ?: byteArrayOf() // Abrechnung
                                val erpPrBinary = extractBinary(erpPrBundle) ?: byteArrayOf() // Quittung

                                profile.apply {
                                    this.invoices.add(
                                        invoiceEntity.apply {
                                            this.parent = profile
                                            this.taskId = taskId
                                            this.accessCode = accessCode
                                            this.timestamp = timeStamp.toRealmInstant()
                                            this.pharmacyOrganization = pharmacy
                                            this.invoice = invoice
                                            this.whenHandedOver = whenHandedOver
                                            this.kbvBinary = kbvBinary
                                            this.erpPrBinary = erpPrBinary
                                            this.invoiceBinary = invoiceBinary
                                        }
                                    )
                                }
                            }
                        )

                        extractKBVBundle(
                            kbvBundle,
                            processOrganization = { name, address, _, iknr, phone, mail ->
                                OrganizationEntityV1().apply {
                                    this.name = name
                                    this.address = address
                                    this.uniqueIdentifier = iknr
                                    this.phone = phone
                                    this.mail = mail
                                }
                            },
                            processPatient = { name, address, birthDate, insuranceIdentifier ->
                                PatientEntityV1().apply {
                                    this.name = name
                                    this.address = address
                                    this.dateOfBirth = birthDate
                                    this.insuranceIdentifier = insuranceIdentifier
                                }
                            },
                            processPractitioner = { name, qualification, practitionerIdentifier ->
                                PractitionerEntityV1().apply {
                                    this.name = name
                                    this.qualification = qualification
                                    this.practitionerIdentifier = practitionerIdentifier
                                }
                            },
                            processInsuranceInformation = { name, statusCode, coverageType ->
                                InsuranceInformationEntityV1().apply {
                                    this.name = name
                                    this.statusCode = statusCode
                                    this.coverageType = CoverageTypeV1.mapTo(coverageType)
                                }
                            },
                            processAddress = { line, postalCode, city ->
                                AddressEntityV1().apply {
                                    this.line1 = line?.getOrNull(0) ?: ""
                                    this.line2 = line?.getOrNull(1) ?: ""
                                    this.postalCode = postalCode ?: ""
                                    this.city = city ?: ""
                                }
                            },
                            processQuantity = { value, unit ->
                                QuantityEntityV1().apply {
                                    this.value = value
                                    this.unit = unit
                                }
                            },
                            processRatio = { numerator, denominator ->
                                RatioEntityV1().apply {
                                    this.numerator = numerator
                                    this.denominator = denominator
                                }
                            },
                            processIngredient = { text, form, identifier, amount, strength ->
                                IngredientEntityV1().apply {
                                    this.text = text
                                    this.form = form
                                    this.number = number
                                    this.amount = amount
                                    this.identifier = identifier.toIdentifierEntityV1()
                                    this.strength = strength
                                }
                            },
                            processMedication =
                            { text,
                                medicationCategory,
                                form,
                                amount,
                                vaccine,
                                manufacturingInstructions,
                                packaging,
                                normSizeCode,
                                identifier,
                                ingredientMedications,
                                ingredients,
                                _,
                                _ ->
                                MedicationEntityV1().apply {
                                    this.text = text ?: ""
                                    this.medicationCategory = when (medicationCategory) {
                                        MedicationCategory.ARZNEI_UND_VERBAND_MITTEL ->
                                            MedicationCategoryV1.ARZNEI_UND_VERBAND_MITTEL

                                        MedicationCategory.BTM -> MedicationCategoryV1.BTM
                                        MedicationCategory.AMVV -> MedicationCategoryV1.AMVV
                                        MedicationCategory.SONSTIGES -> MedicationCategoryV1.SONSTIGES
                                        else -> MedicationCategoryV1.UNKNOWN
                                    }
                                    this.form = form
                                    this.amount = amount
                                    this.vaccine = vaccine
                                    this.manufacturingInstructions = manufacturingInstructions
                                    this.packaging = packaging
                                    this.normSizeCode = normSizeCode
                                    this.ingredientMedications = ingredientMedications.toRealmList()
                                    this.identifier = identifier.toIdentifierEntityV1()
                                    this.ingredients = ingredients.toRealmList()
                                }
                            },
                            processMultiplePrescriptionInfo = { indicator, numbering, start, end ->
                                MultiplePrescriptionInfoEntityV1().apply {
                                    this.indicator = indicator
                                    this.numbering = numbering
                                    this.start = start?.toInstant(TimeZone.UTC)?.toRealmInstant()
                                    this.end = end?.toInstant(TimeZone.UTC)?.toRealmInstant()
                                }
                            },
                            processMedicationRequest =
                            { authoredOn,
                                    dateOfAccident,
                                    location,
                                    accidentType,
                                    emergencyFee,
                                    substitutionAllowed,
                                    dosageInstruction,
                                    quantity,
                                    multiplePrescriptionInfo,
                                    note,
                                    bvg,
                                    additionalFee
                                ->
                                MedicationRequestEntityV1().apply {
                                    this.authoredOn = authoredOn
                                    this.dateOfAccident =
                                        dateOfAccident?.value?.atStartOfDayIn(TimeZone.UTC)?.toRealmInstant()
                                    this.location = location
                                    this.accidentType = when (accidentType) {
                                        AccidentType.Unfall -> AccidentTypeV1.Unfall
                                        AccidentType.Arbeitsunfall -> AccidentTypeV1.Arbeitsunfall
                                        AccidentType.Berufskrankheit -> AccidentTypeV1.Berufskrankheit
                                        AccidentType.None -> AccidentTypeV1.None
                                    }
                                    this.emergencyFee = emergencyFee
                                    this.substitutionAllowed = substitutionAllowed
                                    this.dosageInstruction = dosageInstruction
                                    this.quantity = quantity
                                    this.multiplePrescriptionInfo = multiplePrescriptionInfo
                                    this.note = note
                                    this.bvg = bvg
                                    this.additionalFee = additionalFee
                                }
                            },
                            savePVSIdentifier = {},
                            save = { organization,
                                patient,
                                practitioner,
                                _,
                                medication,
                                medicationRequest ->

                                invoiceEntity = queryFirst("taskId = $0", taskId) ?: run {
                                    copyToRealm(PKVInvoiceEntityV1()).also {
                                        profile.invoices += it
                                    }
                                }

                                invoiceEntity.apply {
                                    this.parent = profile
                                    this.practitionerOrganization = organization
                                    this.patient = patient
                                    this.practitioner = practitioner
                                    this.medicationRequest = medicationRequest.apply {
                                        this.medication = medication
                                    }
                                }
                            }
                        )
                    }
                )
            }
        }
    }

    private fun applyChargeableItem(item: InvoiceData.ChargeableItem): ChargeableItemV1 {
        return ChargeableItemV1().apply {
            when (item.description) {
                is InvoiceData.ChargeableItem.Description.PZN -> {
                    this.descriptionTypeV1 = DescriptionTypeV1.PZN
                    this.description = item.description.pzn
                }

                is InvoiceData.ChargeableItem.Description.TA1 -> {
                    this.descriptionTypeV1 = DescriptionTypeV1.TA1
                    this.description = item.description.ta1
                }

                is InvoiceData.ChargeableItem.Description.HMNR -> {
                    this.descriptionTypeV1 = DescriptionTypeV1.HMNR
                    this.description = item.description.hmnr
                }
            }
            this.text = item.text
            this.factor = item.factor
            this.price = PriceComponentV1().apply {
                this.value = item.price.value
                this.tax = item.price.tax
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    private fun PKVInvoiceEntityV1.toPKVInvoice(): InvoiceData.PKVInvoiceRecord =
        InvoiceData.PKVInvoiceRecord(
            profileId = this.parent?.id ?: "",
            timestamp = this.timestamp.toInstant(),
            pharmacyOrganization = SyncedTaskData.Organization(
                name = this.pharmacyOrganization?.name ?: "",
                uniqueIdentifier = this.pharmacyOrganization?.uniqueIdentifier,
                address = SyncedTaskData.Address(
                    line1 = this.pharmacyOrganization?.address?.line1 ?: "",
                    line2 = this.pharmacyOrganization?.address?.line2 ?: "",
                    postalCode = this.pharmacyOrganization?.address?.postalCode ?: "",
                    city = this.pharmacyOrganization?.address?.city ?: ""
                )
            ),
            practitionerOrganization = SyncedTaskData.Organization(
                name = this.practitionerOrganization?.name,
                address = SyncedTaskData.Address(
                    line1 = this.practitionerOrganization?.address?.line1 ?: "",
                    line2 = this.practitionerOrganization?.address?.line2 ?: "",
                    postalCode = this.practitionerOrganization?.address?.postalCode ?: "",
                    city = this.practitionerOrganization?.address?.city ?: ""
                )
            ),
            practitioner = SyncedTaskData.Practitioner(
                name = this.practitioner?.name,
                qualification = this.practitioner?.qualification,
                practitionerIdentifier = this.practitioner?.practitionerIdentifier
            ),
            patient = SyncedTaskData.Patient(
                name = this.patient?.name,
                address = this.patient?.address?.let {
                    SyncedTaskData.Address(
                        line1 = it.line1,
                        line2 = it.line2,
                        postalCode = it.postalCode,
                        city = it.city
                    )
                },
                birthdate = this.patient?.dateOfBirth,
                insuranceIdentifier = this.patient?.insuranceIdentifier
            ),
            medicationRequest = SyncedTaskData.MedicationRequest(
                medication = this.medicationRequest?.medication?.toMedication(),
                authoredOn = this.medicationRequest?.authoredOn,
                dateOfAccident = this.medicationRequest?.dateOfAccident?.toInstant(),
                location = this.medicationRequest?.location,
                accidentType = when (this.medicationRequest?.accidentType) {
                    AccidentTypeV1.Unfall -> SyncedTaskData.AccidentType.Unfall
                    AccidentTypeV1.Arbeitsunfall -> SyncedTaskData.AccidentType.Arbeitsunfall
                    AccidentTypeV1.Berufskrankheit -> SyncedTaskData.AccidentType.Berufskrankheit
                    else -> SyncedTaskData.AccidentType.None
                },
                emergencyFee = this.medicationRequest?.emergencyFee,
                substitutionAllowed = this.medicationRequest?.substitutionAllowed ?: false,
                dosageInstruction = this.medicationRequest?.dosageInstruction,
                multiplePrescriptionInfo = SyncedTaskData.MultiplePrescriptionInfo(
                    indicator = this.medicationRequest?.multiplePrescriptionInfo?.indicator ?: false,
                    numbering = Ratio(
                        numerator = Quantity(
                            value = this.medicationRequest?.multiplePrescriptionInfo?.numbering?.numerator?.value ?: "",
                            unit = ""
                        ),
                        denominator = Quantity(
                            value =
                            this.medicationRequest?.multiplePrescriptionInfo?.numbering?.denominator?.value ?: "",
                            unit = ""
                        )
                    ),
                    start = this.medicationRequest?.multiplePrescriptionInfo?.start?.toInstant(),
                    end = this.medicationRequest?.multiplePrescriptionInfo?.end?.toInstant()
                ),
                additionalFee = when (this.medicationRequest?.additionalFee) {
                    "0" -> SyncedTaskData.AdditionalFee.NotExempt
                    "1" -> SyncedTaskData.AdditionalFee.Exempt
                    "2" -> SyncedTaskData.AdditionalFee.ArtificialFertilization
                    else -> SyncedTaskData.AdditionalFee.None
                },
                quantity = this.medicationRequest?.quantity ?: 0,
                note = this.medicationRequest?.note,
                bvg = this.medicationRequest?.bvg
            ),
            taskId = this.taskId,
            accessCode = this.accessCode,
            whenHandedOver = this.whenHandedOver,
            invoice = InvoiceData.Invoice(
                totalAdditionalFee = this.invoice?.totalAdditionalFee ?: 0.0,
                totalBruttoAmount = this.invoice?.totalBruttoAmount ?: 0.0,
                currency = this.invoice?.currency ?: "",
                chargeableItems = this.invoice?.chargeableItems?.map {
                    it.toChargeableItem()
                } ?: listOf(),
                additionalDispenseItems = this.invoice?.additionalDispenseItems?.map {
                    it.toChargeableItem()
                } ?: listOf(),
                additionalInformation = this.invoice?.additionalInformation?.toList() ?: listOf()
            ),
            consumed = this.consumed
        )

    fun loadInvoices(profileId: ProfileIdentifier): Flow<List<InvoiceData.PKVInvoiceRecord>> =
        realm.query<PKVInvoiceEntityV1>("parent.id = $0", profileId)
            .asFlow()
            .map { invoices ->
                val result = invoices.list.map { invoice ->
                    invoice.toPKVInvoice()
                }
                result
            }

    fun loadInvoiceByTaskId(taskId: String): Flow<InvoiceData.PKVInvoiceRecord?> =
        realm.query<PKVInvoiceEntityV1>("taskId = $0", taskId)
            .sort("timestamp", Sort.DESCENDING)
            .asFlow()
            .map { result ->
                result.list.firstOrNull()?.toPKVInvoice()
            }

    fun getInvoiceTaskIdAndConsumedStatus(profileId: ProfileIdentifier): Flow<List<InvoiceData.InvoiceStatus>> =
        realm.query<PKVInvoiceEntityV1>("parent.id = $0", profileId)
            .asFlow()
            .map { invoices ->
                invoices.list.map { invoice ->
                    InvoiceData.InvoiceStatus(taskId = invoice.taskId, consumed = invoice.consumed)
                }
            }

    fun getAllUnreadInvoices(): Flow<List<InvoiceData.InvoiceStatus>> =
        realm.query<PKVInvoiceEntityV1>("consumed = false")
            .asFlow()
            .map { invoices ->
                invoices.list.map { invoice ->
                    InvoiceData.InvoiceStatus(taskId = invoice.taskId, consumed = invoice.consumed)
                }
            }

    suspend fun updateInvoiceCommunicationStatus(taskId: String, consumed: Boolean) {
        realm.tryWrite {
            this.query<PKVInvoiceEntityV1>("taskId == $0", taskId)
                .first()
                .find()?.apply {
                    this.consumed = consumed
                } ?: Napier.w("Task ID $taskId not found, unable to update consumed status")
        }
    }

    suspend fun updateRepliedCommunicationStatus(taskId: String, consumed: Boolean) {
        realm.tryWrite {
            this.query<PKVInvoiceEntityV1>("taskId == $0", taskId)
                .first()
                .find()?.apply {
                    this.consumed = consumed
                } ?: Napier.w("Task ID $taskId not found, unable to update consumed status")
        }
    }

    fun loadInvoiceAttachments(taskId: String) =
        realm.queryFirst<PKVInvoiceEntityV1>("taskId = $0", taskId)?.let {
            listOf(
                Triple("${taskId}_verordnung.p7s", "application/pkcs7-mime", it.kbvBinary),
                Triple("${taskId}_abgabedaten.p7s", "application/pkcs7-mime", it.invoiceBinary),
                Triple("${taskId}_quittung.p7s", "application/pkcs7-mime", it.erpPrBinary)
            )
        }

    suspend fun deleteInvoiceById(taskId: String): Unit =
        realm.tryWrite {
            queryFirst<PKVInvoiceEntityV1>("taskId = $0", taskId)?.let { delete(it) }
        }

    private fun ChargeableItemV1.toChargeableItem() = InvoiceData.ChargeableItem(
        description = when (this.descriptionTypeV1) {
            DescriptionTypeV1.PZN -> InvoiceData.ChargeableItem.Description.PZN(this.description)
            DescriptionTypeV1.HMNR -> InvoiceData.ChargeableItem.Description.HMNR(this.description)
            DescriptionTypeV1.TA1 -> InvoiceData.ChargeableItem.Description.TA1(this.description)
        },
        text = this.text,
        factor = this.factor,
        price = InvoiceData.PriceComponent(
            value = this.price?.value ?: 0.0,
            tax = this.price?.tax ?: 0.0
        )
    )

    fun hasUnreadInvoiceMessages(taskIds: List<String>): Flow<Boolean> {
        return if (taskIds.isEmpty()) {
            flowOf(false)
        } else {
            val orQuery = taskIds.indices.joinToString(" || ") { "taskId = $$it" }

            realm.query<PKVInvoiceEntityV1>(
                orQuery,
                *taskIds.toTypedArray()
            )
                .query("consumed = false")
                .count()
                .asFlow()
                .map { it > 0 }
        }
    }
}
