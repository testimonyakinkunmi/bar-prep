package com.barprepng.app.data

import com.google.gson.annotations.SerializedName

data class SubjectData(
    val subject: String,
    @SerializedName("subject_code") val subjectCode: String,
    val weeks: List<WeekData>
)

data class WeekData(
    @SerializedName("week_number") val weekNumber: Int,
    val title: String,
    val description: String,
    val questions: List<Question>
)

data class Question(
    val id: String,
    val week: Int,
    val topic: String,
    val scenario: String?,
    val question: String,
    val options: List<String>,
    @SerializedName("correct_index") val correctIndex: Int,
    val explanation: String,
    @SerializedName("law_reference") val lawReference: String
)

data class QuizSession(
    val sessionId: String,
    val weekNumber: Int,
    val weekTitle: String,
    val questions: List<Question>,
    val answers: MutableList<Int?> = MutableList(questions.size) { null },
    val startTime: Long = System.currentTimeMillis()
) {
    val totalQuestions get() = questions.size
    val answeredCount get() = answers.count { it != null }
    val correctCount get() = answers.indices.count { i ->
        answers[i] != null && answers[i] == questions[i].correctIndex
    }
    val score get() = if (totalQuestions > 0) (correctCount * 100) / totalQuestions else 0
    val isComplete get() = answeredCount == totalQuestions
}

data class QuizAttempt(
    val id: Long = 0,
    val weekNumber: Int,
    val weekTitle: String,
    val score: Int,
    val totalQuestions: Int,
    val correctCount: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMs: Long = 0L
)

data class QuestionAttempt(
    val id: Long = 0,
    val quizAttemptId: Long,
    val questionId: String,
    val weekNumber: Int,
    val selectedIndex: Int,
    val correctIndex: Int,
    val isCorrect: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class StreakData(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastActivityDate: String,
    val totalDaysStudied: Int
)

data class WeekStat(
    val weekNumber: Int,
    val weekTitle: String,
    val totalAttempts: Int,
    val avgScore: Float,
    val bestScore: Int,
    val totalCorrect: Int,
    val totalAnswered: Int,
    val accuracyPercent: Float
)

data class ScorePoint(
    val timestamp: Long,
    val score: Int,
    val weekNumber: Int,
    val weekTitle: String
)

data class QuestionAccuracy(
    val questionId: String,
    val weekNumber: Int,
    val topic: String,
    val timesAttempted: Int,
    val timesCorrect: Int,
    val accuracyPercent: Float,
    val questionText: String
)

data class InsightSummary(
    val totalAttempts: Int,
    val overallAccuracy: Float,
    val bestWeek: WeekStat?,
    val weakestWeek: WeekStat?,
    val weekStats: List<WeekStat>,
    val recentScores: List<ScorePoint>,
    val hardestQuestions: List<QuestionAccuracy>,
    val streakData: StreakData
)

data class MicroQuiz(
    val questions: List<Question>,
    val weekTitle: String
)
