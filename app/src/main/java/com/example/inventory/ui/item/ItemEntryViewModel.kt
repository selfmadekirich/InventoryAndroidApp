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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.inventory.data.Item
import java.text.NumberFormat
import com.example.inventory.data.ItemsRepository


/**
 * ViewModel to validate and insert items in the Room database.
 */
class ItemEntryViewModel(private val itemsRepository: ItemsRepository) : ViewModel() {

    /**
     * Holds current item ui state
     */
    var itemUiState by mutableStateOf(ItemUiState())
        private set

    /**
     * Updates the [itemUiState] with the value provided in the argument. This method also triggers
     * a validation for input values.
     */
    fun updateUiState(itemDetails: ItemDetails) {
        itemUiState =
            ItemUiState(
                itemDetails = itemDetails,
                isEntryValid = validateInput(itemDetails),
                isSupplierEmailValid = validateSupplierEmail(itemDetails),
                isSupplierPhoneValid = validateSupplierPhone(itemDetails),
                isSupplierValid = validateSupplier(itemDetails))
    }

    private fun validateInput(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        return with(uiState) {
            name.isNotBlank() && price.isNotBlank() && quantity.isNotBlank() &&
            validateSupplier(uiState) && validateSupplierPhone(uiState) && validateSupplierEmail(uiState)
        }
    }

    private fun validateSupplier(uiState: ItemDetails = itemUiState.itemDetails): Boolean{
        return with(uiState){
            supplier.isNotBlank()
        }
    }

    private fun validateSupplierPhone(uiState: ItemDetails = itemUiState.itemDetails): Boolean{
        return with(uiState){
            supplierPhone.isNotBlank() && android.util.Patterns.PHONE.matcher(supplierPhone).matches()
        }
    }

    private fun validateSupplierEmail(uiState: ItemDetails = itemUiState.itemDetails): Boolean{
        return with(uiState){
            supplierEmail.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(supplierEmail).matches()
        }
    }

    suspend fun saveItem() {
        if (validateInput()) {
            itemsRepository.insertItem(itemUiState.itemDetails.toItem())
        }
    }
}

/**
 * Represents Ui State for an Item.
 */
data class ItemUiState(
    val itemDetails: ItemDetails = ItemDetails(),
    val isEntryValid: Boolean = false,
    val isSupplierValid: Boolean = true,
    val isSupplierPhoneValid: Boolean = true,
    val isSupplierEmailValid: Boolean = true
)

data class ItemDetails(
    val id: Int = 0,
    val name: String = "",
    val price: String = "",
    val quantity: String = "",
    val supplier: String = "",
    val supplierEmail: String = "",
    val supplierPhone: String = ""
)

/**
 * Extension function to convert [ItemDetails] to [Item]. If the value of [ItemDetails.price] is
 * not a valid [Double], then the price will be set to 0.0. Similarly if the value of
 * [ItemDetails.quantity] is not a valid [Int], then the quantity will be set to 0
 */
fun ItemDetails.toItem(): Item = Item(
    id = id,
    name = name,
    price = price.toDoubleOrNull() ?: 0.0,
    quantity = quantity.toIntOrNull() ?: 0,
    supplier = supplier,
    supplierEmail = supplierEmail,
    supplierPhone = supplierPhone
)

fun Item.formatedPrice(): String {
    return NumberFormat.getCurrencyInstance().format(price)
}

/**
 * Extension function to convert [Item] to [ItemUiState]
 */
fun Item.toItemUiState(isEntryValid: Boolean = false, isSupplierValid: Boolean, isSupplierPhoneValid: Boolean,isSupplierEmailValid: Boolean): ItemUiState = ItemUiState(
    itemDetails = this.toItemDetails(),
    isEntryValid = isEntryValid,
    isSupplierValid = isSupplierValid,
    isSupplierPhoneValid = isSupplierPhoneValid,
    isSupplierEmailValid = isSupplierEmailValid
)

/**
 * Extension function to convert [Item] to [ItemDetails]
 */
fun Item.toItemDetails(): ItemDetails = ItemDetails(
    id = id,
    name = name,
    price = price.toString(),
    quantity = quantity.toString(),
    supplier = supplier,
    supplierEmail = supplierEmail,
    supplierPhone = supplierPhone
)
