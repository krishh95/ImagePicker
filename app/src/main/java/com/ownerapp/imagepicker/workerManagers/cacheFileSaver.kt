package com.ownerapp.imagepicker.workerManagers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ownerapp.imagepicker.constants.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class cacheFileSaver(
        val context: Context,
        val workerParams: WorkerParameters) :
    CoroutineWorker(
    context,
    workerParams
) {
    companion object{
        val TAG=cacheFileSaver::class.java.simpleName
    }
    override suspend fun doWork(): Result {
       val inputUri= workerParams.inputData.getString(Constants.URI)?.toUri() ?: throw java.lang.RuntimeException("")
       val extension= workerParams.inputData.getString(Constants.EXT) ?:throw java.lang.RuntimeException("")
        val ext=Constants.Extensions.valueOf(extension)
        val inputStream=context.contentResolver.openInputStream(inputUri)
            val bytes=when(ext){
                Constants.Extensions.PDF->{
                    inputStream?.readBytes()
                }
                else->{
                    val bitmap= BitmapFactory.decodeStream(inputStream)
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    stream.toByteArray()
                }
            }

        val fileName=UUID.randomUUID().toString().replace("-","").substring(0,15)
        val fileToSave=File(context.cacheDir,"${fileName}${ext.name1}")
        return try {
            val outputStream = FileOutputStream(fileToSave)
            outputStream.write( bytes)
            inputStream?.close()
            outputStream.flush()
            outputStream.close()
            Result.success(
                workDataOf(
                    Constants.URI to fileToSave.toUri().toString()
                )
            )
        }catch (e:java.lang.Exception){
            Result.failure(
                workDataOf(
                    Constants.ERROR to e.localizedMessage
                )
            )
        }
    }

}