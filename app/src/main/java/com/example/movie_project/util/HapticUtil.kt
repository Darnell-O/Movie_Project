package com.example.movie_project.util

import android.view.HapticFeedbackConstants
import android.view.View

object HapticUtil {
    fun performClickFeedback(view: View) {
        view.performHapticFeedback(
            HapticFeedbackConstants.CONFIRM,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }
}
