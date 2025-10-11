package me.samud.minimumlauncher

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class MainActivity : AppCompatActivity() {

    private var pausedTimestamp: Long = 0
    private val resetDelay: Long = 10000 // 10 seconds in milliseconds
    private lateinit var fragmentContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fragmentContainer = findViewById(R.id.fragment_container)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AppListFragment())
                .commitNow()
        }
    }

    override fun onResume() {
        super.onResume()
        val currentTime = System.currentTimeMillis()
        // Reset UI if the app was paused for more than a minute
        if (pausedTimestamp > 0 && (currentTime - pausedTimestamp) > resetDelay) {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (fragment is AppListFragment) {
                fragment.resetUI()
            }
        }

        val navigationMode = GestureNavigationHelper.getNavigationMode(this)
        if (navigationMode != 0) { // Apply transition for gesture nav (1) and older OS (-1)
            fragmentContainer.alpha = 0f
            val fadeIn = ObjectAnimator.ofFloat(fragmentContainer, "alpha", 0f, 1f)
            fadeIn.duration = 650
            fadeIn.start()
        }
    }

    override fun onPause() {
        super.onPause()
        pausedTimestamp = System.currentTimeMillis()
        if (GestureNavigationHelper.getNavigationMode(this) != 0) {
            fragmentContainer.alpha = 0f
        }
    }
}
