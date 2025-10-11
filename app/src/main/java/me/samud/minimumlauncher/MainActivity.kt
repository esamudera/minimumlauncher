package me.samud.minimumlauncher

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

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
        // Reset UI if the app was paused for more than the delay
        if (pausedTimestamp > 0 && (currentTime - pausedTimestamp) > resetDelay) {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (fragment is AppListFragment) {
                fragment.resetUI()
            }
            // Re-apply the transition when the UI is reset
        }

        GestureNavigationHelper.applyGestureNavigationTransition(fragmentContainer, this)
    }

    override fun onPause() {
        super.onPause()
        pausedTimestamp = System.currentTimeMillis()
        // Prepare for the transition on resume
        GestureNavigationHelper.prepareTransition(fragmentContainer, this)
    }
}
