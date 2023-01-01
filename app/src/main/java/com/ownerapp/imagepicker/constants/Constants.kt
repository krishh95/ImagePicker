package com.ownerapp.imagepicker.constants

import com.google.gson.Gson

object Constants {
    val URI="uri"
    val OUTPUTTYPE="OutputType"
    val EXT="ext"
    val ERROR="error"


    open class Extensions protected constructor(val extension:String, val mimeType:String="image/*", val type:Type= Type.IMAGE){
        override fun toString(): String {
            return Gson().toJson(this)
        }
        companion object{
            enum class Type{
                DOC,IMAGE
            }
        }

    }

    class JPEG:Extensions(".jpg")
    class PNG:Extensions(".png")
    class BMP:Extensions(".bmp")
    class PDF:Extensions(".pdf","application/pdf", Companion.Type.DOC)

}