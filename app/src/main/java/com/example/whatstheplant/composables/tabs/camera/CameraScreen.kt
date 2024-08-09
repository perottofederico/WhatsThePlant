package com.example.whatstheplant.composables.tabs.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.app.PendingIntentCompat.getActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.whatstheplant.ui.theme.green
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

private val cameraPermissionRequestCode = 100

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen() {
    val cameraPermisssionState: PermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    MainContent(
        hasPermission = cameraPermisssionState.status.isGranted,
        onRequestPermission = cameraPermisssionState::launchPermissionRequest
    )
}


@Composable
private fun MainContent(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
){
    if (hasPermission){
        CameraContent()
    } else {
        NoPermissionScreen(onRequestPermission)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun CameraContent(
) {
    WindowInsetsCompat.Type.navigationBars()
    val viewModel: CameraViewModel = viewModel()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }
    val scaffoldState = rememberBottomSheetScaffoldState()
    val bitmaps by viewModel.bitmaps.collectAsState()
    val scope = rememberCoroutineScope()

    val result = remember {
        mutableStateOf<Bitmap?>(null)
    }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicturePreview()) {
         result.value = it
    }
    Button(onClick = { launcher.launch() }) {
        Text(text = "Take a picture")
    }

    result.value?.let { image ->
        Image(image.asImageBitmap(), null, modifier = Modifier.fillMaxWidth())
    }

    /*
    BottomSheetScaffold (
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            PhotoBottomSheetContent(
                bitmaps = bitmaps,
                modifier = Modifier.fillMaxWidth()
            )
        }
        ){ paddingValues ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            factory = {context ->
                PreviewView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    setBackgroundColor(Color.BLACK)
                    scaleType = PreviewView.ScaleType.FILL_START
                }.also{ previewView ->
                    previewView.controller = cameraController
                    cameraController.bindToLifecycle(lifecycleOwner)
                }
            }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ){
            IconButton(
                onClick = {
                    cameraController.cameraSelector =
                        if(cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else CameraSelector.DEFAULT_BACK_CAMERA
                }, modifier = Modifier.offset(16.dp, 16.dp)
            ) {
                Icon(Icons.Default.Cameraswitch, contentDescription = "Switch Camera")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(76.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ){
                IconButton(
                    onClick = {

                    }) {
                    Icon(
                        imageVector = Icons.Default.Photo,
                        contentDescription = "Open Gallery"
                    )
                }

                IconButton(
                    onClick = {
                        takePhoto(
                            controller = cameraController,
                            onPhotoTaken = viewModel::onTakePhoto,
                            context = context
                            )
                    }) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Take Picture"
                    )
                }

                IconButton(
                    onClick = {
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    }) {
                    Icon(
                        imageVector = Icons.Default.Photo,
                        contentDescription = "Open Gallery"
                    )
                }
            }
        }
    }
     */
}

private fun takePhoto(
    context: Context,
    controller: LifecycleCameraController,
    onPhotoTaken: (Bitmap) -> Unit
) {
    controller.takePicture(
        ContextCompat.getMainExecutor(context),
        object : OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)

                val matrix = Matrix().apply {
                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                }
                val rotatedBitmap = Bitmap.createBitmap(
                    image.toBitmap(),
                    0,
                    0,
                    image.width,
                    image.height,
                    matrix,
                    true
                )
                onPhotoTaken(rotatedBitmap)
                }
            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e("CAMERA", "Error taking picture: ", exception)
            }
        }
    )
}

@Composable
fun NoPermissionScreen(
    onRequestPermission: () -> Unit
){
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text(
            text = "The app needs to have camera permission to function properly",
            fontSize = 25.sp,
            color = green
            )

        Button(onClick = onRequestPermission, colors = ButtonDefaults.buttonColors(green)) {
            Icon(imageVector = Icons.Default.Camera, contentDescription = "Camera")
            Text(text = "Grant Permission")
        }
    }
}