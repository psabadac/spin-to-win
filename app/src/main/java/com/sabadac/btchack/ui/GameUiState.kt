package com.sabadac.btchack.ui

import java.math.BigInteger

data class GameUiState(
    val bip44BtcAddress: CryptoAddress,
    val bip84BtcAddress: CryptoAddress,
    val ethAddress: CryptoAddress,
    val isSpinning: Boolean,
    val index: BigInteger
)