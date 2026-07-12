package com.barprepng.app.ui.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class QuizFragment : Fragment() {
    companion object {
        fun newInstance(weekNumber: Int): QuizFragment {
            return QuizFragment().apply {
                arguments = Bundle().apply {
                    putInt("week_number", weekNumber)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
