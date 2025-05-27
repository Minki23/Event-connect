package com.example.eventconnect

import LocalFirestore
import LocalStorage
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.eventconnect.ui.components.MainNavigation
import com.example.eventconnect.ui.theme.EventConnectTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var db: FirebaseFirestore

    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    private lateinit var navController: NavHostController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        auth = Firebase.auth
        credentialManager = CredentialManager.create(baseContext)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(
                LocalFirestore provides db,
                LocalStorage provides FirebaseStorage.getInstance(),
            ) {
                EventConnectTheme {
                    navController = rememberNavController()
                    Scaffold(
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        MainNavigation(
                            innerPadding = innerPadding,
                            navController = navController,
                            onGoogleLogin = { launchCredentialManager() })
                    }
                }
            }
        }
    }

    fun launchCredentialManager() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = baseContext,
                    request = request
                )
                handleSignIn(result.credential)
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Credential error type: ${e::class.java.simpleName}")
                Log.e(TAG, "Detailed message: ${e.localizedMessage}")
                e.printStackTrace()
            }
        }
    }

    private fun handleSignIn(credential: Credential) {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.w(TAG, "Credential is not of type Google ID!")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            saveUserToFirestore(user)

            navController.navigate("main") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    private fun saveUserToFirestore(user: FirebaseUser) {
        val userData = hashMapOf(
            "uid" to user.uid,
            "email" to user.email,
            "displayName" to user.displayName,
            "photoUrl" to user.photoUrl?.toString(),
            "lastLogin" to System.currentTimeMillis()
        )

        val usersCollection = db.collection("users")

        usersCollection.document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    usersCollection.document(user.uid)
                        .update("lastLogin", System.currentTimeMillis())
                        .addOnSuccessListener {
                            Log.d(TAG, "User login time updated successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error updating user login time", e)
                        }
                } else {
                    usersCollection.document(user.uid)
                        .set(userData)
                        .addOnSuccessListener {
                            Log.d(TAG, "User added to Firestore successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error adding user to Firestore", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error checking for existing user", e)
            }
    }
}
