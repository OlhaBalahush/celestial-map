//package com.example.sky_map
//
//import kotlinx.serialization.Serializable
//
//@Serializable
//data class CelestialResponse(
//    val data: Data
//)
//
//@Serializable
//data class Data(
//    val dates: List<Dates>
//)
//
//@Serializable
//data class Dates(
//    val entries: List<CelestialEntry>
//)
//
//@Serializable
//data class CelestialEntry(
//    val id: String,
//    val name: String,
//    val position: Position
//)
//
//@Serializable
//data class Position(
//    val horizontal: HorizontalPosition
//)
//
//@Serializable
//data class HorizontalPosition(
//    val altitude: DegreeString,
//    val azimuth: DegreeString
//)
//
//@Serializable
//data class DegreeString(
//    val degrees: Float,
//    val string: String
//)