package de.ur.mi.android.wetter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**Class used to compress a bitmap and turn it into a byte array as well as turn it back into a bitmap**/
public abstract class BitmapCompressor {

    //https://stackoverflow.com/questions/8417034/how-to-make-bitmap-compress-without-change-the-bitmap-size
    //Compresses the passed in bitmap to the specified quality in lossless WEBP Image Format
    public static byte[] compressBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, quality, stream);//0=lowest, 100=highest quality
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    //Decodes the passed in byte array into a bitmap
    public static Bitmap decodeByteArray(byte[] byteArray) {
        Bitmap compressedBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        return compressedBitmap;
    }
}
