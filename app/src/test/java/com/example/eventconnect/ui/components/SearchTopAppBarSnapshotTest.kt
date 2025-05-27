import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.example.eventconnect.ui.components.SearchTopAppBar
import org.junit.Rule
import org.junit.Test


class SearchTopAppBarSnapshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5, // Standard device configuration
        theme = "android:Theme.Material3.Light.NoActionBar" // Essential for Material 3 components
        // If you have a custom app theme, you might use:
        // appTheme = "com.example.eventconnect.ui.theme.YourAppThemeName"
        // (replace YourAppThemeName with your actual theme function name if it's a composable,
        // or the XML theme name if Paparazzi version < 1.3.0 or for non-Compose themes)
    )

    @Test
    fun testSearchTopAppBar_default() {
        paparazzi.snapshot {
            SearchTopAppBar(
                title = "Events",
                onProfileClick = {} // Empty lambda for snapshot testing UI
            )
        }
    }

    @Test
    fun testSearchTopAppBar_longTitle() {
        paparazzi.snapshot {
            SearchTopAppBar(
                title = "Very Long Event Title That Might Wrap or Truncate",
                onProfileClick = {}
            )
        }
    }
}