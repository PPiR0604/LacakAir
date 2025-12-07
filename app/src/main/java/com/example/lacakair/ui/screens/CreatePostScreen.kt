package com.example.lacakair.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    initialImageUri: Uri?,
    onNavigateBack: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onPostCreated: () -> Unit,
    onCreatePost: (Uri, String, Double?, Double?, String?) -> Unit
) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf(initialImageUri) }
    var caption by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf("") }

    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var locationName by remember { mutableStateOf<String?>(null) }
    var isLocationEnabled by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            // Get location
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        latitude = it.latitude
                        longitude = it.longitude
                        isLocationEnabled = true

                        // Get location name using Geocoder
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                            if (addresses?.isNotEmpty() == true) {
                                val address = addresses[0]
                                // Build detailed location name
                                locationName = buildDetailedLocationName(address)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            locationName = "Lokasi"
                        }
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buat Postingan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            imageUri?.let { uri ->
                                isUploading = true
                                uploadProgress = "Memproses gambar..."
                                val lat = if (isLocationEnabled) latitude else null
                                val lon = if (isLocationEnabled) longitude else null
                                val locName = if (isLocationEnabled) locationName else null
                                onCreatePost(uri, caption, lat, lon, locName)
                            }
                        },
                        enabled = imageUri != null && !isUploading
                    ) {
                        Text(
                            "Posting",
                            color = if (imageUri != null && !isUploading)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                        )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToCamera,
                        modifier = Modifier.weight(1f),
                        enabled = !isUploading
                    ) {
                        Column( horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = com.example.lacakair.R.drawable.photo_camera_interface_symbol_for_button), modifier = Modifier.size(56.dp),
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(0.dp))
                            Text("Ambil Foto")
                        }
                    }

                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        enabled = !isUploading
                    ) {
                        Column( horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = com.example.lacakair.R.drawable.image), modifier = Modifier.size(56.dp),
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(0.dp))
                            Text("Dari Galeri")
                        }
                    }
                }
            } else {
                // No image selected
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(com.example.lacakair.R.drawable.image),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Pilih foto untuk postingan",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNavigateToCamera,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column( horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = com.example.lacakair.R.drawable.photo_camera_interface_symbol_for_button), modifier = Modifier.size(56.dp),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(0.dp))
                        Text("Ambil Foto")
                        }
                    }

                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column( horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = com.example.lacakair.R.drawable.image), modifier = Modifier.size(56.dp),
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(0.dp))
                            Text("Dari Galeri")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                label = { Text("Tulis caption...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                maxLines = 5,
                enabled = !isUploading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Location toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isLocationEnabled)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = if (isLocationEnabled)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isLocationEnabled) "Lokasi Ditambahkan" else "Tambahkan Lokasi",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            if (isLocationEnabled && locationName != null) {
                                Text(
                                    text = locationName!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Switch(
                        checked = isLocationEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                // Check permission
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                ) {
                                    // Permission already granted, get location
                                    try {
                                        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                            location?.let {
                                                latitude = it.latitude
                                                longitude = it.longitude
                                                isLocationEnabled = true

                                                try {
                                                    val geocoder = Geocoder(context, Locale.getDefault())
                                                    val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                                                    if (addresses?.isNotEmpty() == true) {
                                                        val address = addresses[0]
                                                        // Build detailed location name
                                                        locationName = buildDetailedLocationName(address)
                                                    }
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                    locationName = "Lokasi"
                                                }
                                            }
                                        }
                                    } catch (e: SecurityException) {
                                        e.printStackTrace()
                                    }
                                } else {
                                    // Request permission
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            } else {
                                isLocationEnabled = false
                                latitude = null
                                longitude = null
                                locationName = null
                            }
                        },
                        enabled = !isUploading
                    )
                }
            }

            if (isUploading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
                if (uploadProgress.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uploadProgress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

// Helper function to build a detailed location name
private fun buildDetailedLocationName(address: android.location.Address): String {
    val parts = mutableListOf<String>()

    // Prioritize more specific address components first
    // 1. Street name or premise (building/landmark)
    if (!address.featureName.isNullOrBlank() && address.featureName != address.subLocality) {
        parts.add(address.featureName!!)
    }

    // 2. Thoroughfare (street name)
    if (!address.thoroughfare.isNullOrBlank()) {
        parts.add(address.thoroughfare!!)
    }

    // 3. Sub-locality (neighborhood/area)
    if (!address.subLocality.isNullOrBlank()) {
        parts.add(address.subLocality!!)
    }

    // 4. Locality (city/town)
    if (!address.locality.isNullOrBlank()) {
        parts.add(address.locality!!)
    }

    // If we have nothing specific, fall back to admin area
    if (parts.isEmpty() && !address.adminArea.isNullOrBlank()) {
        parts.add(address.adminArea!!)
    }

    // Return formatted string (limit to 3 most specific parts for readability)
    return if (parts.isNotEmpty()) {
        parts.take(3).joinToString(", ")
    } else {
        "Lokasi Tidak Diketahui"
    }
}
