package com.pokrikinc.mixpokrikcutter

import android.content.Context
import com.google.gson.JsonObject
import com.pokrikinc.mixpokrikcutter.data.RetrofitProvider
import com.pokrikinc.mixpokrikcutter.data.model.Category
import com.pokrikinc.mixpokrikcutter.data.model.Device
import com.pokrikinc.mixpokrikcutter.data.model.RemoteCatalogCategory
import com.pokrikinc.mixpokrikcutter.data.model.Vendor
import com.pokrikinc.mixpokrikcutter.data.repository.CatalogRepository
import com.pokrikinc.mixpokrikcutter.data.repository.CustomCatalogRepository
import com.pokrikinc.mixpokrikcutter.plotter.DeviceManager
import com.pokrikinc.mixpokrikcutter.plotter.PrintUtil
import com.pokrikinc.mixpokrikcutter.plotter.Received
import com.pokrikinc.mixpokrikcutter.store.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

object AppDataStore {
    data class LoadResult(
        val isSuccess: Boolean,
        val message: String? = null
    )

    @Volatile
    private var catalogLoaded = false

    private var catalog: List<Category> = emptyList()
    private var customCategories: List<RemoteCatalogCategory> = emptyList()
    private var customCatalogBaseUrl: String? = null
    private var images: JsonObject? = null
    private var plts: JsonObject? = null
    private var deviceManager: DeviceManager? = null

    suspend fun ensureCatalogLoaded(context: Context): LoadResult = withContext(Dispatchers.IO) {
        if (catalogLoaded && catalog.isNotEmpty() && images != null) {
            return@withContext LoadResult(true)
        }

        val loadedCatalog = CatalogRepository.loadCatalogFromAssets(context, "db.json")
        if (loadedCatalog.isEmpty()) {
            return@withContext LoadResult(false, "Не удалось загрузить каталог")
        }

        images = CatalogRepository.loadJsonObject(context, "images.json")
        catalog = loadedCatalog
        catalogLoaded = true
        LoadResult(true)
    }

    suspend fun ensureCustomCategoriesLoaded(): LoadResult = withContext(Dispatchers.IO) {
        val currentBaseUrl = PreferenceManager.getBaseUrl().trimEnd('/')
        if (customCatalogBaseUrl != currentBaseUrl) {
            customCategories = emptyList()
            customCatalogBaseUrl = currentBaseUrl
        }
        if (customCategories.isNotEmpty()) {
            return@withContext LoadResult(true)
        }

        return@withContext try {
            val repository = CustomCatalogRepository(RetrofitProvider.getCustomCatalogApi())
            customCategories = repository.loadCategories()
            LoadResult(true)
        } catch (e: Exception) {
            LoadResult(false, e.message ?: "Custom catalog is unavailable")
        }
    }

    suspend fun ensureImagesLoaded(context: Context): JsonObject = withContext(Dispatchers.IO) {
        images ?: CatalogRepository.loadJsonObject(context, "images.json").also { loaded ->
            images = loaded
        }
    }

    suspend fun ensurePltsLoaded(context: Context): JsonObject = withContext(Dispatchers.IO) {
        plts ?: CatalogRepository.loadJsonObject(context, "plts.json").also { loaded ->
            plts = loaded
        }
    }

    suspend fun ensureDeviceManager(): DeviceManager? = withContext(Dispatchers.IO) {
        val existing = deviceManager
        if (existing != null && existing.t485.driverOpen) {
            return@withContext existing
        }

        val manager = existing ?: DeviceManager.getInstance()
        if (manager.start485()) {
            delay(250)
            deviceManager = manager
            manager
        } else {
            null
        }
    }

    suspend fun reconnectDeviceManager(): DeviceManager? = withContext(Dispatchers.IO) {
        val manager = deviceManager ?: DeviceManager.getInstance()
        manager.destroy()
        if (manager.start485()) {
            delay(250)
            deviceManager = manager
            manager
        } else {
            null
        }
    }

    suspend fun warmUpDeviceManager(): Boolean = withContext(Dispatchers.IO) {
        val manager = ensureDeviceManager() ?: return@withContext false
        suspendCancellableCoroutine { continuation ->
            try {
                manager.send(PrintUtil.getState(), object : DeviceManager.Callback {
                    override fun data(success: Boolean, received: Received?) {
                        if (continuation.isActive) {
                            continuation.resume(success && !received?.readData.isNullOrEmpty())
                        }
                    }
                })
            } catch (_: Exception) {
                if (continuation.isActive) {
                    continuation.resume(false)
                }
            }
        }
    }

    fun getCatalog(): List<Category> = catalog

    fun getCustomCategories(): List<RemoteCatalogCategory> = customCategories

    fun resolveImagePath(imageKey: String?): String? {
        if (imageKey.isNullOrBlank()) return null
        val fileName = images?.get(imageKey)?.asString ?: return null
        return "file:///android_asset/files/$fileName"
    }

    fun findCategory(categoryId: String): Category? = catalog.firstOrNull { it.id == categoryId }

    fun findVendor(categoryId: String, vendorId: String): Vendor? {
        return findCategory(categoryId)?.vendors?.firstOrNull { it.id == vendorId }
    }

    fun findDevice(categoryId: String, vendorId: String, deviceId: String): Device? {
        return findVendor(categoryId, vendorId)?.devices?.firstOrNull { it.id == deviceId }
    }

    fun findCustomCategory(categoryId: String): RemoteCatalogCategory? =
        customCategories.firstOrNull { it.id == categoryId }
}
