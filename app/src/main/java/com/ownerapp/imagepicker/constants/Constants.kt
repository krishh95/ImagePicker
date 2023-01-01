package com.ownerapp.imagepicker.constants

import java.io.File

object Constants {
    val URI="uri"
    val EXT="ext"
    val ERROR="error"

    enum class Extensions(val name1:String):java.io.Serializable{
        JPEG(".jpg"),
        PNG(".png"),
        BMP(".bmp"),
        PDF(".pdf"),
    }

    sealed class FileStates(){
        class FileSuccess(val file: File):FileStates()
        class FileError(val error:String):FileStates()
    }
}