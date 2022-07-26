package com.example.demo;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.commons.codec.binary.Hex;
import org.json.simple.JSONObject;
import javax.crypto.Cipher;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Base64;

@SpringBootApplication
public class Demo1Application {

	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static String publicCertKey = "publicCertGLMT.crt";

    public static void main(String[] args) throws Exception {

        JSONObject invoice = new JSONObject();

        invoice.put("amount", 1);
        invoice.put("remarks", "Test");
        invoice.put("accountName", "Нэст эдүкэйшн ХХК");
        invoice.put("accountNumber", 1505187177);

        String checksum = sign(invoice.toJSONString());
        System.out.println("Checksum: " + checksum);
    }

    public static String sign(String plainText) throws Exception {

        byte[] hash = Sha256Encrypt(plainText);
        String hexHash = bytesToHex(hash).toLowerCase();
        PublicKey publicKey = getPublicKey(publicCertKey);
        byte[] sign = encrypt(hexHash, publicKey);
        String hexSign = Hex.encodeHexString(sign);

        return hexSign;
    }

    public static byte[] Sha256Encrypt(String data){

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));

            return hash;

        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public static String bytesToHex(byte[] bytes) {

        char[] hexChars = new char[bytes.length * 2];

        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        return new String(hexChars);
    }

    public static PublicKey getPublicKey(String filename) throws IOException, GeneralSecurityException {

        String publicKeyPEM = getKey(filename);

        return getPublicKeyFromString(publicKeyPEM);
    }

    private static String getKey(String filename) throws IOException {

        String strKeyPEM = "";
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;

        while ((line = br.readLine()) != null) {
            strKeyPEM += line + "\n";
        }

        br.close();

        return strKeyPEM;
    }

    public static PublicKey getPublicKeyFromString(String key) throws IOException, GeneralSecurityException {

        String publicKeyPEM = key;
        publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----\n", "");
        publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
        publicKeyPEM = publicKeyPEM.replace("-----BEGIN CERTIFICATE-----", "");
        publicKeyPEM = publicKeyPEM.replace("-----END CERTIFICATE-----", "");
        publicKeyPEM = publicKeyPEM.replace("\n", "");
        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
        InputStream certstream = new ByteArrayInputStream(encoded);
        Certificate cert = CertificateFactory.getInstance("X.509").generateCertificate(certstream);
        PublicKey publicKey = cert.getPublicKey();

        return publicKey;
    }

    public static byte[] encrypt(String data, PublicKey publicKey) throws Exception {

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(data.getBytes());
    }
}
