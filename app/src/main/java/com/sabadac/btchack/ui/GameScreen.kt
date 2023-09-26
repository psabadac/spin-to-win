package com.sabadac.btchack.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sabadac.btchack.R

@Preview(showSystemUi = true)
@Composable
fun GameScreen(
    gameViewModel: GameViewModel = viewModel()
) {
    val gameUiState by gameViewModel.uiState.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .wrapContentSize(),
    ) {
        AddressField(
            label = stringResource(R.string.private_key_hex),
            address = gameUiState.index.toString(16)
        )
        AddressField(
            label = stringResource(R.string.bip44),
            address = gameUiState.bip44BtcAddress
        )

        AddressField(
            label = stringResource(R.string.bip84),
            address = gameUiState.bip84BtcAddress
        )

        AddressField(
            label = stringResource(R.string.eth),
            address = gameUiState.ethAddress
        )

        Button(onClick = {
            if (gameUiState.isSpinning) {
                gameViewModel.stop()
            } else {
                gameViewModel.start()
            }
        }, modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(if (gameUiState.isSpinning) R.string.pause else R.string.continue_spin))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressField(
    label: String,
    address: String
) {
    val clipboardManager = LocalClipboardManager.current

    OutlinedTextField(
        value = address,
        singleLine = true,
        enabled = false,
        textStyle = TextStyle.Default.copy(fontSize = 14.sp),
        label = { Text(text = label) }, onValueChange = {

        },
        trailingIcon = {
            Image(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy",
                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground),
                modifier = Modifier.clickable {
                    clipboardManager.setText(AnnotatedString(address))
                }
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
}
