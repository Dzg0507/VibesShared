package com.example.vibesshared.ui.ui.utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

fun formatTimestamp(timestamp: Timestamp?): String {
    return if (timestamp != null) {
        val sdf = SimpleDateFormat("MMM dd 'at' hh:mm a", Locale.getDefault())
        sdf.format(timestamp.toDate())
    } else {
        "Unknown date" // or any default string you prefer
    }
}