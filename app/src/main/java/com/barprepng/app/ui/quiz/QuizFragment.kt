package com.barprepng.app.ui.quiz

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.barprepng.app.R
import com.barprepng.app.data.Question
import com.barprepng.app.data.QuizRepository
import com.barprepng.app.data.QuizSession
import com.barprepng.app.databinding.FragmentQuizBinding
import com.barprepng.app.ui.results.ResultsFragment

class QuizFragment : Fragment() {

    companion object {
        private const val ARG_WEEK = "week_number"
        fun newInstance(weekNumber: Int): QuizFragment {
            return QuizFragment().apply {
                arguments = Bundle().apply { putInt(ARG_WEEK, weekNumber) }
            }
        }
    }

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    private lateinit var repo: QuizRepository
    private lateinit var session: QuizSession
    private var currentIndex = 0
    private var selectedOption = -1
    private var answered = false
    private val startTime = System.currentTimeMillis()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = QuizRepository(requireContext())

        val weekNumber = arguments?.getInt(ARG_WEEK, 0) ?: 0
        session = if (weekNumber == 0) repo.buildRandomSession(20)
        else repo.buildWeekSession(weekNumber) ?: run {
            parentFragmentManager.popBackStack(); return
        }

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.tvQuizTitle.text = if (weekNumber == 0) "Random Mix"
        else "Week $weekNumber · ${session.weekTitle.take(30)}"

        binding.btnSubmit.setOnClickListener { handleSubmit() }
        showQuestion(0)
    }

    private fun showQuestion(index: Int) {
        if (index >= session.totalQuestions) { finishQuiz(); return }
        currentIndex = index
        selectedOption = -1
        answered = false

        val q = session.questions[index]
        val pct = (index + 1).toFloat() / session.totalQuestions
        binding.tvQuestionCounter.text = "${index + 1} / ${session.totalQuestions}"
        binding.viewQuizProgress.post {
            val parent = binding.viewQuizProgress.parent as View
            val w = (parent.width * pct).toInt()
            binding.viewQuizProgress.layoutParams = binding.viewQuizProgress.layoutParams.apply { width = w }
            binding.viewQuizProgress.requestLayout()
        }

        // Scenario
        if (!q.scenario.isNullOrBlank()) {
            binding.cardScenario.visibility = View.VISIBLE
            binding.tvScenario.text = q.scenario
        } else {
            binding.cardScenario.visibility = View.GONE
        }

        binding.tvQuestion.text = q.question
        binding.cardExplanation.visibility = View.GONE
        binding.btnSubmit.text = getString(R.string.btn_submit)
        binding.btnSubmit.isEnabled = false

        buildOptions(q)
    }

    private fun buildOptions(q: Question) {
        binding.optionsContainer.removeAllViews()
        val letters = listOf("A", "B", "C", "D")
        q.options.forEachIndexed { i, opt ->
            val v = layoutInflater.inflate(R.layout.item_option, binding.optionsContainer, false)
            v.findViewById<TextView>(R.id.tv_option_letter).text = letters[i]
            v.findViewById<TextView>(R.id.tv_option_text).text = opt
            v.setOnClickListener { selectOption(i, q) }
            binding.optionsContainer.addView(v)
        }
    }

    private fun selectOption(index: Int, q: Question) {
        if (answered) return
        selectedOption = index
        session.answers[currentIndex] = index

        val letters = listOf("A", "B", "C", "D")
        for (j in 0 until binding.optionsContainer.childCount) {
            val child = binding.optionsContainer.getChildAt(j)
            val bg = if (j == index) R.drawable.bg_option_selected else R.drawable.bg_option_default
            child.setBackgroundResource(bg)
            val letter = child.findViewById<TextView>(R.id.tv_option_letter)
            letter.setTextColor(Color.parseColor(if (j == index) "#F0A500" else "#4A6380"))
        }
        binding.btnSubmit.isEnabled = true
    }

    private fun handleSubmit() {
        if (!answered && selectedOption >= 0) {
            revealAnswer()
        } else if (answered) {
            val next = currentIndex + 1
            if (next >= session.totalQuestions) finishQuiz()
            else showQuestion(next)
        }
    }

    private fun revealAnswer() {
        answered = true
        val q = session.questions[currentIndex]
        val correct = q.correctIndex
        val selected = selectedOption

        for (j in 0 until binding.optionsContainer.childCount) {
            val child = binding.optionsContainer.getChildAt(j)
            val bg = when (j) {
                correct -> R.drawable.bg_option_correct
                selected -> if (selected != correct) R.drawable.bg_option_wrong else R.drawable.bg_option_correct
                else -> R.drawable.bg_option_default
            }
            child.setBackgroundResource(bg)
            child.isClickable = false
        }

        val isCorrect = selected == correct
        binding.tvResultLabel.text = if (isCorrect) "✓ CORRECT" else "✗ INCORRECT"
        binding.tvResultLabel.setTextColor(
            Color.parseColor(if (isCorrect) "#00C48C" else "#FF4D6D")
        )
        binding.tvExplanation.text = q.explanation
        binding.tvLawRef.text = q.lawReference
        binding.cardExplanation.visibility = View.VISIBLE

        val isLast = currentIndex == session.totalQuestions - 1
        binding.btnSubmit.text = if (isLast) getString(R.string.btn_finish) else getString(R.string.btn_next)
        binding.btnSubmit.isEnabled = true
    }

    private fun finishQuiz() {
        val duration = System.currentTimeMillis() - startTime
        repo.saveSession(session, duration)

        val resultsFragment = ResultsFragment.newInstance(session)
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .replace(R.id.fragment_container, resultsFragment)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
