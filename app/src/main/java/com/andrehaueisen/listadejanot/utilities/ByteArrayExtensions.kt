package com.andrehaueisen.listadejanot.utilities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

/**
 * Created by andre on 10/2/2017.
 */
fun ByteArray.resamplePic(quality: Int, imageWidth: Int = 110, imageHeight: Int = 110): ByteArray {

    val bmp = BitmapFactory.decodeByteArray(this, 0, this.size)
    val stream = ByteArrayOutputStream()
    val thumbnailBitmap = Bitmap.createScaledBitmap(bmp, imageWidth, imageHeight, false)
    thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

    return stream.toByteArray()
}