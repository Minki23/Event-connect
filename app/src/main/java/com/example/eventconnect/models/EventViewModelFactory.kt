import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.eventconnect.ui.data.EventViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EventViewModelFactory(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventViewModel(auth, db, storage) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
