package com.andrehaueisen.listadejanot.utilities

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import java.io.ByteArrayOutputStream

/**
 * Created by andre on 10/18/2017.
 */

fun Drawable.convertToByteArray(): ByteArray{
    val bitmap = this.convertToBitmap()
    val outputStream = ByteArrayOutputStream()
    bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

    return outputStream.toByteArray()
}

fun Drawable?.convertToBitmap(): Bitmap?{

    if(this != null){
        val bitmap = Bitmap.createBitmap(this.intrinsicWidth, this.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        this.setBounds(0, 0, canvas.width, canvas.height)
        this.draw(canvas)
        return bitmap
    }

    return null
}