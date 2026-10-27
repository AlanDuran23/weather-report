package weatherreport_mvc.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utilidades de hash. {@link #hashSHA256} se usa para los códigos de
 * recuperación (aleatorios y de corta vida); las contraseñas usan
 * {@link #hashContrasena}, con sal y PBKDF2.
 */
public final class HashUtil {

    private HashUtil() {
    }

    public static String hashSHA256(String texto) {
        if (texto == null) {
            texto = "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                String h = Integer.toHexString(0xFF & b);
                if (h.length() == 1) {
                    hex.append('0');
                }
                hex.append(h);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 no disponible", e);
        }
    }

    public static boolean verificar(String textoPlano, String hash) {
        if (textoPlano == null || hash == null) {
            return false;
        }
        return hashSHA256(textoPlano).equalsIgnoreCase(hash);
    }

    // Contraseñas: PBKDF2-HMAC-SHA256 con sal, mismo formato que includes/auth.php
    // del panel web ("pbkdf2_sha256$iteraciones$sal_base64$hash_base64").
    private static final String PBKDF2_PREFIJO = "pbkdf2_sha256";
    private static final int PBKDF2_ITERACIONES = 600_000;
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Hash con sal para guardar una contraseña (no usar SHA-256 simple para esto). */
    public static String hashContrasena(String contrasena) {
        byte[] sal = new byte[16];
        RANDOM.nextBytes(sal);
        byte[] hash = pbkdf2(contrasena, sal, PBKDF2_ITERACIONES, 32);
        Base64.Encoder b64 = Base64.getEncoder();
        return PBKDF2_PREFIJO + "$" + PBKDF2_ITERACIONES + "$"
                + b64.encodeToString(sal) + "$" + b64.encodeToString(hash);
    }

    /** Verifica una contraseña contra el hash guardado (PBKDF2 o SHA-256 antiguo). */
    public static boolean verificarContrasena(String contrasena, String guardado) {
        if (contrasena == null || guardado == null) {
            return false;
        }
        String g = guardado.trim();
        String[] p = g.split("\\$");
        if (p.length == 4 && PBKDF2_PREFIJO.equals(p[0])) {
            try {
                int iteraciones = Integer.parseInt(p[1]);
                byte[] sal = Base64.getDecoder().decode(p[2]);
                byte[] esperado = Base64.getDecoder().decode(p[3]);
                if (iteraciones < 1 || esperado.length == 0) {
                    return false;
                }
                return MessageDigest.isEqual(esperado,
                        pbkdf2(contrasena, sal, iteraciones, esperado.length));
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return MessageDigest.isEqual(
                hashSHA256(contrasena).getBytes(StandardCharsets.US_ASCII),
                g.toLowerCase().getBytes(StandardCharsets.US_ASCII));
    }

    /** Indica si el hash guardado es del formato antiguo y debe regenerarse. */
    public static boolean esHashAntiguo(String guardado) {
        return guardado == null || !guardado.trim().startsWith(PBKDF2_PREFIJO + "$");
    }

    private static byte[] pbkdf2(String contrasena, byte[] sal, int iteraciones, int bytes) {
        PBEKeySpec spec = new PBEKeySpec(contrasena.toCharArray(), sal, iteraciones, bytes * 8);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 no disponible", e);
        } finally {
            spec.clearPassword();
        }
    }
}
