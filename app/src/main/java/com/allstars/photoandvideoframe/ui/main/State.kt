package com.allstars.photoandvideoframe.ui.main

import android.support.annotation.StringRes
import com.allstars.photoandvideoframe.data.GalleryItem

enum class State {
    LOADING,
    SHOW_GALLERY_ITEM,
    REQUEST_PERMISSION,
    ERROR
}

data class StateData(
    val state: State,
    val loadingProgress: Int? = null,
    val galleryItem: GalleryItem? = null,
    @StringRes val errorMessage: Int? = null,
    val showRetryButton: Boolean? = null
) {
    companion object {
        fun loadingIndeterminate() = StateData(state = State.LOADING, loadingProgress = -1)
        fun loadingProgress(progress: Int) = StateData(state = State.LOADING, loadingProgress = progress)
        fun requestPermission() = StateData(state = State.REQUEST_PERMISSION)
        fun showGalleryItem(item: GalleryItem) = StateData(state = State.SHOW_GALLERY_ITEM, galleryItem = item)
        fun error(@StringRes message: Int, showRetryButton: Boolean = true) =
            StateData(state = State.ERROR, errorMessage = message, showRetryButton = showRetryButton)
    }
}