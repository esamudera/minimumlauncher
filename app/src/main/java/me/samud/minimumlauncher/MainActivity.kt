package me.samud.minimumlauncher

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

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

        // Reset UI every time the activity comes to the foreground.
        // This provides a consistent "home screen" state.
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment is AppListFragment) {
            fragment.resetUI()
        }

        // Re-apply the gesture navigation transition on every resume
        GestureNavigationHelper.applyGestureNavigationTransition(fragmentContainer, this)
    }

    override fun onPause() {
        super.onPause()
        // Prepare for the transition on resume
        GestureNavigationHelper.prepareTransition(fragmentContainer, this)
    }
}
