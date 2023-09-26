package com.sabadac.btchack.ui

enum class AddressType {
    Bip44,
    Bip84,
    Eth,
    PrivateKey,
}
data class CryptoAddress(
    val address: String,
    val type: AddressType
)
