package com.decard.exampleSrc.desfire.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class is responsible for secure storage of Mifare DES and AES-128 keys in a Bouncy Castle Keystore (BKS).
 * For the first setup a 'passphrase' is used to derive the keystore password using 'initialize(char[] passphrase)'.
 * The derived keystore password is stored in Encrypted Shared Preferences but the salt is stored in 'Standard' = unencrypted Shared Preferences.
 * The reason for that is simple: if the app & keystore is part of the regular Google Drive backup the Encrypted Shared Preferences don't get part
 * of the backup. If you re-enter the passphrase the correct keystore password is re-generated.
 * The password derivation is done using PBKDF2 with 'PBKDF2WithHmacSHA1' algorithm.
 * <p>
 * 1) add in build(app).gradle:
 * implementation 'androidx.security:security-crypto:1.0.0'
 * 2) construct the class with the context (e.g. 'CustomKeystore customKeystore = new CustomKeystore(getApplicationContext());')
 * 3a) first start: initialize the class with a passphrase
 * - or -
 * 3b) following starts: storeKey or readKey. You can check successful initializing by 'getIsLibraryInitialized'
 * 4) store a secret key (DES or AES) by providing the key number
 * 5) read a secret key (DES or AES) by providing the key number, return is null when key is not present
 * 6) getKeystoreAliases() returns a List<String> containing all stored key aliases ('key_x')
 * <p>
 * For recovery reasons you can re-enter the passphrase and the keystore password is derived using 'recoveryInitialization(char[] passphrase)'
 * <p>
 * Call 'lastErrorMessage' is you want to know why an operation failed
 * <p>
 * The minimum Android SDK version is 23 (M) due to Encrypted Shared Preferences (minimum SDK 23)
 */



public class CustomKeystore {

    private static final String TAG = CustomKeystore.class.getSimpleName();
    private final String keystoreType = "BKS"; // Bouncy Castle Keystore, available on Android SDK 1+
    private final String keystoreFileName = "customkeystorebc.bks"; // located in internal storage / files
    private char[] keystorePassword;
    private byte[] keystorePasswordBytes;
    private final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";
    private int PBKDF2_NUMBER_ITERATIONS = 10000;
    private byte[] PBKDF2_SALT_BYTES;
    private final int PBKDF2_KEY_LENGTH = 256;
    private final String keyAlias = "key_";
    private boolean isKeyAes = false;

    /**
     * section for shared preferences
     */
    private SharedPreferences sharedPreferences;
    private final String UNENCRYPTED_PREFERENCES_FILENAME = "custom_keystore_prefs";
    private final String PBKDF2_SALT = "pbkdf2_salt";
    private final String PBKDF2_ITERATIONS = "pbkdf2_iterations";

    /**
     * section for encrypted shared preferences
     */
    private String MAIN_KEY_ALIAS; // for the masterKey
    private SharedPreferences encryptedSharedPreferences;
    private final String ENCRYPTED_PREFERENCES_FILENAME = "encrypted_custom_keystore_prefs";
    private final String KEYSTORE_PASSWORD_STORAGE = "keystore_password";

    // storing of a private and public EC keypair
    private final String ENCRYPTED_EC_PRIVATE_KEY = "encrypted_ec_private_key";
    private final String ENCRYPTED_EC_PUBLIC_KEY = "encrypted_ec_public_key";

    /**
     * general use
     */

    private Context context;
    private boolean isAndroidSdkVersionTooLow = false;
    private boolean isUnencryptedDataAvailable = false; // checks for a first run
    private boolean isLibraryInitialized = false;
    private String lastErrorMessage = "";

    public CustomKeystore(Context context) {
        lastErrorMessage = "";
        this.context = context;

        Log.d(TAG, "create CustomKeyStore ");
        // this is a hardcoded check to prevent on working when Android SDK is < M = 23
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.e(TAG, "The minimum Android SDK version is below 23 (M), aborted");
            lastErrorMessage = "The minimum Android SDK version is below 23 (M), aborted";
            isAndroidSdkVersionTooLow = true;
            return;
        }
        // check for first run
        if (!isUnencryptedDataAvailable) {
            sharedPreferences = context.getSharedPreferences(UNENCRYPTED_PREFERENCES_FILENAME, Context.MODE_PRIVATE);

            // create or open EncryptedSharedPreferences
            try {
                encryptedSharedPreferences = EncryptedSharedPreferences.create(
                        ENCRYPTED_PREFERENCES_FILENAME,
                        MAIN_KEY_ALIAS,
                        context,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );
            } catch (GeneralSecurityException | IOException e) {
                Log.e(TAG, "Exception: " + e.getMessage());
                lastErrorMessage = "Exception: " + e.getMessage();
                return;
            }
            if (PBKDF2_SALT_BYTES == null) {
                SecureRandom secureRandom = new SecureRandom();
                PBKDF2_SALT_BYTES = new byte[32];
                secureRandom.nextBytes(PBKDF2_SALT_BYTES);
            }
            // store the parameter
            try {
                sharedPreferences.edit().putInt(PBKDF2_ITERATIONS, PBKDF2_NUMBER_ITERATIONS).apply();
                sharedPreferences.edit().putString(PBKDF2_SALT, base64Encoding(PBKDF2_SALT_BYTES)).apply();
            } catch (Exception e) {
                Log.e(TAG, "Error on storage of SALT: " + e.getMessage());
                lastErrorMessage = "Exception: " + e.getMessage();
                return;
            }
        }

