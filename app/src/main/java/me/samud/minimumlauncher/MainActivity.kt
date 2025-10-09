package me.samud.minimumlauncher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var pausedTimestamp: Long = 0
        private val resetDelay: Long = 10000 // 10 seconds in milliseconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
    }

    override fun onPause() {
        super.onPause()
        pausedTimestamp = System.currentTimeMillis()
    }
}
