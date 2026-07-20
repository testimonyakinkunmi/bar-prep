package com.barprepng.app.data

data class Question(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String = ""
)

data class QuizSession(
    val weekNumber: Int,
    val weekTitle: String,
    val questions: List<Question>,
    val answers: List<Int?>,
    val score: Int,
    val correctCount: Int,
    val totalQuestions: Int
)

data class WeekData(
    val weekNumber: Int,
    val title: String,
    val questions: List<Question>
)

data class QuizAttempt(
    val id: Long,
    val weekNumber: Int,
    val weekTitle: String,
    val score: Int,
    val totalQuestions: Int,
    val correctCount: Int,
    val timestamp: Long,
    val durationMs: Long
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

data class StreakData(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastActivityDate: String,
    val totalDaysStudied: Int
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
