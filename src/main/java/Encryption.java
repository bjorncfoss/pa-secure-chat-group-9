import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

public class Encryption {


    /**
     * Encrypts a message using a symmetric key. The same key should be used to decrypt the message. The encryption is
     * using the AES algorithm.
     *
     * @param message the message to encrypt
     * @param secret  the secret key
     *
     * @return the encrypted message in bytes
     */
    public static byte[] encryptMessage ( byte[] message , byte[] secret ) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec ( secret , "AES" );
            Cipher cipher = Cipher.getInstance ( "AES/ECB/PKCS5Padding" );
            cipher.init ( Cipher.ENCRYPT_MODE , secretKey );
            return cipher.doFinal ( message );
        } catch ( Exception e ) {
            throw new RuntimeException ( e );
        }
    }

    /**
     * Decrypts a message using a symmetric key. The same key should be used to encrypt the message. The decryption is
     * using the AES algorithm.
     *
     * @param encryptedMessage the encrypted message
     * @param secret           the secret key
     *
     * @return the decrypted message
     */
    public static byte[] decryptMessage ( byte[] encryptedMessage , byte[] secret ) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec ( secret , "AES" );
            Cipher cipher = Cipher.getInstance ( "AES/ECB/PKCS5Padding" );
            cipher.init ( Cipher.DECRYPT_MODE , secretKey );
            return cipher.doFinal ( encryptedMessage );
        } catch ( Exception e ) {
            throw new RuntimeException ( e );
        }
    }

    public static byte[] encryptRSA ( byte[] message , Key publicKey ) throws Exception {
        Cipher cipher = Cipher.getInstance ( "RSA" );
        cipher.init ( Cipher.ENCRYPT_MODE , publicKey );
        return cipher.doFinal ( message );
    }

    public static byte[] decryptRSA ( byte[] message , Key privateKey ) throws Exception {
        Cipher cipher = Cipher.getInstance ( "RSA" );
        cipher.init ( Cipher.DECRYPT_MODE , privateKey );
        return cipher.doFinal ( message );
    }

    public static KeyPair generateKeyPair ( ) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance ( "RSA" );
        keyPairGenerator.initialize ( 2048 );
        return keyPairGenerator.generateKeyPair ( );
    }
}
