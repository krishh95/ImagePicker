package com.ownerapp.imagepicker.workerManagers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.work.Data
import com.google.gson.Gson
import com.ownerapp.imagepicker.FileChooser
import com.ownerapp.imagepicker.constants.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object CacheFileSaver {
    val TAG = CacheFileSaver::class.java.simpleName

    fun getExtensionString(value: Constants.Extensions): String {
        return value.toString()
    }

    fun doWorkAsync(
        context: Context,
        workerParams: Data
    ) = CoroutineScope(Dispatchers.IO).async {
        try {
            val givenUri = workerParams.getString(Constants.URI)?.toUri()
                ?: throw java.lang.RuntimeException("URI of Image Required")
            val givenExtension = workerParams.getString(Constants.EXT)
                ?: throw java.lang.RuntimeException("Extension of File Required")

            val extensionType = Gson().fromJson(givenExtension, Constants.Extensions::class.java)
            val inputStream = context.contentResolver.openInputStream(givenUri)
            val bytes = when (extensionType.type) {
                Constants.Extensions.Companion.Type.DOC -> {
                    inputStream?.readBytes()
                }
                Constants.Extensions.Companion.Type.IMAGE -> {
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    stream.toByteArray()
                }
            }

            if (bytes == null || bytes.isEmpty()) {
                throw java.lang.RuntimeException("bytes==null")
            }

            val fileName = UUID.randomUUID().toString().replace("-", "").substring(0, 15)
            val fileToSave = File(context.cacheDir, "${fileName}${extensionType.extension}")

            val outputStream = FileOutputStream(fileToSave)
            outputStream.write(bytes)
            inputStream?.close()
            outputStream.flush()
            outputStream.close()
            FileChooser.Companion.FileChooserResponse(
                status = AppCompatActivity.RESULT_OK,
                data = fileToSave.toUri().toString(),
                isBase64 = false,
                error = ""
            )
        } catch (e: java.lang.Exception) {
            FileChooser.Companion.FileChooserResponse(
                data = "",
                error = e.localizedMessage ?: "Internal Error"
            )
        }
    }


}