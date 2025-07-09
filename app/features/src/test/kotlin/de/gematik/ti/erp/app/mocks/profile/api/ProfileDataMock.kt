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

package de.gematik.ti.erp.app.mocks.profile.api

import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.mocks.PROFILE_ID
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import io.mockk.mockk

const val MOCK_SSO_TOKEN = "eyJlbmMiOiJBMjU2R0NNIiwiY3R5IjoiTkpXVCIsImV4cCI6MzMyNz" +
    "U3MTcwMjEsImFsZyI6ImRpciIsImtpZCI6IjAwMDEifQ" +
    "..X8msD7_H9N7lbgsl." +
    "jOd-n06-fnH-oKygEzsp9rveKPR7x9qr2vqwgpoqFXUu63KUev14ASruJx9XHlxbmIkqe" +
    "6TQ4GtPyEoKl5wb9CIPeIZ5qEkHKOEcYUS3MRl2BzdJQl32UKajKxDO_wg99sjpeab0QBy-" +
    "L64DCNqeEbyGAM7NByRd9CwHysbcvfuF3Pw-vHYS9edN65ZHsWEfpbA8ZcvIRlouqyRVtt9o-" +
    "avJXSgMiZbZkmh-qU71hxArXvLnqWuyHOT_fnlcnp7p_5TarKL1JDK70FKI3vT91Sabo_" +
    "fzQdRXPJygiSKqn8-5fcDaJIyLySan0EzRT_mO8cwHDav7kjI5CwCsF4KcOEor6kvy-" +
    "440SoiXZHsRKNMkRu-qrn2g7O5CFP5hpsBKo9j89PB_xvWgrYbYaSpxZBCBog58z8MKt" +
    "ugU5HQ4rglIh2KJeBIDBFj7tqwkc7QJHPsmnI-Uo6I3kB39UkULxKuvgcVo5EeJdc-" +
    "VhWyJyJEXFgDfV-ITSp-ZoYx2L4bIa4i2qWezWyB9ZK6bDv0tnqpta922meCo" +
    "YQgjnFrYDgpF_4nP9aFqreCDcQPXNi2W2ndXeCWFk3-hSfRFvODLOVpyRN5Tpn9i" +
    "73gZJuco_zGvQ2fy06lWP5HHzhNbG1luvEEVSjaKAe6WF5Upi647-Rr_GDNaumf" +
    "6Uws7NRKMD20h5k_m745KjCUyp8mXZKgDJj0cHZxYByR085JvepZVWNia3HsXolK" +
    "g1gj8C0LYqfJJNxaSysXj3-ERFp8P9UwpLFd9C9UYdzyDT2Oc5DitrZDLSAy1o2_" +
    "Z-Nk640ihBa3Btv3y-02_HaKHaht5Uf4sBt0e_PPxYdBmc6UbhGbnRn1ULbZoaC_" +
    "b0WveBbtw3eYxvYbxVHda3snDRVCqUbNa1SaytJE47fTft5p9g2e52i_" +
    "Ougzt279jlEa15Ju-mcW-JuqUXhKtjZtWPtwlx6WcsVkIxPgCYd1ZM6" +
    "kAY0U8MXvSu8EsMq-Z61XCDMBOhOQHwfA7-2vwEb7RRSi8Q4BzZnINI_" +
    "s0dYH6xug5Uwve1CdMgzB2uSgivPKc9SyN5wqdjcfrpSzwdA.s1-YFy125OJvqElKe-qDCw"

val API_MOCK_PROFILE = ProfilesData.Profile(
    id = PROFILE_ID,
    color = ProfilesData.ProfileColorNames.PINK,
    avatar = ProfilesData.Avatar.Baby,
    image = null,
    name = "Erna Mustermann",
    insurantName = "Erna Mustermann",
    insuranceIdentifier = "AOK",
    insuranceType = ProfilesData.InsuranceType.GKV,
    isConsentDrawerShown = true,
    lastAuthenticated = mockk(),
    lastTaskSynced = mockk(),
    active = true,
    singleSignOnTokenScope = null
)

val API_MOCK_WITH_SSO_TOKEN_PROFILE = ProfilesData.Profile(
    id = PROFILE_ID,
    color = ProfilesData.ProfileColorNames.PINK,
    avatar = ProfilesData.Avatar.Baby,
    image = null,
    name = "Erna Mustermann",
    insurantName = "Erna Mustermann",
    insuranceIdentifier = "AOK",
    insuranceType = ProfilesData.InsuranceType.GKV,
    isConsentDrawerShown = true,
    lastAuthenticated = mockk(),
    lastTaskSynced = mockk(),
    active = true,
    singleSignOnTokenScope = IdpData.ExternalAuthenticationToken(
        authenticatorId = "0001",
        authenticatorName = "Authenticator",
        token = IdpData.SingleSignOnToken(MOCK_SSO_TOKEN)
    )
)
