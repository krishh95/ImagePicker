package com.ownerapp.imagepicker

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import androidx.work.WorkInfo.State.*
import com.google.gson.Gson
import com.ownerapp.imagepicker.constants.Constants
import com.ownerapp.imagepicker.workerManagers.CacheFileSaver
import kotlinx.coroutines.*

/***
 * intentKeys:
 * OUTPUT_TYPE  of type
 * enum class OutputType{
        Base64,File
    }
 */
class FileChooser : AppCompatActivity() {
    lateinit var mimeType: Constants.Extensions

    companion object{
        val TAG=FileChooser::class.java.name
        val OUTPUT="OutPut"

        /***
         * status ==0 fetch Error only
         * else get data and other values
         */
        class FileChooserResponse(
            val status:Int= -888,
            val data:String,
            val isBase64:Boolean=false,
            val error:String
        ) {
            override fun toString(): String {
                return Gson().toJson(this)
            }
        }
    }

    private val selection =registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            Log.d(TAG, "imageSelection: "+  result.data)

        CoroutineScope(Dispatchers.IO).launch {
            val data= workDataOf(
                Constants.URI to (result.data?.data as Uri).toString(),
                Constants.EXT to  CacheFileSaver.getExtensionString(mimeType),
            )
            val resultData=CacheFileSaver.doWorkAsync(applicationContext,data).await()
            val intent=Intent()
            intent.putExtra(OUTPUT,resultData.toString())

               this@FileChooser.setResult(7,intent)
               this@FileChooser.finish()
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val alert=AlertDialog.Builder(this)
        .setPositiveButton(
            "Gallery"
        ) { dialog, value ->
            mimeType= Constants.JPEG()
            getContent(mimeType, selection)
        }
        .setNegativeButton(
            "PDF"
        ) { dialog, value ->
            mimeType= Constants.PDF()
            getContent(mimeType, selection)
        }
        .create()
        alert.setCancelable(false)
        alert.setOnDismissListener {
            it.dismiss()
        }
        alert.setOnCancelListener {
           it.dismiss()
        }
        alert.show()
    }

    private fun getContent(mime: Constants.Extensions, selection: ActivityResultLauncher<Intent>) {
        val intent = Intent()
        intent.type = mime.mimeType
        intent.action = Intent.ACTION_GET_CONTENT
        val txt=when(mime){
            is Constants.PDF->{ "Select PDF"}
            else->{ "Select Picture"}
        }
        selection.launch(Intent.createChooser(intent, txt))
    }
}