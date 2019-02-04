package net.saoshyant.Life.app

import android.app.Application
import android.text.TextUtils

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.Volley
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import io.realm.Realm
import io.realm.RealmConfiguration

class MyApplication : Application() {
    private var mRequestQueue: RequestQueue? = null
    private var mImageLoader: ImageLoader? = null
    internal var mLruBitmapCache: LruBitmapCache? = null

    val requestQueue: RequestQueue
        get() {
            if (mRequestQueue == null) {
                mRequestQueue = Volley.newRequestQueue(applicationContext)
            }

            return mRequestQueue!!
        }


    val imageLoader: ImageLoader
        get() {
            requestQueue
            if (mImageLoader == null) {
                lruBitmapCache
                mImageLoader = ImageLoader(this.mRequestQueue, mLruBitmapCache)
            }

            return this.mImageLoader!!
        }
    private val lruBitmapCache: LruBitmapCache
        get() {
            if (mLruBitmapCache == null)
                mLruBitmapCache = LruBitmapCache(1024000)
            return this.mLruBitmapCache!!
        }

    override fun onCreate() {
        super.onCreate()
        Fabric.with(this, Crashlytics())
        instance = this
        Realm.init(this)
        val realmConfiguration = RealmConfiguration.Builder()
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .build()
        Realm.getInstance(realmConfiguration)
        Realm.setDefaultConfiguration(realmConfiguration)
    }

    fun <T> addToRequestQueue(req: Request<T>, tag: String) {
        req.tag = if (TextUtils.isEmpty(tag)) TAG else tag
        requestQueue.add(req)
    }

    fun <T> addToRequestQueue(req: Request<T>) {
        req.tag = TAG
        requestQueue.add(req)
    }

    fun cancelPendingRequests(tag: Any) {
        if (mRequestQueue != null) {
            mRequestQueue!!.cancelAll(tag)
        }
    }

    companion object {
        val TAG = MyApplication::class.java!!.getSimpleName()
        @get:Synchronized
        var instance: MyApplication? = null
            private set
    }

}