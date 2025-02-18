package com.codewithfk.chatter.feature.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.impl.utils.MatrixExt.postRotate
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.codewithfk.chatter.AppID
import com.codewithfk.chatter.AppSign
import com.codewithfk.chatter.MainActivity
import com.codewithfk.chatter.R
import com.codewithfk.chatter.feature.chat.CallButton
import com.codewithfk.chatter.model.Channel
import com.codewithfk.chatter.ui.theme.DarkGrey
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.codewithfk.chatter.camera.CameraPreview
import com.codewithfk.chatter.camera.MainViewModel
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.io.IOException
import android.Manifest
import android.content.ContentValues
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.MediaStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current as MainActivity
    LaunchedEffect(Unit) {
        Firebase.auth.currentUser?.let {
            context.initZegoService(
                appID = AppID,
                appSign = AppSign,
                userID = it.email!!,
                userName = it.email!!
            )
        }
    }

    val viewModel = hiltViewModel<HomeViewModel>()
    val channels = viewModel.channels.collectAsState()
    val addChannel = remember { mutableStateOf(false) }
    val selectedTab = remember { mutableStateOf(0) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black,
                contentColor = Color.White
            ) {
                listOf(
                    Icons.Default.Home to "Chats",
                    Icons.Default.Call to "Calls",
                    Icons.Default.Face to "Camera",
                    Icons.Default.Settings to "Settings"
                ).forEachIndexed { index, (icon, label) ->
                    NavigationBarItem(
                        selected = selectedTab.value == index,
                        onClick = { selectedTab.value = index },
                        icon = { Icon(imageVector = icon, contentDescription = label) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color.Gray,
                            selectedTextColor = Color.White,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFFE5F6FF)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab.value == 0) { // Show FAB only in Chats tab
                Box(
                    modifier = Modifier
                        .padding(14.dp)
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE5F6FF))
                        .clickable { addChannel.value = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (selectedTab.value) {
                0 -> ChatsTab(channels.value, navController, viewModel) // Pass viewModel
                1 -> CallsTab()
                2 -> CameraTab(MainViewModel())
                3 -> SettingsTab(navController)
            }
        }

        if (addChannel.value) {
            ModalBottomSheet(
                onDismissRequest = { addChannel.value = false },
                sheetState = sheetState,
                containerColor = DarkGrey
            ) {
                AddChannelDialog { channelName ->
                    viewModel.addChannel(channelName)
                    addChannel.value = false
                }
            }
        }
    }
}

@Composable
fun ChatsTab(channels: List<Channel>, navController: NavController, viewModel: HomeViewModel) {
    var isSearchExpanded by remember { mutableStateOf(false) } // Moved inside ChatsTab
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) } // Moved inside ChatsTab

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(top = 30.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Chats",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Button(onClick = { isSearchExpanded = !isSearchExpanded }) {
                Text(text = if (isSearchExpanded) "❌" else "🔍")
            }
            IconButton(onClick = { /* More options */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
        }

        // Search Input Field
        if (isSearchExpanded) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Chats") },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.padding(bottom = 45.dp))

        // Chat List inside a Card
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.elevatedCardElevation(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE5F6FF))
                    .padding(8.dp)
            ) {
                items(channels.filter {
                    it.name.contains(searchQuery.text, ignoreCase = true)
                }) { channel ->
                    ChannelItem(
                        channelName = channel.name,
                        modifier = Modifier.clickable {
                            navController.navigate("chat/${channel.id}&${channel.name}")
                        },
                        shouldShowCallButtons = false,
                        onClick = {},
                        onCall = {}
                    )
                }
            }
        }
    }
}


@Composable
fun CallsTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE5F6FF)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Calls History", color = Color.White)
    }
}

@Composable
fun CameraTab(viewModel: MainViewModel) {
    var hasPermission by remember { mutableStateOf(false) }

    if (!hasPermission) {
        RequestCameraPermission { hasPermission = true }
    }

    if (hasPermission) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        val controller = remember {
            LifecycleCameraController(context).apply {
                setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            }
        }

        val bitmaps by viewModel.bitmaps.collectAsState()

        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            CameraPreview(controller, Modifier.fillMaxSize())

            IconButton(
                onClick = { takePhoto(context, controller, viewModel) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 30.dp)
                    .size(64.dp)
                    .background(Color.White, shape = CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Face, contentDescription = "Capture", tint = Color.Black)
            }

            if (bitmaps.isNotEmpty()) {
                Image(
                    bitmap = bitmaps.last().asImageBitmap(),
                    contentDescription = "Last Captured Image",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                )
            }
        }
    }
}







@Composable
fun SettingsTab(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Settings",
            color = Color.White,
            style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = {
                Firebase.auth.signOut()
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Sign Out")
        }
    }
}

@Composable
fun ChannelItem(
    channelName: String,
    modifier: Modifier,
    shouldShowCallButtons: Boolean = false,
    onClick: () -> Unit,
    onCall: (ZegoSendCallInvitationButton) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color = Color(0xFFE5F6FF))
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clickable {
                    onClick()
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Color.Yellow.copy(alpha = 0.3f))

            ) {
                Text(
                    text = channelName[0].uppercase(),
                    color = Color.Black,
                    style = TextStyle(fontSize = 35.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }


            Text(
                text = channelName,
                modifier = Modifier.padding(8.dp),
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        if (shouldShowCallButtons) {
            Row(
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                CallButton(isVideoCall = true, onCall)
                CallButton(isVideoCall = false, onCall)
            }
        }
    }
}


@Composable
fun AddChannelDialog(onAddChannel: (String) -> Unit) {
    val channelName = remember {
        mutableStateOf("")
    }
    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Add Channel")
        Spacer(modifier = Modifier.padding(8.dp))
        TextField(value = channelName.value, onValueChange = {
            channelName.value = it
        }, label = { Text(text = "Channel Name") }, singleLine = true)
        Spacer(modifier = Modifier.padding(8.dp))
        Button(onClick = { onAddChannel(channelName.value) }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Add")
        }
    }
}
private fun takePhoto(context: Context, controller: LifecycleCameraController, viewModel: MainViewModel) {
    controller.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)

                val matrix = Matrix().apply {
                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                }
                val rotatedBitmap = Bitmap.createBitmap(
                    image.toBitmap(),
                    0, 0,
                    image.width, image.height,
                    matrix, true
                )

                viewModel.onTakePhoto(rotatedBitmap) // Store in ViewModel


                image.close()

                saveBitmapToGallery(context, rotatedBitmap)
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e("Camera", "Couldn't take photo: ", exception)
            }
        }
    )
}

fun saveBitmapToGallery(context: Context, bitmap: Bitmap) {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES) // Saves to Pictures folder
        put(MediaStore.Images.Media.IS_PENDING, 1) // For compatibility
    }

    val resolver = context.contentResolver
    val uri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        resolver.openOutputStream(it)?.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
        contentValues.clear()
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0) // Mark as complete
        resolver.update(uri, contentValues, null, null)
    }
}


@Composable
fun RequestCameraPermission(onPermissionGranted: () -> Unit) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            Toast.makeText(context, "Camera permission denied!", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }
}


