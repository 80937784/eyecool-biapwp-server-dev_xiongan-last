package cn.eyecool.tradelog.domain.police;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zfx
 * @ClassName DigestAuthenticator
 * @description DigestAuthenticator
 * @since 2026/9/29 15:12
 **/
public class DigestAuthenticator implements Authenticator {
    private final String username;
    private final String password;
    private final AtomicInteger ncCounter = new AtomicInteger(1);
    private static final SecureRandom RAND = new SecureRandom();

    public DigestAuthenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, Response response) {
        // 避免无限循环重试
        if (response.request().header("Authorization") != null) {
            return null;
        }

        String wwwAuthHeader = response.header("WWW-Authenticate");
        if (wwwAuthHeader == null || !wwwAuthHeader.startsWith("Digest ")) {
            return null;
        }

        Map<String, String> params = parseWwwAuthenticate(wwwAuthHeader);
        String realm = params.get("realm");
        String nonce = params.get("nonce");
        String qop = params.get("qop");
        String opaque = params.get("opaque");

        if (realm == null || nonce == null) {
            return null;
        }

        String method = response.request().method();
        String uri = response.request().url().encodedPath();

        String ha1 = md5(username + ":" + realm + ":" + password);
        String ha2 = md5(method + ":" + uri);

        int ncInt = ncCounter.getAndIncrement();
        String nc = String.format("%08x", ncInt);
        String cnonce = generateCnonce();

        String responseDigest;
        if ("auth".equals(qop)) {
            responseDigest = md5(ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + ha2);
        } else {
            responseDigest = md5(ha1 + ":" + nonce + ":" + ha2);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Digest ");
        sb.append("username=\"").append(username).append("\",");
        sb.append("realm=\"").append(realm).append("\",");
        sb.append("nonce=\"").append(nonce).append("\",");
        sb.append("uri=\"").append(uri).append("\",");
        sb.append("nc=").append(nc).append(",");
        sb.append("cnonce=\"").append(cnonce).append("\",");
        sb.append("qop=\"").append(qop).append("\",");
        sb.append("response=\"").append(responseDigest).append("\"");
        if (opaque != null) {
            sb.append(",opaque=\"").append(opaque).append("\"");
        }

        // 在原有请求基础上追加Authorization头，返回新Request
        return response.request().newBuilder()
                .header("Authorization", sb.toString())
                .build();
    }

    private Map<String, String> parseWwwAuthenticate(String header) {
        Map<String, String> map = new HashMap<>();
        String content = header.replace("Digest ", "");
        String[] parts = content.split(",");
        for (String part : parts) {
            String[] kv = part.trim().split("=", 2);
            if (kv.length == 2) {
                String k = kv[0].trim();
                String v = kv[1].trim().replace("\"", "");
                map.put(k, v);
            }
        }
        return map;
    }

    private String generateCnonce() {
        byte[] bytes = new byte[8];
        RAND.nextBytes(bytes);
        return toHex(bytes);
    }

    private String md5(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(data.getBytes(StandardCharsets.UTF_8));
            return toHex(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                sb.append("0");
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
