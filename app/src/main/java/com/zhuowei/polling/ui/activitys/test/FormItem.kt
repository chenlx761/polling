package com.zhuowei.polling.ui.activitys.test

import android.text.InputType

data class FormItem(
    val id: String,
    val key: String,
    val label: String = "",
    val hint: String = "",
    var value: String = "",
    val inputType: Int = InputType.TYPE_CLASS_TEXT,
    val required: Boolean = false,
    val options: List<String> = emptyList(),
    val description: String = "",
    var checked: Boolean = false,
    val buttonText: String = "",
    val maxCount: Int = 1,
    val selectedAttachments: MutableList<FormAttachment> = mutableListOf()
) {
    companion object {
        const val KEY_INPUT = "input"
        const val KEY_SELECT = "select"
        const val KEY_DATE = "date"
        const val KEY_SWITCH = "switch"
        const val KEY_TEXTAREA = "textarea"
        const val KEY_IMAGE_PICKER = "image_picker"
        const val KEY_FILE_PICKER = "file_picker"
        const val KEY_SUBMIT = "submit"
    }
}

data class FormAttachment(
    val name: String,
    val path: String
)
