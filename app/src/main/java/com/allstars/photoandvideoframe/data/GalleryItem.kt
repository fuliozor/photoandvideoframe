package com.allstars.photoandvideoframe.data

data class GalleryItem(val path: String, val isRemote: Boolean, val dateOfCreate: Long, val type: GalleryItemType)

enum class GalleryItemType {
    VIDEO,
    IMAGE
}