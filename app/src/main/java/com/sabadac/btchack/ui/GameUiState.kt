package com.sabadac.btchack.ui

import java.math.BigInteger

data class GameUiState(
    val bip44BtcAddress: String,
    val bip84BtcAddress: String,
    val ethAddress: String,
    val isSpinning: Boolean,
    val index: BigInteger
)