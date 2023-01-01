package com.ownerapp.imagepicker

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.work.*
import androidx.work.WorkInfo.State.*
import com.ownerapp.imagepicker.constants.Constants
import com.ownerapp.imagepicker.workerManagers.cacheFileSaver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    companion object{
        val TAG=MainActivity::class.java.name
    }
    val imageSelection =registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
        if(result.resultCode== RESULT_OK){
            Log.d(TAG, "imageSelection: "+  result.data)
            getFile(result.data?.data as Uri, Constants.Extensions.JPEG) {
                when(it){
                    is Constants.FileStates.FileSuccess ->{
                        CoroutineScope(Dispatchers.Main).launch {
                            img.setImageURI(it.file.toUri())
                            Log.d(TAG, "imageSelection:it "+it.file.path)
                        }
                    }
                    is Constants.FileStates.FileError ->{
                        CoroutineScope(Dispatchers.Main).launch {
                            txtId.text=it.error
                        }
                    }
                    else ->{}
                }
            }

        }
    }
    val pdfSelection =registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
        if(result.resultCode== RESULT_OK){
            Log.d(TAG, "pdfSelection: it.data "+ result.data)
            getFile(result.data?.data as Uri, Constants.Extensions.PDF) {
                when(it){
                    is Constants.FileStates.FileSuccess ->{
                        CoroutineScope(Dispatchers.Main).launch {
                            img.setImageDrawable(resources.getDrawable(R.drawable.ic_launcher_background))
                            Log.d(TAG, "pdfSelection:it "+it.file.path)
                        }
                    }
                    is Constants.FileStates.FileError ->{
                        CoroutineScope(Dispatchers.Main).launch {
                            txtId.text=it.error
                        }
                    }
                    else ->{}
                }
            }
        }
    }
     lateinit var txtId:TextView
     lateinit var img:ImageView
     lateinit var btn:Button
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtId=findViewById<TextView>(R.id.txt)
        img=findViewById<ImageView>(R.id.img)
        btn=findViewById<Button>(R.id.btn)
        btn.setOnClickListener{
            AlertDialog.Builder(this).setPositiveButton(
                "galleryImage"
            ) { dialog, value ->
                getContent("image/*", imageSelection)

            }.setNegativeButton(
                "PDF"
            ) { dialog, value ->
                getContent("application/pdf", pdfSelection)
            }.create().show()
        }


    }

    private fun  getFile(uri: Uri, ext: Constants.Extensions, action:(Constants.FileStates)->Unit){
            val otmRequest= OneTimeWorkRequestBuilder<cacheFileSaver>()
                .setInputData(
                    workDataOf(
                        Constants.URI to uri.toString(),
                        Constants.EXT to ext.name
                    )
                ).build()
            val worker=WorkManager.getInstance(applicationContext)
            val workInfos=worker.getWorkInfoByIdLiveData(otmRequest.id)
            workInfos.observe(this@MainActivity){
                when(it.state){
                    ENQUEUED -> {}
                    RUNNING -> {}
                    SUCCEEDED ->{
                        val file=it.outputData.getString(Constants.URI)?.toUri()?.toFile() ?: throw java.lang.RuntimeException("")
                        action(Constants.FileStates.FileSuccess(file))
                    }
                    FAILED -> {
                        val error=it.outputData.getString(Constants.ERROR)?:"Internal Error"
                        action(Constants.FileStates.FileError(error))
                    }
                    BLOCKED -> {}
                    CANCELLED -> {}
                }
            }
        worker.enqueue(otmRequest)
    }

    private fun getContent(mime: String, selection: ActivityResultLauncher<Intent>) {
        val i = Intent()
        i.type = mime //"image/*"
        i.action = Intent.ACTION_GET_CONTENT
        val txt=when(pdfSelection){
            imageSelection->{ "Select Picture"}
            pdfSelection->{ "Select PDF"}
            else ->{ "Select .."}
        }
        selection.launch(Intent.createChooser(i, txt))
        //startActivityForResult(Intent.createChooser(i, txt), id);

    }
}