package com.example.whatstheplant.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.whatstheplant.R
import com.example.whatstheplant.composables.MyButton
import com.example.whatstheplant.composables.MyIconButton
import com.example.whatstheplant.composables.MyText
import com.example.whatstheplant.ui.theme.WhatsThePlantTheme

class CameraActivity : ComponentActivity() {

    private var photo: Bitmap? = null

    private var loadedPhoto = false
    private lateinit var loading: MutableState<Boolean>

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 123
        private const val pic_id = 123
        private const val show_result_activity_id = 45
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Camera onCreate", loadedPhoto.toString())
        setContent {
            WhatsThePlantTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    loading = remember { mutableStateOf(false) }
                    //if (loading.value) {
                    //    LoadingScreen(heightOfSection = screenHeightDp.dp, screenHeightDp = screenHeightDp, text = "Finding best matches...")
                    //}
                    //else {
                    //val navController = rememberNavController()
                    //CameraScreen()

                    if (checkCameraPermission()) {
                        openCamera()
                    } else {
                        requestCameraPermission()
                    }
                    //}
                    CameraScreen()
                }
            }
        }
    }

    @Composable
    fun CameraPageContent(loading: MutableState<Boolean>) {
        val context = LocalContext.current
        val configuration: Configuration = context.getResources().getConfiguration()
        var screenWidthDp = configuration.screenWidthDp
        var screenHeightDp = configuration.screenHeightDp
        val navController = rememberNavController()


        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {


            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }
    }

    private fun confirmButtonOnClick(loading: MutableState<Boolean>) {

        loading.value = true

        /*
        CoroutineScope(Dispatchers.IO).launch {
            runBlocking{
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val date = LocalDateTime.now().format(formatter)
                val position = getPositionAsString(lastLocation!!)
                val res = PostsHandler.getBestMatchingPosts(0, date, position, photo!!, this@ScanActivity)
                Log.d("RISPOSTA MATCH", res.toString())
            }

            // Show result of matching in the activity MatchResultActivity
            val intent = Intent(this@ScanActivity, MatchResultActivity::class.java)
            intent.putExtra("user_id", user_id)
            intent.putExtra("username", username)
            startActivityForResult(intent, show_result_activity_id)
        }
         */
    }

    @Composable
    fun CameraScreen() {
        val context = LocalContext.current
        val configuration: Configuration = context.resources.configuration
        var screenWidthDp = configuration.screenWidthDp
        var screenHeightDp = configuration.screenHeightDp
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                MyText(text = "Take a picture!")
                MyIconButton(
                    icon = Icons.Default.PhotoCamera,
                    onClick = {
                        // cameraButtonOnClick()
                    })

                // Dialog state Manager
                val dialogState: MutableState<Boolean> = remember { mutableStateOf(false) }

                val imageModifier = Modifier
                    .width((screenWidthDp * 0.50).dp)
                    .height((screenHeightDp * 0.28).dp)
                    .border(BorderStroke(1.dp, Color(40, 68, 12)))
                    .clickable(onClick = { dialogState.value = true })

                if (loadedPhoto) {
                    val photoBitmap = photo!!.asImageBitmap()
                    Image(
                        painter = BitmapPainter(
                            photoBitmap,
                            IntOffset.Zero,
                            IntSize(photoBitmap.width, photoBitmap.height)
                        ),
                        contentDescription = "photo",
                        modifier = imageModifier,
                        contentScale = ContentScale.FillWidth
                    )
                    // Code to Show and Dismiss Dialog
                    if (dialogState.value) {
                        AlertDialog(
                            containerColor = Color(232, 245, 217, 255),
                            onDismissRequest = { dialogState.value = false },
                            text = {
                                Box(
                                    modifier = Modifier
                                        .width(300.dp)
                                        .height(360.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .border(BorderStroke(2.dp, Color.White))
                                ) {
                                    Image(
                                        painter = BitmapPainter(
                                            photoBitmap,
                                            IntOffset.Zero,
                                            IntSize(photoBitmap.width, photoBitmap.height)
                                        ),
                                        contentDescription = "pic",
                                        modifier = Modifier
                                            .width((screenWidthDp * 0.8).dp)
                                            .height((screenHeightDp / 0.6).dp)
                                            .background(Color(175, 213, 133, 255))
                                            .clip(shape = RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            },
                            confirmButton = { },
                            dismissButton = {
                                MyButton(
                                    text = "Close",
                                    onClick = { dialogState.value = false }
                                )

                            }

                        )
                    }
                } else {
                    val blankphoto =
                        BitmapFactory.decodeResource(context.resources, R.drawable.blank)
                            .asImageBitmap()
                    Image(
                        painter = BitmapPainter(
                            blankphoto,
                            IntOffset.Zero,
                            IntSize(blankphoto.width, blankphoto.height)
                        ),
                        contentDescription = "pic",
                        modifier = Modifier
                            .width((screenWidthDp * 0.50).dp)
                            .height((screenHeightDp * 0.28).dp)
                            .border(BorderStroke(1.dp, Color(40, 68, 12))),
                        contentScale = ContentScale.FillWidth
                    )
                }

                MyButton(text = "Confirm", onClick = { confirmButtonOnClick(loading) })
            }
        }
    }


    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, pic_id)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // When you come back here after taking the photo
        if (requestCode == pic_id && resultCode == RESULT_OK && data != null) {
            photo = data.extras?.get("data") as Bitmap?

            if (photo != null) {
                loadedPhoto = true
                loading.value = true
                loading.value = false
            }
        }

        // When you come back here from MatchResultActivity
        if (requestCode == show_result_activity_id) {
            finish()    // end this activity
        }
    }


    // PERMISSIONS
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    // Camera permissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            }
        }
    }
}