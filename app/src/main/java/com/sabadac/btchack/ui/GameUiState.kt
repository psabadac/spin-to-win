package com.sabadac.btchack.ui

data class GameUiState(
    val bip44BtcAddress: CryptoAddress,
    val bip84BtcAddress: CryptoAddress,
    val ethAddress: CryptoAddress,
    val privateKey: CryptoAddress,
    val isSpinning: Boolean,
)