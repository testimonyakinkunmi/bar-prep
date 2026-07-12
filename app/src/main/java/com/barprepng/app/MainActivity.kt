package com.barprepng.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.barprepng.app.data.QuizRepository
import com.barprepng.app.databinding.ActivityMainBinding
import com.barprepng.app.ui.home.HomeFragment
import com.barprepng.app.ui.insights.InsightsFragment
import com.barprepng.app.ui.progress.ProgressFragment
import com.barprepng.app.ui.quiz.QuizFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomNav()
        if (savedInstanceState == null) loadFragment(HomeFragment())
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { loadFragment(HomeFragment()); true }
                R.id.nav_progress -> { loadFragment(ProgressFragment()); true }
                R.id.nav_insights -> { loadFragment(InsightsFragment()); true }
                else -> false
            }
        }
    }

    fun loadFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        val tx = supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
            .replace(R.id.fragment_container, fragment)
        if (addToBackStack) tx.addToBackStack(null)
        tx.commit()
    }

    fun navigateToQuiz(weekNumber: Int) {
        val fragment = QuizFragment.newInstance(weekNumber)
        loadFragment(fragment, addToBackStack = true)
    }
}
