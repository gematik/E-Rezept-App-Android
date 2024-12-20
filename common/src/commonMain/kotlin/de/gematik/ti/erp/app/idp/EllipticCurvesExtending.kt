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

@file:Suppress("ktlint:max-line-length", "ktlint:argument-list-wrapping", "MandatoryBracesIfStatements", "MagicNumber")

package de.gematik.ti.erp.app.idp

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.idp.EcdsaUsingShaAlgorithmExtending.EcdsaBP256R1UsingSha256
import de.gematik.ti.erp.app.idp.EcdsaUsingShaAlgorithmExtending.EcdsaBP384R1UsingSha384
import de.gematik.ti.erp.app.idp.EcdsaUsingShaAlgorithmExtending.EcdsaBP512R1UsingSha512
import org.jose4j.jwa.AlgorithmFactoryFactory
import org.jose4j.keys.EllipticCurves
import java.math.BigInteger
import java.security.spec.ECFieldFp
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.EllipticCurve

@Requirement(
    "A_17207#1",
    sourceSpecification = "gemSpec_Krypt",
    rationale = "Support for required algorithms implemented using ECDSA."
)
@Requirement(
    "GS-A_4357-02#2",
    sourceSpecification = "gemSpec_Krypt",
    rationale = "Support for required algorithms implemented using ECDSA."
)
@Requirement(
    "A_21332-02#2",
    sourceSpecification = "gemSpec_Krypt",
    rationale = "The brainpool curves (P-256 oder P-384) for signature checking."
)
object EllipticCurvesExtending : EllipticCurves() {
    const val BP_256 = "BP-256"
    const val BP_384 = "BP-384"
    const val BP_512 = "BP-512"
    val BP256 = ECParameterSpec(
        EllipticCurve(
            ECFieldFp(BigInteger("76884956397045344220809746629001649093037950200943055203735601445031516197751")),
            BigInteger("56698187605326110043627228396178346077120614539475214109386828188763884139993"),
            BigInteger("17577232497321838841075697789794520262950426058923084567046852300633325438902")
        ),
        ECPoint(
            BigInteger("63243729749562333355292243550312970334778175571054726587095381623627144114786"),
            BigInteger("38218615093753523893122277964030810387585405539772602581557831887485717997975")
        ),
        BigInteger("76884956397045344220809746629001649092737531784414529538755519063063536359079"),
        1
    )
    private val BP384 = ECParameterSpec(
        EllipticCurve(
            ECFieldFp(BigInteger("21659270770119316173069236842332604979796116387017648600081618503821089934025961822236561982844534088440708417973331")),
            BigInteger("19048979039598244295279281525021548448223459855185222892089532512446337024935426033638342846977861914875721218402342"),
            BigInteger("717131854892629093329172042053689661426642816397448020844407951239049616491589607702456460799758882466071646850065")
        ),
        ECPoint(
            BigInteger("4480579927441533893329522230328287337018133311029754539518372936441756157459087304048546502931308754738349656551198"),
            BigInteger("21354446258743982691371413536748675410974765754620216137225614281636810686961198361153695003859088327367976229294869")
        ),
        BigInteger("21659270770119316173069236842332604979796116387017648600075645274821611501358515537962695117368903252229601718723941"),
        1
    )
    private val BP512 = ECParameterSpec(
        EllipticCurve(
            ECFieldFp(BigInteger("8948962207650232551656602815159153422162609644098354511344597187200057010413552439917934304191956942765446530386427345937963894309923928536070534607816947")),
            BigInteger("6294860557973063227666421306476379324074715770622746227136910445450301914281276098027990968407983962691151853678563877834221834027439718238065725844264138"),
            BigInteger("3245789008328967059274849584342077916531909009637501918328323668736179176583263496463525128488282611559800773506973771797764811498834995234341530862286627")
        ),
        ECPoint(
            BigInteger("6792059140424575174435640431269195087843153390102521881468023012732047482579853077545647446272866794936371522410774532686582484617946013928874296844351522"),
            BigInteger("6592244555240112873324748381429610341312712940326266331327445066687010545415256461097707483288650216992613090185042957716318301180159234788504307628509330")
        ),
        BigInteger("8948962207650232551656602815159153422162609644098354511344597187200057010413418528378981730643524959857451398370029280583094215613882043973354392115544169"),
        1
    )
    private var initializedInSession = false

    fun init(): Boolean = if (initializedInSession) {
        true
    } else try {
        addCurve("BP-256", BP256)
        addCurve("BP-384", BP384)
        addCurve("BP-512", BP512)
        AlgorithmFactoryFactory.getInstance().jwsAlgorithmFactory.registerAlgorithm(
            EcdsaBP256R1UsingSha256()
        )
        AlgorithmFactoryFactory.getInstance().jwsAlgorithmFactory.registerAlgorithm(
            EcdsaBP384R1UsingSha384()
        )
        AlgorithmFactoryFactory.getInstance().jwsAlgorithmFactory.registerAlgorithm(
            EcdsaBP512R1UsingSha512()
        )
        initializedInSession = true
        true
    } catch (e: Exception) {
        throw IllegalStateException("failure on init $e")
    }
}
