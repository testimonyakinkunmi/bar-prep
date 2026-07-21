package com.barprepng.app.data

data class Question(
    val id: String,
    val week: Int,
    val topic: String,
    val scenario: String?,
    val question: String,
    val options: List<String>,
    val correct_index: Int,
    val explanation: String,
    val law_reference: String
)

data class Week(
    val week_number: Int,
    val title: String,
    val description: String,
    val questions: List<Question>
)

data class QuizData(
    val subject: String,
    val subject_code: String,
    val weeks: List<Week>
)

data class QuizAttempt(
    val questionId: String,
    val weekNumber: Int,
    val selectedIndex: Int,
    val isCorrect: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class WeekStats(
    val weekNumber: Int,
    val title: String,
    val totalQuestions: Int,
    val attempted: Int,
    val correct: Int
) {
    val accuracy: Float get() = if (attempted == 0) 0f else correct.toFloat() / attempted.toFloat()
    val accuracyPercent: Int get() = (accuracy * 100).toInt()
}

data class QuizSession(
    val weekNumber: Int,
    val isMicro: Boolean = false,
    val isRandom: Boolean = false,
    val questions: List<Question> = emptyList()
)

data class CorrectionItem(
    val questionNumber: Int,
    val question: Question,
    val selectedIndex: Int,
    val isCorrect: Boolean
)
