package com.learnwitme.core.ui.form

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicForm(
    fields: List<FormFieldConfig>,
    onValuesChanged: (Map<String, String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val formValues = remember { mutableStateMapOf<String, String>() }
    val formErrors = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(fields) {
        fields.forEach { field ->
            if (!formValues.containsKey(field.id)) {
                formValues[field.id] = ""
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        fields.forEach { field ->
            val value = formValues[field.id] ?: ""
            val error = formErrors[field.id]

            when (field.type) {
                "text", "number", "email", "password" -> {
                    val keyboardType = when (field.type) {
                        "number" -> KeyboardType.Number
                        "email" -> KeyboardType.Email
                        else -> KeyboardType.Text
                    }
                    val visualTransformation = if (field.type == "password") {
                        PasswordVisualTransformation()
                    } else {
                        VisualTransformation.None
                    }

                    OutlinedTextField(
                        value = value,
                        onValueChange = { newValue ->
                            formValues[field.id] = newValue
                            onValuesChanged(formValues.toMap())
                            validateField(field, newValue)?.let {
                                formErrors[field.id] = it
                            } ?: run {
                                formErrors.remove(field.id)
                            }
                        },
                        label = { Text(field.label) },
                        placeholder = { Text(field.placeholder) },
                        isError = error != null,
                        supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                        visualTransformation = visualTransformation,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                "switch" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(field.label, style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = value.toBoolean(),
                            onCheckedChange = { checked ->
                                formValues[field.id] = checked.toString()
                                onValuesChanged(formValues.toMap())
                            }
                        )
                    }
                }
                "checkbox" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = value.toBoolean(),
                            onCheckedChange = { checked ->
                                formValues[field.id] = checked.toString()
                                onValuesChanged(formValues.toMap())
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(field.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                "dropdown" -> {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = value,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(field.label) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            field.options.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        formValues[field.id] = option
                                        onValuesChanged(formValues.toMap())
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun validateField(field: FormFieldConfig, value: String): String? {
    if (field.required && value.isBlank()) {
        return "${field.label} is required"
    }
    if (field.regexValidation.isNotEmpty() && value.isNotEmpty()) {
        val regex = Regex(field.regexValidation)
        if (!regex.matches(value)) {
            return if (field.validationErrorMessage.isNotEmpty()) {
                field.validationErrorMessage
            } else {
                "Invalid format for ${field.label}"
            }
        }
    }
    return null
}
