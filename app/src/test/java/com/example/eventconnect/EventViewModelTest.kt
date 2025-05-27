import android.util.Log
import com.example.eventconnect.ui.data.EventFilter
import com.example.eventconnect.ui.data.EventViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.OnFailureListener
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.After
import org.mockito.ArgumentMatchers.anyString
import com.google.firebase.firestore.Query


class EventViewModelTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var storage: FirebaseStorage
    private lateinit var collectionReference: CollectionReference
    private lateinit var documentReference: DocumentReference
    private lateinit var subCollectionReference: CollectionReference
    private lateinit var queryTask: Task<QuerySnapshot>
    private lateinit var documentTask: Task<DocumentSnapshot>
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockQuery: Query


    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        firestore = mock()
        auth = mock()
        firebaseUser = mock()
        storage = mock()
        collectionReference = mock()
        documentReference = mock()
        subCollectionReference = mock()
        queryTask = mock()
        documentTask = mock()
        mockQuery = mock() // Initialize mockQuery

        // Setup auth mocks
        whenever(auth.currentUser).thenReturn(firebaseUser)
        whenever(firebaseUser.uid).thenReturn("test_user_id")

        // Setup Firestore mocks - complete chain
        whenever(firestore.collection(any())).thenReturn(collectionReference)
        whenever(collectionReference.document(any())).thenReturn(documentReference)
        // Mock the whereEqualTo call and return our mockQuery
        whenever(collectionReference.whereEqualTo(anyString(), any())).thenReturn(mockQuery)


        // Mock the subcollection call
        whenever(documentReference.collection(any())).thenReturn(subCollectionReference)

        // Mock the get() calls to return Task objects
        whenever(collectionReference.get()).thenReturn(queryTask)
        whenever(subCollectionReference.get()).thenReturn(queryTask)
        whenever(documentReference.get()).thenReturn(documentTask)
        whenever(mockQuery.get()).thenReturn(queryTask) // Mock get() for the Query object


        // Mock Task method chaining - THIS IS THE KEY PART
        whenever(queryTask.addOnSuccessListener(any<OnSuccessListener<QuerySnapshot>>())).thenReturn(queryTask)
        whenever(queryTask.addOnFailureListener(any<OnFailureListener>())).thenReturn(queryTask)
        whenever(documentTask.addOnSuccessListener(any<OnSuccessListener<DocumentSnapshot>>())).thenReturn(documentTask)
        whenever(documentTask.addOnFailureListener(any<OnFailureListener>())).thenReturn(documentTask)

        // If there are any addSnapshotListener calls, mock them too
        whenever(collectionReference.addSnapshotListener(any())).thenReturn(mock())
        whenever(subCollectionReference.addSnapshotListener(any())).thenReturn(mock())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSetFilterUpdatesSelectedFilter() = runTest {
        val viewModel = EventViewModel(auth = auth, db = firestore, storage = storage)
        viewModel.setFilter(EventFilter.MY_EVENTS)

        // Advance the dispatcher to execute all pending coroutines
        testDispatcher.scheduler.advanceUntilIdle()

        assert(viewModel.selectedFilter.value == EventFilter.MY_EVENTS)
    }


    @Test
    fun `currentUserUid returns correct UID when user is logged in`() = runTest {
        val viewModel = EventViewModel(auth, firestore, storage)
        val uid = viewModel.currentUserUid
        assert(uid == "test_user_id") { "currentUserUid should return 'test_user_id'" }
    }


    @Test
    fun `initial friends list is empty`() = runTest {
        val viewModel = EventViewModel(auth, firestore, storage)

        // We need to ensure the loadUserFriends call in init doesn't block the test indefinitely
        // For this test, we care about the initial state *before* the async call completes
        // Since loadUserFriends uses addOnSuccessListener, we can mock it to do nothing for this specific test
        whenever(collectionReference.get()).thenReturn(queryTask)
        whenever(queryTask.addOnSuccessListener(any<OnSuccessListener<QuerySnapshot>>())).thenReturn(queryTask)
        whenever(queryTask.addOnFailureListener(any<OnFailureListener>())).thenReturn(queryTask)

        testDispatcher.scheduler.advanceUntilIdle() // Allow init block to run

        assert(viewModel.friends.value.isEmpty()) { "Friends list should be empty initially" }
    }
}