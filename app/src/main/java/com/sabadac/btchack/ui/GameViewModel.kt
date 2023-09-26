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
            "",
            "",
            "",
            false,
            BigInteger.ONE)
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    private val networkParameters = NetworkParameters.fromID(NetworkParameters.ID_MAINNET)

    init {
        val two = BigInteger.valueOf(2)
        val bip44BtcAddress = privateKeyToLegacyBip44BtcAddress(two)
        val bip84BtcAddress = privateKeyToNativeSegWitBip84BtcAddress(two)
        val ethAddress = privateKeyToEthAddress(two)

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
            emit(CryptoAddress(i.toString(), AddressType.PrivateKey))

            val bip44BtcAddress = privateKeyToLegacyBip44BtcAddress(privateKey)
            emit(CryptoAddress(bip44BtcAddress, AddressType.Bip44))

            val bip84BtcAddress = privateKeyToNativeSegWitBip84BtcAddress(privateKey)
            emit(CryptoAddress(bip84BtcAddress, AddressType.Bip84))

            val ethAddress = privateKeyToEthAddress(privateKey)
            emit(CryptoAddress(ethAddress, AddressType.Eth))
            i = i.plus(BigInteger.ONE)
        }

        stop()
    }

    private fun updateAddresses(cryptoAddress: CryptoAddress) {
        _uiState.update { currentState ->
            when (cryptoAddress.type) {
                AddressType.PrivateKey -> currentState.copy(index = BigInteger(cryptoAddress.address))
                AddressType.Bip44 -> currentState.copy(bip44BtcAddress = cryptoAddress.address)
                AddressType.Bip84 -> currentState.copy(bip84BtcAddress = cryptoAddress.address)
                AddressType.Eth  -> currentState.copy(ethAddress = cryptoAddress.address)
            }
        }
    }
}