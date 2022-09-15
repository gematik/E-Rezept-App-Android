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

package de.gematik.ti.erp.app.fhir

import de.gematik.ti.erp.app.common.strings.Strings

fun Strings.statusMapping() = mapOf(
    "1" to kbvMemberStatus1,
    "3" to kbvMemberStatus3,
    "5" to kbvMemberStatus5
)

fun Strings.normSizeMapping() = mapOf(
    "KA" to kbvNormSizeKa,
    "KTP" to kbvNormSizeKtp,
    "N1" to kbvNormSizeN1,
    "N2" to kbvNormSizeN2,
    "N3" to kbvNormSizeN3,
    "NB" to kbvNormSizeNb,
    "Sonstiges" to kbvNormSizeSonstiges
)

fun Strings.codeToDosageFormMapping() = mapOf(
    "AEO" to kbvCodeDosageFormAeo,
    "AMP" to kbvCodeDosageFormAmp,
    "APA" to kbvCodeDosageFormApa,
    "ASN" to kbvCodeDosageFormAsn,
    "ASO" to kbvCodeDosageFormAso,
    "ATO" to kbvCodeDosageFormAto,
    "ATR" to kbvCodeDosageFormAtr,
    "AUB" to kbvCodeDosageFormAub,
    "AUC" to kbvCodeDosageFormAuc,
    "AUG" to kbvCodeDosageFormAug,
    "AUS" to kbvCodeDosageFormAus,
    "BAD" to kbvCodeDosageFormBad,
    "BAL" to kbvCodeDosageFormBal,
    "BAN" to kbvCodeDosageFormBan,
    "BEU" to kbvCodeDosageFormBeu,
    "BIN" to kbvCodeDosageFormBin,
    "BON" to kbvCodeDosageFormBon,
    "BPL" to kbvCodeDosageFormBpl,
    "BRE" to kbvCodeDosageFormBre,
    "BTA" to kbvCodeDosageFormBta,
    "CRE" to kbvCodeDosageFormCre,
    "DFL" to kbvCodeDosageFormDfl,
    "DIL" to kbvCodeDosageFormDil,
    "DIS" to kbvCodeDosageFormDis,
    "DKA" to kbvCodeDosageFormDka,
    "DOS" to kbvCodeDosageFormDos,
    "DRA" to kbvCodeDosageFormDra,
    "DRM" to kbvCodeDosageFormDrm,
    "DSC" to kbvCodeDosageFormDsc,
    "DSS" to kbvCodeDosageFormDss,
    "EDP" to kbvCodeDosageFormEdp,
    "EIN" to kbvCodeDosageFormEin,
    "ELE" to kbvCodeDosageFormEle,
    "ELI" to kbvCodeDosageFormEli,
    "EMU" to kbvCodeDosageFormEmu,
    "ESS" to kbvCodeDosageFormEss,
    "ESU" to kbvCodeDosageFormEsu,
    "EXT" to kbvCodeDosageFormExt,
    "FBE" to kbvCodeDosageFormFbe,
    "FBW" to kbvCodeDosageFormFbw,
    "FDA" to kbvCodeDosageFormFda,
    "FER" to kbvCodeDosageFormFer,
    "FET" to kbvCodeDosageFormFet,
    "FLA" to kbvCodeDosageFormFla,
    "FLE" to kbvCodeDosageFormFle,
    "FLU" to kbvCodeDosageFormFlu,
    "FMR" to kbvCodeDosageFormFmr,
    "FOL" to kbvCodeDosageFormFol,
    "FRB" to kbvCodeDosageFormFrb,
    "FSE" to kbvCodeDosageFormFse,
    "FTA" to kbvCodeDosageFormFta,
    "GEK" to kbvCodeDosageFormGek,
    "GEL" to kbvCodeDosageFormGel,
    "GLI" to kbvCodeDosageFormGli,
    "GLO" to kbvCodeDosageFormGlo,
    "GMR" to kbvCodeDosageFormGmr,
    "GPA" to kbvCodeDosageFormGpa,
    "GRA" to kbvCodeDosageFormGra,
    "GSE" to kbvCodeDosageFormGse,
    "GUL" to kbvCodeDosageFormGul,
    "HAS" to kbvCodeDosageFormHas,
    "HKM" to kbvCodeDosageFormHkm,
    "HKP" to kbvCodeDosageFormHkp,
    "HPI" to kbvCodeDosageFormHpi,
    "HVW" to kbvCodeDosageFormHvw,
    "IFA" to kbvCodeDosageFormIfa,
    "IFB" to kbvCodeDosageFormIfb,
    "IFD" to kbvCodeDosageFormIfd,
    "IFE" to kbvCodeDosageFormIfe,
    "IFF" to kbvCodeDosageFormIff,
    "IFK" to kbvCodeDosageFormIfk,
    "IFL" to kbvCodeDosageFormIfl,
    "IFS" to kbvCodeDosageFormIfs,
    "IHA" to kbvCodeDosageFormIha,
    "IHP" to kbvCodeDosageFormIhp,
    "IIE" to kbvCodeDosageFormIie,
    "IIL" to kbvCodeDosageFormIil,
    "IIM" to kbvCodeDosageFormIim,
    "IKA" to kbvCodeDosageFormIka,
    "ILO" to kbvCodeDosageFormIlo,
    "IMP" to kbvCodeDosageFormImp,
    "INF" to kbvCodeDosageFormInf,
    "INH" to kbvCodeDosageFormInh,
    "INI" to kbvCodeDosageFormIni,
    "INL" to kbvCodeDosageFormInl,
    "INS" to kbvCodeDosageFormIns,
    "IST" to kbvCodeDosageFormIst,
    "ISU" to kbvCodeDosageFormIsu,
    "IUP" to kbvCodeDosageFormIup,
    "KAN" to kbvCodeDosageFormKan,
    "KAP" to kbvCodeDosageFormKap,
    "KAT" to kbvCodeDosageFormKat,
    "KDA" to kbvCodeDosageFormKda,
    "KEG" to kbvCodeDosageFormKeg,
    "KER" to kbvCodeDosageFormKer,
    "KGU" to kbvCodeDosageFormKgu,
    "KID" to kbvCodeDosageFormKid,
    "KII" to kbvCodeDosageFormKii,
    "KKS" to kbvCodeDosageFormKks,
    "KLI" to kbvCodeDosageFormKli,
    "KLT" to kbvCodeDosageFormKlt,
    "KMP" to kbvCodeDosageFormKmp,
    "KMR" to kbvCodeDosageFormKmr,
    "KOD" to kbvCodeDosageFormKod,
    "KOM" to kbvCodeDosageFormKom,
    "KON" to kbvCodeDosageFormKon,
    "KPG" to kbvCodeDosageFormKpg,
    "KRI" to kbvCodeDosageFormKri,
    "KSS" to kbvCodeDosageFormKss,
    "KSU" to kbvCodeDosageFormKsu,
    "KTA" to kbvCodeDosageFormKta,
    "LAN" to kbvCodeDosageFormLan,
    "LII" to kbvCodeDosageFormLii,
    "LIQ" to kbvCodeDosageFormLiq,
    "LOE" to kbvCodeDosageFormLoe,
    "LOT" to kbvCodeDosageFormLot,
    "LOV" to kbvCodeDosageFormLov,
    "LSE" to kbvCodeDosageFormLse,
    "LTA" to kbvCodeDosageFormLta,
    "LUP" to kbvCodeDosageFormLup,
    "LUT" to kbvCodeDosageFormLut,
    "MIL" to kbvCodeDosageFormMil,
    "MIS" to kbvCodeDosageFormMis,
    "MIX" to kbvCodeDosageFormMix,
    "MRG" to kbvCodeDosageFormMrg,
    "MRP" to kbvCodeDosageFormMrp,
    "MTA" to kbvCodeDosageFormMta,
    "MUW" to kbvCodeDosageFormMuw,
    "NAG" to kbvCodeDosageFormNag,
    "NAO" to kbvCodeDosageFormNao,
    "NAS" to kbvCodeDosageFormNas,
    "NAW" to kbvCodeDosageFormNaw,
    "NDS" to kbvCodeDosageFormNds,
    "NSA" to kbvCodeDosageFormNsa,
    "NTR" to kbvCodeDosageFormNtr,
    "OCU" to kbvCodeDosageFormOcu,
    "OEL" to kbvCodeDosageFormOel,
    "OHT" to kbvCodeDosageFormOht,
    "OVU" to kbvCodeDosageFormOvu,
    "PAM" to kbvCodeDosageFormPam,
    "PAS" to kbvCodeDosageFormPas,
    "PEL" to kbvCodeDosageFormPel,
    "PEN" to kbvCodeDosageFormPen,
    "PER" to kbvCodeDosageFormPer,
    "PFL" to kbvCodeDosageFormPfl,
    "PFT" to kbvCodeDosageFormPft,
    "PHI" to kbvCodeDosageFormPhi,
    "PHV" to kbvCodeDosageFormPhv,
    "PIE" to kbvCodeDosageFormPie,
    "PIF" to kbvCodeDosageFormPif,
    "PII" to kbvCodeDosageFormPii,
    "PIJ" to kbvCodeDosageFormPij,
    "PIK" to kbvCodeDosageFormPik,
    "PIS" to kbvCodeDosageFormPis,
    "PIV" to kbvCodeDosageFormPiv,
    "PKI" to kbvCodeDosageFormPki,
    "PLE" to kbvCodeDosageFormPle,
    "PLF" to kbvCodeDosageFormPlf,
    "PLG" to kbvCodeDosageFormPlg,
    "PLH" to kbvCodeDosageFormPlh,
    "PLI" to kbvCodeDosageFormPli,
    "PLK" to kbvCodeDosageFormPlk,
    "PLS" to kbvCodeDosageFormPls,
    "PLV" to kbvCodeDosageFormPlv,
    "PPL" to kbvCodeDosageFormPpl,
    "PRS" to kbvCodeDosageFormPrs,
    "PSE" to kbvCodeDosageFormPse,
    "PST" to kbvCodeDosageFormPst,
    "PUD" to kbvCodeDosageFormPud,
    "PUL" to kbvCodeDosageFormPul,
    "RED" to kbvCodeDosageFormRed,
    "REK" to kbvCodeDosageFormRek,
    "RET" to kbvCodeDosageFormRet,
    "RGR" to kbvCodeDosageFormRgr,
    "RKA" to kbvCodeDosageFormRka,
    "RMS" to kbvCodeDosageFormRms,
    "RSC" to kbvCodeDosageFormRsc,
    "RSU" to kbvCodeDosageFormRsu,
    "RUT" to kbvCodeDosageFormRut,
    "SAF" to kbvCodeDosageFormSaf,
    "SAL" to kbvCodeDosageFormSal,
    "SAM" to kbvCodeDosageFormSam,
    "SCH" to kbvCodeDosageFormSch,
    "SEI" to kbvCodeDosageFormSei,
    "SHA" to kbvCodeDosageFormSha,
    "SIR" to kbvCodeDosageFormSir,
    "SLZ" to kbvCodeDosageFormSlz,
    "SMF" to kbvCodeDosageFormSmf,
    "SMT" to kbvCodeDosageFormSmt,
    "SMU" to kbvCodeDosageFormSmu,
    "SPA" to kbvCodeDosageFormSpa,
    "SPF" to kbvCodeDosageFormSpf,
    "SPL" to kbvCodeDosageFormSpl,
    "SPR" to kbvCodeDosageFormSpr,
    "SPT" to kbvCodeDosageFormSpt,
    "SRI" to kbvCodeDosageFormSri,
    "SSU" to kbvCodeDosageFormSsu,
    "STA" to kbvCodeDosageFormSta,
    "STB" to kbvCodeDosageFormStb,
    "STI" to kbvCodeDosageFormSti,
    "STR" to kbvCodeDosageFormStr,
    "SUB" to kbvCodeDosageFormSub,
    "SUE" to kbvCodeDosageFormSue,
    "SUL" to kbvCodeDosageFormSul,
    "SUP" to kbvCodeDosageFormSup,
    "SUS" to kbvCodeDosageFormSus,
    "SUT" to kbvCodeDosageFormSut,
    "SUV" to kbvCodeDosageFormSuv,
    "SWA" to kbvCodeDosageFormSwa,
    "TAB" to kbvCodeDosageFormTab,
    "TAE" to kbvCodeDosageFormTae,
    "TAM" to kbvCodeDosageFormTam,
    "TEE" to kbvCodeDosageFormTee,
    "TEI" to kbvCodeDosageFormTei,
    "TES" to kbvCodeDosageFormTes,
    "TIN" to kbvCodeDosageFormTin,
    "TKA" to kbvCodeDosageFormTka,
    "TLE" to kbvCodeDosageFormTle,
    "TMR" to kbvCodeDosageFormTmr,
    "TON" to kbvCodeDosageFormTon,
    "TPN" to kbvCodeDosageFormTpn,
    "TPO" to kbvCodeDosageFormTpo,
    "TRA" to kbvCodeDosageFormTra,
    "TRI" to kbvCodeDosageFormTri,
    "TRO" to kbvCodeDosageFormTro,
    "TRS" to kbvCodeDosageFormTrs,
    "TRT" to kbvCodeDosageFormTrt,
    "TSA" to kbvCodeDosageFormTsa,
    "TSD" to kbvCodeDosageFormTsd,
    "TSE" to kbvCodeDosageFormTse,
    "TSS" to kbvCodeDosageFormTss,
    "TST" to kbvCodeDosageFormTst,
    "TSY" to kbvCodeDosageFormTsy,
    "TTR" to kbvCodeDosageFormTtr,
    "TUB" to kbvCodeDosageFormTub,
    "TUE" to kbvCodeDosageFormTue,
    "TUP" to kbvCodeDosageFormTup,
    "TVW" to kbvCodeDosageFormTvw,
    "UTA" to kbvCodeDosageFormUta,
    "VAL" to kbvCodeDosageFormVal,
    "VAR" to kbvCodeDosageFormVar,
    "VCR" to kbvCodeDosageFormVcr,
    "VER" to kbvCodeDosageFormVer,
    "VGE" to kbvCodeDosageFormVge,
    "VKA" to kbvCodeDosageFormVka,
    "VLI" to kbvCodeDosageFormVli,
    "VOV" to kbvCodeDosageFormVov,
    "VST" to kbvCodeDosageFormVst,
    "VSU" to kbvCodeDosageFormVsu,
    "VTA" to kbvCodeDosageFormVta,
    "WAT" to kbvCodeDosageFormWat,
    "WGA" to kbvCodeDosageFormWga,
    "WKA" to kbvCodeDosageFormWka,
    "WKM" to kbvCodeDosageFormWkm,
    "WUE" to kbvCodeDosageFormWue,
    "XDG" to kbvCodeDosageFormXdg,
    "XDS" to kbvCodeDosageFormXds,
    "XFE" to kbvCodeDosageFormXfe,
    "XGM" to kbvCodeDosageFormXgm,
    "XHA" to kbvCodeDosageFormXha,
    "XHS" to kbvCodeDosageFormXhs,
    "XNC" to kbvCodeDosageFormXnc,
    "XPK" to kbvCodeDosageFormXpk,
    "XTC" to kbvCodeDosageFormXtc,
    "ZAM" to kbvCodeDosageFormZam,
    "ZBU" to kbvCodeDosageFormZbu,
    "ZCR" to kbvCodeDosageFormZcr,
    "ZGE" to kbvCodeDosageFormZge,
    "ZKA" to kbvCodeDosageFormZka,
    "ZPA" to kbvCodeDosageFormZpa
)
