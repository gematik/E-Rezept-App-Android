/*
 * Copyright (c) 2022 gematik GmbH
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

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.isWhiteSpaceWithNewline
import com.pinterest.ktlint.core.ast.lineNumber
import com.pinterest.ktlint.core.ast.upsertWhitespaceAfterMe
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiCommentImpl
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.children
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes

val licenceHeader = """
    /*
     * ${'$'}{GEMATIK_COPYRIGHT_STATEMENT}
     */
""".trimIndent()

class LicenceRule : Rule("licence-header") {
    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        if (node.elementType == KtStubElementTypes.FILE) {
            if ((node.psi as KtFile).isScript()) {
                return
            }

            val commentChild = node.findChildByType(KtTokens.BLOCK_COMMENT)

            val licenceHeaderFound =
                commentChild != null && commentChild.lineNumber() == 1 && commentChild.text.trim() == licenceHeader.trim()

            if (!licenceHeaderFound) {
                emit(node.startOffset, "Licence header missing", true)

                if (autoCorrect) {
                    node.children().forEach { child ->
                        // remove all duplicates or misplaced licence headers
                        if (child.elementType == KtTokens.BLOCK_COMMENT && child.text == licenceHeader) {
                            val firstNode = child.treePrev?.takeIf { it.isWhiteSpaceWithNewline() } ?: child
                            val lastNode = child.treeNext?.takeIf { it.isWhiteSpaceWithNewline() }

                            node.removeRange(firstNode, lastNode)
                        }
                    }
                    val licenceNode = PsiCommentImpl(KtTokens.BLOCK_COMMENT, licenceHeader)
                    node.addChild(
                        licenceNode,
                        node.firstChildNode
                    )
                    licenceNode.upsertWhitespaceAfterMe("\n\n")
                }
            }
        }
    }
}
