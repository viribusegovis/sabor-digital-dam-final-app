package pt.ipt.dam.sabordigital.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Base64
import android.widget.ImageView
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayInputStream


/**
 * Helper object for dealing with image operations.
 *
 * Provides functionality to decode Base64-encoded images, fix their orientation using EXIF data,
 * and set the resulting image in an ImageView.
 */
object ImageHelper {

    /**
     * Decodes a Base64-encoded string into a Bitmap, fixes its orientation if necessary,
     * and sets it on the provided ImageView.
     *
     * @param imageView The ImageView where the decoded image will be set.
     * @param base64String The Base64-encoded string representing the image.
     */
    fun setImageFromBase64(imageView: ImageView, base64String: String) {
        // Remove prefix if present (e.g., "data:image/png;base64,")
        val pureBase64 = if (base64String.contains(",")) {
            base64String.substringAfter(",")
        } else {
            base64String
        }
        // Decode the Base64 string into a byte array
        val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)
        // Convert the byte array into a Bitmap
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        // Fix the image orientation if required using EXIF information
        val rotatedBitmap = rotateImageIfRequired(bitmap, decodedBytes)
        // Set the final Bitmap to the ImageView
        imageView.setImageBitmap(rotatedBitmap)
    }

    /**
     * Checks the EXIF orientation of the image and rotates the Bitmap if necessary.
     *
     * @param bitmap The original bitmap.
     * @param byteArray The byte array representing the encoded image.
     * @return A Bitmap rotated to the correct orientation if needed, or the original bitmap otherwise.
     */
    private fun rotateImageIfRequired(bitmap: Bitmap, byteArray: ByteArray): Bitmap {
        val inputStream = ByteArrayInputStream(byteArray)
        val exif = ExifInterface(inputStream)
        // Retrieve the orientation attribute from EXIF
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        // Return the appropriately rotated bitmap based on orientation value
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
            else -> bitmap
        }
    }

    /**
     * Rotates the provided Bitmap by the specified number of degrees.
     *
     * @param bitmap The Bitmap to be rotated.
     * @param degrees The angle of rotation in degrees.
     * @return The rotated Bitmap.
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply {
            postRotate(degrees)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}