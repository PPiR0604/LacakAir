package com.example.lacakair.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.lacakair.data.Post
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    posts: List<Post> = emptyList(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var selectedPosts by remember { mutableStateOf<List<Post>>(emptyList()) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Peta Lokasi") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasLocationPermission) {
                OpenStreetMapView(
                    context = context,
                    posts = posts,
                    onMarkerClick = { postsAtLocation ->
                        selectedPosts = postsAtLocation
                    }
                )
            } else {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Izinkan akses lokasi untuk melihat peta",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }) {
                        Text("Berikan Izin")
                    }
                }
            }
        }
    }

    // Dialog untuk menampilkan detail post dengan slider
    if (selectedPosts.isNotEmpty()) {
        PostCarouselDialog(
            posts = selectedPosts,
            onDismiss = { selectedPosts = emptyList() }
        )
    }
}

@Composable
fun OpenStreetMapView(
    context: Context,
    posts: List<Post>,
    onMarkerClick: (List<Post>) -> Unit
) {
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }

    // Group posts by location (with tolerance for nearby posts)
    fun groupPostsByLocation(posts: List<Post>): Map<String, List<Post>> {
        val grouped = mutableMapOf<String, MutableList<Post>>()
        val tolerance = 0.0001 // Approximately 10 meters

        posts.filter { it.hasLocation() }.forEach { post ->
            var foundGroup = false

            for ((key, group) in grouped) {
                val firstPost = group.first()
                val latDiff = abs(firstPost.latitude!! - post.latitude!!)
                val lonDiff = abs(firstPost.longitude!! - post.longitude!!)

                if (latDiff < tolerance && lonDiff < tolerance) {
                    group.add(post)
                    foundGroup = true
                    break
                }
            }

            if (!foundGroup) {
                val key = "${post.latitude}_${post.longitude}"
                grouped[key] = mutableListOf(post)
            }
        }

        return grouped
    }

    // Log untuk debugging
    LaunchedEffect(posts) {
        val postsWithLocation = posts.filter { it.hasLocation() }
        val groupedPosts = groupPostsByLocation(posts)
        android.util.Log.d("MapScreen", "Total posts: ${posts.size}")
        android.util.Log.d("MapScreen", "Posts with location: ${postsWithLocation.size}")
        android.util.Log.d("MapScreen", "Grouped locations: ${groupedPosts.size}")
        groupedPosts.forEach { (key, groupPosts) ->
            android.util.Log.d("MapScreen", "Location $key has ${groupPosts.size} posts")
        }
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Update markers when posts change
    LaunchedEffect(posts) {
        mapView?.let { map ->
            // Clear old overlays except current location marker
            val currentLocationMarker = map.overlays.firstOrNull()
            map.overlays.clear()
            if (currentLocationMarker != null) {
                map.overlays.add(currentLocationMarker)
            }

            // Group posts by location and add markers
            val groupedPosts = groupPostsByLocation(posts)

            groupedPosts.forEach { (_, postsAtLocation) ->
                val firstPost = postsAtLocation.first()
                val marker = Marker(map)
                marker.icon = resizeDrawable(context, com.example.lacakair.R.drawable.placeholder, 80, 80)
                marker.position = GeoPoint(firstPost.latitude!!, firstPost.longitude!!)
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                // Set title to show number of posts if more than one
                if (postsAtLocation.size > 1) {
                    marker.title = "${postsAtLocation.size} postingan"
                    marker.snippet = "Klik untuk melihat semua"
                } else {
                    marker.title = firstPost.userName
                    marker.snippet = firstPost.caption
                }

                marker.setOnMarkerClickListener { _, _ ->
                    onMarkerClick(postsAtLocation)
                    true
                }
                map.overlays.add(marker)
            }

            map.invalidate()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapView?.onDetach()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)

                    // Set default location (Jakarta, Indonesia)
                    val defaultLocation = GeoPoint(-6.2088, 106.8456)
                    controller.setZoom(12.0)
                    controller.setCenter(defaultLocation)

                    mapView = this

                    // Try to get current location
                    try {
                        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            location?.let {
                                val geoPoint = GeoPoint(it.latitude, it.longitude)
                                currentLocation = geoPoint
                                controller.setCenter(geoPoint)

                                // Add marker at current location
                                val marker = Marker(this)
                                marker.icon = resizeDrawable(context, com.example.lacakair.R.drawable.location, 80, 80)
                                marker.position = geoPoint
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                marker.title = "Lokasi Anda"
                                marker.snippet = "Anda berada di sini"
                                overlays.add(0, marker)
                                invalidate()
                            }
                        }
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.onResume()
            }
        )

        // Info badge showing number of posts
        if (posts.isNotEmpty()) {
            val postsWithLocation = posts.count { it.hasLocation() }
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "$postsWithLocation lokasi ditampilkan",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Floating action button to center on current location
        FloatingActionButton(
            onClick = {
                currentLocation?.let { location ->
                    mapView?.controller?.animateTo(location)
                } ?: run {
                    try {
                        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            location?.let {
                                val geoPoint = GeoPoint(it.latitude, it.longitude)
                                currentLocation = geoPoint
                                mapView?.controller?.animateTo(geoPoint)
                            }
                        }
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp).size(64.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                modifier = Modifier.padding(16.dp),
                painter = painterResource(com.example.lacakair.R.drawable.maps_and_flags),
                contentDescription = "Lokasi Saya",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
fun resizeDrawable(context: Context, drawableId: Int, width: Int, height: Int): android.graphics.drawable.Drawable? {
    val drawable = ContextCompat.getDrawable(context, drawableId)
    val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    drawable?.setBounds(0, 0, canvas.width, canvas.height)
    drawable?.draw(canvas)
    return android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
}

@Composable
fun PostCarouselDialog(
    posts: List<Post>,
    onDismiss: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    val currentPost = posts[currentIndex]

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header dengan nama user
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentPost.userName.firstOrNull()?.uppercase() ?: "?",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = currentPost.userName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (currentPost.locationName != null) {
                                Text(
                                    text = currentPost.locationName,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Counter badge jika ada multiple posts
                    if (posts.size > 1) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${currentIndex + 1}/${posts.size}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Image with navigation buttons
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = currentPost.imageUrl,
                        contentDescription = "Post image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    // Navigation buttons for multiple posts
                    if (posts.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Previous button
                            if (currentIndex > 0) {
                                IconButton(
                                    onClick = { currentIndex-- },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            Color.Black.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Previous",
                                        tint = Color.White
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(40.dp))
                            }

                            // Next button
                            if (currentIndex < posts.size - 1) {
                                IconButton(
                                    onClick = { currentIndex++ },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            Color.Black.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Next",
                                        tint = Color.White
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(40.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Caption
                if (currentPost.caption.isNotEmpty()) {
                    Text(
                        text = currentPost.caption,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Likes
                Text(
                    text = "${currentPost.likes.size} suka",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Tutup")
                }
            }
        }
    }
}
