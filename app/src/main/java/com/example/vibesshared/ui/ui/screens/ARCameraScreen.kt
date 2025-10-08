package com.example.vibesshared.ui.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.vibesshared.ui.ui.ar.*
import com.example.vibesshared.ui.ui.theme.*
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ARCameraScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val arCameraService = remember { ARCameraService(context) }
    val scope = rememberCoroutineScope()
    
    var currentFilter by remember { mutableStateOf<ARFilter?>(null) }
    var current3DObject by remember { mutableStateOf<AR3DObject?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(true) }
    var show3DObjects by remember { mutableStateOf(false) }
    var captureMode by remember { mutableStateOf(CaptureMode.PHOTO) }
    
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    
    val gradientColors = listOf(
        ElectricPurple, NeonPink, VividBlue, SunsetOrange, LimeGreen
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(colors = gradientColors)
            )
    ) {
        // Camera Preview
        ARCameraPreview(
            modifier = Modifier.fillMaxSize(),
            currentFilter = currentFilter,
            current3DObject = current3DObject,
            cameraExecutor = cameraExecutor
        )
        
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    "AR Camera",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            ),
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(onClick = { 
                    showFilters = !showFilters
                    show3DObjects = false
                }) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = "Filters",
                        tint = if (showFilters) LimeGreen else Color.White
                    )
                }
                
                IconButton(onClick = { 
                    show3DObjects = !show3DObjects
                    showFilters = false
                }) {
                    Icon(
                        imageVector = Icons.Filled.ViewInAr,
                        contentDescription = "3D Objects",
                        tint = if (show3DObjects) LimeGreen else Color.White
                    )
                }
            }
        )
        
        // Capture Mode Selector
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CaptureModeButton(
                mode = CaptureMode.PHOTO,
                isSelected = captureMode == CaptureMode.PHOTO,
                onClick = { captureMode = CaptureMode.PHOTO },
                icon = Icons.Filled.CameraAlt
            )
            
            CaptureModeButton(
                mode = CaptureMode.VIDEO,
                isSelected = captureMode == CaptureMode.VIDEO,
                onClick = { captureMode = CaptureMode.VIDEO },
                icon = Icons.Filled.Videocam
            )
            
            CaptureModeButton(
                mode = CaptureMode.STORY,
                isSelected = captureMode == CaptureMode.STORY,
                onClick = { captureMode = CaptureMode.STORY },
                icon = Icons.Filled.AutoStories
            )
        }
        
        // Filters Panel
        AnimatedVisibility(
            visible = showFilters,
            enter = slideInVertically() + fadeIn()
        ) {
            FiltersPanel(
                filters = arCameraService.availableFilters,
                currentFilter = currentFilter,
                onFilterSelected = { currentFilter = it },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
        
        // 3D Objects Panel
        AnimatedVisibility(
            visible = show3DObjects,
            enter = slideInVertically() + fadeIn()
        ) {
            Objects3DPanel(
                objects = arCameraService.available3DObjects,
                currentObject = current3DObject,
                onObjectSelected = { current3DObject = it },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
        
        // Bottom Controls
        if (!showFilters && !show3DObjects) {
            BottomControls(
                captureMode = captureMode,
                isRecording = isRecording,
                onCaptureClick = {
                    scope.launch {
                        when (captureMode) {
                            CaptureMode.PHOTO -> {
                                // Capture photo
                            }
                            CaptureMode.VIDEO -> {
                                isRecording = !isRecording
                            }
                            CaptureMode.STORY -> {
                                // Start story recording
                            }
                        }
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

@Composable
fun ARCameraPreview(
    modifier: Modifier = Modifier,
    currentFilter: ARFilter?,
    current3DObject: AR3DObject?,
    cameraExecutor: ExecutorService
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        modifier = modifier,
        update = { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                
                val preview = Preview.Builder().build()
                val imageCapture = ImageCapture.Builder().build()
                
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                    
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                } catch (exc: Exception) {
                    // Handle camera binding error
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}

@Composable
fun CaptureModeButton(
    mode: CaptureMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier
            .size(48.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) VividBlue else Color.White.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = mode.name,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun FiltersPanel(
    filters: List<ARFilter>,
    currentFilter: ARFilter?,
    onFilterSelected: (ARFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "AR Filters",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // No Filter option
                    FilterItem(
                        filter = ARFilter("none", "Original", "No filter", FilterType.FACE_EFFECT, "📷"),
                        isSelected = currentFilter == null,
                        onClick = { onFilterSelected(ARFilter("none", "Original", "No filter", FilterType.FACE_EFFECT, "📷")) }
                    )
                }
                
                items(filters) { filter ->
                    FilterItem(
                        filter = filter,
                        isSelected = currentFilter?.id == filter.id,
                        onClick = { onFilterSelected(filter) }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterItem(
    filter: ARFilter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Card(
            modifier = Modifier
                .size(80.dp)
                .border(
                    width = if (isSelected) 3.dp else 0.dp,
                    color = if (isSelected) LimeGreen else Color.Transparent,
                    shape = CircleShape
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) VividBlue.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.2f)
            ),
            shape = CircleShape
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter.icon,
                    fontSize = 32.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = filter.name,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun Objects3DPanel(
    objects: List<AR3DObject>,
    currentObject: AR3DObject?,
    onObjectSelected: (AR3DObject) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "3D Objects",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // No Object option
                    Object3DItem(
                        object3D = AR3DObject("none", "None", "No 3D object", Object3DType.PARTICLE_EFFECT, "🚫"),
                        isSelected = currentObject == null,
                        onClick = { onObjectSelected(AR3DObject("none", "None", "No 3D object", Object3DType.PARTICLE_EFFECT, "🚫")) }
                    )
                }
                
                items(objects) { object3D ->
                    Object3DItem(
                        object3D = object3D,
                        isSelected = currentObject?.id == object3D.id,
                        onClick = { onObjectSelected(object3D) }
                    )
                }
            }
        }
    }
}

@Composable
fun Object3DItem(
    object3D: AR3DObject,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Card(
            modifier = Modifier
                .size(80.dp)
                .border(
                    width = if (isSelected) 3.dp else 0.dp,
                    color = if (isSelected) SunsetOrange else Color.Transparent,
                    shape = CircleShape
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) ElectricPurple.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.2f)
            ),
            shape = CircleShape
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = object3D.icon,
                    fontSize = 32.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = object3D.name,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun BottomControls(
    captureMode: CaptureMode,
    isRecording: Boolean,
    onCaptureClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gallery Button
        IconButton(
            onClick = { /* Open gallery */ }
        ) {
            Icon(
                imageVector = Icons.Filled.PhotoLibrary,
                contentDescription = "Gallery",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        
        // Capture Button
        Card(
            modifier = Modifier
                .size(80.dp)
                .clickable { onCaptureClick() },
            colors = CardDefaults.cardColors(
                containerColor = if (isRecording) Color.Red else Color.White
            ),
            shape = CircleShape
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isRecording) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = "Stop Recording",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Icon(
                        imageVector = when (captureMode) {
                            CaptureMode.PHOTO -> Icons.Filled.CameraAlt
                            CaptureMode.VIDEO -> Icons.Filled.Videocam
                            CaptureMode.STORY -> Icons.Filled.AutoStories
                        },
                        contentDescription = "Capture",
                        tint = when (captureMode) {
                            CaptureMode.PHOTO -> Color.Black
                            CaptureMode.VIDEO -> Color.Black
                            CaptureMode.STORY -> Color.Black
                        },
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        
        // Switch Camera Button
        IconButton(
            onClick = { /* Switch camera */ }
        ) {
            Icon(
                imageVector = Icons.Filled.Cameraswitch,
                contentDescription = "Switch Camera",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

enum class CaptureMode {
    PHOTO, VIDEO, STORY
}