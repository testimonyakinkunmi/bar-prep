package com.barprepng.app.ui.insights

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.barprepng.app.MainActivity
import com.barprepng.app.R
import com.barprepng.app.data.InsightSummary
import com.barprepng.app.data.QuestionAccuracy
import com.barprepng.app.data.QuizRepository
import com.barprepng.app.databinding.FragmentInsightsBinding

class InsightsFragment : Fragment() {

    private var _binding: FragmentInsightsBinding? = null
    private val binding get() = _binding!!
    private lateinit var repo: QuizRepository
    private lateinit var summary: InsightSummary
    private var currentTab = 0  // 0=score,1=heatmap,2=accuracy,3=weak

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInsightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = QuizRepository(requireContext())
        summary = repo.getInsightSummary()

        if (summary.totalAttempts == 0) {
            binding.tvInsightsEmpty.visibility = View.VISIBLE
            binding.cardChart.visibility = View.GONE
            return
        }

        setupChips()
        showTab(0)
    }

    private fun setupChips() {
        binding.chipScore.setOnClickListener { showTab(0) }
        binding.chipHeatmap.setOnClickListener { showTab(1) }
        binding.chipAccuracy.setOnClickListener { showTab(2) }
        binding.chipWeak.setOnClickListener { showTab(3) }
    }

    private fun showTab(tab: Int) {
        currentTab = tab
        val chips = listOf(binding.chipScore, binding.chipHeatmap, binding.chipAccuracy, binding.chipWeak)
        chips.forEachIndexed { i, chip ->
            if (i == tab) {
                chip.setTextColor(Color.parseColor("#0A0F1E"))
                chip.setBackgroundResource(R.drawable.bg_streak_badge)
            } else {
                chip.setTextColor(Color.parseColor("#8FA8C8"))
                chip.setBackgroundResource(R.drawable.bg_week_chip)
            }
        }

        binding.cardChart.visibility = if (tab == 0) View.VISIBLE else View.GONE
        binding.cardHeatmap.visibility = if (tab == 1) View.VISIBLE else View.GONE
        binding.containerAccuracy.visibility = if (tab == 2) View.VISIBLE else View.GONE
        binding.containerWeak.visibility = if (tab == 3) View.VISIBLE else View.GONE

        when (tab) {
            0 -> renderScoreTrend()
            1 -> renderHeatmap()
            2 -> renderAccuracy()
            3 -> renderWeakAreas()
        }
    }

    private fun renderScoreTrend() {
        binding.tvChartTitle.text = "Score Trend · All Quizzes"
        val scores = summary.recentScores
        if (scores.isEmpty()) {
            binding.chartView.visibility = View.GONE
            binding.tvChartEmpty.visibility = View.VISIBLE
        } else {
            binding.chartView.visibility = View.VISIBLE
            binding.tvChartEmpty.visibility = View.GONE
            binding.chartView.setData(scores)
        }
    }

    private fun renderHeatmap() {
        val stats = summary.weekStats
        if (stats.isEmpty()) {
            binding.cardHeatmap.visibility = View.GONE
            return
        }
        binding.heatmapView.setData(stats)
    }

    private fun renderAccuracy() {
        val hard = summary.hardestQuestions
        if (hard.isEmpty()) {
            binding.containerAccuracy.visibility = View.GONE
            binding.tvInsightsEmpty.visibility = View.VISIBLE
            binding.tvInsightsEmpty.text = "Answer at least 2 attempts per question to see accuracy data."
            return
        }
        binding.rvAccuracy.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = AccuracyAdapter(hard)
            isNestedScrollingEnabled = false
        }
    }

    private fun renderWeakAreas() {
        val weakWeeks = summary.weekStats.filter { it.accuracyPercent < 70f }.sortedBy { it.accuracyPercent }
        if (weakWeeks.isEmpty()) {
            binding.containerWeak.visibility = View.GONE
            val noWeakView = binding.tvInsightsEmpty
            noWeakView.visibility = View.VISIBLE
            noWeakView.text = "No weak areas detected — all topics above 70%! 🎉"
            return
        }

        binding.rvWeak.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = WeakAreaAdapter(weakWeeks)
            isNestedScrollingEnabled = false
        }

        binding.btnDrillWeak.setOnClickListener {
            val hardQ = summary.hardestQuestions
            val session = repo.buildDifficultSession(hardQ)
            // Build a session from weak weeks and navigate
            (activity as? MainActivity)?.navigateToQuiz(0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class AccuracyAdapter(private val items: List<QuestionAccuracy>) :
    RecyclerView.Adapter<AccuracyAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val pct: TextView = v.findViewById(R.id.tv_acc_pct)
        val topic: TextView = v.findViewById(R.id.tv_acc_topic)
        val attempts: TextView = v.findViewById(R.id.tv_acc_attempts)
        val question: TextView = v.findViewById(R.id.tv_acc_question)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_accuracy, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.pct.text = "${item.accuracyPercent.toInt()}%"
        holder.pct.setTextColor(Color.parseColor(when {
            item.accuracyPercent >= 70 -> "#00C48C"
            item.accuracyPercent >= 50 -> "#F0A500"
            else -> "#FF4D6D"
        }))
        holder.topic.text = "Week ${item.weekNumber} · ${item.topic.take(30)}"
        holder.attempts.text = "${item.timesAttempted} tries"
        holder.question.text = item.questionText.ifBlank { item.questionId }
    }

    override fun getItemCount() = items.size
}

class WeakAreaAdapter(private val items: List<com.barprepng.app.data.WeekStat>) :
    RecyclerView.Adapter<WeakAreaAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val pct: TextView = v.findViewById(R.id.tv_acc_pct)
        val topic: TextView = v.findViewById(R.id.tv_acc_topic)
        val attempts: TextView = v.findViewById(R.id.tv_acc_attempts)
        val question: TextView = v.findViewById(R.id.tv_acc_question)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_accuracy, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.pct.text = "${item.accuracyPercent.toInt()}%"
        holder.pct.setTextColor(Color.parseColor("#FF4D6D"))
        holder.topic.text = "Week ${item.weekNumber}"
        holder.attempts.text = "${item.totalAttempts} attempts"
        holder.question.text = item.weekTitle
    }

    override fun getItemCount() = items.size
}
