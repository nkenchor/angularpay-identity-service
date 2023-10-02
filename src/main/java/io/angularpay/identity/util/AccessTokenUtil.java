package io.angularpay.identity.util;

import io.angularpay.identity.configurations.AngularPayConfiguration;
import io.angularpay.identity.helpers.JwtHelper;
import io.angularpay.identity.models.Token;
import io.angularpay.identity.models.TokenType;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static io.angularpay.identity.models.TokenType.ACCESS_TOKEN;
import static io.angularpay.identity.models.TokenType.REFRESH_TOKEN;

@Service
@Slf4j
public class AccessTokenUtil {

    private static final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RS512;
    private PrivateKey privateKey;
    private RSAPublicKey publicKey;
    private final AngularPayConfiguration.Security security;
    private final AngularPayConfiguration.Session session;
    private final JwtHelper jwtHelper;

    public AccessTokenUtil(
            AngularPayConfiguration angularPayConfiguration,
            JwtHelper jwtHelper) {
        this.security = angularPayConfiguration.getSecurity();
        this.session = angularPayConfiguration.getSession();
        this.jwtHelper = jwtHelper;

        this.initKeyPair(RsaKeys.builder()
                .base64PrivateKey(this.security.getPrivateKey())
                .base64PublicKey(this.security.getPublicKey())
                .build());
//         should match what's in the yaml file
//        RsaUtil.printKeys(new RsaKeys(this.security.getPrivateKey(), this.security.getPublicKey()));
//         utility method to generate private/public key pair
//        RsaKeys rsaKeys = RsaUtil.generateKeyPair();
//        this.initKeyPair(rsaKeys);
    }

    private void initKeyPair(RsaKeys rsaKeys) {
        try {
            byte[] privateKeyBytes = Base64.getDecoder().decode(rsaKeys.getBase64PrivateKey());
            privateKey = getPrivateKey(privateKeyBytes);

            byte[] publicKeyBytes = Base64.getDecoder().decode(rsaKeys.getBase64PublicKey());
            publicKey = getPublicKey(publicKeyBytes);
        } catch (Exception exception) {
            throw new RuntimeException("Unable to parse public/private key pair", exception);
        }
    }

    private RSAPublicKey getPublicKey(byte[] publicKeyBytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(publicKeyBytes);
        return (RSAPublicKey) keyFactory.generatePublic(keySpecX509);
    }

    private PrivateKey getPrivateKey(byte[] privateKeyBytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(privateKeyBytes);
        return keyFactory.generatePrivate(keySpecPKCS8);
    }

    public Optional<Token> generateToken(TokenGenerationParameters tokenGenerationParameters) {
        try {
            String accessToken = generateToken(ACCESS_TOKEN, tokenGenerationParameters);
            String refreshToken = generateToken(REFRESH_TOKEN, tokenGenerationParameters);
            return Optional.of(Token.builder()
                    .accessToken(accessToken)
                    .accessTokenExpiresIn(this.session.getDefaultAccessTokenExpirySeconds())
                    .refreshToken(refreshToken)
                    .refreshTokenExpiresIn(this.session.getDefaultRefreshTokenExpirySeconds())
                    .build());
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    private String generateToken(TokenType tokenType, TokenGenerationParameters tokenGenerationParameters) {
        Date now = Date.from(ZonedDateTime.now().toInstant());
        JwtBuilder builder = Jwts.builder()
                .signWith(signatureAlgorithm, privateKey)
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("kid", UUID.randomUUID().toString())
                .setId(UUID.randomUUID().toString())
                .setIssuer(this.security.getIssuer())
                .claim("azp", tokenGenerationParameters.getDeviceId())
                .claim("typ", tokenType.getType())
                .claim("user_reference", tokenGenerationParameters.getUserReference())
                .claim("auth_time", now)
                .setSubject(tokenGenerationParameters.getUsername())
                .setAudience(tokenGenerationParameters.getDeviceId())
                .setIssuedAt(now);

        long tokenExpirySeconds;
        if (tokenType == ACCESS_TOKEN) {
            builder.claim("roles", tokenGenerationParameters.getRoles());
            tokenExpirySeconds = this.session.getDefaultAccessTokenExpirySeconds();
        } else {
            tokenExpirySeconds = this.session.getDefaultRefreshTokenExpirySeconds();
        }

        builder.setExpiration(Date.from(ZonedDateTime.now()
                .plusSeconds(tokenExpirySeconds)
                .toInstant()));

        return builder.compact();
    }

    public boolean verifyToken(String accessToken) {
        try {
            Jwts.parser().setSigningKey(this.publicKey).parseClaimsJws(accessToken).getBody();
            return true;
        } catch (SignatureException | MalformedJwtException | ExpiredJwtException |
                UnsupportedJwtException | IllegalArgumentException exception) {
            return false;
        }
    }
}



