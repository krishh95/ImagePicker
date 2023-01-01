package com.ownerapp.imagepicker.workerManagers

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.ownerapp.imagepicker.FileChooser
import com.ownerapp.imagepicker.constants.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object CacheFileSaver {
    val TAG = CacheFileSaver::class.java.simpleName

    fun<T> doWorkAsync(
        resolver: ContentResolver,
        cacheDir: File,
        inputUri: T,
        mimeType: Constants.Extensions
    ) = CoroutineScope(Dispatchers.IO).async {
        var inputStream:InputStream?=null
        try {
            val bytesArray= when (inputUri) {
                is String -> {

                    val givenUri = inputUri.toUri()
                    inputStream=resolver.openInputStream(givenUri)
                    when (mimeType.type) {
                        Constants.Extensions.Companion.Type.DOC -> {
                            inputStream?.readBytes()
                        }
                        Constants.Extensions.Companion.Type.IMAGE -> {
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            val stream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                            stream.toByteArray()
                        }
                        else->{null}
                    }

                }
                is Bitmap -> {

                    val stream = ByteArrayOutputStream()
                    inputUri.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    stream.toByteArray()
                }
                else -> {
                    null
                }

            }

            if (bytesArray == null || bytesArray.isEmpty()) {
                throw java.lang.RuntimeException("bytes==null")
            }

            val fileName = UUID.randomUUID().toString().replace("-", "").substring(0, 15)
            val fileToSave = File(cacheDir, "${fileName}${mimeType.extension}")

            val outputStream = FileOutputStream(fileToSave)
            outputStream.write(bytesArray)
            inputStream?.close()
            outputStream.flush()
            outputStream.close()
            FileChooser.Companion.FileChooserResponse(
                status = AppCompatActivity.RESULT_OK,
                data = fileToSave.toUri().toString(),
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