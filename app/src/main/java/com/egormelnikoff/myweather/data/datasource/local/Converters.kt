package com.egormelnikoff.myweather.data.datasource.local

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    @TypeConverter
    fun fromListDouble(doubles: List<Double>?): String? {
        return doubles?.let {
            doubles.joinToString { it.toString() }
        }
    }

    @TypeConverter
    fun toListDouble(doublesString: String?): List<Double>? {
        return doublesString?.let {
            doublesString.split(", ").map { it.toDouble() }
        }
    }

    @TypeConverter
    fun fromListInt(integers: List<Int>?): String? {
        return integers?.let {
            integers.joinToString { it.toString() }
        }
    }

    @TypeConverter
    fun toListInt(integersString: String?): List<Int>? {
        return integersString?.let {
            integersString.split(", ").map { it.toInt() }
        }
    }

    @TypeConverter
    fun toLocalDateTimeString(localDateTime: LocalDateTime?): String? {
        return localDateTime?.let {
            localDateTime.format(DateTimeFormatter.ISO_DATE_TIME)
        }
    }

    @TypeConverter
    fun toLocalDateTime(localDateTimeString: String?): LocalDateTime? {
        return localDateTimeString?.let {
            LocalDateTime.parse(localDateTimeString, DateTimeFormatter.ISO_DATE_TIME)
        }
    }

    @TypeConverter
    fun fromListLocalDateTime(list: List<LocalDateTime>): String {
        return list.joinToString(",") { it.toString() }
    }

    @TypeConverter
    fun toListLocalDateTime(data: String): List<LocalDateTime> {
        return if (data.isBlank()) emptyList()
        else data.split(",").map { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun fromListLocalDate(list: List<LocalDate>): String {
        return list.joinToString(",") { it.toString() }
    }

    @TypeConverter
    fun toListLocalDate(data: String): List<LocalDate> {
        return if (data.isBlank()) emptyList()
        else data.split(",").map { LocalDate.parse(it) }
    }
}