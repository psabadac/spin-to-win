package com.sabadac.btchack.ui.test

import com.sabadac.btchack.ui.GameViewModel
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.math.BigInteger

class GameViewModelTest {
    private val two = BigInteger.valueOf(2)
    private val viewModel = GameViewModel()

    @Test
    fun bip44_Test() {
        val expectedAddress = "1cMh228HTCiwS8ZsaakH8A8wze1JR5ZsP"
        val actualAddress = viewModel.privateKeyToLegacyBip44BtcAddress(two)
        assertEquals(expectedAddress, actualAddress)
    }

    @Test
    fun bip84_Test() {
        val expectedAddress = "bc1qq6hag67dl53wl99vzg42z8eyzfz2xlkvxechjp"
        val actualAddress = viewModel.privateKeyToNativeSegWitBip84BtcAddress(two)
        assertEquals(expectedAddress, actualAddress)
    }

    @Test
    fun eth_Test() {
        val expectedAddress = "0x2b5ad5c4795c026514f8317c7a215e218dccd6cf"
        val actualAddress = viewModel.privateKeyToEthAddress(two)
        assertEquals(expectedAddress, actualAddress)
    }
}