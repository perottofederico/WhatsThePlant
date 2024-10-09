package com.example.whatstheplant.composables.tabs

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Looper
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.whatstheplant.BuildConfig
import com.example.whatstheplant.R
import com.example.whatstheplant.api.plantid.identifyPlantImage
import com.example.whatstheplant.api.plantid.model.MyLocation
import com.example.whatstheplant.api.plantid.model.Plant
import com.example.whatstheplant.composables.MyBigButton
import com.example.whatstheplant.ui.theme.darkGreen
import com.example.whatstheplant.ui.theme.gray
import com.example.whatstheplant.viewModel.PlantViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


@Composable
fun CameraScreen(
    navController: NavController, apiResult: MutableState<Plant?>,
    plantViewModel: PlantViewModel
) {
    val context = LocalContext.current

    MainContent(
        context = context,
        navController,
        apiResult,
        plantViewModel
    )
}


@Composable
private fun MainContent(
    context: Context,
    navController: NavController,
    apiResult: MutableState<Plant?>,
    plantViewModel: PlantViewModel
) {
    CameraContent(context, navController, apiResult, plantViewModel)
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "RestrictedApi")
@Composable
private fun CameraContent(
    context: Context, navController: NavController, apiResult: MutableState<Plant?>, plantViewModel: PlantViewModel
) {
    var loading by remember {
        mutableStateOf(false)
    }

    //WindowInsetsCompat.Type.navigationBars()

    val file = context.createImageFile()
    val uri = remember {
        FileProvider.getUriForFile(
            Objects.requireNonNull(context), BuildConfig.APPLICATION_ID + ".provider", file
        )
    }
    var capturedImageUri by remember {
        mutableStateOf<Uri>(Uri.EMPTY)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        capturedImageUri = if (success) {
            uri
        } else {
            Uri.EMPTY
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = {
            if (it != null) {
                capturedImageUri = it
            }
        })

    //camera
    val requestCameraPermissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                cameraLauncher.launch(uri)
            } else {
                Toast.makeText(context,
                    "Camera Permissions have been denied.",
                    LENGTH_LONG
                ).show()
            }
        }

    //gps
    val coroutineScope = rememberCoroutineScope()
    val requestPermissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                loading = true
                coroutineScope.launch {
                    val position = getLocation(context)
                    Log.d("RESPONSE", position.toString())
                    callPlantIdApi(
                        context,
                        capturedImageUri,
                        position,
                        apiResult,
                        navController,
                        onComplete = {
                            plantViewModel.incrementScannedCount()
                            loading = false
                        }
                    )
                }

            } else {
                loading = true
                callPlantIdApi(
                    context,
                    capturedImageUri,
                    position = null,
                    apiResult,
                    navController,
                    onComplete = {
                        plantViewModel.incrementScannedCount()
                        loading = false
                    }
                )
            }
        }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = 0.dp,
                bottom = 100.dp,
                start = 8.dp,
                end = 8.dp
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (capturedImageUri.path?.isNotEmpty() == true) {
            Image(
                modifier = Modifier
                    .padding(
                        top = 0.dp,
                        bottom = 0.dp,
                        start = 8.dp,
                        end = 8.dp
                    )
                    .size(
                        400.dp
                    ),
                painter = rememberAsyncImagePainter(capturedImageUri),
                contentDescription = null,
                contentScale = ContentScale.Fit
            )
            MyBigButton(
                onClick = {

                    when {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED -> coroutineScope.launch {
                            loading = true
                            val position = getLocation(context)
                            Log.d("RESPONSE", position.toString())
                            callPlantIdApi(
                                context,
                                capturedImageUri,
                                position,
                                apiResult,
                                navController,
                                onComplete = {
                                    plantViewModel.incrementScannedCount()
                                    loading = false
                                }
                            )
                        }

                        ActivityCompat.shouldShowRequestPermissionRationale(
                            context as Activity, Manifest.permission.ACCESS_FINE_LOCATION
                        ) -> {
                            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }

                        else -> {
                            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    }
                },
                text = "Confirm"
            )
            Text(
                "Tip: Location permissions are optional, but they improve the results",
                textAlign = TextAlign.Center,
                color = Color.DarkGray,
                fontStyle = FontStyle.Italic
            )
            HorizontalDivider(
                color = Color.Gray,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                thickness = 2.dp
            )
        } else {
            Text(text = "No Image Selected")
        }

        //Spacer(modifier = Modifier.padding(16.dp))

        //Camera button
        MyBigButton(
            onClick = {
                when {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        cameraLauncher.launch(uri)
                    }

                    ActivityCompat.shouldShowRequestPermissionRationale(
                        context as Activity, Manifest.permission.CAMERA
                    ) -> requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)

                    else -> {
                        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            },
            text = "Take a picture"
        )

        Text(
            text = "Or",
            style = MaterialTheme.typography.labelLarge,
            fontSize = 16.sp
        )

        //Gallery button
        MyBigButton(
            onClick = {
                imagePicker.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
            text = "Choose from the gallery"
        )

    }
    // Progress Indicator
    if (loading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray.copy(alpha = 0.5F)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .wrapContentSize(Alignment.Center)
                    .size(60.dp),
                color = darkGreen,
                trackColor = Color.White
            )
        }
    }
}


@RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)
suspend fun getLocation(context: Context): MyLocation {
    return suspendCancellableCoroutine { continuation ->
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(1000)
            .setMaxUpdateDelayMillis(500)
        .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.locations.lastOrNull()
                if (location != null) {// Check if the accuracy is within acceptable bounds (e.g., 10 meters)
                    Log.d("LOCATION", location.toString())
                    if (location.accuracy <= 10f) {
                        // Good accuracy, return the result
                        continuation.resume(MyLocation(location.latitude, location.longitude))
                        fusedLocationClient.removeLocationUpdates(this) // Stop updates
                    } else {
                        // Not accurate enough, wait for another update
                        Log.d("Location", "Location not accurate enough: ${location.accuracy} meters")
                    }
                } else {
                    continuation.resumeWithException(Exception("Location is null"))
                }
            }
        }
        // Request location update once
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        // If the coroutine is canceled, make sure to clean up the location callback
        continuation.invokeOnCancellation {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}

@SuppressLint("SimpleDateFormat")
fun Context.createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName, /* prefix */
        ".jpg", /* suffix */
        externalCacheDir      /* directory */
    )
    return image
}

fun uriToBitmap(uri: Uri, contentResolver: ContentResolver): Bitmap? {
    return try {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@SuppressLint("RestrictedApi")
fun callPlantIdApi(
    context: Context,
    capturedImageUri: Uri,
    position: MyLocation?,
    apiResult: MutableState<Plant?>,
    navController: NavController,
    onComplete: () -> Unit
) {
    (context as? ComponentActivity)?.lifecycleScope?.launch {
        val bitmap = uriToBitmap(capturedImageUri, context.contentResolver)
        bitmap?.let {
            val result = identifyPlantImage(
                context.getString(
                    R.string.plantid_api_key
                ),
                position,
                bitmap
            )
            result?.let {
                Log.d("PlantID", "result: $result")
                apiResult.value = result
                onComplete()
                /*
                val root = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                        .toString()
                )
                val file3 = File(root, "test3.txt")
                file3.writeText(result.toString())
                */

                navController.navigate("APIRESULT")
            } ?: run {
                Log.e("PlantID", "error")
                Toast.makeText(
                    context,
                    "There was an error with the API. Please try again.",
                    LENGTH_LONG
                ).show()
                onComplete()
            }
        }
    }
}


@Preview(showSystemUi = true, showBackground = true)
@Composable
fun CameraContentPreview() {


    Column(
        modifier = Modifier
            //.fillMaxSize()
            .padding(
                top = 20.dp,
                bottom = 4.dp,
                start = 8.dp,
                end = 8.dp
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://cdn.pixabay.com/photo/2020/03/26/19/37/poppies-4971583_1280.jpg")
                    .placeholder(R.drawable.poppies)
                    .build()
            )

            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(400.dp)
            )
        }


        MyBigButton(
            onClick = {},
            text = "Confirm"
        )
        Row {
            Text(
                text = "Tip: when prompted, grant location permissions for better results",
                modifier = Modifier.padding(start = 32.dp, end = 32.dp),
                textAlign = TextAlign.Center
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                thickness = 2.dp,
                color = gray
            )
            Text(
                text = "Or",
                modifier = Modifier.padding(10.dp),
                fontSize = 20.sp
            )
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                thickness = 2.dp,
                color = gray
            )
        }

        //Camera button
        MyBigButton(
            onClick = { },
            text = "Take a picture"
        )

        Text(
            text = "Or",
            modifier = Modifier.padding(0.dp),
            fontSize = 16.sp
        )
        //Gallery button
        MyBigButton(
            onClick = {},
            text = "Choose from the gallery"
        )
    }
}
