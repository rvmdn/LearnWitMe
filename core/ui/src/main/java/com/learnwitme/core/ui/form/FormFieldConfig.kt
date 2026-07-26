package com.learnwitme.core.ui.form

import kotlinx.serialization.Serializable

@Serializable
data class FormFieldConfig(
    val id: String,
    val label: String,
    val type: String, // "text", "number", "email", "password", "switch", "checkbox", "dropdown"
    val placeholder: String = "",
    val required: Boolean = false,
    val regexValidation: String = "",
    val validationErrorMessage: String = "",
    val options: List<String> = emptyList()
)
