package com.pokrikinc.mixpokrikcutter.ui.screens.vendor

import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.pokrikinc.mixpokrikcutter.data.model.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VendorItem(
    val id: String,
    val name: String,
    val imgUrl: String
)

class VendorViewModel : ViewModel() {

    private val _vendorItems = MutableStateFlow<List<VendorItem>>(emptyList())
    val vendorItems: StateFlow<List<VendorItem>> = _vendorItems.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadVendors(
        categoryId: String?,
        catalog: List<Category>,
        images: JsonObject
    ) {
        if (categoryId == null) {
            _errorMessage.value = "Не передана категория"
            return
        }

        val category = catalog.find { it.id == categoryId }

        if (category == null || category.vendors.isEmpty()) {
            _errorMessage.value = "Список поставщиков пуст"
            return
        }

        val vendors = category.vendors

        val items = vendors.map { vendor ->
            val imageName = images?.get(vendor.img)
            val imgUrl = imageName?.let { "file:///android_asset/files/${it.asString}" } ?: ""
            VendorItem(vendor.id, vendor.name, imgUrl)
        }

        _vendorItems.value = items
        _errorMessage.value = null
    }
}
