package no.nav.uføre.søknad

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.time.LocalDate

/**
 * Domenemodell for «Søknad om uføretrygd» (NAV 12-06.05), generert fra JSON Schema
 * `https://skjemabygging.nav.no/forms/nav120605/spec?revision=76` (docs/schema-prod.json).
 *
 * Feltnavn følger JSON-nøklene. Nummererte eller tvetydige nøkler (f.eks. `fornavn1`,
 * `bruttoArligNaeringsinntekt2`) har fått lesbare navn, med opprinnelig nøkkel i `@SerialName`.
 */
@Serializable
data class Søknad(
    val language: String,
    val formRevision: Int,
    val data: Søknadsinnsending,
)

@Serializable
data class Søknadsinnsending(
    val data: Søknadsdata,
    val attachments: List<VedleggFelt> = emptyList(),
    val selfDeclaration: Boolean,
)

@Serializable
data class Søknadsdata(
    // Arbeid og næring ved siden av uføretrygd
    @SerialName("erDuIArbeidNa") val skalDuJobbeVedSidenAvUforetrygden: JaNei,
    val oppgiForventetInntektPerManed: Double? = null,
    val erDuNaeringsdrivende: JaNei,
    val skalArbeide: SkalArbeide? = null,
    val firmaetsOrganisasjonsnummer: String? = null,
    @SerialName("firmaetsNavn1") val firmaetsNavn: String? = null,
    val driverDuGardsbruk: JaNei,

    // Personopplysninger
    val dineOpplysninger: DineOpplysninger? = null,
    val hvaErDinSivilstatus: Sivilstatus,

    // Ektefelle eller partner
    @SerialName("fornavn") val ektefelleFornavn: String? = null,
    @SerialName("etternavn") val ektefelleEtternavn: String? = null,
    val harDinEktefelleEllerPartnerNorskFodselsnummerEllerDNummer: JaNei? = null,
    val ektefelleEllerPartnersFodselsnummerEllerDNummer: String? = null,
    @SerialName("ektefelleEllerPartnersFodselsdatoDdMmAaaa") val ektefelleEllerPartnersFodselsdato: Dato? = null,
    val erDinEktefelleEllerPartnerUtenlandskStatsborger: JaNei? = null,
    val hvaErDinEktefelleEllerPartnersStatsborgerskap: String? = null,
    val haddeDinEktefelleEllerPartnerEtAnnetNavnForDereGiftetDere: JaNei? = null,
    val ektefellesEllerPartnersTidligereNavn: String? = null,
    val erDereBosattPaSammeAdresse: JaNei? = null,

    // Samboer
    @SerialName("fornavn1") val samboerFornavn: String? = null,
    @SerialName("etternavn1") val samboerEtternavn: String? = null,
    val harDinSamboerNorskFodselsnummerEllerDNummer: JaNei? = null,
    val samboersFodselsnummerEllerDNummer: String? = null,
    @SerialName("samboersFodselsdatoDdMmAaaa") val samboersFodselsdato: Dato? = null,
    val erDinSamboerUtenlandskStatsborger: JaNei? = null,
    val hvaErDinSamboersStatsborgerskap: String? = null,
    val erDuOgDinSamboerBosattPaSammeAdresse: JaNei? = null,
    val narBleDereSamboereManedOgAr: String? = null,
    val harDuOgDinSamboerFellesBarn: JaNei? = null,

    // Arbeid og næring i dag / da du ble syk
    val arbeidIDag: ArbeidIDag? = null,
    val naeringsdrivende: Naeringsdrivende? = null,
    val varDuArbeidstakerDaDuBleSyk: JaNei,
    @SerialName("arbeidsgiversNavn") val arbeidsgivereDaDuBleSyk: List<ArbeidsgiverDaDuBleSyk> = emptyList(),
    val varDuNaeringsdrivendeEllerGardbrukerDaDuBleSyk: JaNei,
    val harDuAvsluttetNaeringsvirksomheten: JaNei? = null,
    val harDuAvtjentMilitaerEllerSivilForstegangstjeneste: JaNei,
    val hvilketArstallAvtjenteDuMilitaerEllerSivilForstegangstjeneste: Int? = null,

    // Utenlandsopphold og statsborgerskap
    val harDuBoddOgEllerArbeidetUtenforNorgeEtterAtDuFylte16Ar: JaNei,
    val opplysningerOmArbeidOgOppholdIUtlandet: List<OppholdIUtlandet> = emptyList(),
    val erDuNorskStatsborger: JaNei,
    val statsborgerskap: String? = null,
    val mottarDuPensjonFraUtlandet: JaNei,
    val opplysningerOmPensjonFraUtlandet: List<PensjonFraUtlandet> = emptyList(),

    // Yrkesskade og barn
    val harDuEnGodkjentYrkesskadeEllerYrkessykdomSomErArsakTilAtDuSokerOmUforetrygd: JaNei,
    val harDuBarnUnder18Ar: JaNei,
    val erDinSamboerForelderTilBarnetDuSokerBarnetilleggFor: JaNei? = null,
    val erDinEktefelleEllerPartnerForelderTilBarnetDuSokerBarnetilleggFor: JaNei? = null,
    val opplysningerOmBarnUnder18Ar: List<Barn> = emptyList(),

    // Inntekt – søker
    val aretInntektsopplysningeneGjelderFor: Int? = null,
    val harDuEllerForventerDuAFaArbeidsinntekt: JaNei? = null,
    val arligBruttoArbeidsinntekt: Double? = null,
    val mottarDuEnEllerFlerePensjonsgivendeYtelserFraNav: JaNei? = null,
    val forventetBruttoArligPensjonsgivendeYtelser: Double? = null,
    val harDuEllerForventerDuAFaNaeringsinntekt: JaNei? = null,
    val bruttoArligNaeringsinntekt: Double? = null,
    val harDuEllerForventerDuAFaInntektFraUtlandet: JaNei? = null,
    val bruttoArsinntektFraUtlandet: Double? = null,
    val mottarDuPensjonFraAndreEnnNavOffentligEllerPrivat: JaNei? = null,
    val bruttoArligPensjonFraAndreEnnNav: Double? = null,
    val mottarDuFamiliepleieytelseOgEllerKrigspensjonFraNav: JaNei? = null,
    val bruttoArligFamiliepleieytelseOgKrigspensjonFraNav: Double? = null,
    @SerialName("mottarDuPensjonFraUtlandet1") val mottarDuPensjonFraUtlandetIInntektsaret: JaNei? = null,
    val bruttoArligPensjonFraUtlandet: Double? = null,

    // Inntekt – ektefelle eller partner
    val harDinEktefelleEllerPartnerArbeidsinntekt: JaNei? = null,
    @SerialName("belopArligBruttoArbeidsinntekt") val ektefelleArligBruttoArbeidsinntekt: Double? = null,
    val mottarDinEktefelleEllerPartnerEnEllerFlerePensjonsgivendeYtelserFraNav: JaNei? = null,
    @SerialName("forventetBruttoArligPensjonsgivendeYtelser2") val ektefelleForventetBruttoArligPensjonsgivendeYtelser: Double? = null,
    val harDinEktefelleEllerPartnerNaeringsinntekt: JaNei? = null,
    @SerialName("bruttoArligNaeringsinntekt1") val ektefelleBruttoArligNaeringsinntekt: Double? = null,
    val harDinEktefelleEllerPartnerInntektFraUtlandet: JaNei? = null,
    @SerialName("bruttoArsinntektFraUtlandet2") val ektefelleBruttoArsinntektFraUtlandet: Double? = null,
    val mottarDinEktefelleEllerPartnerPensjonFraAndreEnnNavOffentligEllerPrivat: JaNei? = null,
    @SerialName("bruttoArligPensjonFraAndreEnnNav2") val ektefelleBruttoArligPensjonFraAndreEnnNav: Double? = null,
    val mottarDinEktefelleEllerPartnerIntroduksjonsstonadSupplerendeStonadEllerKrigspensjonFraNav: JaNei? = null,
    @SerialName("bruttoArligIntroduksjonsstonadSupplerendeStonadOgKrigspensjonFraNav")
    val ektefelleBruttoArligIntroduksjonsstonadSupplerendeStonadOgKrigspensjonFraNav: Double? = null,
    val mottarEktefelleEllerPartnerPensjonFraUtlandet: JaNei? = null,
    @SerialName("bruttoArligPensjonFraUtlandet2") val ektefelleBruttoArligPensjonFraUtlandet: Double? = null,

    // Inntekt – samboer
    @SerialName("harDinSamboerPartnerArbeidsinntekt") val harDinSamboerArbeidsinntekt: JaNei? = null,
    @SerialName("belopArligBruttoArbeidsinntekt1") val samboerArligBruttoArbeidsinntekt: Double? = null,
    val mottarDinSamboerEnEllerFlerePensjonsgivendeYtelserFraNav: JaNei? = null,
    @SerialName("forventetBruttoArligPensjonsgivendeYtelser3") val samboerForventetBruttoArligPensjonsgivendeYtelser: Double? = null,
    val harDinSamboerNaeringsinntekt: JaNei? = null,
    @SerialName("bruttoArligNaeringsinntekt2") val samboerBruttoArligNaeringsinntekt: Double? = null,
    val harDinSamboerInntektFraUtlandet: JaNei? = null,
    @SerialName("bruttoArsinntektFraUtlandet3") val samboerBruttoArsinntektFraUtlandet: Double? = null,
    val mottarDinSamboerPensjonFraAndreEnnNavOffentligEllerPrivat: JaNei? = null,
    @SerialName("bruttoArligPensjonFraAndreEnnNav3") val samboerBruttoArligPensjonFraAndreEnnNav: Double? = null,
    val mottarSamboerenDinIntroduksjonsstonadSupplerendeStonadEllerKrigspensjonFraNav: JaNei? = null,
    @SerialName("bruttoArligIntroduksjonsstonadSupplerendeStonadOgKrigspensjonFraNav2")
    val samboerBruttoArligIntroduksjonsstonadSupplerendeStonadOgKrigspensjonFraNav: Double? = null,
    val mottarSamboerenDinPensjonFraUtlandet: JaNei? = null,
    @SerialName("bruttoArligPensjonFraUtlandet3") val samboerBruttoArligPensjonFraUtlandet: Double? = null,

    // Bosted utenfor Norge/EØS
    val borDuUtenforNorgeEllerEos: JaNei,
    val harDuHattOppholdINorge: JaNei? = null,
    val periodeneDuHarBoddINorge: List<OppholdINorge> = emptyList(),
    @SerialName("erDuIArbeidNa1") val erDuIArbeidNa: JaNei? = null,
    val narSluttetDuAArbeideManedOgAr: String? = null,
    val harDuUtdanning: JaNei? = null,
    val utdanningenDin: List<Utdanning> = emptyList(),
    val harDuArbeidsforhold: JaNei? = null,
    val arbeidsforholdeneDine: List<Arbeidsforhold> = emptyList(),

    // Dokumentasjon og erklæring
    val harDuAvtaltMedNavAtDuSkalSendeMerDokumentasjon: JaNei,
    val hvilkenDokumentasjonHarDuAvtaltMedNavDuSkalSende: String? = null,
    @SerialName("jegErKjentMedAtNavKanInnhenteDeOpplysningeneSomErNodvendigForAAvgjoreSoknadenMinOpplysningeneErGittEtterBesteSkjonnOgOverbevisningOgErSaFullstendigeSomMuligErklaeringenOgUnderskriftenGjelderHeleSoknadenOmUforetrygd")
    val erKjentMedOpplysningspliktOgSamtykke: Boolean,

    // Vedlegg
    val kopiAvFodselsattest: VedleggFelt? = null,
    val kopiAvBarnetsLegitimasjon: VedleggFelt? = null,
    val forsorgelsesEllerSamvaersavtale: VedleggFelt? = null,
    val passEllerVigselsattestTilDinEktefelleEllerPartner: VedleggFelt? = null,
    val passTilDinSamboer: VedleggFelt? = null,
    val kopiAvSisteLonnslipp: VedleggFelt? = null,
    val dokumentasjonPaArbeidsforhold: VedleggFelt? = null,
    val inntektsskjemaForGardbrukereUforetrygdNav120609: VedleggFelt? = null,
    val inntektsskjemaForNaeringsdrivendeOgAnsattIEgetAksjeselskapUforetrygdNav120607: VedleggFelt? = null,
    val dokumentasjonPaAvsluttetNaeringsvirksomhet: VedleggFelt? = null,
    val dokumentasjonSomErAvtaltMedNav: VedleggFelt? = null,
    @Serializable(with = VedleggslisteSerializer::class)
    val annenDokumentasjon: List<VedleggFelt>? = null,
)

