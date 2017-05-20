package com.andrehaueisen.listadejanot.utilities

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.DisplayMetrics
import android.view.WindowManager

/**
 * Created by andre on 5/17/2017.
 */
class ImageProcessor {

    companion object{

        fun resamplePic(context: Context, resources: Resources, imageId : Int): Bitmap {

            // Get device screen size information
            val metrics = DisplayMetrics()
            val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            manager.defaultDisplay.getMetrics(metrics)

            val targetH = metrics.heightPixels
            val targetW = metrics.widthPixels

            // Get the dimensions of the original bitmap
            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = true

            BitmapFactory.decodeResource(resources, imageId, bmOptions)

            val photoW = bmOptions.outWidth
            val photoH = bmOptions.outHeight

            // Determine how much to scale down the image
            val scaleFactor = Math.min(photoW / targetW, photoH / targetH)

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false
            bmOptions.inSampleSize = scaleFactor

            return BitmapFactory.decodeResource(resources, imageId)
        }
    }
}