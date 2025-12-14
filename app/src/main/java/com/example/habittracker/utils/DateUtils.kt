package com.example.habittracker.utils

import java.time.LocalDate
import java.time.ZoneId

fun getDayRange(date: LocalDate = LocalDate.now()): Pair<Long, Long> {
    val zoneId = ZoneId.systemDefault()

    val startOfDay = date.atStartOfDay(zoneId).toInstant().toEpochMilli()

    val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

    return Pair(startOfDay,endOfDay)
}
fun LocalDate.toEpochMilli():Long {
    return this.atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}
fun getNow(): Long{
    return LocalDate.now().toEpochMilli()
}