package tech.demoproject.android_chat_app.utilities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/***
 * Created by HoangRyan aka LilDua on 12/30/2023.
 */
public class PasswordHasher {
    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] digest = md.digest();

            // Convert the byte array to a hexadecimal string
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
