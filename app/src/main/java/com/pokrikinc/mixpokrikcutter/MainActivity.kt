package com.pokrikinc.mixpokrikcutter

import android.app.DownloadManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pokrikinc.mixpokrikcutter.data.RetrofitProvider
import com.pokrikinc.mixpokrikcutter.data.model.AppUpdateInfo
import com.pokrikinc.mixpokrikcutter.store.PreferenceManager
import com.pokrikinc.mixpokrikcutter.ui.classic.CatalogFragment
import com.pokrikinc.mixpokrikcutter.ui.classic.CatalogSectionFragment
import com.pokrikinc.mixpokrikcutter.ui.classic.CustomCategoryPartsFragment
import com.pokrikinc.mixpokrikcutter.ui.classic.DeviceFragment
import com.pokrikinc.mixpokrikcutter.ui.classic.PartsFragment
import com.pokrikinc.mixpokrikcutter.ui.classic.QueueDetailsFragment
import com.pokrikinc.mixpokrikcutter.ui.classic.QueuesFragment
import com.pokrikinc.mixpokrikcutter.ui.classic.SettingsFragment
import com.pokrikinc.mixpokrikcutter.ui.classic.VendorFragment
import com.pokrikinc.mixpokrikcutter.update.AppUpdateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "AppUpdate"
    }

    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var installPermissionDialog: AlertDialog? = null
    private var updateDownloadDialog: AlertDialog? = null
    private var updateDownloadJob: Job? = null
    private var updateDownloadProgressBar: ProgressBar? = null
    private var updateDownloadMessageView: TextView? = null
    private var updateDownloadActionButton: Button? = null

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

        checkForAppUpdates(silent = true)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        if (AppUpdateManager.getPendingDownloadProgress(this)?.isRunning == true) {
            startTrackingUpdateDownload()
        }
        if (AppUpdateManager.shouldRequestInstallPermission(this)) {
            showInstallPermissionDialog()
        } else {
            AppUpdateManager.requestInstallIfReady(this)
        }
    }

    override fun onDestroy() {
        installPermissionDialog?.dismiss()
        installPermissionDialog = null
        stopTrackingUpdateDownload()
        super.onDestroy()
        activityScope.cancel()
    }

    fun checkForAppUpdates(silent: Boolean) {
        activityScope.launch {
            val baseUrl = PreferenceManager.getBaseUrl()
            Log.d(TAG, "Checking for updates. baseUrl=$baseUrl")
            val result = AppUpdateManager.checkForUpdate(baseUrl)
            result.onSuccess { updateInfo ->
                if (updateInfo == null) {
                    Log.d(TAG, "No updates available for baseUrl=$baseUrl")
                    if (!silent) {
                        Toast.makeText(
                            this@MainActivity,
                            R.string.update_not_found,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@onSuccess
                }

                Log.d(
                    TAG,
                    "Update available. versionCode=${updateInfo.versionCode}, " +
                        "versionName=${updateInfo.versionName}, apkUrl=${updateInfo.apkUrl}, " +
                        "mandatory=${updateInfo.mandatory}"
                )
                if (!isFinishing && !isDestroyed) {
                    showUpdateDialog(updateInfo)
                }
            }.onFailure { error ->
                Log.e(TAG, "Update check failed for baseUrl=$baseUrl", error)
                if (!silent) {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.update_check_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun setTitleAndBack(title: String, showBack: Boolean) {
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(showBack)
    }

    fun openCatalogSection(source: String) {
        pushFragment(CatalogSectionFragment.newInstance(source))
    }

    fun openVendors(source: String, categoryId: String) {
        if (source == CatalogSource.CUSTOM) {
            pushFragment(CustomCategoryPartsFragment.newInstance(categoryId))
        } else {
            pushFragment(VendorFragment.newInstance(source, categoryId))
        }
    }

    fun openDevices(source: String, categoryId: String, vendorId: String) {
        pushFragment(DeviceFragment.newInstance(source, categoryId, vendorId))
    }

    fun openParts(source: String, categoryId: String, vendorId: String, deviceId: String) {
        pushFragment(PartsFragment.newInstance(source, categoryId, vendorId, deviceId))
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

    private fun showUpdateDialog(updateInfo: AppUpdateInfo) {
        val message = updateInfo.notes
            ?.takeIf { it.isNotBlank() }
            ?.let { getString(R.string.update_available_notes, updateInfo.versionName, it) }
            ?: getString(R.string.update_available_message, updateInfo.versionName)

        val builder = AlertDialog.Builder(this)
            .setTitle(R.string.update_available_title)
            .setMessage(message)
            .setCancelable(!updateInfo.mandatory)
            .setPositiveButton(R.string.action_install) { _, _ ->
                runCatching {
                    AppUpdateManager.enqueueUpdateDownload(this, updateInfo)
                }.onSuccess {
                    startTrackingUpdateDownload()
                    Toast.makeText(this, R.string.update_download_started, Toast.LENGTH_SHORT)
                        .show()
                }.onFailure {
                    Toast.makeText(this, R.string.update_download_failed, Toast.LENGTH_SHORT)
                        .show()
                }
            }

        if (!updateInfo.mandatory) {
            builder.setNegativeButton(R.string.action_later) { dialog, _ ->
                dialog.dismiss()
            }
        }

        builder.show()
    }

    private fun showInstallPermissionDialog() {
        val currentDialog = installPermissionDialog
        if (currentDialog?.isShowing == true || isFinishing || isDestroyed) {
            return
        }

        installPermissionDialog = AlertDialog.Builder(this)
            .setTitle(R.string.update_install_permission_title)
            .setMessage(R.string.update_install_permission_required)
            .setCancelable(true)
            .setPositiveButton(R.string.action_open_settings) { _, _ ->
                AppUpdateManager.openInstallPermissionSettings(this)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        installPermissionDialog?.setOnDismissListener {
            installPermissionDialog = null
        }
        installPermissionDialog?.show()
    }

    private fun startTrackingUpdateDownload() {
        showUpdateDownloadDialog()
        if (updateDownloadJob?.isActive == true) {
            return
        }

        updateDownloadJob = activityScope.launch {
            while (true) {
                val progress = AppUpdateManager.getPendingDownloadProgress(this@MainActivity)
                if (progress == null) {
                    dismissUpdateDownloadDialog()
                    updateDownloadJob = null
                    break
                }

                updateDownloadProgressBar?.isIndeterminate = progress.totalBytes <= 0L
                updateDownloadProgressBar?.progress = progress.progressPercent
                updateDownloadMessageView?.text = when {
                    progress.isSuccessful -> getString(R.string.update_download_progress_done)
                    progress.isFailed -> getString(R.string.update_download_progress_failed)
                    else -> AppUpdateManager.getDownloadStatusMessage(this@MainActivity)
                        ?: getString(R.string.update_download_progress_preparing)
                }
                updateDownloadActionButton(progress)

                if (progress.isSuccessful || progress.isFailed) {
                    updateDownloadJob = null
                    break
                }

                delay(500)
            }
        }
    }

    private fun stopTrackingUpdateDownload() {
        updateDownloadJob?.cancel()
        updateDownloadJob = null
        dismissUpdateDownloadDialog()
    }

    private fun showUpdateDownloadDialog() {
        if (updateDownloadDialog?.isShowing == true || isFinishing || isDestroyed) {
            return
        }

        val density = resources.displayMetrics.density
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val padding = (24 * density).toInt()
            setPadding(padding, padding, padding, padding)
        }

        val messageView = TextView(this).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            text = getString(R.string.update_download_progress_preparing)
        }

        val progressBar = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            isIndeterminate = true
            max = 100
        }

        val actionButton = AppCompatButton(this).apply {
            visibility = View.GONE
        }

        container.addView(
            messageView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
        container.addView(
            progressBar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (16 * density).toInt()
            }
        )
        container.addView(
            actionButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (20 * density).toInt()
            }
        )

        updateDownloadProgressBar = progressBar
        updateDownloadMessageView = messageView
        updateDownloadActionButton = actionButton
        updateDownloadDialog = AlertDialog.Builder(this)
            .setTitle(R.string.update_download_progress_title)
            .setView(container)
            .setCancelable(true)
            .create()
        updateDownloadDialog?.setOnDismissListener {
            updateDownloadDialog = null
            updateDownloadProgressBar = null
            updateDownloadMessageView = null
            updateDownloadActionButton = null
        }
        updateDownloadDialog?.show()
    }

    private fun dismissUpdateDownloadDialog() {
        updateDownloadDialog?.dismiss()
        updateDownloadDialog = null
        updateDownloadProgressBar = null
        updateDownloadMessageView = null
        updateDownloadActionButton = null
    }

    private fun updateDownloadActionButton(progress: AppUpdateManager.DownloadProgress) {
        val actionButton = updateDownloadActionButton ?: return

        when {
            progress.isSuccessful -> {
                actionButton.text = getString(R.string.action_install)
                actionButton.setOnClickListener {
                    if (AppUpdateManager.shouldRequestInstallPermission(this)) {
                        showInstallPermissionDialog()
                    } else {
                        AppUpdateManager.requestInstallIfReady(this, openSettingsIfNeeded = true)
                    }
                }
                actionButton.visibility = View.VISIBLE
            }

            progress.isFailed -> {
                actionButton.text = getString(android.R.string.ok)
                actionButton.setOnClickListener {
                    dismissUpdateDownloadDialog()
                }
                actionButton.visibility = View.VISIBLE
            }

            else -> {
                actionButton.visibility = View.GONE
            }
        }
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action != AppUpdateManager.ACTION_INSTALL_DOWNLOADED_UPDATE) {
            return
        }

        if (AppUpdateManager.shouldRequestInstallPermission(this)) {
            showInstallPermissionDialog()
        } else {
            AppUpdateManager.requestInstallIfReady(this, openSettingsIfNeeded = true)
        }
        intent.action = null
    }
}
