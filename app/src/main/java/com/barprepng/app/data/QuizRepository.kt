package com.barprepng.app.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class QuizRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("barprep_prefs", Context.MODE_PRIVATE)

    private val gson = Gson()
    private var cachedData: QuizData? = null

    // ── Load JSON ──────────────────────────────────────────────────────────
    fun loadQuizData(): QuizData {
        cachedData?.let { return it }
        val raw = context.resources.openRawResource(
            context.resources.getIdentifier("quiz_data", "raw", context.packageName)
        ).bufferedReader().use { it.readText() }
        val type = object : TypeToken<QuizData>() {}.type
        val data: QuizData = gson.fromJson(raw, type)
        cachedData = data
        return data
    }

    fun getWeeks(): List<Week> = loadQuizData().weeks

    fun getWeek(number: Int): Week? = getWeeks().find { it.week_number == number }

    fun getRandomQuestions(count: Int): List<Question> =
        getWeeks().flatMap { it.questions }.shuffled().take(count)

    fun getMicroQuizQuestions(): List<Question> =
        getWeeks().flatMap { it.questions }.shuffled().take(5)

    // ── Attempt persistence ────────────────────────────────────────────────
    private fun attemptKey(weekNumber: Int) = "attempts_week_$weekNumber"

    fun saveAttempt(attempt: QuizAttempt) {
        val key = attemptKey(attempt.weekNumber)
        val existing = getAttempts(attempt.weekNumber).toMutableList()
        existing.removeAll { it.questionId == attempt.questionId }
        existing.add(attempt)
        prefs.edit().putString(key, gson.toJson(existing)).apply()
        recordStudyDay()
    }

    fun saveAttempts(attempts: List<QuizAttempt>) {
        attempts.forEach { saveAttempt(it) }
    }

    fun getAttempts(weekNumber: Int): List<QuizAttempt> {
        val json = prefs.getString(attemptKey(weekNumber), null) ?: return emptyList()
        val type = object : TypeToken<List<QuizAttempt>>() {}.type
        return try { gson.fromJson(json, type) } catch (e: Exception) { emptyList() }
    }

    fun getAllAttempts(): List<QuizAttempt> =
        getWeeks().flatMap { getAttempts(it.week_number) }

    fun getWeekStats(weekNumber: Int): WeekStats {
        val week = getWeek(weekNumber) ?: return WeekStats(weekNumber, "", 0, 0, 0)
        val attempts = getAttempts(weekNumber)
        val correct = attempts.count { it.isCorrect }
        return WeekStats(weekNumber, week.title, week.questions.size, attempts.size, correct)
    }

    fun getAllWeekStats(): List<WeekStats> = getWeeks().map { getWeekStats(it.week_number) }

    // ── Streak tracking ────────────────────────────────────────────────────
    private fun recordStudyDay() {
        val today = todayString()
        val studiedDays = getStudiedDays().toMutableSet()
        studiedDays.add(today)
        prefs.edit().putStringSet("studied_days", studiedDays).apply()
    }

    fun getStudiedDays(): Set<String> = prefs.getStringSet("studied_days", emptySet()) ?: emptySet()

    fun getCurrentStreak(): Int {
        val days = getStudiedDays().sorted().reversed()
        if (days.isEmpty()) return 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var streak = 0
        var check = Calendar.getInstance()
        for (day in days) {
            val dayDate = sdf.parse(day) ?: break
            val checkStr = sdf.format(check.time)
            if (day == checkStr) {
                streak++
                check.add(Calendar.DAY_OF_YEAR, -1)
            } else break
        }
        return streak
    }

    fun getLongestStreak(): Int {
        val days = getStudiedDays().sorted()
        if (days.isEmpty()) return 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var longest = 1
        var current = 1
        for (i in 1 until days.size) {
            val prev = sdf.parse(days[i - 1]) ?: continue
            val curr = sdf.parse(days[i]) ?: continue
            val diff = (curr.time - prev.time) / (1000 * 60 * 60 * 24)
            if (diff == 1L) {
                current++
                if (current > longest) longest = current
            } else current = 1
        }
        return longest
    }

    // ── Score history for line chart ───────────────────────────────────────
    fun getScoreHistory(): List<Pair<String, Float>> {
        val all = getAllAttempts().sortedBy { it.timestamp }
        if (all.isEmpty()) return emptyList()
        val sdf = SimpleDateFormat("MM/dd", Locale.getDefault())
        val byDay = all.groupBy { sdf.format(Date(it.timestamp)) }
        return byDay.map { (day, attempts) ->
            val acc = attempts.count { it.isCorrect }.toFloat() / attempts.size.toFloat() * 100f
            day to acc
        }.takeLast(14)
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private fun todayString(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    fun resetAllData() {
        prefs.edit().clear().apply()
    }
}
