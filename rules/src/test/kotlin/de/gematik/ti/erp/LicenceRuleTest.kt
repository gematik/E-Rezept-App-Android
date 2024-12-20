/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp

import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.test.format
import com.pinterest.ktlint.test.lint
import kotlin.test.Test
import kotlin.test.assertEquals

class LicenceRuleTest {
    @Test
    fun `lint missing licence header`() {
        val lintErrors =
            LicenceRule().lint(
                """
                /* */
                
                package a.b.c
                
                import d.e.f
                """.trimIndent()
            )

        val expected =
            LintError(
                1,
                1,
                "licence-header",
                "Licence header missing"
            )

        assertEquals(listOf(expected), lintErrors)
    }

    @Test
    fun `lint found licence header`() {
        val lintErrors =
            LicenceRule().lint(
                """
                /*
                 * Copyright 2024, gematik GmbH
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
                
                package a.b.c
                
                import d.e.f
                """.trimIndent()
            )

        assertEquals(emptyList(), lintErrors)
    }

    @Test
    fun `lint found licence header in wrong position`() {
        val lintErrors =
            LicenceRule().lint(
                """
                /* */
                
                /*
                 * Copyright 2024, gematik GmbH
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
                
                package a.b.c
                
                import d.e.f
                """.trimIndent()
            )

        val expected = listOf<LintError>(
            LintError(
                1,
                1,
                "licence-header",
                "Licence header missing"
            ),
            LintError(
                3,
                1,
                "licence-header",
                "Misplaced licence header found"
            )
        )
        assertEquals(expected, lintErrors)
    }

    @Test
    fun `format licence header - comment first`() {
        val given =
            LicenceRule().format(
                """
                /* Some comment */
                
                package a.b.c
                
                import d.e.f
                """.trimIndent()
            )

        val expected =
            """
            /*
             * Copyright 2024, gematik GmbH
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
            
            /* Some comment */
            
            package a.b.c
            
            import d.e.f
            """.trimIndent()

        assertEquals(expected, given)
    }

    @Test
    fun `format licence header - old placeholder`() {
        val given =
            LicenceRule().format(
                """
                /*
                 * ${'$'}{GEMATIK_COPYRIGHT_STATEMENT}
                 */
                
                package a.b.c
                
                import d.e.f
                """.trimIndent()
            )

        val expected =
            """
            /*
             * Copyright 2024, gematik GmbH
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

            package a.b.c
            
            import d.e.f
            """.trimIndent()

        assertEquals(expected, given)
    }

    @Test
    fun `format licence header - package first`() {
        val given = LicenceRule().format(
            """
            package a.b.c
            
            import d.e.f
            import d.e.f
            import d.e.f
            """.trimIndent()
        )

        val expected =
            """
            /*
             * Copyright 2024, gematik GmbH
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
            
            package a.b.c
            
            import d.e.f
            import d.e.f
            import d.e.f
            """.trimIndent()

        assertEquals(expected, given)
    }

    @Test
    fun `format licence header - wrong position`() {
        val given =
            LicenceRule().format(
                """
                package a.b.c
                
                /*
                 * Copyright 2024, gematik GmbH
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
                
                import d.e.f
                """.trimIndent()
            )

        val expected =
            """
            /*
             * Copyright 2024, gematik GmbH
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
            
            package a.b.c
            
            import d.e.f
            """.trimIndent()

        assertEquals(expected, given)
    }

    @Test
    fun `format licence header - script - expect same output`() {
        val given =
            LicenceRule().format(
                """
                /*
                 * Copyright 2024, gematik GmbH
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
                
                import a.b.c
    
                abc {}
                """.trimIndent(),
                script = true
            )

        val expected =
            """
            /*
             * Copyright 2024, gematik GmbH
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
                
            import a.b.c

            abc {}
            """.trimIndent()

        assertEquals(expected, given)
    }
}
