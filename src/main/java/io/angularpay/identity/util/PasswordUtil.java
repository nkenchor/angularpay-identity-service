package io.angularpay.identity.util;

import io.angularpay.identity.models.IsSamePasswordParameter;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class PasswordUtil {
    private final MessageDigest messageDigest;

    public PasswordUtil() {
        try {
            this.messageDigest = MessageDigest.getInstance("SHA3-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to get MessageDigest instance", e);
        }
    }

    public String toHashedPassword(String password) {
        byte[] hashedBytes = messageDigest.digest(password.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hashedBytes);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public boolean isSamePassword(IsSamePasswordParameter isSamePasswordParameter) {
        String hash = this.toHashedPassword(isSamePasswordParameter.getPlaintextPassword());
        return isSamePasswordParameter.getHashedPassword().equalsIgnoreCase(hash);
    }
}
