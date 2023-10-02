package io.angularpay.identity.helpers;

import io.angularpay.identity.models.JwtPayload;
import io.angularpay.identity.models.JwtToken;
import org.springframework.stereotype.Service;

import java.util.Base64;

import static io.angularpay.identity.helpers.Helper.fromJson;

@Service
public class JwtHelper {

    public JwtToken toJwtToken(String accessToken) {
        String[] chunks = accessToken.split("\\.");

        Base64.Decoder decoder = Base64.getDecoder();
        String payload = new String(decoder.decode(chunks[1]));
        String header = new String(decoder.decode(chunks[0]));
        String signature = chunks[2];

        return JwtToken.builder()
                .header(header)
                .payload(payload)
                .signature(signature)
                .build();
    }

    public JwtPayload getJwtPayload(String accessToken) {
        JwtToken jwtToken = this.toJwtToken(accessToken);
        return fromJson(jwtToken.getPayload(), JwtPayload.class);
    }
}
