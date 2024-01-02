package tech.demoproject.android_chat_app.utilities;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/***
 * Created by HoangRyan aka LilDua on 12/30/2023.
 */
public class EncryptionUtils {
    public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    public byte[] encryptMessage(String message, byte[] recipientPublicKey) throws Exception {
        PublicKey publicKey = keyBytesToPublicKey(recipientPublicKey);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(message.getBytes());
    }

    public String decryptMessage(byte[] encryptedMessage, byte[] privateKey) throws Exception {
        PrivateKey privateKeyObj = keyBytesToPrivateKey(privateKey);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKeyObj);
        byte[] decryptedBytes = cipher.doFinal(encryptedMessage);
        return new String(decryptedBytes);
    }

    private PublicKey keyBytesToPublicKey(byte[] encodedKey) throws Exception {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    private PrivateKey keyBytesToPrivateKey(byte[] encodedKey) throws Exception {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
}
