package com.sabadac.btchack.ui

data class GameUiState(
    val passphrase: CryptoAddress,
    val privateKey: CryptoAddress,
    val bip44BtcAddress: CryptoAddress,
    val bip84BtcAddress: CryptoAddress,
    val ethAddress: CryptoAddress,
    val isSpinning: Boolean,

)