package com.example.inventory.ui.settings

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

lateinit var context: Context
class SettingsViewModel: ViewModel() {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "encrypted_settings",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    val checkboxStates = mutableMapOf(
        "sensitive_data_visible" to sharedPreferences.getBoolean("sensitive_data_visible", false),
        "share_is_active" to sharedPreferences.getBoolean("share_is_active", false),
        "use_default_quantity" to  sharedPreferences.getBoolean("use_default_quantity", false)
    )


    fun getSettingValue(settingName: String): Boolean {
        return sharedPreferences.getBoolean(settingName, false)
    }



    fun setSettingValue(settingName: String, isChecked: Boolean) {
        checkboxStates[settingName] =  isChecked
        saveToPreferences(settingName, isChecked)
    }

    private fun saveToPreferences(settingName: String, isChecked: Boolean) {
        sharedPreferences.edit().putBoolean(settingName, isChecked).apply()
    }

    var defaultQuantity = sharedPreferences.getString("default_quantity", "42")

    fun setQuantity(quantity: Int){
        defaultQuantity = quantity.toString()
        sharedPreferences.edit().putString("default_quantity", defaultQuantity).apply()
    }
}