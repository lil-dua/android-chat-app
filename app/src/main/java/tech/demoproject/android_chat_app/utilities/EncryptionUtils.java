package tech.demoproject.android_chat_app.utilities;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/***
 * Created by HoangRyan aka LilDua on 12/30/2023.
 */
public class EncryptionUtils {
    public String ivStringKey = "";
    public String encryptMessage(String message) throws Exception {
        // Generate AES key and initialization vector
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        keygen.init(256);

        SecretKey key = keygen.generateKey();
        Cipher cipherIV = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipherIV.init(Cipher.ENCRYPT_MODE, key);

        byte[] iv = cipherIV.getIV();
        String ivString = Base64.encodeToString(iv, Base64.URL_SAFE);
        ivStringKey = ivString;
        // Perform encryption
        IvParameterSpec ivParameterSpec = new IvParameterSpec(Base64.decode(ivString, Base64.URL_SAFE));
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(
                Constants.SECRET_KEY.toCharArray(),
                Base64.decode(Constants.SALT, Base64.URL_SAFE),
                10000,
                256
        );

        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secretKeyEncrypt = new SecretKeySpec(tmp.getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeyEncrypt, ivParameterSpec);

        byte[] encryptedBytes = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(encryptedBytes, Base64.URL_SAFE);
    }

    public String decryptMessage(String message, String iv) throws Exception {
        IvParameterSpec ivParameterSpecDecrypt = new IvParameterSpec(Base64.decode(iv, Base64.URL_SAFE));
        SecretKeyFactory factoryDecrypt = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKeySpec specDecrypt = new PBEKeySpec(
                Constants.SECRET_KEY.toCharArray(),
                Base64.decode(Constants.SALT, Base64.URL_SAFE),
                10000,
                256
        );

        SecretKey tmpDecrypt = factoryDecrypt.generateSecret(specDecrypt);
        SecretKeySpec secretKeyDecrypt = new SecretKeySpec(tmpDecrypt.getEncoded(), "AES");
        Cipher cipherDecrypt = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKeyDecrypt, ivParameterSpecDecrypt);

        byte[] decryptedBytes = cipherDecrypt.doFinal(Base64.decode(message, Base64.URL_SAFE));
        String decryptedMessage = new String(decryptedBytes, StandardCharsets.UTF_8);

        return decryptedMessage;
    }
}
