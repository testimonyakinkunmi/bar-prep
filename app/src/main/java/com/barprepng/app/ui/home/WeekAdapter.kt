package com.barprepng.app.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barprepng.app.R
import com.barprepng.app.data.WeekData
import com.barprepng.app.data.WeekStat

class WeekAdapter(
    private val weeks: List<WeekData>,
    private val stats: Map<Int, WeekStat>,
    private val onWeekClick: (Int) -> Unit
) : RecyclerView.Adapter<WeekAdapter.VH>() {

    private val weekColors = listOf(
        "#7C4DFF", "#00BCD4", "#FF6B6B", "#4CAF50", "#FF9800",
        "#E91E63", "#2196F3", "#009688", "#FF5722", "#607D8B",
        "#9C27B0", "#3F51B5", "#00ACC1", "#43A047", "#FB8C00",
        "#D81B60", "#1E88E5", "#00897B", "#E53935"
    )

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val badge: TextView = view.findViewById(R.id.tv_week_badge)
        val title: TextView = view.findViewById(R.id.tv_week_title)
        val qCount: TextView = view.findViewById(R.id.tv_question_count)
        val progressBar: View = view.findViewById(R.id.view_progress)
        val bestScore: TextView = view.findViewById(R.id.tv_best_score)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_week_card, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val week = weeks[position]
        val stat = stats[week.weekNumber]
        val colorHex = weekColors[position % weekColors.size]

        holder.badge.text = week.weekNumber.toString()
        holder.badge.setBackgroundColor(android.graphics.Color.parseColor(colorHex))
        holder.title.text = week.title
        holder.qCount.text = "${week.questions.size} questions"

        if (stat != null) {
            val pct = stat.bestScore / 100f
            holder.progressBar.post {
                val parent = holder.progressBar.parent as View
                val w = (parent.width * pct).toInt()
                holder.progressBar.layoutParams = holder.progressBar.layoutParams.apply {
                    width = w
                }
                holder.progressBar.requestLayout()
            }
            holder.bestScore.text = "Best ${stat.bestScore}%"
        } else {
            holder.progressBar.layoutParams = holder.progressBar.layoutParams.apply { width = 0 }
            holder.bestScore.text = ""
        }

        holder.itemView.setOnClickListener { onWeekClick(week.weekNumber) }
    }

    override fun getItemCount() = weeks.size
}
