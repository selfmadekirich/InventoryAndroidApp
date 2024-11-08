/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.inventory.ui.item

import android.content.Context
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.inventory.data.ItemsRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.Item
import com.google.gson.Gson
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import java.util.stream.Stream
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * ViewModel to retrieve, update and delete an item from the [ItemsRepository]'s data source.
 */
class ItemDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val itemsRepository: ItemsRepository
) : ViewModel() {

    private val itemId: Int = checkNotNull(savedStateHandle[ItemDetailsDestination.itemIdArg])

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    fun reduceQuantityByOne() {
        viewModelScope.launch {
            val currentItem = uiState.value.itemDetails.toItem()
            if (currentItem.quantity > 0) {
                itemsRepository.updateItem(currentItem.copy(quantity = currentItem.quantity - 1))
            }
        }
    }

    private fun toJson(item: Item): String{
        val gson = Gson()
        return gson.toJson(item)
    }

    private fun encryptData(data: String, uri: Uri): ByteArray {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (keyStore.containsAlias("my_secret_key_$uri")) {
            keyStore.deleteEntry("my_secret_key_$uri")
        }

        if (!keyStore.containsAlias("my_secret_key_$uri")) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                "my_secret_key_$uri",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build()
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
        val secretKey = keyStore.getKey("my_secret_key_$uri", null) as SecretKey
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val iv = cipher.iv

        val encryptedData = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

        return iv + encryptedData
    }

    suspend fun deleteItem() {
        itemsRepository.deleteItem(uiState.value.itemDetails.toItem())
    }

    fun saveFileToCache(context: Context, item: Item, uri : Uri): File {
        val encrypted = encryptData(toJson(item), uri)
        val cacheDir = context.cacheDir
        val cacheFile = File(cacheDir, "product_cache.enc")

        FileOutputStream(cacheFile).use { outputStream ->
            outputStream.write(encrypted)
        }
        return cacheFile
    }

    fun dumpCacheToSharedStorage(context: Context, cacheFile: File, uri: Uri) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            FileInputStream(cacheFile).use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    val uiState: StateFlow<ItemDetailsUiState> =
        itemsRepository.getItemStream(itemId)
            .filterNotNull()
            .map {
                ItemDetailsUiState(outOfStock = it.quantity <= 0, itemDetails = it.toItemDetails())
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = ItemDetailsUiState()
            )
}

/**
 * UI state for ItemDetailsScreen
 */
data class ItemDetailsUiState(
    val outOfStock: Boolean = true,
    val itemDetails: ItemDetails = ItemDetails()
)
