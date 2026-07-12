package com.barprepng.app.ui.results

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barprepng.app.R

class ReviewAdapter(private val items: List<ReviewItem>) :
    RecyclerView.Adapter<ReviewAdapter.VH>() {

    private val letters = listOf("A", "B", "C", "D")

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val status: TextView = v.findViewById(R.id.tv_review_status)
        val qNum: TextView = v.findViewById(R.id.tv_review_q_num)
        val question: TextView = v.findViewById(R.id.tv_review_question)
        val yourAnswer: TextView = v.findViewById(R.id.tv_review_your_answer)
        val correctAnswer: TextView = v.findViewById(R.id.tv_review_correct_answer)
        val explanation: TextView = v.findViewById(R.id.tv_review_explanation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_review_answer, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val isCorrect = item.selectedIndex == item.correctIndex

        holder.status.text = if (isCorrect) "✓" else "✗"
        holder.status.setTextColor(Color.parseColor(if (isCorrect) "#00C48C" else "#FF4D6D"))
        holder.qNum.text = "Q${item.questionNum}"
        holder.question.text = item.questionText

        val selectedLetter = if (item.selectedIndex >= 0) letters.getOrElse(item.selectedIndex) { "?" } else "—"
        val selectedText = item.options.getOrElse(item.selectedIndex) { "No answer" }
        holder.yourAnswer.text = "Your answer: $selectedLetter. $selectedText"
        holder.yourAnswer.setTextColor(Color.parseColor(if (isCorrect) "#00C48C" else "#FF4D6D"))

        if (!isCorrect) {
            holder.correctAnswer.visibility = View.VISIBLE
            val correctLetter = letters.getOrElse(item.correctIndex) { "?" }
            val correctText = item.options.getOrElse(item.correctIndex) { "" }
            holder.correctAnswer.text = "Correct: $correctLetter. $correctText"
            holder.explanation.visibility = View.VISIBLE
            holder.explanation.text = item.explanation
        } else {
            holder.correctAnswer.visibility = View.GONE
            holder.explanation.visibility = View.GONE
        }

        // Background
        holder.itemView.setBackgroundResource(
            if (isCorrect) R.drawable.bg_option_correct else R.drawable.bg_option_wrong
        )
    }

    override fun getItemCount() = items.size
}
