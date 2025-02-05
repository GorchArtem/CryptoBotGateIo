package com.adamgenesis.gateio.client;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

public class SignatureService {
    private final String secretKey;

    public SignatureService(String secretKey) {
        this.secretKey = secretKey;
    }

    public String generateSignature(String channel, String event, long serverTime) {
        try {
            String message = String.format("channel=%s&event=%s&time=%d", channel, event, serverTime);
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            String signature = HexFormat.of().formatHex(rawHmac);

            return signature;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании подписи API Gate.io", e);
        }
    }
}
