package com.example.lacakair.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object ImageUploader {
    // ImgBB API Key - Free, bisa daftar di https://api.imgbb.com/
    private const val IMGBB_API_KEY = "115adb898f67d4147a56ef82d097976f" // Ganti dengan key Anda
    private const val IMGBB_UPLOAD_URL = "https://api.imgbb.com/1/upload"

    // Maksimal ukuran gambar untuk upload cepat
    private const val MAX_IMAGE_SIZE = 800 // Dikurangi dari 1024 ke 800px untuk lebih cepat
    private const val JPEG_QUALITY = 60 // Dikurangi dari 80 ke 60 untuk ukuran lebih kecil
    private const val CONNECTION_TIMEOUT = 30000 // 30 detik
    private const val READ_TIMEOUT = 30000 // 30 detik

    suspend fun uploadImage(context: Context, imageUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d("ImageUploader", "Starting upload for: $imageUri")

            // Baca dan kompres gambar dengan ukuran lebih kecil
            val bitmap = loadAndCompressBitmap(context, imageUri)
            Log.d("ImageUploader", "Bitmap size: ${bitmap.width}x${bitmap.height}")

            // Convert to Base64 (tanpa newline untuk upload HTTP)
            val base64Image = bitmapToBase64(bitmap)
            Log.d("ImageUploader", "Base64 length: ${base64Image.length}")

            // Upload ke ImgBB
            val url = URL("$IMGBB_UPLOAD_URL?key=$IMGBB_API_KEY")
            val connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "POST"
                doOutput = true
                doInput = true
                connectTimeout = CONNECTION_TIMEOUT
                readTimeout = READ_TIMEOUT
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                setRequestProperty("Accept", "application/json")
            }

            // Encode data dengan proper URL encoding
            val postData = "image=${URLEncoder.encode(base64Image, "UTF-8")}"

            Log.d("ImageUploader", "Sending request...")
            connection.outputStream.use {
                it.write(postData.toByteArray(Charsets.UTF_8))
                it.flush()
            }

            // Baca response
            val responseCode = connection.responseCode
            Log.d("ImageUploader", "Response code: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d("ImageUploader", "Response: $response")

                val jsonResponse = JSONObject(response)

                if (jsonResponse.getBoolean("success")) {
                    val imageUrl = jsonResponse.getJSONObject("data").getString("url")
                    Log.d("ImageUploader", "Upload success: $imageUrl")
                    Result.success(imageUrl)
                } else {
                    val error = jsonResponse.optJSONObject("error")?.toString() ?: "Upload failed"
                    Log.e("ImageUploader", "Upload failed: $error")
                    Result.failure(Exception("Upload gagal: $error"))
                }
            } else {
                val error = try {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP Error $responseCode"
                } catch (e: Exception) {
                    "HTTP Error $responseCode"
                }
                Log.e("ImageUploader", "HTTP Error: $error")
                Result.failure(Exception("HTTP Error $responseCode: $error"))
            }
        } catch (e: Exception) {
            Log.e("ImageUploader", "Upload exception", e)
            Result.failure(Exception("Upload gagal: ${e.message}"))
        }
    }

    private fun loadAndCompressBitmap(context: Context, uri: Uri): Bitmap {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)

        // Decode dengan sample size untuk memori efisien
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream?.close()

        // Hitung sample size
        options.inSampleSize = calculateInSampleSize(options, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
        options.inJustDecodeBounds = false

        // Decode dengan sample size
        val inputStream2 = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream2, null, options)
        inputStream2?.close()

        if (bitmap == null) {
            throw Exception("Gagal membaca gambar")
        }

        // Kompres lebih lanjut jika masih terlalu besar
        val ratio = Math.min(
            MAX_IMAGE_SIZE.toFloat() / bitmap.width,
            MAX_IMAGE_SIZE.toFloat() / bitmap.height
        )

        return if (ratio < 1) {
            val newWidth = (bitmap.width * ratio).toInt()
            val newHeight = (bitmap.height * ratio).toInt()
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            if (scaledBitmap != bitmap) {
                bitmap.recycle() // Bebaskan memori
            }
            scaledBitmap
        } else {
            bitmap
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        // Kompres dengan kualitas lebih rendah untuk ukuran lebih kecil
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        Log.d("ImageUploader", "Compressed image size: ${byteArray.size / 1024}KB")

        // Gunakan NO_WRAP untuk menghindari newline characters
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
