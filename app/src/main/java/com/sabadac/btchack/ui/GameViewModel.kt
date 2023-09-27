package com.sabadac.btchack.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.bitcoinj.core.Address
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.script.Script
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import java.math.BigInteger

class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        GameUiState(
            CryptoAddress("", AddressType.Bip44, true),
            CryptoAddress("", AddressType.Bip84, true),
            CryptoAddress("", AddressType.Eth, true),
            isSpinning = false,
            index = BigInteger.ONE
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    private val networkParameters = NetworkParameters.fromID(NetworkParameters.ID_MAINNET)

    init {
        val two = BigInteger.valueOf(2)
        val bip44BtcAddress = CryptoAddress(privateKeyToLegacyBip44BtcAddress(two), AddressType.Bip44, true)
        val bip84BtcAddress = CryptoAddress(privateKeyToNativeSegWitBip84BtcAddress(two), AddressType.Bip84, true)
        val ethAddress = CryptoAddress(privateKeyToEthAddress(two), AddressType.Eth, true)

        _uiState.update {
            currentState ->
            currentState.copy(
                bip44BtcAddress = bip44BtcAddress,
                bip84BtcAddress = bip84BtcAddress,
                ethAddress = ethAddress,
                index = two
            )
        }
    }

    fun start() {
        _uiState.update { currentState ->
            currentState.copy(isSpinning = true)
        }

        viewModelScope.launch(Dispatchers.IO) {
            spin().collect { cryptoAddress ->
                updateAddresses(cryptoAddress)
            }
        }
    }

    fun stop() {
        _uiState.update { currentState ->
            currentState.copy(isSpinning = false)
        }
    }

    fun updateCheck(addressType: AddressType) {
        _uiState.update { currentState ->
            when(addressType) {
                AddressType.PrivateKey -> currentState
                AddressType.Bip44 -> currentState.copy(bip44BtcAddress = uiState.value.bip44BtcAddress.copy(isEnabled = !uiState.value.bip44BtcAddress.isEnabled))
                AddressType.Bip84 -> currentState.copy(bip84BtcAddress = uiState.value.bip84BtcAddress.copy(isEnabled = !uiState.value.bip84BtcAddress.isEnabled))
                AddressType.Eth -> currentState.copy(ethAddress = uiState.value.ethAddress.copy(isEnabled = !uiState.value.ethAddress.isEnabled))
            }
        }

        if (!uiState.value.bip44BtcAddress.isEnabled
            && !uiState.value.bip84BtcAddress.isEnabled
            && !uiState.value.ethAddress.isEnabled) {
            stop()
        }
    }

    fun privateKeyToLegacyBip44BtcAddress(privateKey: BigInteger): String =
        Address.fromKey(
            networkParameters,
            ECKey.fromPrivate(privateKey),
            Script.ScriptType.P2PKH
        ).toString()

    fun privateKeyToNativeSegWitBip84BtcAddress(privateKey: BigInteger): String =
        Address.fromKey(
            networkParameters,
            ECKey.fromPrivate(privateKey),
            Script.ScriptType.P2WPKH
        ).toString()

    fun privateKeyToEthAddress(privateKey: BigInteger): String = Credentials.create(ECKeyPair.create(privateKey)).address

    @OptIn(ExperimentalStdlibApi::class)
    private fun spin() : Flow<CryptoAddress> = flow {
        var i = uiState.value.index

        while (uiState.value.isSpinning) {
            val privateKey = i
            emit(CryptoAddress(i.toString(), AddressType.PrivateKey, true))

            if (uiState.value.bip44BtcAddress.isEnabled) {
                val bip44BtcAddress = privateKeyToLegacyBip44BtcAddress(privateKey)
                emit(uiState.value.bip44BtcAddress.copy(address = bip44BtcAddress))
            }

            if (uiState.value.bip84BtcAddress.isEnabled) {
                val bip84BtcAddress = privateKeyToNativeSegWitBip84BtcAddress(privateKey)
                emit(uiState.value.bip84BtcAddress.copy(address = bip84BtcAddress))
            }

            if (uiState.value.ethAddress.isEnabled) {
                val ethAddress = privateKeyToEthAddress(privateKey)
                emit(uiState.value.ethAddress.copy(address = ethAddress))
            }

            i = i.plus(BigInteger.ONE)
        }

        stop()
    }

    private fun updateAddresses(cryptoAddress: CryptoAddress) {
        _uiState.update { currentState ->
            when (cryptoAddress.type) {
                AddressType.PrivateKey -> currentState.copy(index = BigInteger(cryptoAddress.address))
                AddressType.Bip44 -> currentState.copy(bip44BtcAddress = cryptoAddress)
                AddressType.Bip84 -> currentState.copy(bip84BtcAddress = cryptoAddress)
                AddressType.Eth  -> currentState.copy(ethAddress = cryptoAddress)
            }
        }
    }
}