package com.barprepng.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        const val DB_NAME = "barprep.db"
        const val DB_VERSION = 1

        // Tables
        const val TABLE_QUIZ_ATTEMPTS = "quiz_attempts"
        const val TABLE_QUESTION_ATTEMPTS = "question_attempts"
        const val TABLE_STREAK = "streak_data"

        // quiz_attempts columns
        const val COL_ID = "id"
        const val COL_WEEK_NUMBER = "week_number"
        const val COL_WEEK_TITLE = "week_title"
        const val COL_SCORE = "score"
        const val COL_TOTAL_QUESTIONS = "total_questions"
        const val COL_CORRECT_COUNT = "correct_count"
        const val COL_TIMESTAMP = "timestamp"
        const val COL_DURATION_MS = "duration_ms"

        // question_attempts columns
        const val COL_QUIZ_ATTEMPT_ID = "quiz_attempt_id"
        const val COL_QUESTION_ID = "question_id"
        const val COL_SELECTED_INDEX = "selected_index"
        const val COL_CORRECT_INDEX = "correct_index"
        const val COL_IS_CORRECT = "is_correct"

        // streak columns
        const val COL_CURRENT_STREAK = "current_streak"
        const val COL_LONGEST_STREAK = "longest_streak"
        const val COL_LAST_ACTIVITY_DATE = "last_activity_date"
        const val COL_TOTAL_DAYS = "total_days_studied"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_QUIZ_ATTEMPTS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_WEEK_NUMBER INTEGER NOT NULL,
                $COL_WEEK_TITLE TEXT NOT NULL,
                $COL_SCORE INTEGER NOT NULL,
                $COL_TOTAL_QUESTIONS INTEGER NOT NULL,
                $COL_CORRECT_COUNT INTEGER NOT NULL,
                $COL_TIMESTAMP INTEGER NOT NULL,
                $COL_DURATION_MS INTEGER DEFAULT 0
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_QUESTION_ATTEMPTS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_QUIZ_ATTEMPT_ID INTEGER NOT NULL,
                $COL_QUESTION_ID TEXT NOT NULL,
                $COL_WEEK_NUMBER INTEGER NOT NULL,
                $COL_SELECTED_INDEX INTEGER NOT NULL,
                $COL_CORRECT_INDEX INTEGER NOT NULL,
                $COL_IS_CORRECT INTEGER NOT NULL,
                $COL_TIMESTAMP INTEGER NOT NULL,
                FOREIGN KEY($COL_QUIZ_ATTEMPT_ID) REFERENCES $TABLE_QUIZ_ATTEMPTS($COL_ID)
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_STREAK (
                $COL_ID INTEGER PRIMARY KEY DEFAULT 1,
                $COL_CURRENT_STREAK INTEGER DEFAULT 0,
                $COL_LONGEST_STREAK INTEGER DEFAULT 0,
                $COL_LAST_ACTIVITY_DATE TEXT DEFAULT '',
                $COL_TOTAL_DAYS INTEGER DEFAULT 0
            )
        """)

        db.execSQL("INSERT INTO $TABLE_STREAK VALUES (1, 0, 0, '', 0)")

        // Indices
        db.execSQL("CREATE INDEX idx_qa_week ON $TABLE_QUIZ_ATTEMPTS($COL_WEEK_NUMBER)")
        db.execSQL("CREATE INDEX idx_qa_timestamp ON $TABLE_QUIZ_ATTEMPTS($COL_TIMESTAMP)")
        db.execSQL("CREATE INDEX idx_ques_qid ON $TABLE_QUESTION_ATTEMPTS($COL_QUESTION_ID)")
        db.execSQL("CREATE INDEX idx_ques_week ON $TABLE_QUESTION_ATTEMPTS($COL_WEEK_NUMBER)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_QUESTION_ATTEMPTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_QUIZ_ATTEMPTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STREAK")
        onCreate(db)
    }

    // ── Write operations ────────────────────────────────────────────────────
    fun saveQuizAttempt(session: QuizSession, durationMs: Long): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_WEEK_NUMBER, session.weekNumber)
            put(COL_WEEK_TITLE, session.weekTitle)
            put(COL_SCORE, session.score)
            put(COL_TOTAL_QUESTIONS, session.totalQuestions)
            put(COL_CORRECT_COUNT, session.correctCount)
            put(COL_TIMESTAMP, System.currentTimeMillis())
            put(COL_DURATION_MS, durationMs)
        }
        val quizId = db.insert(TABLE_QUIZ_ATTEMPTS, null, cv)

        // Save per-question attempts
        session.questions.forEachIndexed { i, q ->
            val ans = session.answers[i]
            if (ans != null) {
                val qcv = ContentValues().apply {
                    put(COL_QUIZ_ATTEMPT_ID, quizId)
                    put(COL_QUESTION_ID, q.id)
                    put(COL_WEEK_NUMBER, session.weekNumber)
                    put(COL_SELECTED_INDEX, ans)
                    put(COL_CORRECT_INDEX, q.correctIndex)
                    put(COL_IS_CORRECT, if (ans == q.correctIndex) 1 else 0)
                    put(COL_TIMESTAMP, System.currentTimeMillis())
                }
                db.insert(TABLE_QUESTION_ATTEMPTS, null, qcv)
            }
        }
        updateStreak()
        return quizId
    }

    private fun updateStreak() {
        val db = writableDatabase
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val c = db.rawQuery("SELECT * FROM $TABLE_STREAK WHERE $COL_ID = 1", null)
        if (c.moveToFirst()) {
            val lastDate = c.getString(c.getColumnIndexOrThrow(COL_LAST_ACTIVITY_DATE))
            val current = c.getInt(c.getColumnIndexOrThrow(COL_CURRENT_STREAK))
            val longest = c.getInt(c.getColumnIndexOrThrow(COL_LONGEST_STREAK))
            val totalDays = c.getInt(c.getColumnIndexOrThrow(COL_TOTAL_DAYS))
            c.close()

            val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                Date(System.currentTimeMillis() - 86400000L)
            )

            val newStreak = when (lastDate) {
                today -> current  // already counted today
                yesterday -> current + 1
                else -> 1
            }
            val newTotal = if (lastDate != today) totalDays + 1 else totalDays
            val newLongest = maxOf(longest, newStreak)

            val cv = ContentValues().apply {
                put(COL_CURRENT_STREAK, newStreak)
                put(COL_LONGEST_STREAK, newLongest)
                put(COL_LAST_ACTIVITY_DATE, today)
                put(COL_TOTAL_DAYS, newTotal)
            }
            db.update(TABLE_STREAK, cv, "$COL_ID = 1", null)
        } else {
            c.close()
        }
    }

    // ── Read operations ─────────────────────────────────────────────────────
    fun getStreakData(): StreakData {
        val db = readableDatabase
        val c = db.rawQuery("SELECT * FROM $TABLE_STREAK WHERE $COL_ID = 1", null)
        return if (c.moveToFirst()) {
            val sd = StreakData(
                currentStreak = c.getInt(c.getColumnIndexOrThrow(COL_CURRENT_STREAK)),
                longestStreak = c.getInt(c.getColumnIndexOrThrow(COL_LONGEST_STREAK)),
                lastActivityDate = c.getString(c.getColumnIndexOrThrow(COL_LAST_ACTIVITY_DATE)),
                totalDaysStudied = c.getInt(c.getColumnIndexOrThrow(COL_TOTAL_DAYS))
            )
            c.close()
            sd
        } else {
            c.close()
            StreakData(0, 0, "", 0)
        }
    }

    fun getRecentAttempts(limit: Int = 30): List<QuizAttempt> {
        val db = readableDatabase
        val list = mutableListOf<QuizAttempt>()
        val c = db.rawQuery(
            "SELECT * FROM $TABLE_QUIZ_ATTEMPTS ORDER BY $COL_TIMESTAMP DESC LIMIT ?",
            arrayOf(limit.toString())
        )
        while (c.moveToNext()) {
            list.add(QuizAttempt(
                id = c.getLong(c.getColumnIndexOrThrow(COL_ID)),
                weekNumber = c.getInt(c.getColumnIndexOrThrow(COL_WEEK_NUMBER)),
                weekTitle = c.getString(c.getColumnIndexOrThrow(COL_WEEK_TITLE)),
                score = c.getInt(c.getColumnIndexOrThrow(COL_SCORE)),
                totalQuestions = c.getInt(c.getColumnIndexOrThrow(COL_TOTAL_QUESTIONS)),
                correctCount = c.getInt(c.getColumnIndexOrThrow(COL_CORRECT_COUNT)),
                timestamp = c.getLong(c.getColumnIndexOrThrow(COL_TIMESTAMP)),
                durationMs = c.getLong(c.getColumnIndexOrThrow(COL_DURATION_MS))
            ))
        }
        c.close()
        return list
    }

    fun getWeekStats(): List<WeekStat> {
        val db = readableDatabase
        val list = mutableListOf<WeekStat>()
        val c = db.rawQuery("""
            SELECT $COL_WEEK_NUMBER, $COL_WEEK_TITLE,
                COUNT(*) as total_attempts,
                AVG($COL_SCORE) as avg_score,
                MAX($COL_SCORE) as best_score,
                SUM($COL_CORRECT_COUNT) as total_correct,
                SUM($COL_TOTAL_QUESTIONS) as total_answered
            FROM $TABLE_QUIZ_ATTEMPTS
            GROUP BY $COL_WEEK_NUMBER
            ORDER BY $COL_WEEK_NUMBER ASC
        """, null)
        while (c.moveToNext()) {
            val totalCorrect = c.getInt(c.getColumnIndexOrThrow("total_correct"))
            val totalAnswered = c.getInt(c.getColumnIndexOrThrow("total_answered"))
            list.add(WeekStat(
                weekNumber = c.getInt(c.getColumnIndexOrThrow(COL_WEEK_NUMBER)),
                weekTitle = c.getString(c.getColumnIndexOrThrow(COL_WEEK_TITLE)),
                totalAttempts = c.getInt(c.getColumnIndexOrThrow("total_attempts")),
                avgScore = c.getFloat(c.getColumnIndexOrThrow("avg_score")),
                bestScore = c.getInt(c.getColumnIndexOrThrow("best_score")),
                totalCorrect = totalCorrect,
                totalAnswered = totalAnswered,
                accuracyPercent = if (totalAnswered > 0) (totalCorrect * 100f) / totalAnswered else 0f
            ))
        }
        c.close()
        return list
    }

    fun getScoreHistory(weekNumber: Int = -1): List<ScorePoint> {
        val db = readableDatabase
        val list = mutableListOf<ScorePoint>()
        val query = if (weekNumber == -1) {
            "SELECT $COL_TIMESTAMP, $COL_SCORE, $COL_WEEK_NUMBER, $COL_WEEK_TITLE FROM $TABLE_QUIZ_ATTEMPTS ORDER BY $COL_TIMESTAMP ASC LIMIT 60"
        } else {
            "SELECT $COL_TIMESTAMP, $COL_SCORE, $COL_WEEK_NUMBER, $COL_WEEK_TITLE FROM $TABLE_QUIZ_ATTEMPTS WHERE $COL_WEEK_NUMBER = $weekNumber ORDER BY $COL_TIMESTAMP ASC LIMIT 60"
        }
        val c = db.rawQuery(query, null)
        while (c.moveToNext()) {
            list.add(ScorePoint(
                timestamp = c.getLong(c.getColumnIndexOrThrow(COL_TIMESTAMP)),
                score = c.getInt(c.getColumnIndexOrThrow(COL_SCORE)),
                weekNumber = c.getInt(c.getColumnIndexOrThrow(COL_WEEK_NUMBER)),
                weekTitle = c.getString(c.getColumnIndexOrThrow(COL_WEEK_TITLE))
            ))
        }
        c.close()
        return list
    }

    fun getHardestQuestions(limit: Int = 20): List<QuestionAccuracy> {
        val db = readableDatabase
        val list = mutableListOf<QuestionAccuracy>()
        val c = db.rawQuery("""
            SELECT $COL_QUESTION_ID, $COL_WEEK_NUMBER,
                COUNT(*) as times_attempted,
                SUM($COL_IS_CORRECT) as times_correct
            FROM $TABLE_QUESTION_ATTEMPTS
            GROUP BY $COL_QUESTION_ID
            HAVING times_attempted >= 2
            ORDER BY (CAST(times_correct AS REAL)/times_attempted) ASC
            LIMIT ?
        """, arrayOf(limit.toString()))
        while (c.moveToNext()) {
            val attempted = c.getInt(c.getColumnIndexOrThrow("times_attempted"))
            val correct = c.getInt(c.getColumnIndexOrThrow("times_correct"))
            list.add(QuestionAccuracy(
                questionId = c.getString(c.getColumnIndexOrThrow(COL_QUESTION_ID)),
                weekNumber = c.getInt(c.getColumnIndexOrThrow(COL_WEEK_NUMBER)),
                topic = "",
                timesAttempted = attempted,
                timesCorrect = correct,
                accuracyPercent = if (attempted > 0) (correct * 100f) / attempted else 0f,
                questionText = ""
            ))
        }
        c.close()
        return list
    }

    fun getTotalAttempts(): Int {
        val db = readableDatabase
        val c = db.rawQuery("SELECT COUNT(*) FROM $TABLE_QUIZ_ATTEMPTS", null)
        val count = if (c.moveToFirst()) c.getInt(0) else 0
        c.close()
        return count
    }

    fun getOverallAccuracy(): Float {
        val db = readableDatabase
        val c = db.rawQuery("""
            SELECT SUM($COL_CORRECT_COUNT) as tc, SUM($COL_TOTAL_QUESTIONS) as tq
            FROM $TABLE_QUIZ_ATTEMPTS
        """, null)
        val acc = if (c.moveToFirst()) {
            val tc = c.getInt(0)
            val tq = c.getInt(1)
            if (tq > 0) (tc * 100f) / tq else 0f
        } else 0f
        c.close()
        return acc
    }
}
