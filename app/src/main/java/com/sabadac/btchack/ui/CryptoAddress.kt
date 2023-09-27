package com.sabadac.btchack.ui

import androidx.annotation.StringRes
import com.sabadac.btchack.R

enum class AddressType(@StringRes val label: Int) {
    Bip44(R.string.bip44),
    Bip84(R.string.bip84),
    Eth(R.string.eth),
    PrivateKey(R.string.private_key_hex),
}
data class CryptoAddress(
    val address: String,
    val type: AddressType,
    val isEnabled: Boolean
)
