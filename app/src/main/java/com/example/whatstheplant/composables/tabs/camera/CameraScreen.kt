package com.example.whatstheplant.composables.tabs.camera

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.net.Uri
import android.os.Environment
import android.os.Looper
import android.util.Log
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.example.whatstheplant.ui.theme.green
import com.example.whatstheplant.viewModel.PlantViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.InputStream
import java.text.DateFormat.getDateTimeInstance
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    navController: NavController, apiResult: MutableState<Plant?>,
    plantViewModel: PlantViewModel
) {
    val context = LocalContext.current
    val cameraPermissionState: PermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    MainContent(
        context = context,
        hasCameraPermission = cameraPermissionState.status.isGranted,
        onRequestPermission = cameraPermissionState::launchPermissionRequest,
        navController,
        apiResult,
        plantViewModel
    )
}


@Composable
private fun MainContent(
    context: Context,
    hasCameraPermission: Boolean,
    onRequestPermission: () -> Unit,
    navController: NavController,
    apiResult: MutableState<Plant?>,
    plantViewModel: PlantViewModel
) {
    //if (hasCameraPermission) {
    CameraContent(context, navController, apiResult, plantViewModel)
    //} else {
    //    NoPermissionScreen(onRequestPermission)
    //}
}


@OptIn(ExperimentalPermissionsApi::class)
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
        if (success) {
            capturedImageUri = uri
        } else {
            capturedImageUri = Uri.EMPTY
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

fun Context.createImageFile(): File {
    // Create an image file name
    val timestamptest = getDateTimeInstance()
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    Log.d("Timestamp", "$timestamptest")
    Log.d("Timestamp", timeStamp)
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
            var result = identifyPlantImage(
                context.getString(
                    R.string.plantid_api_key
                ),
                position,
                bitmap
            )
            result?.let {
                Log.d("PlantID", "result: $result")
                apiResult.value = result

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
                result = Gson().fromJson(
                    "{\n" +
                            "    \"access_token\": \"CtrjYkVtwJseMWs\",\n" +
                            "    \"model_version\": \"plant_id:4.0.2\",\n" +
                            "    \"custom_id\": null,\n" +
                            "    \"input\": {\n" +
                            "        \"latitude\": ${position?.latitude},\n" +
                            "        \"longitude\": ${position?.longitude},\n" +
                            "        \"similar_images\": true,\n" +
                            "        \"images\": [\n" +
                            "            \"https://plant.id/media/imgs/0f6ed1169d8442319fda1f9987e4210f.jpg\"\n" +
                            "        ],\n" +
                            "        \"datetime\": \"2024-08-05T08:16:45.899943+00:00\"\n" +
                            "    },\n" +
                            "    \"result\": {\n" +
                            "        \"is_plant\": {\n" +
                            "            \"probability\": 0.99096996,\n" +
                            "            \"threshold\": 0.5,\n" +
                            "            \"binary\": true\n" +
                            "        },\n" +
                            "        \"classification\": {\n" +
                            "            \"suggestions\": [\n" +
                            "                {\n" +
                            "                    \"id\": \"872243f84209c0c2\",\n" +
                            "                    \"name\": \"Buddleja davidii\",\n" +
                            "                    \"probability\": 0.9892,\n" +
                            "                    \"similar_images\": [\n" +
                            "                        {\n" +
                            "                            \"id\": \"909f07fbf17c7dab80a175a1649173b24ae6adb6\",\n" +
                            "                            \"url\": \"https://plant-id.ams3.cdn.digitaloceanspaces.com/similar_images/4/909/f07fbf17c7dab80a175a1649173b24ae6adb6.jpeg\",\n" +
                            "                            \"license_name\": \"CC BY-NC-SA 4.0\",\n" +
                            "                            \"license_url\": \"https://creativecommons.org/licenses/by-nc-sa/4.0/\",\n" +
                            "                            \"citation\": \"FlowerChecker s.r.o.\",\n" +
                            "                            \"similarity\": 0.758,\n" +
                            "                            \"url_small\": \"https://plant-id.ams3.cdn.digitaloceanspaces.com/similar_images/4/909/f07fbf17c7dab80a175a1649173b24ae6adb6.small.jpeg\"\n" +
                            "                        },\n" +
                            "                        {\n" +
                            "                            \"id\": \"808c7d58dabe9c3486549ea3e83de2fd9e86d581\",\n" +
                            "                            \"url\": \"https://plant-id.ams3.cdn.digitaloceanspaces.com/similar_images/4/808/c7d58dabe9c3486549ea3e83de2fd9e86d581.jpeg\",\n" +
                            "                            \"license_name\": \"CC BY-NC-SA 4.0\",\n" +
                            "                            \"license_url\": \"https://creativecommons.org/licenses/by-nc-sa/4.0/\",\n" +
                            "                            \"citation\": \"FlowerChecker s.r.o.\",\n" +
                            "                            \"similarity\": 0.741,\n" +
                            "                            \"url_small\": \"https://plant-id.ams3.cdn.digitaloceanspaces.com/similar_images/4/808/c7d58dabe9c3486549ea3e83de2fd9e86d581.small.jpeg\"\n" +
                            "                        }\n" +
                            "                    ],\n" +
                            "                    \"details\": {\n" +
                            "                        \"common_names\": [\n" +
                            "                            \"orange-eyed butterfly-bush\",\n" +
                            "                            \"Butterfly bush\",\n" +
                            "                            \"summer lilac\",\n" +
                            "                            \"orange-eye butterfly bush\",\n" +
                            "                            \"Chinese Sagewood\"\n" +
                            "                        ],\n" +
                            "                        \"taxonomy\": {\n" +
                            "                            \"class\": \"Magnoliopsida\",\n" +
                            "                            \"genus\": \"Buddleja\",\n" +
                            "                            \"order\": \"Lamiales\",\n" +
                            "                            \"family\": \"Scrophulariaceae\",\n" +
                            "                            \"phylum\": \"Tracheophyta\",\n" +
                            "                            \"kingdom\": \"Plantae\"\n" +
                            "                        },\n" +
                            "                        \"url\": \"https://en.wikipedia.org/wiki/Buddleja_davidii\",\n" +
                            "                        \"gbif_id\": 3173338,\n" +
                            "                        \"inaturalist_id\": 75916,\n" +
                            "                        \"rank\": \"species\",\n" +
                            "                        \"description\": {\n" +
                            "                            \"value\": \"Buddleja davidii (spelling variant Buddleia davidii), also called summer lilac, butterfly-bush, or orange eye, is a species of flowering plant in the family Scrophulariaceae, native to Sichuan and Hubei provinces in central China, and also Japan. It is widely used as an ornamental plant, and many named varieties are in cultivation. The genus was named Buddleja after Reverend Adam Buddle, an English botanist. The species name davidii honors the French missionary and explorer in China, Father Armand David, who was the first European to report the shrub. It was found near Ichang by Dr Augustine Henry about 1887 and sent to St Petersburg.  Another botanist-missionary in China, Jean-André Soulié, sent seed to the French nursery Vilmorin, and B. davidii entered commerce in the 1890s.B. davidii was accorded the RHS Award of Merit (AM) in 1898, and the Award of Garden Merit (AGM) in 1941.\",\n" +
                            "                            \"citation\": \"https://en.wikipedia.org/wiki/Buddleja_davidii\",\n" +
                            "                            \"license_name\": \"CC BY-SA 3.0\",\n" +
                            "                            \"license_url\": \"https://creativecommons.org/licenses/by-sa/3.0/\"\n" +
                            "                        },\n" +
                            "                        \"synonyms\": [\n" +
                            "                            \"Buddleia davidii\",\n" +
                            "                            \"Buddleja davidii subsp. glabrescens\",\n" +
                            "                            \"Buddleja davidii subsp. nanhoensis\",\n" +
                            "                            \"Buddleja davidii subsp. veitchiana\",\n" +
                            "                            \"Buddleja davidii var. alba\",\n" +
                            "                            \"Buddleja davidii var. glabrescens\",\n" +
                            "                            \"Buddleja davidii var. magnifera\",\n" +
                            "                            \"Buddleja davidii var. magnifica\",\n" +
                            "                            \"Buddleja davidii var. nanhoensis\",\n" +
                            "                            \"Buddleja davidii var. superba\",\n" +
                            "                            \"Buddleja davidii var. veitchiana\",\n" +
                            "                            \"Buddleja davidii var. wilsonii\",\n" +
                            "                            \"Buddleja delavayi var. tomentosa\",\n" +
                            "                            \"Buddleja heliophila var. adenophora\",\n" +
                            "                            \"Buddleja shaanxiensis\",\n" +
                            "                            \"Buddleja shimidzuana\",\n" +
                            "                            \"Buddleja striata\",\n" +
                            "                            \"Buddleja striata var. zhouquensis\",\n" +
                            "                            \"Buddleja variabilis\",\n" +
                            "                            \"Buddleja variabilis subsp. nanhoensis\",\n" +
                            "                            \"Buddleja variabilis subsp. prostrata\",\n" +
                            "                            \"Buddleja variabilis subsp. superba\",\n" +
                            "                            \"Buddleja variabilis subsp. veitchiana\",\n" +
                            "                            \"Buddleja variabilis subsp. wilsonii\",\n" +
                            "                            \"Buddleja variabilis var. magnifica\",\n" +
                            "                            \"Buddleja variabilis var. nanhoensis\",\n" +
                            "                            \"Buddleja variabilis var. prostrata\",\n" +
                            "                            \"Buddleja variabilis var. superba\",\n" +
                            "                            \"Buddleja variabilis var. veitchiana\",\n" +
                            "                            \"Buddleja variabilis var. wilsonii\",\n" +
                            "                            \"Buddleja veitchiana\"\n" +
                            "                        ],\n" +
                            "                        \"image\": {\n" +
                            "                            \"value\": \"https://plant-id.ams3.cdn.digitaloceanspaces.com/knowledge_base/wikidata/f2a/f2a2bb3653d76b454d65fad5943923f3f16932e3.jpg\",\n" +
                            "                            \"citation\": \"//commons.wikimedia.org/wiki/User:IKAl\",\n" +
                            "                            \"license_name\": \"CC BY-SA 2.5\",\n" +
                            "                            \"license_url\": \"https://creativecommons.org/licenses/by-sa/2.5/\"\n" +
                            "                        },\n" +
                            "                        \"edible_parts\": null,\n" +
                            "                        \"watering\": {\n" +
                            "                            \"max\": 2,\n" +
                            "                            \"min\": 2\n" +
                            "                        },\n" +
                            "                        \"best_light_condition\": \"This plant thrives in full sun, needing at least six hours of direct sunlight each day to perform its best. It can tolerate partial shade, but too much shade can result in fewer flowers and a leggy growth habit. Planting it in a sunny spot will encourage robust growth and abundant blooms, making it a standout in any garden.\",\n" +
                            "                        \"best_soil_type\": \"For optimal growth, this plant prefers well-drained soil that is moderately fertile. It can tolerate a range of soil types, including sandy, loamy, and clay soils, as long as there is good drainage. Adding organic matter like compost can improve soil fertility and structure, helping the plant to establish and thrive.\",\n" +
                            "                        \"common_uses\": \"Common uses for this plant include ornamental landscaping and wildlife gardens. It is often used as a focal point in garden beds, borders, and as a hedge. Its ability to attract butterflies and bees makes it a valuable addition to pollinator gardens. Additionally, its cut flowers can be used in floral arrangements, adding a splash of color and fragrance to indoor spaces.\",\n" +
                            "                        \"cultural_significance\": \"In various cultures, this plant is valued for its ornamental beauty and its ability to attract butterflies and other pollinators. It is often planted in butterfly gardens and is associated with themes of transformation and beauty. Its vibrant flowers and pleasant fragrance make it a popular choice in many gardens around the world.\",\n" +
                            "                        \"toxicity\": \"This plant is considered mildly toxic to both humans and animals if ingested. It can cause stomach upset and other gastrointestinal issues. While it is not highly toxic, it is best to keep it out of reach of pets and children to prevent accidental ingestion. Always wash hands after handling the plant to avoid any potential skin irritation.\",\n" +
                            "                        \"best_watering\": \"Watering this plant requires a balanced approach. It prefers well-drained soil and does not like to sit in water. Water it deeply but infrequently, allowing the soil to dry out between waterings. During the growing season, typically spring and summer, it may need more frequent watering, especially in hot, dry conditions. In contrast, reduce watering in the fall and winter when the plant is not actively growing.\",\n" +
                            "                        \"language\": \"en\",\n" +
                            "                        \"entity_id\": \"872243f84209c0c2\"\n" +
                            "                    }\n" +
                            "                },\n" +
                            "                {\n" +
                            "                    \"id\": \"3514ca9d9bfbba10\",\n" +
                            "                    \"name\": \"Buddleja japonica\",\n" +
                            "                    \"probability\": 0.0108,\n" +
                            "                    \"similar_images\": [\n" +
                            "                        {\n" +
                            "                            \"id\": \"23054bfca484d221f66f172d03242896f1ea9cdb\",\n" +
                            "                            \"url\": \"https://plant-id.ams3.cdn.digitaloceanspaces.com/similar_images/4/230/54bfca484d221f66f172d03242896f1ea9cdb.jpeg\",\n" +
                            "                            \"license_name\": \"CC BY-SA 4.0\",\n" +
                            "                            \"license_url\": \"https://creativecommons.org/licenses/by-sa/4.0/\",\n" +
                            "                            \"citation\": \"Valentina Diakovasiliou\",\n" +
                            "                            \"similarity\": 0.723,\n" +
                            "                            \"url_small\": \"https://plant-id.ams3.cdn.digitaloceanspaces.com/similar_images/4/230/54bfca484d221f66f172d03242896f1ea9cdb.small.jpeg\"\n" +
                            "                        },\n" +
                            "                        {\n" +
                            "                            \"id\": \"de6fb384640a6b498d452cd4da81f2cd52be41fe\",\n" +
                            "                            \"url\": \"https://plant-id.ams3.cdn.digitaloceanspaces.com/similar_images/4/de6/fb384640a6b498d452cd4da81f2cd52be41fe.jpeg\",\n" +
                            "                            \"license_name\": \"CC BY 4.0\",\n" +
                            "                            \"license_url\": \"https://creativecommons.org/licenses/by/4.0/\",\n" +
                            "                            \"citation\": \"joffrey calvel\",\n" +
                            "                            \"similarity\": 0.697,\n" +
                            "                            \"url_small\": \"https://plant-id.ams3.cdn.digitaloceanspaces.com/similar_images/4/de6/fb384640a6b498d452cd4da81f2cd52be41fe.small.jpeg\"\n" +
                            "                        }\n" +
                            "                    ],\n" +
                            "                    \"details\": {\n" +
                            "                        \"common_names\": null,\n" +
                            "                        \"taxonomy\": {\n" +
                            "                            \"class\": \"Magnoliopsida\",\n" +
                            "                            \"genus\": \"Buddleja\",\n" +
                            "                            \"order\": \"Lamiales\",\n" +
                            "                            \"family\": \"Scrophulariaceae\",\n" +
                            "                            \"phylum\": \"Tracheophyta\",\n" +
                            "                            \"kingdom\": \"Plantae\"\n" +
                            "                        },\n" +
                            "                        \"url\": \"https://en.wikipedia.org/wiki/Buddleja_japonica\",\n" +
                            "                        \"gbif_id\": 4055769,\n" +
                            "                        \"inaturalist_id\": 509187,\n" +
                            "                        \"rank\": \"species\",\n" +
                            "                        \"description\": {\n" +
                            "                            \"value\": \"Buddleja japonica is a deciduous shrub native to Honshu and Shikoku,  Japan, where it grows on mountain slopes amid scrub. The shrub was named and described by Hemsley in 1889, and introduced to Western cultivation in 1896.\",\n" +
                            "                            \"citation\": \"https://en.wikipedia.org/wiki/Buddleja_japonica\",\n" +
                            "                            \"license_name\": \"CC BY-SA 3.0\",\n" +
                            "                            \"license_url\": \"https://creativecommons.org/licenses/by-sa/3.0/\"\n" +
                            "                        },\n" +
                            "                        \"synonyms\": [\n" +
                            "                            \"Buddleja japonica f. albiflora\"\n" +
                            "                        ],\n" +
                            "                        \"image\": {\n" +
                            "                            \"value\": \"https://plant-id.ams3.cdn.digitaloceanspaces.com/knowledge_base/wikidata/0a1/0a119525a9eac1ff4b5a745aec74191088216d4a.jpg\",\n" +
                            "                            \"citation\": \"https://commons.wikimedia.org/wiki/File:Buddleja_japonica1.jpg\",\n" +
                            "                            \"license_name\": \"CC BY-SA 3.0\",\n" +
                            "                            \"license_url\": \"https://creativecommons.org/licenses/by-sa/3.0/\"\n" +
                            "                        },\n" +
                            "                        \"edible_parts\": null,\n" +
                            "                        \"watering\": null,\n" +
                            "                        \"best_light_condition\": \"This plant thrives in full sun to partial shade. It needs at least six hours of direct sunlight each day for optimal growth and flowering. If grown in partial shade, it may produce fewer flowers. However, it can tolerate some shade, especially in hotter climates where intense afternoon sun might be too harsh.\",\n" +
                            "                        \"best_soil_type\": \"Well-draining soil is essential for healthy growth. A mix of loamy soil with some sand or perlite works well to ensure proper drainage. The soil should be rich in organic matter to provide necessary nutrients. Avoid heavy clay soils that retain too much moisture, as this can lead to root problems.\",\n" +
                            "                        \"common_uses\": \"Common uses include planting in garden beds, borders, and as a part of mixed shrub plantings. It is also used in butterfly gardens to attract pollinators. The plant's flowers can be cut and used in floral arrangements. Additionally, it can serve as a decorative element in public parks and private gardens due to its attractive blooms.\",\n" +
                            "                        \"cultural_significance\": \"In some cultures, this plant is valued for its ornamental beauty and is often used in gardens and landscapes. It is sometimes associated with attracting butterflies and other pollinators, making it a popular choice for butterfly gardens. Its flowers are also used in traditional floral arrangements.\",\n" +
                            "                        \"toxicity\": \"This plant is generally considered non-toxic to both humans and animals. There are no known harmful effects if touched or ingested. However, it's always a good practice to supervise pets and children around any plant, as individual reactions can vary.\",\n" +
                            "                        \"best_watering\": \"Watering should be done regularly but not excessively. The soil should be kept moist, especially during the growing season. It's important to let the top inch of soil dry out between waterings to prevent root rot. During the winter months, reduce the frequency of watering as the plant's growth slows down.\",\n" +
                            "                        \"language\": \"en\",\n" +
                            "                        \"entity_id\": \"3514ca9d9bfbba10\"\n" +
                            "                    }\n" +
                            "                }\n" +
                            "            ]\n" +
                            "        }\n" +
                            "    },\n" +
                            "    \"status\": \"COMPLETED\",\n" +
                            "    \"sla_compliant_client\": false,\n" +
                            "    \"sla_compliant_system\": true,\n" +
                            "    \"created\": 1722845805.899943,\n" +
                            "    \"completed\": 1722845806.315829\n" +
                            "}",
                    Plant::class.java
                )
                apiResult.value = result
                onComplete()
                navController.navigate("APIRESULT")
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