@Serializable
enum class JaNei {
    @SerialName("ja") JA,
    @SerialName("nei") NEI,
}

@Serializable
enum class Sivilstatus {
    @SerialName("enslig") ENSLIG,
    @SerialName("giftEllerPartner") GIFT_ELLER_PARTNER,
    @SerialName("samboer") SAMBOER,
    @SerialName("skilt") SKILT,
    @SerialName("separert") SEPARERT,
    @SerialName("enkeEllerEnkemann") ENKE_ELLER_ENKEMANN,
}

@Serializable
data class SkalArbeide(
    val informasjonOmFirma: String? = null,
    val jegOnskerALeggeVedOpplysningerOmArbeidsforholdSomEgetVedlegg: Boolean? = null,
)

@Serializable
data class DineOpplysninger(
    val fornavn: String,
    val etternavn: String,
    val identitet: Identitet,
    /** Formen varierer med adressetype, og schemaet tillater vilkårlige felter. */
    val adresse: JsonObject? = null,
    val adresseVarighet: AdresseVarighet? = null,
)

@Serializable
data class Identitet(
    val harDuFodselsnummer: String? = null,
    val identitetsnummer: String? = null,
    val fodselsdato: String? = null,
)

@Serializable
data class AdresseVarighet(
    val gyldigFraOgMed: Dato,
    @Serializable(with = DatoEllerTomStrengSerializer::class)
    val gyldigTilOgMed: LocalDate? = null,
)

