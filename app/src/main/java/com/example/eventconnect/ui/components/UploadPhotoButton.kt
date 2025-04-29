import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun UploadPhotoButton(context: Context) {
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            photoUri = uri
            Toast.makeText(context, "Wybrano zdjęcie z galerii", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Nie wybrano zdjęcia", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            Toast.makeText(context, "Zrobiono zdjęcie!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Nie udało się zrobić zdjęcia", Toast.LENGTH_SHORT).show()
        }
    }

    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }

    Button(
        onClick = {
            // Pokazujemy wybór: galeria czy aparat
            val options = listOf("Wybierz z galerii", "Zrób nowe zdjęcie")
            androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Wybierz opcję")
                .setItems(options.toTypedArray()) { _, which ->
                    when (which) {
                        0 -> {
                            // Galeria
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        1 -> {
                            // Aparat
                            val photoFile = createImageFile(context)
                            currentPhotoUri = FileProvider.getUriForFile(
                                context,
                                context.packageName + ".provider",
                                photoFile
                            )
                            cameraLauncher.launch(currentPhotoUri)
                        }
                    }
                }
                .show()
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Upload Photo", color = Color.White)
    }
}

// Funkcja pomocnicza do tworzenia pliku na zdjęcie
fun createImageFile(context: Context): File {
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        "JPEG_${System.currentTimeMillis()}_", /* prefix */
        ".jpg", /* suffix */
        storageDir /* directory */
    )
}
