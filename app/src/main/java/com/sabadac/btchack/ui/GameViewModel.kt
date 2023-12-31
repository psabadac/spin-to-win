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
import java.security.MessageDigest

class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        GameUiState(
            passphrase = CryptoAddress(address = "", AddressType.Passphrase, isEnabled = false),
            privateKey = CryptoAddress("", AddressType.PrivateKey, true),
            bip44BtcAddress = CryptoAddress("", AddressType.Bip44, true),
            bip84BtcAddress = CryptoAddress("", AddressType.Bip84, true),
            ethAddress = CryptoAddress("", AddressType.Eth, true),
            isSpinning = false,
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    private var index = BigInteger.valueOf(2)
    private val networkParameters = NetworkParameters.fromID(NetworkParameters.ID_MAINNET)
    private val max = BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364140", 16)
    private val sha256 = MessageDigest.getInstance("SHA-256")

    init {
        val two = BigInteger.valueOf(2)
        val bip44BtcAddress =
            CryptoAddress(privateKeyToLegacyBip44BtcAddress(two), AddressType.Bip44, true)
        val bip84BtcAddress =
            CryptoAddress(privateKeyToNativeSegWitBip84BtcAddress(two), AddressType.Bip84, true)
        val ethAddress = CryptoAddress(privateKeyToEthAddress(two), AddressType.Eth, true)
        val privateKey = CryptoAddress(two.toString(16), AddressType.PrivateKey, true)

        _uiState.update { currentState ->
            currentState.copy(
                bip44BtcAddress = bip44BtcAddress,
                bip84BtcAddress = bip84BtcAddress,
                ethAddress = ethAddress,
                privateKey = privateKey
            )
        }
    }

    fun resume() {
        _uiState.update { currentState ->
            currentState.copy(isSpinning = true)
        }

        viewModelScope.launch(Dispatchers.IO) {
            spin().collect { cryptoAddress ->
                updateAddresses(cryptoAddress)
            }
        }
    }

    fun pause() {
        _uiState.update { currentState ->
            currentState.copy(isSpinning = false)
        }
    }

    fun updateChecksum(passphrase: String) {
        viewModelScope.launch(Dispatchers.IO) {
            updateAddresses(uiState.value.passphrase.copy(address = passphrase))
            index = BigInteger(sha256.digest(passphrase.toByteArray()))
            if (index < BigInteger.ZERO || index > max) return@launch

            val privateKey = index

            val radix = if (uiState.value.privateKey.isEnabled) 16 else 10
            updateAddresses(uiState.value.privateKey.copy(address = index.toString(radix)))

            if (uiState.value.bip44BtcAddress.isEnabled) {
                val bip44BtcAddress = privateKeyToLegacyBip44BtcAddress(privateKey)
                updateAddresses(uiState.value.bip44BtcAddress.copy(address = bip44BtcAddress))
            }

            if (uiState.value.bip84BtcAddress.isEnabled) {
                val bip84BtcAddress = privateKeyToNativeSegWitBip84BtcAddress(privateKey)
                updateAddresses(uiState.value.bip84BtcAddress.copy(address = bip84BtcAddress))
            }

            if (uiState.value.ethAddress.isEnabled) {
                val ethAddress = privateKeyToEthAddress(privateKey)
                updateAddresses(uiState.value.ethAddress.copy(address = ethAddress))
            }
        }
    }

    fun updateCheck(addressType: AddressType) {
        _uiState.update { currentState ->
            when (addressType) {
                AddressType.Passphrase -> currentState
                AddressType.PrivateKey -> {
                    val radix = if (uiState.value.privateKey.isEnabled) 10 else 16
                    currentState.copy(
                        privateKey = uiState.value.privateKey.copy(
                            isEnabled = !uiState.value.privateKey.isEnabled,
                            address = index.toString(radix)
                        )
                    )
                }

                AddressType.Bip44 -> currentState.copy(
                    bip44BtcAddress = uiState.value.bip44BtcAddress.copy(
                        isEnabled = !uiState.value.bip44BtcAddress.isEnabled
                    )
                )

                AddressType.Bip84 -> currentState.copy(
                    bip84BtcAddress = uiState.value.bip84BtcAddress.copy(
                        isEnabled = !uiState.value.bip84BtcAddress.isEnabled
                    )
                )

                AddressType.Eth -> currentState.copy(
                    ethAddress = uiState.value.ethAddress.copy(
                        isEnabled = !uiState.value.ethAddress.isEnabled
                    )
                )
            }
        }

        if (!uiState.value.bip44BtcAddress.isEnabled
            && !uiState.value.bip84BtcAddress.isEnabled
            && !uiState.value.ethAddress.isEnabled
        ) {
            pause()
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

    fun privateKeyToEthAddress(privateKey: BigInteger): String =
        Credentials.create(ECKeyPair.create(privateKey)).address

    private fun spin(): Flow<CryptoAddress> = flow {

        while (uiState.value.isSpinning) {
            val privateKey = index
            val radix = if (uiState.value.privateKey.isEnabled) 16 else 10
            emit(uiState.value.privateKey.copy(address = index.toString(radix)))

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

            index = index.plus(BigInteger.ONE)
        }

        pause()
    }

    private fun updateAddresses(cryptoAddress: CryptoAddress) {
        _uiState.update { currentState ->
            when (cryptoAddress.type) {
                AddressType.Passphrase -> currentState.copy(passphrase = cryptoAddress)
                AddressType.PrivateKey -> currentState.copy(privateKey = cryptoAddress)
                AddressType.Bip44 -> currentState.copy(bip44BtcAddress = cryptoAddress)
                AddressType.Bip84 -> currentState.copy(bip84BtcAddress = cryptoAddress)
                AddressType.Eth -> currentState.copy(ethAddress = cryptoAddress)
            }
        }
    }
}