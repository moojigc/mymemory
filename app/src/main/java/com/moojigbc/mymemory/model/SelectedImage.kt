package com.moojigbc.mymemory.model

import android.net.Uri

data class SelectedImage(
    var uri: Uri,
    val position: Int
) {
}