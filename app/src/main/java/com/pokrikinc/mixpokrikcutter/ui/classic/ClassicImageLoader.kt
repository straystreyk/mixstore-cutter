package com.pokrikinc.mixpokrikcutter.ui.classic

import android.widget.ImageView
import coil3.ImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.target
import coil3.svg.SvgDecoder

object ClassicImageLoader {
    @Volatile
    private var imageLoader: ImageLoader? = null

    fun load(imageView: ImageView, imageUrl: String?, onResult: ((Boolean) -> Unit)? = null) {
        if (imageUrl.isNullOrBlank()) {
            imageView.setImageDrawable(null)
            onResult?.invoke(false)
            return
        }

        val context = imageView.context
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .target(imageView)
            .listener(
                onSuccess = { _, _ -> onResult?.invoke(true) },
                onError = { _, _ -> onResult?.invoke(false) }
            )
            .memoryCacheKey(imageUrl)
            .diskCacheKey(imageUrl)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()

        getImageLoader(context).enqueue(request)
    }

    private fun getImageLoader(context: android.content.Context): ImageLoader {
        imageLoader?.let { return it }

        return synchronized(this) {
            imageLoader ?: ImageLoader.Builder(context.applicationContext)
                .components {
                    add(SvgDecoder.Factory())
                }
                .build()
                .also { imageLoader = it }
        }
    }
}