        // encrypted shared preferences
        // Although you can define your own key generation parameter specification, it's
        // recommended that you use the value specified here.
        KeyGenParameterSpec keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC;
        try {
            MAIN_KEY_ALIAS = MasterKeys.getOrCreate(keyGenParameterSpec);
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Exception: " + e.getMessage());
            lastErrorMessage = "Exception: " + e.getMessage();
            return;
        }
        // generate keystore file
        if (!isFilePresent(keystoreFileName)) {
            boolean crSuc = createKeyStore();
            Log.d(TAG, "create keystore success ? : " + crSuc);
        }
        checkIsLibraryInitialized();

        if (isLibraryInitialized) {
            Log.d(TAG, "Library initialized");
            lastErrorMessage = "Library initialized";
        } else {
            Log.d(TAG, "Library NOT initialized");
            lastErrorMessage = "Library NOT initialized";
        }
    }

    public boolean initialize(char[] passphrase) {
        lastErrorMessage = "";
        Log.d(TAG,"initialize library");
        if (isAndroidSdkVersionTooLow) {
            Log.e(TAG, "The minimum Android SDK version is below 23 (M), aborted");
            lastErrorMessage = "The minimum Android SDK version is below 23 (M), aborted";
            return false;
        }
        try {
            if (PBKDF2_SALT_BYTES == null) {
                SecureRandom secureRandom = new SecureRandom();
                PBKDF2_SALT_BYTES = new byte[32];
                secureRandom.nextBytes(PBKDF2_SALT_BYTES);
            }
            SecretKeyFactory secretKeyFactory = null;
            secretKeyFactory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            KeySpec keySpec = new PBEKeySpec(passphrase, PBKDF2_SALT_BYTES, PBKDF2_NUMBER_ITERATIONS, PBKDF2_KEY_LENGTH);
            keystorePasswordBytes = secretKeyFactory.generateSecret(keySpec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Log.e(TAG, "Exception: " + e.getMessage());
            lastErrorMessage = "Exception on generating the derived keystore password: " + e.getMessage();
            return false;
        }
        // store the unencrypted data
        try {
            sharedPreferences.edit().putInt(PBKDF2_ITERATIONS, PBKDF2_NUMBER_ITERATIONS).apply();
            sharedPreferences.edit().putString(PBKDF2_SALT, base64Encoding(PBKDF2_SALT_BYTES)).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error on storage of SALT: " + e.getMessage());
            lastErrorMessage = "Exception: " + e.getMessage();
            return false;
        }
        Log.d(TAG, "storage of SALT SUCCESS");
        // store the encrypted data
        encryptedSharedPreferences
                .edit()
                .putString(KEYSTORE_PASSWORD_STORAGE, base64Encoding(keystorePasswordBytes))
                .apply();
        //keystorePassword = bytesToChars(keystorePasswordBytes);
        keystorePassword = convertByteArrayToCharArray(keystorePasswordBytes);
        isLibraryInitialized = true;
        lastErrorMessage = "library is initialized";
        Log.d(TAG,lastErrorMessage);
        return true;
    }

    /**
     * The recoveryInitialization is used after a recovery of app data using Google Drive. During recovery the keystore and the salt are restored
     * but not the derived keystore password. This method recovers the derived keystore password.
     *
     * @param passphrase
     * @return true on success
     */
    public boolean recoveryInitialization(char[] passphrase) {
        lastErrorMessage = "";
        if (isAndroidSdkVersionTooLow) {
            Log.e(TAG, "The minimum Android SDK version is below 23 (M), aborted");
            lastErrorMessage = "The minimum Android SDK version is below 23 (M), aborted";
            return false;
        }
        // check that a salt is already stored
        boolean success = getPbkdf2Salt();
        if (!success) {
            Log.e(TAG, "There is no PBKDF2 salt stored, aborted");
            lastErrorMessage = "There is no PBKDF2 salt stored, aborted";
            return false;
        }
        success = getPbkdf2NumberIterations();
        if (!success) {
            Log.e(TAG, "There is no PBKDF2 number of iterations stored, aborted");
            lastErrorMessage = "There is no PBKDF2 number of iterations stored, aborted";
            return false;
        }
        try {
            // use the stored PBKDF2_SALT_BYTES
            SecretKeyFactory secretKeyFactory = null;
            secretKeyFactory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            KeySpec keySpec = new PBEKeySpec(passphrase, PBKDF2_SALT_BYTES, PBKDF2_NUMBER_ITERATIONS, PBKDF2_KEY_LENGTH);
            keystorePasswordBytes = secretKeyFactory.generateSecret(keySpec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Log.e(TAG, "Exception: " + e.getMessage());
            lastErrorMessage = "Exception on generating the derived keystore password: " + e.getMessage();
            return false;
        }
        // store the encrypted data
        encryptedSharedPreferences
                .edit()
                .putString(KEYSTORE_PASSWORD_STORAGE, base64Encoding(keystorePasswordBytes))
                .apply();
        keystorePassword = convertByteArrayToCharArray(keystorePasswordBytes);
        isLibraryInitialized = true;
        lastErrorMessage = "library is initialized, the derived keystore password is restored";
        return true;
    }

    private boolean getPbkdf2NumberIterations() {
        PBKDF2_NUMBER_ITERATIONS = sharedPreferences.getInt(PBKDF2_ITERATIONS, -1);
        if (PBKDF2_NUMBER_ITERATIONS > 1) {
            return true;
        } else {
            return false;
        }
    }

    private boolean getPbkdf2Salt() {
        String data = sharedPreferences.getString(PBKDF2_SALT, "");
        if (!TextUtils.isEmpty(data)) {
            PBKDF2_SALT_BYTES = base64Decoding(data);
            return true;
        }
        return false;
    }

    private boolean getKeystorePasswordBytes() {
        String data = encryptedSharedPreferences.getString(KEYSTORE_PASSWORD_STORAGE, "");
        if (!TextUtils.isEmpty(data)) {
            keystorePasswordBytes = base64Decoding(data);
            if (keystorePasswordBytes == null) {
                Log.e(TAG, "getKeystorePasswordBytes failed");
                return false;
            }
            keystorePassword = convertByteArrayToCharArray(keystorePasswordBytes);
            return true;
        }
        return false;
    }


    // checks if the salt and iteration is available from storage
    private boolean checkIsUnencryptedDataAvailable() {
        if ((getPbkdf2NumberIterations()) && (getPbkdf2Salt())) {
            isUnencryptedDataAvailable = true;
            return true;
        } else {
            isUnencryptedDataAvailable = false;
            return false;
        }
    }


    // checks if the salt, iteration and keystore password is available from storage
    private boolean checkIsLibraryInitialized() {
        if ((getPbkdf2NumberIterations()) && (getPbkdf2Salt()) && (getKeystorePasswordBytes())) {
            isLibraryInitialized = true;
            return true;
        } else {
            isLibraryInitialized = false;
            return false;
        }
    }

    /**
     * section for keystore handling
     */

    private boolean createKeyStore() {
        boolean keystorePasswordAvailable = getKeystorePasswordBytes();
        if (!keystorePasswordAvailable) {
            Log.e(TAG, "No keystorePassword present, aborted: " + keystoreFileName);
            return false;
        }
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance(keystoreType);
            ks.load(null, keystorePassword);
            FileOutputStream fos = context.openFileOutput(keystoreFileName, Context.MODE_PRIVATE);
            ks.store(fos, keystorePassword);
            return true;
        } catch (KeyStoreException | CertificateException | IOException |
                 NoSuchAlgorithmException e) {
            Log.e(TAG, "Exception: " + e.getMessage());
            lastErrorMessage = "Exception: " + e.getMessage();
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean storeKey(byte keyNumber, byte[] key) {
        if (isAndroidSdkVersionTooLow) {
            Log.e(TAG, "The minimum Android SDK version is below 23 (M), aborted");
            lastErrorMessage = "The minimum Android SDK version is below 23 (M), aborted";
            return false;
        }
        // sanity checks on keys
        if (key == null) {
            Log.e(TAG, "key is NULL, aborted");
            lastErrorMessage = "key is NULL, aborted";
            return false;
        }
        if ((key.length != 8) && (key.length != 16)) {
            Log.e(TAG, "key length is not 8 or 16, aborted");
            lastErrorMessage = "key length is not 8 or 16, aborted";
            return false;
        }
        if (key.length == 16) isKeyAes = true;
        // build alias name
        StringBuilder sb = new StringBuilder();
        sb.append(keyAlias);
        sb.append(keyNumber);
        String alias = sb.toString();
        Log.d(TAG, "alias: " + alias);
        boolean keystorePasswordAvailable = getKeystorePasswordBytes();
        if (!keystorePasswordAvailable) {
            Log.e(TAG, "No keystorePassword present, aborted: " + keystoreFileName);
            lastErrorMessage = "No keystorePassword present, aborted: " + keystoreFileName;
            return false;
        }
        if (!isFilePresent(keystoreFileName)) {
            Log.e(TAG, "No keystoreFile present, aborted: " + keystoreFileName);
            lastErrorMessage = "No keystoreFile present, aborted: " + keystoreFileName;
            return false;
        }
        try {
            SecretKey secretKey;
            if (isKeyAes) {
                secretKey = new SecretKeySpec(key, "AES");
            } else {
                secretKey = new SecretKeySpec(key, "DES");
            }
            KeyStore keyStore = KeyStore.getInstance(keystoreType);
            FileInputStream fileInputStream = context.openFileInput(keystoreFileName);
            keyStore.load(fileInputStream, keystorePassword);

            if (keyStore.containsAlias(alias)) {
                Log.d(TAG, "alias is present in keyStore, overwritten: " + alias);
            }
            //Creating the KeyStore.ProtectionParameter object
            KeyStore.ProtectionParameter protectionParam = new KeyStore.PasswordProtection(keystorePassword);
            //Creating SecretKeyEntry object
            KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(secretKey);
            keyStore.setEntry(alias, secretKeyEntry, protectionParam);
            Log.d(TAG, "key is stored");
            FileOutputStream fos = context.openFileOutput(keystoreFileName, Context.MODE_PRIVATE);
            keyStore.store(fos, keystorePassword);
            lastErrorMessage = "key is stored";
            return true;
        } catch (IOException | GeneralSecurityException e) {
            Log.e(TAG, "Exception on keystore usage, aborted");
            Log.e(TAG, "Exception: " + e.getMessage());
            lastErrorMessage = "Exception: " + e.getMessage();
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public byte[] readKey(byte keyNumber) {
        if (isAndroidSdkVersionTooLow) {
            Log.e(TAG, "The minimum Android SDK version is below 23 (M), aborted");
            lastErrorMessage = "The minimum Android SDK version is below 23 (M), aborted";
            return null;
        }
        Log.d(TAG, "readKey");
        // build alias name
        StringBuilder sb = new StringBuilder();
        sb.append(keyAlias);
        sb.append(keyNumber);
        String alias = sb.toString();
        Log.d(TAG, "readKey, alias: " + alias);
        boolean keystorePasswordAvailable = getKeystorePasswordBytes();
        if (!keystorePasswordAvailable) {
            Log.e(TAG, "No keystorePassword present, aborted: " + keystoreFileName);
            lastErrorMessage = "No keystorePassword present, aborted: " + keystoreFileName;
            return null;
        }
        if (!isFilePresent(keystoreFileName)) {
            Log.d(TAG, "No keystoreFile present, aborted: " + keystoreFileName);
            lastErrorMessage = "No keystoreFile present, aborted: " + keystoreFileName;
            return null;
        } else {
            try {
                KeyStore keyStore = KeyStore.getInstance(keystoreType);
                FileInputStream fileInputStream = context.openFileInput(keystoreFileName);
                keyStore.load(fileInputStream, keystorePassword);
                //Creating the KeyStore.ProtectionParameter object
                KeyStore.ProtectionParameter protectionParam = new KeyStore.PasswordProtection(keystorePassword);
                // Creating the KeyStore.SecretKeyEntry object
                KeyStore.SecretKeyEntry secretKeyEnt = (KeyStore.SecretKeyEntry) keyStore.getEntry(alias, protectionParam);
                // Creating SecretKey object
                if (secretKeyEnt == null) {
                    Log.e(TAG, "no entry found, aborted");
                    lastErrorMessage = "no entry found, aborted";
                    return null;
                }
                SecretKey secretKey = secretKeyEnt.getSecretKey();
                Log.d(TAG, "Algorithm used to generate key : " + secretKey.getAlgorithm());
                byte[] retrievedKey = secretKey.getEncoded();
                lastErrorMessage = "success";
                return retrievedKey;
            } catch (IOException | GeneralSecurityException e) {
                Log.e(TAG, "Exception on keystore usage, aborted");
                Log.e(TAG, "Exception: " + e.getMessage());
                lastErrorMessage = "Exception: " + e.getMessage();
                return null;
            }
        }
    }

    public List<String> getKeystoreAliases() {
        Log.d(TAG, "getKeystoreAliases");
        if (!isFilePresent(keystoreFileName)) {
            Log.d(TAG, "No keystoreFile present, aborted: " + keystoreFileName);
            return null;
        } else {
            try {
                KeyStore keyStore = KeyStore.getInstance(keystoreType);
                FileInputStream fileInputStream = context.openFileInput(keystoreFileName);
                keyStore.load(fileInputStream, keystorePassword);

                Enumeration<String> aliases = keyStore.aliases();
                List<String> list = new ArrayList<>();
                while (aliases.hasMoreElements()) {
                    String ne = aliases.nextElement();
                    list.add(ne);
                }
                Log.d(TAG, "list has entries: " + list.size());
                lastErrorMessage = "success, list has entries: " + list.size();
                return list;
            } catch (IOException | GeneralSecurityException e) {
                Log.e(TAG, "Exception on keystore usage, aborted");
                Log.e(TAG, "Exception: " + e.getMessage());
                lastErrorMessage = "Exception: " + e.getMessage();
                return null;
            }
        }
    }

    /**
     * section for Encrypted Shared Preferences
     */

    public boolean saveEcPrivateKey(byte[] privateKeyBytesEc) {
        if ((privateKeyBytesEc == null) || (privateKeyBytesEc.length < 1)) return false;
        return saveToEncryptedSharedPreferences(ENCRYPTED_EC_PRIVATE_KEY, privateKeyBytesEc);
    }

    public boolean saveEcPublicKey(byte[] publicKeyBytesEc) {
        if ((publicKeyBytesEc == null) || (publicKeyBytesEc.length < 1)) return false;
        return saveToEncryptedSharedPreferences(ENCRYPTED_EC_PUBLIC_KEY, publicKeyBytesEc);
    }

    public byte[] readEcPrivateKeyEncoded() {
        return readFromEncryptedSharedPreferences(ENCRYPTED_EC_PRIVATE_KEY);
    }

    public byte[] readEcPublicKeyEncoded() {
        return readFromEncryptedSharedPreferences(ENCRYPTED_EC_PUBLIC_KEY);
    }

    private boolean saveToEncryptedSharedPreferences(String keyName, byte[] key) {
        // store the encrypted data
        encryptedSharedPreferences
                .edit()
                .putString(keyName, base64Encoding(key))
                .apply();
        return true;
    }

    private byte[] readFromEncryptedSharedPreferences(String keyName) {
        String data = encryptedSharedPreferences.getString(keyName, "");
        if (!TextUtils.isEmpty(data)) {
            byte[] keyBytes = base64Decoding(data);
            if (keyBytes == null) {
                Log.e(TAG, "getKeyBytes failed");
                return null;
            }
            return keyBytes;
        }
        return null;
    }

    /**
     * section for files
     */
    public boolean isFilePresent(String fileName) {
        File path = context.getFilesDir();
        File file = new File(path, fileName);
        return file.exists();
    }

    /**
     * section for converter
     */

    public static String base64Encoding(byte[] input) {
        return Base64.encodeToString(input, Base64.NO_WRAP);
    }

    public static byte[] base64Decoding(String input) {
        return Base64.decode(input, Base64.NO_WRAP);
    }

    // conversion from www.java2s.com
    // http://www.java2s.com/example/java-utility-method/byte-array-to-char-index-0.html
    private char[] convertByteArrayToCharArray(byte[] bytes) {
        char[] buffer = new char[bytes.length >> 1];
        for (int i = 0; i < buffer.length; i++) {
            int bpos = i << 1;
            char c = (char) (((bytes[bpos] & 0x00FF) << 8) + (bytes[bpos + 1] & 0x00FF));
            buffer[i] = c;
        }
        return buffer;
    }

    // http://www.java2s.com/example/java-utility-method/char-to-byte-array-index-0.html
    private byte[] convertCharArrayToByteArray(char[] buffer) {
        byte[] b = new byte[buffer.length << 1];
        for (int i = 0; i < buffer.length; i++) {
            int bpos = i << 1;
            b[bpos] = (byte) ((buffer[i] & 0xFF00) >> 8);
            b[bpos + 1] = (byte) (buffer[i] & 0x00FF);
        }
        return b;
    }

    /**
     * section for getter
     */

    public boolean isLibraryInitialized() {
        return isLibraryInitialized;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
}

