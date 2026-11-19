package no.nav.uføre.søknad

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import java.time.LocalDate

typealias Dato = @Serializable(with = DatoSerializer::class) LocalDate

typealias VedleggFelt = @Serializable(with = VedleggSerializer::class) Vedlegg

object DatoSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("no.nav.uføre.søknad.Dato", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDate) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString())
}

/** Schemaet tillater `""` i stedet for dato; tolkes som `null`. */
object DatoEllerTomStrengSerializer : KSerializer<LocalDate?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("no.nav.uføre.søknad.DatoEllerTomStreng", PrimitiveKind.STRING).nullable

    override fun serialize(encoder: Encoder, value: LocalDate?) = encoder.encodeString(value?.toString() ?: "")

    override fun deserialize(decoder: Decoder): LocalDate? {
        val verdi = (decoder as JsonDecoder).decodeJsonElement()
        if (verdi !is JsonPrimitive || verdi is JsonNull || verdi.content.isBlank()) return null
        return LocalDate.parse(verdi.content)
    }
}

/** Normaliserer `"valg"` og `{ "key": "valg" }` til `{ "value": "valg" }`. */
object VedleggSerializer : JsonTransformingSerializer<Vedlegg>(Vedlegg.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement = when {
        element is JsonPrimitive -> JsonObject(mapOf("value" to element))
        element is JsonObject && "key" in element ->
            JsonObject(element - "key" + ("value" to element.getValue("key")))
        else -> element
    }
}

/** Normaliserer enkeltvedlegg til en liste. Serialisering bruker alltid listeformen. */
object VedleggslisteSerializer : JsonTransformingSerializer<List<Vedlegg>>(ListSerializer(VedleggSerializer)) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        if (element is JsonArray) element else JsonArray(listOf(element))
}
