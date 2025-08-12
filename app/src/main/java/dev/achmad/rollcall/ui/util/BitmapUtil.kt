package dev.achmad.rollcall.ui.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dev.achmad.rollcall.core.di.util.inject
import dev.achmad.rollcall.core.network.GET
import dev.achmad.rollcall.core.network.NetworkHelper
import dev.achmad.rollcall.core.network.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

suspend fun fetchImageAsBitmap(url: String): Bitmap? = withContext(Dispatchers.IO) {
    try {
        val networkHelper = inject<NetworkHelper>()
        val request = GET(url)
        val response = networkHelper.client.newCall(request).await()
        if (!response.isSuccessful) return@withContext null

        val inputStream = response.body.byteStream()
        BitmapFactory.decodeStream(inputStream)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}