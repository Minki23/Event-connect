import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToLog
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import androidx.test.platform.app.InstrumentationRegistry
import android.util.Log
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import com.example.eventconnect.MainActivity
import org.junit.After // Added import
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UITests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun tearDown() {
        attemptLogout()
    }

    private fun attemptLogout() {
        runCatching {
            composeTestRule.onNodeWithTag("profile_button").assertExists()

            composeTestRule.onNodeWithTag("profile_button").performClick()
            composeTestRule.onNodeWithText("Log Out").performClick()
            try {
                composeTestRule.waitUntil(timeoutMillis = 5000L) {
                    composeTestRule.onAllNodesWithText("Login with google").fetchSemanticsNodes().isNotEmpty()
                }
                Log.d("UI_TEST", "Logout successful in attemptLogout.")
            } catch (e: androidx.compose.ui.test.ComposeTimeoutException) {
                Log.e("UI_TEST", "Timeout waiting for login screen after logout in attemptLogout. 'Login with google' not found.")
                composeTestRule.onRoot().printToLog("HIERARCHY_AFTER_LOGOUT_TIMEOUT_IN_ATTEMPT_LOGOUT")
            }
        }.onFailure {
            Log.d("UI_TEST", "Attempted logout in attemptLogout: User might already be logged out or not on a screen with logout controls. Details: ${it.message}")
        }
    }

    @Test
    fun clickLoginButton_showsLoginScreen() {
        composeTestRule.onNodeWithText("Login with google").performClick()

        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)

        val accountSelector = By.textContains("michaszymczak23@gmail.com")
        val timeout = 5000L

        if (!device.wait(Until.hasObject(accountSelector), timeout)) {
            composeTestRule.onRoot().printToLog("DEBUG_TREE_AFTER_CLICK_LOGIN_WITH_GOOGLE")
            throw AssertionError("Google Sign-In account picker with '${accountSelector}' not found or timed out. Check Logcat for DEBUG_TREE and inspect the UI with UI Automator Viewer.")
        }

        val accountElement = device.findObject(accountSelector)
        if (accountElement != null) {
            accountElement.click()
        } else {
            throw AssertionError("Account element '${accountSelector}' not found even after wait.")
        }


        try {
            composeTestRule.waitUntil(timeoutMillis = 10000L) {
                composeTestRule.onAllNodesWithText("Events").fetchSemanticsNodes().isNotEmpty()
            }
        } catch (e: androidx.compose.ui.test.ComposeTimeoutException) {
            Log.e("UI_TEST", "Timeout waiting for 'Events' screen after login. 'Events' not found.")
            composeTestRule.onRoot().printToLog("HIERARCHY_AFTER_LOGIN_TIMEOUT")
            throw e
        }

        composeTestRule.onNodeWithText("Events").assertIsDisplayed()
    }

    @Test
    fun loginAndCreateEvent_eventAppearsInList() {
        clickLoginButton_showsLoginScreen()
        try {
            composeTestRule.waitUntil(timeoutMillis = 5000L) {
                composeTestRule.onAllNodesWithText("Add Event").fetchSemanticsNodes().isNotEmpty()
            }
        } catch (e: androidx.compose.ui.test.ComposeTimeoutException) {
            Log.e("UI_TEST", "Timeout waiting for 'Add Event' node after login.")
            composeTestRule.onRoot().printToLog("HIERARCHY_LOGIN_CREATE_EVENT_TIMEOUT_ADD_EVENT")
            throw e
        }
        composeTestRule.onNodeWithText("Add Event").performClick()

        val eventName = "My Test Event - ${System.currentTimeMillis()}"
        val eventDescription = "This is a detailed description for the test event."
        val location = "Test Location"
        composeTestRule.onNodeWithText("Event Name").performTextInput(eventName)
        composeTestRule.onNodeWithText("Location").performTextInput(location)
        composeTestRule.onNodeWithText("Description").performTextInput(eventDescription)

        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)
        val dialogInteractionTimeout = 5000L

        composeTestRule.onNodeWithTag("date_picker").performClick()
        if (!device.wait(Until.hasObject(By.text("OK")), dialogInteractionTimeout)) {
            Log.e("UI_TEST", "Timeout waiting for Date Picker 'OK' button.")
            composeTestRule.onRoot().printToLog("HIERARCHY_DATE_PICKER_OK_TIMEOUT")
            throw AssertionError("Date Picker 'OK' button not found or timed out.")
        }
        val datePickerOkButton = device.findObject(By.text("OK"))
        if (datePickerOkButton != null) {
            datePickerOkButton.click()
        } else {
            val alternativeDatePickerOkButton = device.findObject(By.res("android:id/button1"))
            alternativeDatePickerOkButton?.click() ?: throw AssertionError("Date picker 'OK' button (alternative) not found. Inspect with uiautomatorviewer.")
        }
        try {
            composeTestRule.waitUntil(timeoutMillis = dialogInteractionTimeout) {
                composeTestRule
                    .onAllNodesWithTag("time_picker")
                    .filter(androidx.compose.ui.test.isEnabled())
                    .fetchSemanticsNodes().isNotEmpty()
            }
        } catch (e: androidx.compose.ui.test.ComposeTimeoutException) {
            Log.e("UI_TEST", "Timeout waiting for UI to be ready after Date Picker OK click (time_picker not enabled/found).")
            composeTestRule.onRoot().printToLog("HIERARCHY_AFTER_DATE_PICKER_OK_TIMEOUT")
            throw e
        }

        composeTestRule.onNodeWithTag("time_picker").performClick()
        if (!device.wait(Until.hasObject(By.text("OK")), dialogInteractionTimeout)) {
            Log.e("UI_TEST", "Timeout waiting for Time Picker 'OK' button.")
            composeTestRule.onRoot().printToLog("HIERARCHY_TIME_PICKER_OK_TIMEOUT")
            throw AssertionError("Time Picker 'OK' button not found or timed out.")
        }
        val timePickerOkButton = device.findObject(By.text("OK"))
        if (timePickerOkButton != null) {
            timePickerOkButton.click()
        } else {
            val alternativeTimePickerOkButton = device.findObject(By.res("android:id/button1"))
            alternativeTimePickerOkButton?.click() ?: throw AssertionError("Time picker 'OK' button (alternative) not found. Inspect with uiautomatorviewer.")
        }
        try {
            composeTestRule.waitUntil(timeoutMillis = dialogInteractionTimeout) {
                composeTestRule
                    .onAllNodesWithTag("save_event_button")
                    .filter(androidx.compose.ui.test.isEnabled())
                    .fetchSemanticsNodes().isNotEmpty()
            }
        } catch (e: androidx.compose.ui.test.ComposeTimeoutException) {
            Log.e("UI_TEST", "Timeout waiting for UI to be ready after Time Picker OK click (save_event_button not enabled/found).")
            composeTestRule.onRoot().printToLog("HIERARCHY_AFTER_TIME_PICKER_OK_TIMEOUT")
            throw e
        }

        composeTestRule.onNodeWithTag("save_event_button").performClick()

        try {
            composeTestRule.waitUntil(timeoutMillis = 10000L) {
                composeTestRule.onAllNodesWithText(eventName).fetchSemanticsNodes().isNotEmpty()
            }
        } catch (e: androidx.compose.ui.test.ComposeTimeoutException) {
            Log.e("UI_TEST", "Timeout waiting for event '${eventName}' to appear in list.")
            composeTestRule.onRoot().printToLog("HIERARCHY_EVENT_LIST_TIMEOUT")
            throw e
        }
        composeTestRule.onNodeWithText(eventName).assertIsDisplayed()
    }

    fun scrollUntilVisible(name: String, maxTries: Int = 10) {
        repeat(maxTries) {
            if (composeTestRule.onNodeWithTag("event_item_${name}").isDisplayed()) return
            composeTestRule.onNodeWithTag("event_list").performTouchInput { swipeUp() }
            composeTestRule.waitForIdle()
        }
        throw AssertionError("Item with text '$name' not found after $maxTries scrolls.")
    }

    @Test
    fun navigateToEventDetails_displaysCorrectInformation() {

        val initialEventName = "My Test Event - ${System.currentTimeMillis()}"
        val editedEventName = "Edited Event - ${System.currentTimeMillis()}"
        val editedDescription = "Edited Description"
        val editedLocation = "Edited Location"
        clickLoginButton_showsLoginScreen()

        try {
            composeTestRule.waitUntil(timeoutMillis = 5000L) {
                composeTestRule.onAllNodesWithText("Add Event").fetchSemanticsNodes().isNotEmpty()
            }
        } catch (e: androidx.compose.ui.test.ComposeTimeoutException) {
            Log.e("UI_TEST", "Timeout waiting for 'Add Event' node after login.")
            composeTestRule.onRoot().printToLog("HIERARCHY_LOGIN_CREATE_EVENT_TIMEOUT_ADD_EVENT")
            throw e
        }
        composeTestRule.onNodeWithText("Add Event").performClick()

        val eventName = "My Test Event - ${System.currentTimeMillis()}"
        val eventDescription = "This is a detailed description for the test event."
        val location = "Test Location"
        composeTestRule.onNodeWithText("Event Name").performTextInput(eventName)
        composeTestRule.onNodeWithText("Location").performTextInput(location)
        composeTestRule.onNodeWithText("Description").performTextInput(eventDescription)

        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)
        val dialogInteractionTimeout = 5000L

        composeTestRule.onNodeWithTag("date_picker").performClick()
        if (!device.wait(Until.hasObject(By.text("OK")), dialogInteractionTimeout)) {
            Log.e("UI_TEST", "Timeout waiting for Date Picker 'OK' button.")
            composeTestRule.onRoot().printToLog("HIERARCHY_DATE_PICKER_OK_TIMEOUT")
            throw AssertionError("Date Picker 'OK' button not found or timed out.")
        }
        val datePickerOkButton = device.findObject(By.text("OK"))
        if (datePickerOkButton != null) {
            datePickerOkButton.click()
        } else {
            val alternativeDatePickerOkButton = device.findObject(By.res("android:id/button1"))
            alternativeDatePickerOkButton?.click() ?: throw AssertionError("Date picker 'OK' button (alternative) not found. Inspect with uiautomatorviewer.")
        }
        try {
            composeTestRule.waitUntil(timeoutMillis = dialogInteractionTimeout) {
                composeTestRule
                    .onAllNodesWithTag("time_picker")
                    .filter(androidx.compose.ui.test.isEnabled())
                    .fetchSemanticsNodes().isNotEmpty()
            }
        } catch (e: androidx.compose.ui.test.ComposeTimeoutException) {
            Log.e("UI_TEST", "Timeout waiting for UI to be ready after Date Picker OK click (time_picker not enabled/found).")
            composeTestRule.onRoot().printToLog("HIERARCHY_AFTER_DATE_PICKER_OK_TIMEOUT")
            throw e
        }

        composeTestRule.onNodeWithTag("time_picker").performClick()
        if (!device.wait(Until.hasObject(By.text("OK")), dialogInteractionTimeout)) {
            Log.e("UI_TEST", "Timeout waiting for Time Picker 'OK' button.")
            composeTestRule.onRoot().printToLog("HIERARCHY_TIME_PICKER_OK_TIMEOUT")
            throw AssertionError("Time Picker 'OK' button not found or timed out.")
        }
        val timePickerOkButton = device.findObject(By.text("OK"))
        if (timePickerOkButton != null) {
            timePickerOkButton.click()
        } else {
            val alternativeTimePickerOkButton = device.findObject(By.res("android:id/button1"))
            alternativeTimePickerOkButton?.click() ?: throw AssertionError("Time picker 'OK' button (alternative) not found. Inspect with uiautomatorviewer.")
        }
       try {
            composeTestRule.waitUntil(timeoutMillis = dialogInteractionTimeout) {
                composeTestRule
                    .onAllNodesWithTag("save_event_button")
                    .filter(androidx.compose.ui.test.isEnabled())
                    .fetchSemanticsNodes().isNotEmpty()
            }
        } catch (e: androidx.compose.ui.test.ComposeTimeoutException) {
            Log.e("UI_TEST", "Timeout waiting for UI to be ready after Time Picker OK click (save_event_button not enabled/found).")
            composeTestRule.onRoot().printToLog("HIERARCHY_AFTER_TIME_PICKER_OK_TIMEOUT")
            throw e
        }

        composeTestRule.onNodeWithTag("save_event_button").performClick()

        try {
            composeTestRule.waitUntil(timeoutMillis = 10000L) {
                composeTestRule.onAllNodesWithText(eventName).fetchSemanticsNodes().isNotEmpty()
            }
        } catch (e: androidx.compose.ui.test.ComposeTimeoutException) {
            Log.e("UI_TEST", "Timeout waiting for event '${eventName}' to appear in list.")
            composeTestRule.onRoot().printToLog("HIERARCHY_EVENT_LIST_TIMEOUT")
            throw e
        }
        composeTestRule.waitUntil(timeoutMillis = 10000L) {
            composeTestRule.onAllNodesWithText("Host").fetchSemanticsNodes().isNotEmpty()
        }
        scrollUntilVisible(eventName)
        composeTestRule.onNodeWithText(eventName).performClick()
        composeTestRule.waitUntil(timeoutMillis = 10000L) {
            composeTestRule.onNodeWithTag("event_name_field").isDisplayed()
        }
        composeTestRule.onNodeWithTag("event_name_field").performTextClearance()
        composeTestRule.onNodeWithTag("event_name_field").performTextInput(editedEventName)
        composeTestRule.onNodeWithTag("location_field").performTextClearance()
        composeTestRule.onNodeWithTag("location_field").performTextInput(editedLocation)
        composeTestRule.onNodeWithTag("description_field").performTextClearance()
        composeTestRule.onNodeWithTag("description_field").performTextInput(editedDescription)

        try {
            composeTestRule.waitUntil(timeoutMillis = 5000L) {
                composeTestRule.onAllNodesWithTag("save_edited_event").fetchSemanticsNodes().isNotEmpty()
            }
        } catch (e: androidx.compose.ui.test.ComposeTimeoutException) {
            Log.e("UI_TEST", "Timeout waiting for 'save_edited_event'.")
            composeTestRule.onRoot().printToLog("HIERARCHY_EDIT_TEST_SAVE_EDIT_BUTTON_TIMEOUT")
            throw e
        }
        composeTestRule.onNodeWithTag("save_edited_event").performClick()

        try {
            composeTestRule.waitUntil(timeoutMillis = 10000L) {

                val newNameNodes = composeTestRule.onAllNodesWithText(editedEventName).fetchSemanticsNodes()
                val oldNameNodes = composeTestRule.onAllNodesWithText(initialEventName).fetchSemanticsNodes()
                newNameNodes.isNotEmpty() && oldNameNodes.isEmpty()
            }
        } catch (e: androidx.compose.ui.test.ComposeTimeoutException) {
            Log.e("UI_TEST", "Timeout waiting for edited event name '${editedEventName}' to appear and old name to disappear.")
            composeTestRule.onRoot().printToLog("HIERARCHY_EDIT_TEST_VERIFY_EDIT_TIMEOUT")
            throw e
        }
        composeTestRule.onNodeWithText(editedEventName).performClick()
        try {
            composeTestRule.waitUntil(timeoutMillis = 10000L) {
                val nameNodes = composeTestRule.onAllNodesWithText(editedEventName).fetchSemanticsNodes()
                val descNodes = composeTestRule.onAllNodesWithText(editedDescription).fetchSemanticsNodes()
                val locNodes = composeTestRule.onAllNodesWithText(editedLocation).fetchSemanticsNodes()
                nameNodes.isNotEmpty() && descNodes.isNotEmpty() && locNodes.isNotEmpty()
            }
        } catch (e: androidx.compose.ui.test.ComposeTimeoutException) {
            Log.e("UI_TEST", "Timeout waiting for all edited details on details screen.")
            composeTestRule.onRoot().printToLog("HIERARCHY_EDIT_TEST_VERIFY_DETAILS_TIMEOUT")
            throw e
        }

        composeTestRule.onNodeWithText(editedEventName).assertIsDisplayed()
        composeTestRule.onNodeWithText(editedDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText(editedLocation).assertIsDisplayed()
        Log.d("UI_TEST", "All edited details verified on details screen for '${editedEventName}'.")
    }

    @Test
    fun logoutUser_returnsToLoginScreen() {
        clickLoginButton_showsLoginScreen()

        composeTestRule.onNodeWithTag("profile_button").performClick()

        composeTestRule.onNodeWithText("Log Out").performClick()

        composeTestRule.onNodeWithText("Login with google").assertIsDisplayed()
    }

    @Test
    fun navigateViaBottomNavigationBar_showsCorrectScreens() {

        clickLoginButton_showsLoginScreen()

        val navItemsToText = mapOf(
            "home" to "Events",
            "add_event" to "Create Event",
            "friends" to "Your Friends"
        )

        navItemsToText.forEach { (navItem, expectedText) ->
            composeTestRule.onNodeWithTag("bottom_nav_$navItem").performClick()
            Log.d("UI_TEST", "checked $navItem")
            composeTestRule.onNodeWithText(expectedText).assertIsDisplayed()
        }

    }

    @After
    fun fullCleanupAfterTest() {
        Log.d("UI_TEST", "Executing @After cleanup.")
        attemptLogout()
    }
}