@Serializable
data class ArbeidIDag(
    val mottarDuFostergodtgjorelseEllerOmsorgslonn: JaNei? = null,
    val mottarDuEllerVilDuFaEtterlonnEllerAnnetSluttoppgjorFraArbeidsgiver: JaNei? = null,
)

@Serializable
data class Naeringsdrivende(
    val erDuAnsattIEgenBedriftAksjeselskap: JaNei? = null,
    val mottarDuStyrehonorarOgEllerGodtgjorelserIForbindelseMedVerv: JaNei? = null,
)

@Serializable
data class ArbeidsgiverDaDuBleSyk(
    @SerialName("arbeidsgiversNavn1") val arbeidsgiversNavn: String? = null,
    /** 0–100 */
    val stillingsprosent: Double? = null,
)

@Serializable
data class Land(
    val value: String,
    val label: String,
)

@Serializable
data class OppholdIUtlandet(
    val oppholdsland: Land? = null,
    @SerialName("oppholdFraManedOgAr1") val fraManedOgAr: String? = null,
    @SerialName("oppholdTilManedOgAr1") val tilManedOgAr: String? = null,
    val boddEllerArbeidet: BoddEllerArbeidet? = null,
)

@Serializable
data class BoddEllerArbeidet(
    val bodd: Boolean? = null,
    val arbeidet: Boolean? = null,
)

