package com.allstars.photoandvideoframe.data

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.support.annotation.WorkerThread
import com.allstars.photoandvideoframe.isOnline
import java.io.File
import java.lang.Thread.sleep
import java.util.*

class Repository {
    @WorkerThread
    fun getRemoteItems(): List<GalleryItem>? {
        //In real app we will get links on videos from server
        if (!isOnline()) {
            return null
        }

        sleep(1_000)
        return listOf(
            GalleryItem(
                "http://techslides.com/demos/sample-videos/small.mp4",
                true,
                System.currentTimeMillis(),
                GalleryItemType.VIDEO
            ),
            GalleryItem(
                "https://tekeye.uk/html/images/Joren_Falls_Izu_Japan.mp4",
                true,
                System.currentTimeMillis(),
                GalleryItemType.VIDEO
            ), GalleryItem(
                "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_10mb.mp4",
                true,
                System.currentTimeMillis(),
                GalleryItemType.VIDEO
            ), GalleryItem(
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4",
                true,
                System.currentTimeMillis(),
                GalleryItemType.VIDEO
            )
        )
    }

    fun getLocalItems(context: Context): List<GalleryItem> {
        val galleryItems = ArrayList<GalleryItem>()

        prepareGalleryItems(
            readFilesByUri(
                context,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            ), GalleryItemType.IMAGE, galleryItems
        )

        prepareGalleryItems(
            readFilesByUri(
                context,
                android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI
            ), GalleryItemType.IMAGE, galleryItems
        )

        prepareGalleryItems(
            readFilesByUri(
                context,
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            ), GalleryItemType.VIDEO, galleryItems
        )

        prepareGalleryItems(
            readFilesByUri(
                context,
                android.provider.MediaStore.Video.Media.INTERNAL_CONTENT_URI
            ), GalleryItemType.VIDEO, galleryItems
        )

        galleryItems.sortWith(kotlin.Comparator { o1, o2 -> (o2.dateOfCreate - o1.dateOfCreate).toInt() })

        return galleryItems
    }

    private fun prepareGalleryItems(paths: List<String>, type: GalleryItemType, galleryItems: ArrayList<GalleryItem>) {
        paths.forEach {
            val file = File(it)
            if (file.exists() && file.canRead()) {
                galleryItems.add(GalleryItem(file.absolutePath, false, file.lastModified(), type))
            }
        }
    }

    private fun readFilesByUri(context: Context, uri: Uri): ArrayList<String> {
        val listOfFiles = ArrayList<String>()

        val projection = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

        val cursor = context.contentResolver.query(uri, projection, null, null, null)

        try {
            if (cursor != null) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                while (cursor.moveToNext()) {
                    listOfFiles.add(cursor.getString(columnIndex))
                }

            }
        } catch (e: Exception) {
            throw e
        } finally {
            cursor?.close()
        }

        return listOfFiles
    }
}