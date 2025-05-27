package com.example.eventconnect

import LocalFirestore
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.eventconnect.ui.screens.MainScreen
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.QueryDocumentSnapshot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

@RunWith(AndroidJUnit4::class)
class MainScreenTabTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bottomTabs_navigateToCorrectScreens() {
        // Create mock Firestore and related objects
        val mockFirestore = mock(FirebaseFirestore::class.java)
        val mockCollection = mock(CollectionReference::class.java)
        val mockDocument = mock(DocumentReference::class.java)
        val mockDocumentSnapshot = mock(DocumentSnapshot::class.java)
        val mockQuery = mock(Query::class.java)
        val mockQuerySnapshot = mock(QuerySnapshot::class.java)
        val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
        val mockDocTask: Task<DocumentSnapshot> = Tasks.forResult(mockDocumentSnapshot)

        // Mock QuerySnapshot with proper empty collections - THIS IS THE KEY FIX
        `when`(mockQuerySnapshot.isEmpty).thenReturn(true)
        `when`(mockQuerySnapshot.documents).thenReturn(emptyList())
        `when`(mockQuerySnapshot.iterator()).thenReturn(emptyList<QueryDocumentSnapshot>().toMutableList().iterator())
        `when`(mockQuerySnapshot.size()).thenReturn(0)

        // Mock DocumentSnapshot
        `when`(mockDocumentSnapshot.exists()).thenReturn(false)
        `when`(mockDocumentSnapshot.data).thenReturn(emptyMap<String, Any>())

        // Mock Firestore collection calls
        `when`(mockFirestore.collection(any())).thenReturn(mockCollection)
        `when`(mockFirestore.document(any())).thenReturn(mockDocument)

        // Mock collection methods
        `when`(mockCollection.whereEqualTo(any<String>(), any())).thenReturn(mockQuery)
        `when`(mockCollection.orderBy(any<String>())).thenReturn(mockQuery)
        `when`(mockCollection.limit(any())).thenReturn(mockQuery)
        `when`(mockCollection.get()).thenReturn(mockTask)
        `when`(mockCollection.document(any())).thenReturn(mockDocument)

        // Mock document methods
        `when`(mockDocument.collection(any())).thenReturn(mockCollection)
        `when`(mockDocument.get()).thenReturn(mockDocTask)

        // Mock query methods (for method chaining)
        `when`(mockQuery.whereEqualTo(any<String>(), any())).thenReturn(mockQuery)
        `when`(mockQuery.orderBy(any<String>())).thenReturn(mockQuery)
        `when`(mockQuery.limit(any())).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(mockTask)

        // Launch the MainScreen composable with mock Firestore
        composeTestRule.setContent {
            CompositionLocalProvider(LocalFirestore provides mockFirestore) {
                MainScreen(onGoogleLogin = {})
            }
        }

        // Wait for composition to settle
        composeTestRule.waitForIdle()

        // 🏠 HOME tab should be selected by default
        composeTestRule.onNodeWithText("Home").assertIsDisplayed()

        // ➕ Tap "Add Event" tab
        composeTestRule.onNodeWithContentDescription("Add Event", useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Create Event").assertIsDisplayed()

        // 👥 Tap "Friends" tab
        composeTestRule.onNodeWithContentDescription("Friends", useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithText("Friends")[0].assertIsDisplayed()
    }
}