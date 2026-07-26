package com.learnwitme.core.ui.form

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FormValidationTest {

    @Test
    fun testValidateField_required_fail() {
        val field = FormFieldConfig(
            id = "email",
            label = "Email Address",
            type = "email",
            required = true
        )
        val error = validateField(field, "")
        assertEquals("Email Address is required", error)
    }

    @Test
    fun testValidateField_required_success() {
        val field = FormFieldConfig(
            id = "email",
            label = "Email Address",
            type = "email",
            required = true
        )
        val error = validateField(field, "test@test.com")
        assertNull(error)
    }

    @Test
    fun testValidateField_regex_fail() {
        val field = FormFieldConfig(
            id = "email",
            label = "Email Address",
            type = "email",
            regexValidation = "^[A-Za-z0-9+_.-]+@(.+)$",
            validationErrorMessage = "Invalid Email Address Format"
        )
        val error = validateField(field, "invalid-email")
        assertEquals("Invalid Email Address Format", error)
    }

    @Test
    fun testValidateField_regex_success() {
        val field = FormFieldConfig(
            id = "email",
            label = "Email Address",
            type = "email",
            regexValidation = "^[A-Za-z0-9+_.-]+@(.+)$",
            validationErrorMessage = "Invalid Email Address Format"
        )
        val error = validateField(field, "test@example.com")
        assertNull(error)
    }
}
