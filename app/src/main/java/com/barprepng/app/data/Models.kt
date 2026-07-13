package com.barprepng.app.data

data class WeekData(
    val weekTitle: String,
    val weekStat: WeekStat
)

data class WeekStat(
    val weekTitle: String,
    val score: Int,
    val totalQuestions: Int,
    val correctCount: Int
)

data class ScorePoint(
    val weekTitle: String,
    val score: Float
)

data class StreakData(
    val currentStreak: Int,
    val lastActiveDate: Long
)

data class QuestionAccuracy(
    val questionId: Int,
    val isCorrect: Boolean
)
