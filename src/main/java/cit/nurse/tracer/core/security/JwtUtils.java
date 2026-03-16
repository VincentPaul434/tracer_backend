package cit.nurse.tracer.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import jakarta.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);
    private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.RS256;

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final boolean staticKeysLoaded;

    public JwtUtils(
        @Value("${app.security.jwt.private-key:}") String privateKeyEncoded,
        @Value("${app.security.jwt.public-key:}") String publicKeyEncoded,
        Environment environment
    ) {
        this.staticKeysLoaded = StringUtils.hasText(privateKeyEncoded) && StringUtils.hasText(publicKeyEncoded);
        KeyPair keyPair = loadOrGenerateKeyPair(privateKeyEncoded, publicKeyEncoded, environment);
        this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
    }

    @PostConstruct
    void logKeyStatus() {
        if (staticKeysLoaded) {
            log.info("JWT Security: Static RSA Keys Loaded Successfully");
        }
    }

    public String generateSubmissionToken(UUID submissionId, Duration ttl) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(ttl);

        return Jwts.builder()
            .setSubject(submissionId.toString())
            .claim("submissionId", submissionId.toString())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiresAt))
            .signWith(privateKey, SIGNATURE_ALGORITHM)
            .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public UUID extractSubmissionId(String token) {
        Claims claims = parseClaims(token).getBody();
        return UUID.fromString(claims.getSubject());
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(token);
    }

    private KeyPair loadOrGenerateKeyPair(String privateKeyPem, String publicKeyPem, Environment environment) {
        if (StringUtils.hasText(privateKeyPem) && StringUtils.hasText(publicKeyPem)) {
            return new KeyPair(parsePublicKey(publicKeyPem), parsePrivateKey(privateKeyPem));
        }

        if (environment.acceptsProfiles("prod")) {
            log.error("JWT RSA keys are missing in 'prod' profile. Configure app.security.jwt.private-key and app.security.jwt.public-key immediately.");
        } else {
            log.warn("JWT RSA keys are not configured; generating an ephemeral in-memory key pair for this process.");
        }
        return generateKeyPair();
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to generate RSA key pair", ex);
        }
    }

    private RSAPrivateKey parsePrivateKey(String pem) {
        try {
            String normalized = normalizePem(pem, "PRIVATE KEY");
            byte[] decoded = Decoders.BASE64.decode(normalized);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(spec);
            return (RSAPrivateKey) privateKey;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid RSA private key PEM", ex);
        }
    }

    private RSAPublicKey parsePublicKey(String pem) {
        try {
            String normalized = normalizePem(pem, "PUBLIC KEY");
            byte[] decoded = Decoders.BASE64.decode(normalized);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
            return (RSAPublicKey) publicKey;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid RSA public key PEM", ex);
        }
    }

    private String normalizePem(String pem, String type) {
        return pem
            .replace("-----BEGIN " + type + "-----", "")
            .replace("-----END " + type + "-----", "")
            .replaceAll("\\s", "");
    }
}
