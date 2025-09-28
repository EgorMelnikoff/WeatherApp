package com.egormelnikoff.myweather.data.datasource.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromListDouble(doubles: List<Double>?): String? {
        if (doubles == null) {
            return null
        }
        return doubles.joinToString { it.toString() }
    }

    @TypeConverter
    fun toListDouble(doublesString: String?): List<Double>? {
        if (doublesString == null) {
            return null
        }

        return doublesString.split(", ").map { it.toDouble() }
    }

    @TypeConverter
    fun fromListInt(integers: List<Int>?): String? {
        if (integers == null) {
            return null
        }
        return integers.joinToString { it.toString() }
    }

    @TypeConverter
    fun toListInt(integersString: String?): List<Int>? {
        if (integersString == null) {
            return null
        }

        return integersString.split(", ").map { it.toInt() }
    }

    @TypeConverter
    fun fromListString(strings: List<String>?): String? {
        if (strings == null) {
            return null
        }
        return strings.joinToString { it }
    }

    @TypeConverter
    fun toListString(stringsString: String?): List<String>? {
        if (stringsString == null) {
            return null
        }

        return stringsString.split(", ")
    }
}