package pt.ipt.dam.sabordigital.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Base64
import android.widget.ImageView
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayInputStream

object ImageHelper {
    fun setImageFromBase64(imageView: ImageView, base64String: String) {
        // Remove prefix if present
        val pureBase64 = if (base64String.contains(",")) {
            base64String.substringAfter(",")
        } else {
            base64String
        }
        // Decode to byte array
        val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)
        // Convert the bytes into a Bitmap
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

        // Use EXIF to fix orientation (if necessary)
        val rotatedBitmap = rotateImageIfRequired(bitmap, decodedBytes)

        // Then set the Bitmap in the ImageView
        imageView.setImageBitmap(rotatedBitmap)
    }

    private fun rotateImageIfRequired(bitmap: Bitmap, byteArray: ByteArray): Bitmap {
        val inputStream = ByteArrayInputStream(byteArray)
        val exif = ExifInterface(inputStream)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
            else -> bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}