@Serializable
data class PensjonFraUtlandet(
    val landDuMottarPensjonFra: Land? = null,
    val typePensjon: String? = null,
    val belopPerArForSkatt: Double? = null,
)

@Serializable
data class Barn(
    val personalia: BarnPersonalia? = null,
    val borBarnetOgBeggeForeldrenePaSammeAdresse: JaNei? = null,
    val harDereAvtaleOmDeltBostedEtterBarneloven36: JaNei? = null,
    val borBarnetMedDeg: JaNei? = null,
    val forsorgerDuBarnet: JaNei? = null,
    val betalesDetBidragForBarnetViaNav: JaNei? = null,
    val erBarnetDittFosterbarn: JaNei? = null,
)

@Serializable
data class BarnPersonalia(
    val fornavn: String? = null,
    val etternavn: String? = null,
    val harDetteBarnetNorskFodselsnummerEllerDNummer: JaNei? = null,
    val fodselsnummerDNummer: String? = null,
    @SerialName("fodselsdatoDdMmAaaa") val fodselsdato: Dato? = null,
    val harBarnetEllerBarnaLegitimasjonMedBilde: JaNei? = null,
)

@Serializable
data class OppholdINorge(
    @SerialName("oppholdINorgeFraManedOgAr1") val fraManedOgAr: String? = null,
    @SerialName("oppholdINorgeTilManedOgAr1") val tilManedOgAr: String? = null,
)

@Serializable
data class Utdanning(
    val skoleKurs: String? = null,
    val utdanningFraManedOgAr: String? = null,
    val utdanningTilManedOgAr: String? = null,
    val gradTittel: String? = null,
)

@Serializable
data class Arbeidsforhold(
    val arbeidsgiver: String? = null,
    val stilling: String? = null,
    /** 0–100 */
    val stillingsprosent: Double? = null,
    @SerialName("ansattFraManedOgAr1") val ansattFraManedOgAr: String? = null,
    @SerialName("ansattTilManedOgAr1") val ansattTilManedOgAr: String? = null,
)

/**
 * Samlet representasjon av vedleggsfeltene. Schemaet tillater tre former
 * (fullt vedleggsobjekt, `{ "key": … }` eller bare valget som streng);
 * [VedleggSerializer] normaliserer alle til denne klassen.
 */
@Serializable
data class Vedlegg(
    val attachmentId: String? = null,
    val navId: String? = null,
    val type: Vedleggstype? = null,
    @SerialName("value") val valg: Vedleggsvalg? = null,
    @SerialName("title") val tittel: String? = null,
    @SerialName("additionalDocumentation") val tilleggsinformasjon: String? = null,
    @SerialName("files") val filer: List<Vedleggsfil> = emptyList(),
)

@Serializable
enum class Vedleggstype {
    @SerialName("default") DEFAULT,
    @SerialName("other") OTHER,
}

@Serializable
enum class Vedleggsvalg {
    @SerialName("leggerVedNaa") LEGGER_VED_NAA,
    @SerialName("ettersender") ETTERSENDER,
    @SerialName("levertTidligere") LEVERT_TIDLIGERE,
    /** Kun for «Annen dokumentasjon». */
    @SerialName("nei") NEI,
}

@Serializable
data class Vedleggsfil(
    val fileId: String,
    val attachmentId: String,
    val innsendingId: String,
    val fileName: String,
    val size: Double,
)
