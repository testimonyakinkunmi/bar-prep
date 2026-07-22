package com.barprepng.app.data

import android.content.Context
import com.google.gson.Gson
import java.io.InputStreamReader

class QuizRepository(private val context: Context) {

    private val db = DatabaseHelper(context)
    private val subjectData: SubjectData by lazy { loadData() }

    private fun loadData(): SubjectData {
        val inputStream = context.resources.openRawResource(
            context.resources.getIdentifier("quiz_data", "raw", context.packageName)
        )
        val reader = InputStreamReader(inputStream)
        return Gson().fromJson(reader, SubjectData::class.java)
    }

    fun getAllWeeks(): List<WeekData> = subjectData.weeks
    fun getWeek(weekNumber: Int): WeekData? = subjectData.weeks.find { it.weekNumber == weekNumber }
    fun getSubjectName(): String = subjectData.subject

    fun buildWeekSession(weekNumber: Int, questionCount: Int = -1): QuizSession? {
        val week = getWeek(weekNumber) ?: return null
        val questions = if (questionCount > 0 && questionCount < week.questions.size) {
            week.questions.shuffled().take(questionCount)
        } else {
            week.questions.shuffled()
        }
        return QuizSession(
            sessionId = "W${weekNumber}_${System.currentTimeMillis()}",
            weekNumber = weekNumber,
            weekTitle = week.title,
            questions = questions
        )
    }

    fun buildRandomSession(questionCount: Int = 20): QuizSession {
        val allQuestions = subjectData.weeks.flatMap { it.questions }
        val selected = allQuestions.shuffled().take(questionCount)
        return QuizSession(
            sessionId = "RAND_${System.currentTimeMillis()}",
            weekNumber = 0,
            weekTitle = "Random Mix",
            questions = selected
        )
    }

    fun buildMicroQuiz(count: Int = 4): MicroQuiz {
        val randomWeek = subjectData.weeks.random()
        val questions = randomWeek.questions.shuffled().take(count)
        return MicroQuiz(questions = questions, weekTitle = randomWeek.title)
    }

    fun buildDifficultSession(hardQuestions: List<QuestionAccuracy>): QuizSession {
        val allQuestions = subjectData.weeks.flatMap { it.questions }
        val hardIds = hardQuestions.map { it.questionId }.toSet()
        val difficult = allQuestions.filter { it.id in hardIds }
        val selected = if (difficult.size >= 5) difficult.shuffled().take(20)
        else (difficult + allQuestions.shuffled()).take(20)
        return QuizSession(
            sessionId = "HARD_${System.currentTimeMillis()}",
            weekNumber = -1,
            weekTitle = "Weak Areas Focus",
            questions = selected
        )
    }

    fun saveSession(session: QuizSession, durationMs: Long): Long =
        db.saveQuizAttempt(session, durationMs)

    fun getStreakData(): StreakData = db.getStreakData()

    fun getInsightSummary(): InsightSummary {
        val weekStats = db.getWeekStats()
        val sorted = weekStats.sortedBy { it.accuracyPercent }
        val hardest = db.getHardestQuestions(20)
        val allQuestionsMap = subjectData.weeks.flatMap { it.questions }.associateBy { it.id }
        val enrichedHardest = hardest.map { qa ->
            val q = allQuestionsMap[qa.questionId]
            qa.copy(topic = q?.topic ?: "", questionText = q?.question?.take(80) ?: qa.questionId)
        }
        return InsightSummary(
            totalAttempts = db.getTotalAttempts(),
            overallAccuracy = db.getOverallAccuracy(),
            bestWeek = sorted.lastOrNull(),
            weakestWeek = sorted.firstOrNull(),
            weekStats = weekStats,
            recentScores = db.getScoreHistory(),
            hardestQuestions = enrichedHardest,
            streakData = db.getStreakData()
        )
    }

    fun getRecentAttempts(limit: Int = 20) = db.getRecentAttempts(limit)
    fun getScoreHistory(weekNumber: Int = -1) = db.getScoreHistory(weekNumber)
    fun getWeekStats() = db.getWeekStats()
}
