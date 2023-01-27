/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp

import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.test.format
import com.pinterest.ktlint.test.lint
import kotlin.test.assertEquals
import kotlin.test.Test

class LicenceRuleTest {
    @Test
    fun `lint missing licence header`() {
        val lintErrors = LicenceRule().lint(
            """
            /* */
            
            package a.b.c
            
            import d.e.f
            """.trimIndent()
        )

        val expected = LintError(
            1,
            1,
            "licence-header",
            "Licence header missing"
        )

        assertEquals(listOf(expected), lintErrors)
    }

    @Test
    fun `lint found licence header`() {
        val lintErrors = LicenceRule().lint(
            """
            /*
             * ${'$'}{GEMATIK_COPYRIGHT_STATEMENT}
             */
            
            package a.b.c
            
            import d.e.f
            """.trimIndent()
        )

        assertEquals(emptyList(), lintErrors)
    }

    @Test
    fun `lint found licence header in wrong position`() {
        val lintErrors = LicenceRule().lint(
            """
            /* */
            
            /*
             * ${'$'}{GEMATIK_COPYRIGHT_STATEMENT}
             */
            
            package a.b.c
            
            import d.e.f
            """.trimIndent()
        )

        val expected = LintError(
            1,
            1,
            "licence-header",
            "Licence header missing"
        )

        assertEquals(listOf(expected), lintErrors)
    }

    @Test
    fun `format licence header - comment first`() {
        val given = LicenceRule().format(
            """
            /* Some comment */
            
            package a.b.c
            
            import d.e.f
            """.trimIndent()
        )

        val expected = """
            /*
             * ${'$'}{GEMATIK_COPYRIGHT_STATEMENT}
             */
            
            /* Some comment */
            
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

        val expected = """
            /*
             * ${'$'}{GEMATIK_COPYRIGHT_STATEMENT}
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
        val given = LicenceRule().format(
            """
            package a.b.c
            
            /*
             * ${'$'}{GEMATIK_COPYRIGHT_STATEMENT}
             */
            
            import d.e.f
            """.trimIndent()
        )

        val expected = """
            /*
             * ${'$'}{GEMATIK_COPYRIGHT_STATEMENT}
             */
            
            package a.b.c
            
            import d.e.f
        """.trimIndent()

        assertEquals(expected, given)
    }

    @Test
    fun `format licence header - script - expect same output`() {
        val given = LicenceRule().format(
            """
            import a.b.c
            
            /*
             * ${'$'}{GEMATIK_COPYRIGHT_STATEMENT}
             */
            
            abc {}
            """.trimIndent(),
            script = true
        )

        val expected = """
            import a.b.c
            
            /*
             * ${'$'}{GEMATIK_COPYRIGHT_STATEMENT}
             */
            
            abc {}
        """.trimIndent()

        assertEquals(expected, given)
    }
}
