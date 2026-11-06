package com.spring.eventhub.users.service;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.spring.eventhub.config.AppSecurityProperties;

@Component 
public class ApiKeyHasher {
    private static final String HMAC_ALGORITHM = "HmacSHA256";

   
    private final String pepper;
    public ApiKeyHasher(AppSecurityProperties securityProperties){
        this.pepper = securityProperties.pepper();
    }

  

    public String hashHmacSHA256(String input) {
        if (input == null || input.isEmpty()) {
                throw new IllegalArgumentException("The input is null");
            }
        
        try {

            
            SecretKeySpec secretKeySpec = new SecretKeySpec(pepper.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
           
            mac.init(secretKeySpec);
            byte[] encodedHash = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return byteToHex(encodedHash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error creating Hash HMAC", e);

        }
    }

    private String byteToHex(byte[] hash) {
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
}
