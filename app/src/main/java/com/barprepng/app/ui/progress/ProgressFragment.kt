package com.barprepng.app.ui.progress

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.barprepng.app.R
import com.barprepng.app.data.QuizAttempt
import com.barprepng.app.data.QuizRepository
import com.barprepng.app.data.WeekStat
import com.barprepng.app.databinding.FragmentProgressBinding
import java.text.SimpleDateFormat
import java.util.*

class ProgressFragment : Fragment() {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!
    private lateinit var repo: QuizRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = QuizRepository(requireContext())
        loadData()
    }

    private fun loadData() {
        val weekStats = repo.getWeekStats()
        val recent = repo.getRecentAttempts(20)
        val streak = repo.getStreakData()

        val totalCorrect = recent.sumOf { it.correctCount }
        val totalQ = recent.sumOf { it.totalQuestions }
        val overallAcc = if (totalQ > 0) (totalCorrect * 100) / totalQ else 0

        binding.tvTotalAttempts.text = recent.size.toString()
        binding.tvOverallAccuracy.text = "$overallAcc%"
        binding.tvStreakProgress.text = "${streak.currentStreak}🔥"

        if (weekStats.isEmpty() && recent.isEmpty()) {
            binding.tvNoData.visibility = View.VISIBLE
            binding.rvWeekStats.visibility = View.GONE
            binding.rvRecent.visibility = View.GONE
        } else {
            binding.tvNoData.visibility = View.GONE
            binding.rvWeekStats.visibility = View.VISIBLE
            binding.rvRecent.visibility = View.VISIBLE

            binding.rvWeekStats.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = WeekStatAdapter(weekStats)
                isNestedScrollingEnabled = false
            }
            binding.rvRecent.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = RecentAttemptAdapter(recent)
                isNestedScrollingEnabled = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class WeekStatAdapter(private val items: List<WeekStat>) : RecyclerView.Adapter<WeekStatAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.tv_wstat_title)
        val best: TextView = v.findViewById(R.id.tv_wstat_best)
        val bar: View = v.findViewById(R.id.view_wstat_bar)
        val avg: TextView = v.findViewById(R.id.tv_wstat_avg)
        val attempts: TextView = v.findViewById(R.id.tv_wstat_attempts)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_week_stat, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.title.text = "Week ${item.weekNumber} · ${item.weekTitle}"
        holder.best.text = "Best: ${item.bestScore}%"
        holder.avg.text = "Avg ${item.avgScore.toInt()}%"
        holder.attempts.text = " · ${item.totalAttempts} attempt${if (item.totalAttempts == 1) "" else "s"}"
        holder.bar.post {
            val parent = holder.bar.parent as View
            val w = (parent.width * (item.bestScore / 100f)).toInt()
            holder.bar.layoutParams = holder.bar.layoutParams.apply { width = maxOf(w, 0) }
            holder.bar.requestLayout()
        }
    }

    override fun getItemCount() = items.size
}

class RecentAttemptAdapter(private val items: List<QuizAttempt>) : RecyclerView.Adapter<RecentAttemptAdapter.VH>() {

    private val sdf = SimpleDateFormat("MMM d 'at' h:mm a", Locale.getDefault())

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.tv_recent_title)
        val date: TextView = v.findViewById(R.id.tv_recent_date)
        val score: TextView = v.findViewById(R.id.tv_recent_score)
        val fraction: TextView = v.findViewById(R.id.tv_recent_fraction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_recent_attempt, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val label = if (item.weekNumber == 0) "Random Mix" else "Week ${item.weekNumber}"
        holder.title.text = "$label · ${item.weekTitle}"
        holder.date.text = sdf.format(Date(item.timestamp))
        holder.score.text = "${item.score}%"
        holder.fraction.text = "${item.correctCount}/${item.totalQuestions}"
        holder.score.setTextColor(Color.parseColor(when {
            item.score >= 75 -> "#00C48C"
            item.score >= 60 -> "#F0A500"
            else -> "#FF4D6D"
        }))
    }

    override fun getItemCount() = items.size
}
