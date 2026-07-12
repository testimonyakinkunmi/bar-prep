package com.barprepng.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.barprepng.app.MainActivity
import com.barprepng.app.R
import com.barprepng.app.data.MicroQuiz
import com.barprepng.app.data.Question
import com.barprepng.app.data.QuizRepository
import com.barprepng.app.data.WeekData
import com.barprepng.app.databinding.FragmentHomeBinding
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var repo: QuizRepository
    private lateinit var weekAdapter: WeekAdapter
    private var currentMicroQuiz: MicroQuiz? = null
    private var microIndex = 0
    private var microAnswered = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = QuizRepository(requireContext())

        setGreeting()
        loadStreak()
        setupWeekList()
        setupMicroQuiz()
        setupRandomCard()
    }

    private fun setGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when {
            hour < 12 -> "Good morning, Counsel ⚖️"
            hour < 17 -> "Good afternoon, Counsel ⚖️"
            else -> "Good evening, Counsel ⚖️"
        }
    }

    private fun loadStreak() {
        val streak = repo.getStreakData()
        binding.tvStreakCount.text = streak.currentStreak.toString()
    }

    private fun setupWeekList() {
        val weeks = repo.getAllWeeks()
        val weekStats = repo.getWeekStats().associateBy { it.weekNumber }

        weekAdapter = WeekAdapter(weeks, weekStats) { weekNumber ->
            (activity as? MainActivity)?.navigateToQuiz(weekNumber)
        }
        binding.rvWeeks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = weekAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupRandomCard() {
        binding.cardRandom.setOnClickListener {
            (activity as? MainActivity)?.navigateToQuiz(0)
        }
    }

    private fun setupMicroQuiz() {
        loadMicroQuestion()
        binding.btnMicroSkip.setOnClickListener {
            microIndex++
            val quiz = currentMicroQuiz ?: return@setOnClickListener
            if (microIndex >= quiz.questions.size) {
                currentMicroQuiz = repo.buildMicroQuiz(4)
                microIndex = 0
            }
            loadMicroQuestion()
        }
    }

    private fun loadMicroQuestion() {
        if (currentMicroQuiz == null) {
            currentMicroQuiz = repo.buildMicroQuiz(4)
            microIndex = 0
        }
        val quiz = currentMicroQuiz ?: return
        if (microIndex >= quiz.questions.size) {
            currentMicroQuiz = repo.buildMicroQuiz(4)
            microIndex = 0
        }
        val q = quiz.questions[microIndex]
        binding.tvMicroWeek.text = quiz.weekTitle.take(22)
        binding.tvMicroQuestion.text = q.question
        microAnswered = false
        binding.btnMicroSkip.text = "Skip → Next Question"

        binding.microOptionsContainer.removeAllViews()
        val letters = listOf("A", "B", "C", "D")
        q.options.forEachIndexed { i, opt ->
            val optView = layoutInflater.inflate(R.layout.item_micro_option, binding.microOptionsContainer, false)
            optView.findViewById<TextView>(R.id.tv_option_letter).text = letters[i]
            optView.findViewById<TextView>(R.id.tv_option_text).text = opt
            optView.setOnClickListener { handleMicroAnswer(optView, i, q) }
            binding.microOptionsContainer.addView(optView)
        }
    }

    private fun handleMicroAnswer(selectedView: View, selectedIndex: Int, q: Question) {
        if (microAnswered) return
        microAnswered = true

        val container = binding.microOptionsContainer
        for (j in 0 until container.childCount) {
            val child = container.getChildAt(j)
            val bg = if (j == q.correctIndex) R.drawable.bg_option_correct
            else if (j == selectedIndex) R.drawable.bg_option_wrong
            else R.drawable.bg_option_default
            child.setBackgroundResource(bg)
        }
        binding.btnMicroSkip.text = "Next Question →"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
