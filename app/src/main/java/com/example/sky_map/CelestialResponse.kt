package  com.example.sky_map

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class CelestialResponse(
    val data: Data
)

@Serializable
data class Data(
    val dates: Dates,
    val observer: Observer,
    val table: Table
)

@Serializable
data class Dates(
    val from: String,
    val to: String
)

@Serializable
data class Observer(
    val location: CLocation
)

@Serializable
data class CLocation(
    val longitude: Double,
    val latitude: Double,
    val elevation: Int
)

@Serializable
data class Table(
    val header: List<String>,
    val rows: List<Row>
)

@Serializable
data class Row(
    val entry: Entry
)

@Serializable
data class Entry(
    val id: String,
    val name: String,
    val cells: List<Cell> = emptyList()
)

@Serializable
data class Cell(
    val date: String,
    val id: String,
    val distance: Distance,
    val position: Position
)

@Serializable
data class Distance(
    val fromEarth: DistanceData
)

@Serializable
data class DistanceData(
    val au: String,
    val km: String
)

@Serializable
data class Position(
    val horizontal: Horizontal,
    val equatorial: Equatorial,
    val constellation: Constellation,
    val extralnfo: ExtraInfo
)

@Serializable
data class Horizontal(
    val altitude: Angle,
    val azimuth: Angle
)

@Serializable
data class Equatorial(
    val rightAscension: HourAngle,
    val declination: Angle
)

@Serializable
data class Constellation(
    val id: String,
    val short: String,
    val name: String
)

@Serializable
data class ExtraInfo(
    val elongation: Double,
    val magnitude: Double,
    val phase: Phase
)

@Serializable
data class Angle(
    @Serializable(with = AngleSerializer::class)
    val degrees: Double,
    val string: String
)

@Serializable
data class HourAngle(
    @Serializable(with = HourAngleSerializer::class)
    val hours: Double,
    val string: String
)

@Serializable
data class Phase(
    val angle: Double,
    val fraction: String,
    val string: String
)

object AngleSerializer : KSerializer<Double> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Angle", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Double) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Double {
        val value = decoder.decodeString()
        return value.dropLast(1).toDouble() // Removing the last character (') and converting to Double
    }
}

object HourAngleSerializer : KSerializer<Double> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("HourAngle", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Double) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Double {
        val value = decoder.decodeString()
        return value.dropLast(1).toDouble() // Removing the last character (') and converting to Double
    }
}