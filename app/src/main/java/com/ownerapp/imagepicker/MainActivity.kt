package com.ownerapp.imagepicker

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.gson.Gson
import com.ownerapp.imagepicker.FileChooser.Companion.ERROR_CONST
import com.ownerapp.imagepicker.constants.Constants
import java.io.File

class MainActivity : AppCompatActivity() {
    lateinit var txtId: TextView
    lateinit var img: ImageView
    lateinit var btn: Button
    private val selection =registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
        Log.d(FileChooser.TAG, "imageSelection: "+  result.data)

        val fileChooserResponse=Gson().fromJson(
            result.data?.getStringExtra(FileChooser.OUTPUT),
            FileChooser.Companion.FileChooserResponse::class.java
        )

       if(fileChooserResponse.status == RESULT_OK) {
           txtId.text= File(fileChooserResponse.data).absolutePath
       }else{
           txtId.text=fileChooserResponse.error
       }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.file_chooser)
        txtId=findViewById(R.id.txt)
        img=findViewById(R.id.img)
        btn=findViewById(R.id.btn)
        btn.setOnClickListener{
            val intent=Intent(this,FileChooser::class.java)
            selection.launch(intent)
        }
    }



}