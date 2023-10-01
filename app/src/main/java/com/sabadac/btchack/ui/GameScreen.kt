package com.sabadac.btchack.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
        PassphraseField(
            passphrase = gameUiState.passphrase,
        ) {
            gameViewModel.updateChecksum(it)
        }

        AddressField(
            address = gameUiState.privateKey,
        ) {
            gameViewModel.updateCheck(it)
        }

        AddressField(
            address = gameUiState.bip44BtcAddress,
        ) {
            gameViewModel.updateCheck(it)
        }

        AddressField(
            address = gameUiState.bip84BtcAddress
        ) {
            gameViewModel.updateCheck(it)
        }

        AddressField(
            address = gameUiState.ethAddress
        ) {
            gameViewModel.updateCheck(it)
        }

        Button(
            onClick = {
                if (gameUiState.isSpinning) {
                    gameViewModel.pause()
                } else {
                    gameViewModel.resume()
                }
            }, modifier = Modifier.padding(16.dp),
            enabled = gameUiState.ethAddress.isEnabled || gameUiState.bip44BtcAddress.isEnabled || gameUiState.bip84BtcAddress.isEnabled
        ) {
            Text(text = stringResource(if (gameUiState.isSpinning) R.string.pause else R.string.resume))
        }
    }
}

@Composable
fun AddressField(
    address: CryptoAddress,
    updateCheck: (AddressType) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = address.address,
            singleLine = true,
            enabled = false,
            textStyle = TextStyle.Default.copy(fontSize = 14.sp),
            label = {
                Text(
                    text = stringResource(id = if (address.type == AddressType.PrivateKey && !address.isEnabled) R.string.private_key_dec else address.type.label)
                )
            },
            onValueChange = {

            },
            trailingIcon = {
                Image(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground),
                    modifier = Modifier.clickable {
                        clipboardManager.setText(AnnotatedString(address.address))
                    }
                )
            },
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = address.isEnabled,
            onCheckedChange = {
                updateCheck(address.type)
            },
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
fun PassphraseField(
    passphrase: CryptoAddress,
    updateChecksums: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = passphrase.address,
            singleLine = true,
            enabled = true,
            textStyle = TextStyle.Default.copy(fontSize = 14.sp),
            label = {
                Text(
                    text = stringResource(id = passphrase.type.label)
                )
            },
            onValueChange = {
                updateChecksums(it)
            },
            trailingIcon = {
                Image(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground),
                    modifier = Modifier.clickable {
                        clipboardManager.setText(AnnotatedString(passphrase.address))
                    }
                )
            },
            modifier = Modifier.weight(1f)
        )
    }
}