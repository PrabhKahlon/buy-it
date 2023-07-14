package com.buyit

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.icu.util.Currency
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileInputStream

/**
 * Util file taken from from demo during lecture from https://www.sfu.ca/~xingdong/Teaching/CMPT362/lecture14/lecture14.html and the CameraDemoKotlin.zip project
 */

object Util {
    fun checkPermissions(activity: Activity?) {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), 0)
        }
    }

    fun getBitmap(context: Context, imgUri: Uri, imageFile: File): Bitmap {
        val imageStream = context.contentResolver.openInputStream(imgUri)
        val bitmap = BitmapFactory.decodeStream(imageStream)
        var newBitmap = bitmap
        if(imageStream != null) {
            val ei = FileInputStream(imageFile).use { ExifInterface(it) }
            val orientation: Int = ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
            Log.d("UTIL", orientation.toString())
            when(orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> {
                    newBitmap = rotateImageBitmap(bitmap, 90f)
                }
                ExifInterface.ORIENTATION_ROTATE_180 -> {
                    newBitmap = rotateImageBitmap(bitmap, 180f)
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> {
                    newBitmap = rotateImageBitmap(bitmap, 270f)
                }
            }
        }
        return newBitmap
    }

    fun rotateImageBitmap(bitmap: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.setRotate(angle)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun currencyConversion(defaultPrice: Double, context: Context): Double {
        val sharedPrefs = context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
        val currency = sharedPrefs.getString("currencyUnitString", "USD")
        val dollarRatio = when(currency) {
            "CAD" -> 1.2931
            "EUR" -> 0.9823
            "GBP" -> 0.8283
            "AUD" -> 1.4472
            else -> 1.0
        }
        val convertedPrice = defaultPrice * dollarRatio
        return convertedPrice
    }

    fun getCurrencySymbol(context: Context): String {
        val sharedPrefs = context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
        val currency = sharedPrefs.getString("currencyUnitString", "USD")
        val currencySymbol = Currency.getInstance(currency).symbol
        return currencySymbol
    }
}