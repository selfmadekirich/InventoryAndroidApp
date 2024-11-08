package com.example.inventory.ui.settings

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.InventoryTopAppBar
import com.example.inventory.R
import com.example.inventory.ui.AppViewModelProvider
import com.example.inventory.ui.item.ItemDetails
import com.example.inventory.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch
object SettingsDestination : NavigationDestination {
    override val route = "settings"
    override val titleRes = R.string.settings
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    var text by remember { mutableStateOf(viewModel.defaultQuantity) }
    var qInput = text?.toIntOrNull() != null
    Scaffold(
        topBar = {
            InventoryTopAppBar(
                title = stringResource(SettingsDestination.titleRes),
                canNavigateBack = qInput,
                navigateUp = navigateBack
            )
        }, floatingActionButton = {
        }, modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {

            CheckBoxRow(label = "Hide sensitive data", settingName = "sensitive_data_visible", viewModel = viewModel)
            CheckBoxRow(label = "Prohibit sending data from the application", settingName = "share_is_active", viewModel = viewModel)
            CheckBoxRow(label = "Use default quantity in stock", settingName = "use_default_quantity", viewModel = viewModel)

            text?.let {
                TextField(
                    value = it,
                    onValueChange = { newText ->
                        val nq = newText.toIntOrNull()
                        qInput = nq != null
                        text = newText
                        if (nq != null && nq > 0) {
                            viewModel.setQuantity(nq)
                        }
                    },
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .fillMaxWidth(0.4f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor =
                        if (qInput) MaterialTheme.colorScheme.secondaryContainer
                        else Color(0xFFFFC0CB),
                        unfocusedContainerColor =
                        if (qInput) MaterialTheme.colorScheme.secondaryContainer
                        else Color(0xFFFFC0CB),
                        disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Quantity") }
                )
            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        navigateBack()
                    }
                },
                enabled = qInput,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth(0.9f)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = stringResource(R.string.save_action))
            }
        }
    }
}
@Composable
fun CheckBoxRow(label: String, settingName: String, viewModel: SettingsViewModel) {
    val checkedState = viewModel.checkboxStates.getValue(settingName)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checkedState ,
            onCheckedChange = { viewModel.setSettingValue(settingName, it) }
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, style = MaterialTheme.typography.headlineLarge)
    }
}