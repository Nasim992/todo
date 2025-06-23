package com.rnd.todo

import android.icu.util.Calendar
import androidx.compose.foundation.layout.add
import java.util.Date

object DateUtils {

    fun getStartOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    fun getEndOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }

    fun getTodayStartMillis(): Long = getStartOfDay(Date()).time // Use java.util.Date()
    fun getTodayEndMillis(): Long = getEndOfDay(Date()).time   // Use java.util.Date()


    fun getTomorrowStartMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.time = Date() // Use java.util.Date()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return getStartOfDay(calendar.time).time
    }


    fun getTomorrowEndMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.time = Date() // Use java.util.Date()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return getEndOfDay(calendar.time).time
    }

    fun getStartOfWeekMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.time = Date() // Use java.util.Date()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        return getStartOfDay(calendar.time).time
    }

    fun getEndOfWeekMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.time = Date() // Use java.util.Date()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        return calendar.timeInMillis
    }

    fun isOverdue(dueDateMillis: Long?, todayStartMillis: Long = getTodayStartMillis()): Boolean {
        return dueDateMillis != null && dueDateMillis < todayStartMillis
    }

    fun isToday(dueDateMillis: Long?, todayStartMillis: Long = getTodayStartMillis(), todayEndMillis: Long = getTodayEndMillis()): Boolean {
        return dueDateMillis != null && dueDateMillis in todayStartMillis..todayEndMillis
    }

    fun isTomorrow(dueDateMillis: Long?, tomorrowStartMillis: Long = getTomorrowStartMillis(), tomorrowEndMillis: Long = getTomorrowEndMillis()): Boolean {
        return dueDateMillis != null && dueDateMillis in tomorrowStartMillis..tomorrowEndMillis
    }

    fun isThisWeek(dueDateMillis: Long?, todayStartMillis: Long = getTodayStartMillis(), endOfWeekMillis: Long = getEndOfWeekMillis()): Boolean {
        return dueDateMillis != null && dueDateMillis >= todayStartMillis && dueDateMillis <= endOfWeekMillis
    }
}