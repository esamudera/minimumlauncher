package me.samud.minimumlauncher

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var pausedTimestamp: Long = 0
    private val resetDelay: Long = 10000 // 10 seconds in milliseconds
    private lateinit var fragmentContainer: FrameLayout

    private var isResumedFromHomeKey = false

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // The Home button/gesture sends an intent with the CATEGORY_HOME flag
        if (intent?.hasCategory(Intent.CATEGORY_HOME) == true) {
            isResumedFromHomeKey = true
        }
    }

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

        // Apply the gesture navigation transition ONLY if resumed from the home key/gesture.
        if (isResumedFromHomeKey) {
            GestureNavigationHelper.applyGestureNavigationTransition(fragmentContainer, this)
        }

        isResumedFromHomeKey = false
    }

    override fun onPause() {
        super.onPause()
        pausedTimestamp = System.currentTimeMillis()
    }
}
