package me.samud.minimumlauncher

// Removed imports for Android classes no longer directly used in this test
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class AppLoaderTest {

    private lateinit var mockAppSource: AppSource // Changed from mockPackageManager
    private lateinit var appLoader: AppLoader

    @Before
    fun setUp() {
        // Mock the AppSource
        mockAppSource = mock(AppSource::class.java)
        appLoader = AppLoader(mockAppSource) // AppLoader now takes AppSource
    }

    @Test
    fun `loadAndSortApps should return apps sorted alphabetically by name`() {
        // 1. Arrange: Prepare a list of unsorted AppInfo objects
        val unsortedApps = listOf(
            AppInfo("App B", "com.example.appb"),
            AppInfo("App A", "com.example.appa"),
            AppInfo("App C", "com.example.appc")
        )

        // Mock the appSource.getLaunchableApps() call
        `when`(mockAppSource.getLaunchableApps()).thenReturn(unsortedApps)

        // 2. Act: Call the method under test
        val loadedApps = appLoader.loadAndSortApps()

        // 3. Assert: Verify the list is sorted
        val expectedSortedApps = listOf(
            AppInfo("App A", "com.example.appa"),
            AppInfo("App B", "com.example.appb"),
            AppInfo("App C", "com.example.appc")
        )

        assertEquals("List size should match", expectedSortedApps.size, loadedApps.size)
        for (i in expectedSortedApps.indices) {
            assertEquals("Mismatched app name at index $i", expectedSortedApps[i].name, loadedApps[i].name)
            assertEquals("Mismatched package name at index $i", expectedSortedApps[i].packageName, loadedApps[i].packageName)
        }
    }

    @Test
    fun `loadAndSortApps should handle empty list from appSource`() {
        // 1. Arrange: Mock appSource to return an empty list
        `when`(mockAppSource.getLaunchableApps()).thenReturn(emptyList())

        // 2. Act: Call the method under test
        val loadedApps = appLoader.loadAndSortApps()

        // 3. Assert: Verify the returned list is empty
        assertEquals("Loaded apps should be empty", true, loadedApps.isEmpty())
    }
}
