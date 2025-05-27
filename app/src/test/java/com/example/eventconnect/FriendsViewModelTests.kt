package com.example.eventconnect

import com.example.eventconnect.ui.data.FriendsViewModel
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class FriendsViewModelTests {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var collectionReference: CollectionReference
    private lateinit var documentReference: DocumentReference
    private lateinit var query: Query
    private lateinit var queryTask: Task<QuerySnapshot>
    private lateinit var documentTask: Task<DocumentSnapshot>
    private lateinit var writeTask: Task<Void>

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: FriendsViewModel

    private val currentUserId = "test_user_id"
    private val currentUserEmail = "test@example.com"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        firestore = mock()
        collectionReference = mock()
        documentReference = mock()
        query = mock()
        queryTask = mock()
        documentTask = mock()
        writeTask = mock()

        whenever(firestore.collection(anyString())).thenReturn(collectionReference)
        whenever(collectionReference.document(anyString())).thenReturn(documentReference)
        whenever(documentReference.collection(anyString())).thenReturn(collectionReference)

        whenever(collectionReference.whereEqualTo(anyString(), any())).thenReturn(query)
        whenever(query.whereEqualTo(anyString(), any())).thenReturn(query)
        whenever(query.get()).thenReturn(queryTask)
        whenever(collectionReference.get()).thenReturn(queryTask)
        whenever(documentReference.get()).thenReturn(documentTask)
        whenever(collectionReference.add(any<Map<String, Any>>())).thenReturn(mock<Task<DocumentReference>>())

        whenever(queryTask.addOnSuccessListener(any<OnSuccessListener<QuerySnapshot>>())).thenReturn(queryTask)
        whenever(queryTask.addOnFailureListener(any<OnFailureListener>())).thenReturn(queryTask)

        whenever(documentTask.addOnSuccessListener(any<OnSuccessListener<DocumentSnapshot>>())).thenReturn(documentTask)
        whenever(documentTask.addOnFailureListener(any<OnFailureListener>())).thenReturn(documentTask)

        whenever(documentReference.set(any<Map<String, Any>>())).thenReturn(writeTask)
        whenever(writeTask.addOnSuccessListener(any<OnSuccessListener<Void>>())).thenReturn(writeTask)
        whenever(writeTask.addOnFailureListener(any<OnFailureListener>())).thenReturn(writeTask)

        val batch = mock<WriteBatch>()
        whenever(firestore.batch()).thenReturn(batch)
        whenever(batch.delete(any<DocumentReference>())).thenReturn(batch)
        whenever(batch.commit()).thenReturn(writeTask)

        viewModel = FriendsViewModel(firestore, currentUserId, currentUserEmail)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty lists and no error`() = runTest {
        assertTrue("Friends list should be empty initially", viewModel.friends.value.isEmpty())
        assertTrue("Search results should be empty initially", viewModel.searchResults.value.isEmpty())
        assertTrue("All users list should be empty initially", viewModel.allUsers.value.isEmpty())
        assertEquals("Error should be empty initially", "", viewModel.error.value)
    }

    @Test
    fun `fetchAllUsers failure sets error message and resets isLoading`() = runTest {
        val exception = Exception("Test fetch error")

        whenever(firestore.collection("users")).thenReturn(collectionReference)

        whenever(queryTask.addOnFailureListener(any<OnFailureListener>())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnFailureListener>(0)
            listener.onFailure(exception)
            queryTask
        }

        viewModel.fetchAllUsers()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.allUsers.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertEquals("Failed to load users: Test fetch error", viewModel.error.value)
    }

    @Test
    fun `searchUsers with empty query clears searchResults`() = runTest {
        viewModel.searchUsers("")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.searchResults.value.isEmpty())
    }

    @Test
    fun `searchUsers failure sets error`() = runTest {
        val exception = Exception("Search error")

        whenever(firestore.collection("users")).thenReturn(collectionReference)

        whenever(queryTask.addOnFailureListener(any<OnFailureListener>())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnFailureListener>(0)
            listener.onFailure(exception)
            queryTask
        }

        viewModel.searchUsers("test")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.searchResults.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertEquals("Search failed: Search error", viewModel.error.value)
    }

    @Test
    fun `sendFriendRequest with empty email sets error`() = runTest {
        viewModel.sendFriendRequest("")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Receiver email cannot be empty", viewModel.error.value)
    }

    @Test
    fun `createUser with empty name sets error`() = runTest {
        viewModel.createUser("", "test@example.com")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Please fill all fields", viewModel.error.value)
    }

    @Test
    fun `createUser with empty email sets error`() = runTest {
        viewModel.createUser("Test Name", "")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Please fill all fields", viewModel.error.value)
    }

    @Test
    fun `addFriendByEmail with empty email sets error`() = runTest {
        viewModel.addFriendByEmail("")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Please enter an email", viewModel.error.value)
    }

    @Test
    fun `addFriend with empty userId sets error`() = runTest {
        viewModel.addFriend("", "test@example.com")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Id cannot be empty", viewModel.error.value)
    }

    @Test
    fun `addFriend with empty email sets error`() = runTest {
        viewModel.addFriend("testId", "")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Email cannot be empty", viewModel.error.value)
    }

}
