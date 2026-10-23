import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Genera el hash de clave de un facturador en el formato que lee ClavePbkdf2.
 * Uso: java despliegue/GenerarHashClave.java   (pide la clave sin mostrarla ni dejarla en el historial)
 */
public class GenerarHashClave {
    private static final int ITERACIONES = 210_000;

    public static void main(String[] argumentos) throws Exception {
        var consola = System.console();
        if (consola == null) throw new IllegalStateException("Ejecútelo en una terminal interactiva");
        char[] clave = consola.readPassword("Clave del facturador: ");
        byte[] sal = new byte[16];
        new SecureRandom().nextBytes(sal);
        var especificacion = new PBEKeySpec(clave, sal, ITERACIONES, 256);
        byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(especificacion).getEncoded();
        especificacion.clearPassword();
        Arrays.fill(clave, '\0');
        var base64 = Base64.getEncoder();
        System.out.println("pbkdf2-sha256:" + ITERACIONES + ":" + base64.encodeToString(sal) + ":" + base64.encodeToString(hash));
    }
}
