package com.allstars.photoandvideoframe.ui.main

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.annotation.StringRes
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.allstars.photoandvideoframe.R
import com.allstars.photoandvideoframe.data.GalleryItem
import com.allstars.photoandvideoframe.data.GalleryItemType
import com.allstars.photoandvideoframe.ui.main.fragments.ImageFragment
import com.allstars.photoandvideoframe.ui.main.fragments.SettingsDialogFragment
import com.allstars.photoandvideoframe.ui.main.fragments.VideoFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.include_error.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), VideoFragment.OnVideoErrorListener,
    SettingsDialogFragment.OnChaneIntervalListener {

    private lateinit var mainViewModel: MainViewModel

    private var isStarted = false

    private var interval: Long = 5_000

    private lateinit var sharedPreference: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreference = PreferenceManager.getDefaultSharedPreferences(this)

        interval = sharedPreference.getLong(KEY_INTERVAL, interval)

        frameLayout.setOnClickListener {
            SettingsDialogFragment.newInstance(TimeUnit.MILLISECONDS.toSeconds(interval)).show(supportFragmentManager, SettingsDialogFragment.TAG)
        }

        init()
    }

    override fun onResume() {
        super.onResume()

        isStarted = true

        mainViewModel.showCurrentGalleryItem()
    }

    override fun onPause() {
        super.onPause()

        isStarted = false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
                    showError(
                        R.string.error_no_permission,
                        R.string.btn_go_to_settings, View.OnClickListener {
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        })
                }
                return
            }
        }
    }

    override fun onVideoError() {
        mainViewModel.showNextGalleryItem()
    }

    override fun onIntervalChanged(newInterval: Long) {
        interval = TimeUnit.SECONDS.toMillis(newInterval)
        sharedPreference.edit().putLong(KEY_INTERVAL, interval).apply()
    }

    private fun init() {
        showProgress()

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        observeViewModels()
    }

    private fun observeViewModels() {
        mainViewModel.state.observe(this, Observer<StateData> {
            when (it!!.state) {
                State.LOADING -> {
                    showProgress(it.loadingProgress!!)
                }
                State.SHOW_GALLERY_ITEM -> showGalleryItem(it.galleryItem!!)
                State.REQUEST_PERMISSION -> requestPermission()
                State.ERROR -> showError(it.errorMessage!!, R.string.btn_try_again, View.OnClickListener {
                    mainViewModel.showNextGalleryItem()
                })
            }
        })
    }

    private fun showGalleryItem(item: GalleryItem) {
        if (!isStarted) return

        layoutError.visibility = View.GONE
        progressBar.visibility = View.GONE
        frameLayout.visibility = View.VISIBLE

        val fragment = if (item.type == GalleryItemType.IMAGE) {
            ImageFragment.newInstance(item.path)
        } else {
            VideoFragment.newInstance(item.path)
        }

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .replace(R.id.frameLayout, fragment)
            .commitAllowingStateLoss()

        frameLayout.postDelayed({
            if (!isStarted) {
                return@postDelayed
            }

            mainViewModel.showNextGalleryItem()
        }, interval)
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_PERMISSION
        )
    }

    private fun showProgress(progress: Int = -1) {
        frameLayout.visibility = View.GONE
        layoutError.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        if (progress > 0) {
            progressBar.isIndeterminate = false
            progressBar.progress = progress
        } else {
            progressBar.isIndeterminate = true
        }
    }

    private fun showError(@StringRes message: Int, @StringRes buttonText: Int? = null, clickListener: View.OnClickListener? = null) {
        frameLayout.visibility = View.INVISIBLE
        progressBar.visibility = View.GONE
        layoutError.visibility = View.VISIBLE

        tvError.setText(message)

        if (buttonText != null && clickListener != null) {
            btnRetry.setText(buttonText)
            btnRetry.visibility = View.VISIBLE
            btnRetry.setOnClickListener(clickListener)
        } else {
            btnRetry.visibility = View.GONE
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName

        private const val REQUEST_PERMISSION: Int = 100

        private const val KEY_INTERVAL = "KEY_INTERVAL"
    }
}
