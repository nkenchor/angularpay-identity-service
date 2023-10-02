package io.angularpay.identity.util;

import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.util.StringUtils;

import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RsaUtil {

    private static final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RS512;

    private static RSAPublicKey getPublicKey(byte[] publicKeyBytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(publicKeyBytes);
        return (RSAPublicKey) keyFactory.generatePublic(keySpecX509);
    }

    private static PrivateKey getPrivateKey(byte[] privateKeyBytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(privateKeyBytes);
        return keyFactory.generatePrivate(keySpecPKCS8);
    }

    public static RsaKeys generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            String nonFormattedPublicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            String nonFormattedPrivateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

            RsaKeys rsaKey = RsaKeys.builder().base64PrivateKey(nonFormattedPrivateKey).base64PublicKey(nonFormattedPublicKey).build();
            printKeys(rsaKey);
            return rsaKey;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String certificateFormat(String unformatted) {
        if (!StringUtils.hasText(unformatted) || unformatted.length() < 64) return unformatted;
        return unformatted.replaceAll("(.{64})", "$1\n");
    }

    static void printKeys(RsaKeys rsaKeys) {
        // private key
        System.out.println(rsaKeys.getBase64PrivateKey());
        System.out.println("-----BEGIN PRIVATE KEY-----");
        System.out.println(certificateFormat(rsaKeys.getBase64PrivateKey()));
        System.out.println("-----END PRIVATE KEY-----\n");

        // public key
        System.out.println(rsaKeys.getBase64PublicKey());
        System.out.println("-----BEGIN PUBLIC KEY-----");
        System.out.println(certificateFormat(rsaKeys.getBase64PublicKey()));
        System.out.println("-----END PUBLIC KEY-----\n");
    }
}



