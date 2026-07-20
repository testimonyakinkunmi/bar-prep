package com.barprepng.app.ui.results

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.barprepng.app.MainActivity
import com.barprepng.app.R
import com.barprepng.app.data.QuizSession
import com.barprepng.app.data.QuizRepository
import com.barprepng.app.databinding.FragmentResultsBinding
import com.barprepng.app.ui.home.HomeFragment

class ResultsFragment : Fragment() {

    companion object {
        private const val ARG_WEEK = "result_week"
        private const val ARG_SCORE = "result_score"
        private const val ARG_CORRECT = "result_correct"
        private const val ARG_TOTAL = "result_total"
        private const val ARG_TITLE = "result_title"

        fun newInstance(session: QuizSession): ResultsFragment {
            return ResultsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_WEEK, session.weekNumber)
                    putInt(ARG_SCORE, session.score)
                    putInt(ARG_CORRECT, session.correctCount)
                    putInt(ARG_TOTAL, session.totalQuestions)
                    putString(ARG_TITLE, session.weekTitle)
                    // Store per-question data for review
                    putStringArrayList("q_texts", ArrayList(session.questions.map { it.question }))
                    putStringArrayList("q_explanations", ArrayList(session.questions.map { it.explanation }))
                    putIntegerArrayList("q_correct", ArrayList(session.questions.map { it.correctIndex }))
                    putIntegerArrayList("q_selected", ArrayList(session.answers.map { it ?: -1 }))
                    putStringArrayList("q_options_a", ArrayList(session.questions.map { it.options.getOrElse(0){"" } }))
                    putStringArrayList("q_options_b", ArrayList(session.questions.map { it.options.getOrElse(1){"" } }))
                    putStringArrayList("q_options_c", ArrayList(session.questions.map { it.options.getOrElse(2){"" } }))
                    putStringArrayList("q_options_d", ArrayList(session.questions.map { it.options.getOrElse(3){"" } }))
                }
            }
        }
    }

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val score = arguments?.getInt(ARG_SCORE, 0) ?: 0
        val correct = arguments?.getInt(ARG_CORRECT, 0) ?: 0
        val total = arguments?.getInt(ARG_TOTAL, 0) ?: 0
        val title = arguments?.getString(ARG_TITLE, "") ?: ""
        val weekNum = arguments?.getInt(ARG_WEEK, 0) ?: 0
        val wrong = total - correct

        val qTexts = arguments?.getStringArrayList("q_texts") ?: arrayListOf()
        val qExplanations = arguments?.getStringArrayList("q_explanations") ?: arrayListOf()
        val qCorrect = arguments?.getIntegerArrayList("q_correct") ?: arrayListOf()
        val qSelected = arguments?.getIntegerArrayList("q_selected") ?: arrayListOf()
        val optionsA = arguments?.getStringArrayList("q_options_a") ?: arrayListOf()
        val optionsB = arguments?.getStringArrayList("q_options_b") ?: arrayListOf()
        val optionsC = arguments?.getStringArrayList("q_options_c") ?: arrayListOf()
        val optionsD = arguments?.getStringArrayList("q_options_d") ?: arrayListOf()

        // Header
        val emoji = when {
            score >= 90 -> "🏆"
            score >= 75 -> "🎯"
            score >= 60 -> "📚"
            else -> "⚠️"
        }
        binding.tvResultHeading.text = "$emoji ${when {
            score >= 90 -> "Outstanding!"
            score >= 75 -> "Well Done!"
            score >= 60 -> "Keep Practising"
            else -> "Review This Topic"
        }}"
        binding.tvResultWeek.text = if (weekNum == 0) "Random Mix · $total questions"
        else "Week $weekNum · $title"

        binding.tvScorePct.text = "$score%"
        binding.tvCorrectCount.text = correct.toString()
        binding.tvWrongCount.text = wrong.toString()

        binding.cardVerdict.setCardBackgroundColor(Color.parseColor(when {
            score >= 90 -> "#002010"
            score >= 75 -> "#001A10"
            score >= 60 -> "#1A1400"
            else -> "#200010"
        }))
        binding.tvVerdict.text = when {
            score >= 90 -> "Exceptional performance! You have a strong command of this topic."
            score >= 75 -> "Solid result. Review the questions you missed before moving on."
            score >= 60 -> "Decent attempt. Focus on the highlighted explanations below."
            else -> "This topic needs more attention. Study the explanations carefully."
        }
        binding.tvVerdict.setTextColor(Color.parseColor(when {
            score >= 90 -> "#00C48C"
            score >= 75 -> "#4FC3F7"
            score >= 60 -> "#F0A500"
            else -> "#FF4D6D"
        }))

        // Review list
        val reviewItems = qTexts.indices.map { i ->
            val opts = listOf(
                optionsA.getOrElse(i) { "" },
                optionsB.getOrElse(i) { "" },
                optionsC.getOrElse(i) { "" },
                optionsD.getOrElse(i) { "" }
            )
            ReviewItem(
                questionNum = i + 1,
                questionText = qTexts.getOrElse(i) { "" },
                options = opts,
                selectedIndex = qSelected.getOrElse(i) { -1 },
                correctIndex = qCorrect.getOrElse(i) { 0 },
                explanation = qExplanations.getOrElse(i) { "" }
            )
        }

        binding.rvReview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ReviewAdapter(reviewItems)
            isNestedScrollingEnabled = false
        }

        binding.btnRetry.setOnClickListener {
            (activity as? MainActivity)?.navigateToQuiz(weekNum)
        }
        binding.btnHome.setOnClickListener {
            (activity as? MainActivity)?.let {
                it.loadFragment(HomeFragment())
                it.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                    com.barprepng.app.R.id.bottomNav
                )?.selectedItemId = com.barprepng.app.R.id.nav_home
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class ReviewItem(
    val questionNum: Int,
    val questionText: String,
    val options: List<String>,
    val selectedIndex: Int,
    val correctIndex: Int,
    val explanation: String
)
