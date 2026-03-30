package com.pokrikinc.mixpokrikcutter

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pokrikinc.mixpokrikcutter.data.RetrofitProvider
import com.pokrikinc.mixpokrikcutter.store.PreferenceManager
import com.pokrikinc.mixpokrikcutter.ui.classic.CatalogFragment
import com.pokrikinc.mixpokrikcutter.ui.classic.DeviceFragment
import com.pokrikinc.mixpokrikcutter.ui.classic.PartsFragment
import com.pokrikinc.mixpokrikcutter.ui.classic.QueueDetailsFragment
import com.pokrikinc.mixpokrikcutter.ui.classic.QueuesFragment
import com.pokrikinc.mixpokrikcutter.ui.classic.SettingsFragment
import com.pokrikinc.mixpokrikcutter.ui.classic.VendorFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var toolbar: Toolbar
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var progressOverlay: FrameLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PreferenceManager.init(this)
        RetrofitProvider.init()

        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        progressOverlay = findViewById(R.id.progress_overlay)
        progressBar = findViewById(R.id.progress_bar)
        progressText = findViewById(R.id.progress_text)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = getString(R.string.app_name)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        supportFragmentManager.addOnBackStackChangedListener {
            syncNavigationState()
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_catalog -> {
                    openCatalogRoot()
                    true
                }

                R.id.menu_queues -> {
                    openQueuesRoot()
                    true
                }

                R.id.menu_settings -> {
                    openSettingsRoot()
                    true
                }

                else -> false
            }
        }

        if (savedInstanceState == null) {
            loadInitialData()
        } else {
            syncNavigationState()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel()
    }

    fun setTitleAndBack(title: String, showBack: Boolean) {
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(showBack)
    }

    fun openVendors(categoryId: String) {
        pushFragment(VendorFragment.newInstance(categoryId))
    }

    fun openDevices(categoryId: String, vendorId: String) {
        pushFragment(DeviceFragment.newInstance(categoryId, vendorId))
    }

    fun openParts(categoryId: String, vendorId: String, deviceId: String) {
        pushFragment(PartsFragment.newInstance(categoryId, vendorId, deviceId))
    }

    fun openQueueDetails(queueId: Int) {
        pushFragment(QueueDetailsFragment.newInstance(queueId))
    }

    private fun loadInitialData() {
        setLoading(true, getString(R.string.loading_catalog))
        activityScope.launch {
            try {
                val result = AppDataStore.ensureCatalogLoaded(applicationContext)
                if (result.isSuccess) {
                    setLoading(true, getString(R.string.loading_plotter))
                    try {
                        AppDataStore.warmUpDeviceManager()
                    } catch (_: Exception) {
                    }
                    setLoading(false)
                    bottomNavigation.selectedItemId = R.id.menu_queues
                    openQueuesRoot()
                } else {
                    setLoading(true, result.message ?: getString(R.string.loading_error))
                    progressBar.visibility = View.GONE
                }
            } catch (_: Exception) {
                setLoading(true, getString(R.string.loading_error))
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun openCatalogRoot() {
        clearBackStack()
        showRootFragment(CatalogFragment(), CatalogFragment::class.java.name)
        setTitleAndBack(getString(R.string.title_catalog), false)
    }

    private fun openQueuesRoot() {
        clearBackStack()
        showRootFragment(QueuesFragment(), QueuesFragment::class.java.name)
        setTitleAndBack(getString(R.string.title_queues), false)
    }

    private fun openSettingsRoot() {
        clearBackStack()
        showRootFragment(SettingsFragment(), SettingsFragment::class.java.name)
        setTitleAndBack(getString(R.string.title_settings), false)
    }

    private fun showRootFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .commit()
    }

    private fun pushFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(fragment::class.java.name)
            .commit()
    }

    private fun clearBackStack() {
        supportFragmentManager.popBackStack(
            null,
            androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun syncNavigationState() {
        val hasBackStack = supportFragmentManager.backStackEntryCount > 0
        supportActionBar?.setDisplayHomeAsUpEnabled(hasBackStack)
    }

    private fun setLoading(isVisible: Boolean, message: String = "") {
        progressOverlay.visibility = if (isVisible) View.VISIBLE else View.GONE
        progressBar.visibility = if (isVisible) View.VISIBLE else View.GONE
        progressText.text = message
    }
}
