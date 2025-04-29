import androidx.compose.runtime.compositionLocalOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


val LocalFirestore = compositionLocalOf<FirebaseFirestore> { error("No Firestore provided") }
val LocalStorage = compositionLocalOf<FirebaseStorage> { error("No Storage provided") }