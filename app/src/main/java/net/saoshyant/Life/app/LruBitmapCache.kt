package net.saoshyant.Life.app

import android.graphics.Bitmap
import android.support.v4.util.LruCache
import com.android.volley.toolbox.ImageLoader.ImageCache

class LruBitmapCache(maxSize: Int) : LruCache<String, Bitmap>(maxSize), ImageCache {

    override fun sizeOf(key: String, value: Bitmap): Int {
        return value.rowBytes * value.height
    }

    override fun getBitmap(url: String): Bitmap? {
        return get(url)
    }

    override fun putBitmap(url: String, bitmap: Bitmap) {
        put(url, bitmap)
    }

}