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

val licencePlaceholderHeader =
    """
    /*
     * ${'$'}{GEMATIK_COPYRIGHT_STATEMENT}
     */
    """.trimIndent()

val oldLicenceHeader =
    """
    /*
     * Copyright 2023, gematik GmbH
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
    """.trimIndent()

val licenceHeader =
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
    """.trimIndent()

@Suppress("NestedBlockDepth")
class LicenceRule : Rule("licence-header") {
    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        if (node.elementType == KtStubElementTypes.FILE && (node.psi as? KtFile)?.isScript() == false) {
            node.children().forEach { child ->
                if (child.elementType == KtTokens.BLOCK_COMMENT) {
                    when {
                        child.isLicencePlaceholderHeader() -> {
                            emit(child.startOffset, "Licence header placeholder found", true)
                            if (autoCorrect) removeNodeWithWhitespace(node, child)
                        }
                        child.isOldLicenceHeader() -> {
                            emit(child.startOffset, "Deprecated licence header found", true)
                            if (autoCorrect) removeNodeWithWhitespace(node, child)
                        }
                        child.isMisplacedLicenceHeader() -> {
                            emit(child.startOffset, "Misplaced licence header found", true)
                            if (autoCorrect) removeNodeWithWhitespace(node, child)
                        }
                    }
                }
            }

            if (!node.firstChildNode.isLicenceHeader()) {
                emit(node.startOffset, "Licence header missing", true)
                if (autoCorrect) addLicenceHeader(node)
            }
        }
    }

    private fun ASTNode.isLicenceHeader() =
        this.elementType == KtTokens.BLOCK_COMMENT && this.text.trim() == licenceHeader.trim() && this.lineNumber() == 1

    private fun ASTNode.isLicencePlaceholderHeader() =
        this.text.trim() == licencePlaceholderHeader.trim()

    private fun ASTNode.isOldLicenceHeader() =
        this.text.trim() == oldLicenceHeader.trim()

    private fun ASTNode.isMisplacedLicenceHeader() =
        this.text.trim() == licenceHeader.trim() && this.lineNumber() != 1

    private fun removeNodeWithWhitespace(node: ASTNode, child: ASTNode) {
        val firstNode = child.treePrev?.takeIf { it.isWhiteSpaceWithNewline() }
        val lastNode = child.treeNext?.takeIf { it.isWhiteSpaceWithNewline() }
        if (firstNode != null && lastNode != null) {
            node.removeRange(firstNode, lastNode)
        } else {
            node.removeChild(child)
        }
    }

    private fun addLicenceHeader(node: ASTNode) {
        val licenceNode = PsiCommentImpl(KtTokens.BLOCK_COMMENT, licenceHeader)
        node.addChild(licenceNode, node.firstChildNode)
        licenceNode.upsertWhitespaceAfterMe("\n\n")
    }
}
