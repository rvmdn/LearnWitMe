package com.learnwitme.core.security

/**
 * Interface abstraction for providing the database encryption passphrase.
 * Implementations manage key generation and secure retrieval via Android Keystore.
 */
interface DatabasePassphraseProvider {
    /**
     * Retrieves the byte array passphrase used to open the SQLCipher database.
     */
    fun getPassphrase(): ByteArray
}
