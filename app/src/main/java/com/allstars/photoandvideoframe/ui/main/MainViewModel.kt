package com.allstars.photoandvideoframe.ui.main

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.os.Handler
import android.support.annotation.MainThread
import android.support.v4.content.ContextCompat
import android.util.Log
import com.allstars.photoandvideoframe.R
import com.allstars.photoandvideoframe.data.GalleryItem
import com.allstars.photoandvideoframe.data.Repository
import com.allstars.photoandvideoframe.md5
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


class MainViewModel(application: Application) : AndroidViewModel(application) {

    val state = MutableLiveData<StateData>()

    private val repository = Repository()

    private val handler: Handler

    private var galleryItems: List<GalleryItem>? = null

    private var currentGalleryItem = 0

    private var isPermissionRequested: Boolean = false

    private var job: Job? = null

    init {
        handler = Handler {
            state.value = StateData.loadingProgress(it.obj as Int)
            true
        }
    }

    @MainThread
    fun showNextGalleryItem() {
        currentGalleryItem++
        showCurrentGalleryItem()
    }

    @MainThread
    fun showCurrentGalleryItem() {
        if (ContextCompat.checkSelfPermission(
                getApplication(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (!isPermissionRequested) {
                state.value = StateData.requestPermission()
            }

            isPermissionRequested = true

            return
        }

        if (job != null && job!!.isActive) {
            return
        }

        job = GlobalScope.launch(Dispatchers.IO) {
            try {
                if (galleryItems == null) {
                    state.postValue(StateData.loadingIndeterminate())

                    val items = repository.getRemoteItems()

                    galleryItems = if (!items.isNullOrEmpty()) {
                        items
                    } else {
                        repository.getLocalItems(getApplication())
                    }
                }

                if (galleryItems!!.isEmpty()) {
                    state.postValue(StateData.error(R.string.error_images_and_videos_not_found))
                    return@launch
                }

                var item = prepareItem()

                if (item.isRemote) {
                    val file = downloadFile(item.path)

                    //Can't load file switch to local files
                    if (file == null) {
                        galleryItems = repository.getLocalItems(getApplication())
                        job = null
                        showCurrentGalleryItem()
                        return@launch
                    }

                    //check video file, it can be broken
                    val metaRetriever = MediaMetadataRetriever()
                    metaRetriever.setDataSource(file.absolutePath)
                    val value = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                    metaRetriever.release()

                    if (value == null) {
                        //something wrong with video
                        //remove it and go to next
                        file.delete()
                        job = null
                        showNextGalleryItem()
                        return@launch
                    }


                    item = GalleryItem(file.absolutePath, false, file.lastModified(), item.type)
                }

                state.postValue(StateData.showGalleryItem(item))

            } catch (e: Exception) {
                Log.e(TAG, "Error", e)
                state.postValue(StateData.error(R.string.error_something_wrong))
            }
        }
    }

    private fun prepareItem(): GalleryItem {
        if (galleryItems == null) {
            throw IllegalStateException("galleryItems is null")
        }

        if (currentGalleryItem >= galleryItems!!.size) {
            currentGalleryItem = 0
        }

        return galleryItems!![currentGalleryItem]
    }


    private fun downloadFile(url: String): File? {
        val file = File(getApplication<Application>().filesDir, url.md5())

        var input: InputStream? = null
        var output: OutputStream? = null
        var connection: HttpURLConnection? = null
        try {

            if (file.exists()) {
                return file
            }

            val query = URL(url)
            connection = query.openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return null
            }

            val fileLength = connection.contentLength

            // download the file
            input = connection.inputStream
            output = FileOutputStream(file)

            val data = ByteArray(4096)
            var total: Long = 0
            var count: Int
            while (true) {
                count = input.read(data)

                if (count == -1) {
                    break
                }

                total += count.toLong()
                if (fileLength > 0) {
                    val percent = (total * 100 / fileLength).toInt()

                    //This is the first method for send some info from background thread to main one
                    state.postValue(StateData.loadingProgress(percent))

                    //This is the second method for send som info from background thread to main one
                    //handler.sendMessage(handler.obtainMessage(0, percent))

                    //Or we can use AsyncTask

                }
                output.write(data, 0, count)
            }

            return file
        } catch (e: Exception) {
            Log.e(TAG, "Error", e)
            file.delete()
            return null
        } finally {
            try {
                output?.close()
                input?.close()
            } catch (ignored: IOException) {
            }

            connection?.disconnect()
        }
    }

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
    }

}