package com.bervan.encryption;

import com.bervan.common.model.PersistableTableData;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 16; // 128 bits

    /**
     * Encrypts text using AES-256-GCM
     * @param plainText text to encrypt
     * @param password user password
     * @return encrypted text with IV prepended (Base64 encoded)
     */
    public static String encrypt(String plainText, String password) {
        try {
            SecretKey secretKey = deriveKeyFromPassword(password);

            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

            byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] result = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encryptedData, 0, result, iv.length, encryptedData.length);

            return Base64.getEncoder().encodeToString(result);

        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypts text using AES-256-GCM
     * @param encryptedText encrypted text (Base64 encoded with IV prepended)
     * @param password user password
     * @return decrypted plain text
     */
    public static String decrypt(String encryptedText, String password) {
        try {
            byte[] data = Base64.getDecoder().decode(encryptedText);

            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedData = new byte[data.length - GCM_IV_LENGTH];
            System.arraycopy(data, 0, iv, 0, iv.length);
            System.arraycopy(data, iv.length, encryptedData, 0, encryptedData.length);

            SecretKey secretKey = deriveKeyFromPassword(password);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

            byte[] decryptedData = cipher.doFinal(encryptedData);

            return new String(decryptedData, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Decryption failed - wrong password or corrupted data", e);
        }
    }

    /**
     * Derives a 256-bit key from password using PBKDF2
     */
    private static SecretKey deriveKeyFromPassword(String password) {
        try {
            // Simple key derivation - for production use PBKDF2WithHmacSHA256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] key = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(key, ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Key derivation failed", e);
        }
    }

    /**
     * Validates if text is encrypted (starts with valid Base64)
     */
    public static boolean isEncrypted(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(text);
            return decoded.length > GCM_IV_LENGTH + GCM_TAG_LENGTH;
        } catch (Exception e) {
            return false;
        }
    }

    public static <ID extends Serializable, T extends PersistableTableData<ID>> T decryptAll(T item, String password) {
        T newItem = null;
        try {
            newItem = (T) item.getClass().getConstructor().newInstance();
            for (Field declaredField : item.getClass().getDeclaredFields()) {
                declaredField.setAccessible(true);
                if (declaredField.getType().equals(String.class)) {
                    String fieldValue = (String) declaredField.get(item);
                    if (isEncrypted(fieldValue)) {
                        declaredField.set(newItem, decrypt(fieldValue, password));
                    } else {
                        declaredField.set(newItem, fieldValue);
                    }
                } else {
                    Object fieldValue = declaredField.get(item);
                    declaredField.set(newItem, fieldValue);
                }

                declaredField.setAccessible(false);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return newItem;
    }
}