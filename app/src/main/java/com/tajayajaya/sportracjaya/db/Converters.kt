package com.tajayajaya.sportracjaya.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class Converters {
    @TypeConverter
    fun toBitmap(byteArray: ByteArray):Bitmap{
        return BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
    }

    @TypeConverter
    fun fromBitmap(bmp: Bitmap): ByteArray{
        val outputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG,100,outputStream)
        return outputStream.toByteArray()
    }
}
/*
it what will it be doing is
Run will take bmp and convert it into byteArray and byteArray will save into db

     **Conversion**
Bitmap => convert => byte array (8 bits = 1 byte) => save to db
getfromdb => byteArray => convert to bmp => bitmap

we had to tell the database it's TypeConverter function so that and then room will automatically look up
according to the annotations
 */