
// *------------------------*
// *     UnitTests.java     *
// *------------------------*

// Include necessary imports
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

// Main class for all Unit Tests
public class UnitTests {

    @Test
    @DisplayName("Tests the Main Server Connection!")
    public void testMainServer() {
        try {
            ServerSocket serverSocket = null;
            try {
                MainServer.main(new String[]{});
                serverSocket = new ServerSocket(9000);
                fail("Server socket should already be in use");
            } catch (BindException e) {
                // expected exception, server socket is already in use
            } finally {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            }
        } catch (Exception e) {
            fail("Main method should not throw an exception");
        }
    }

    // Certificate.java related

    @Nested
    @DisplayName("Test: Certificate.java ")
    class CertificateTest {

        @Test
        public void testCertificate() throws Exception {

            KeyPair keyPair = Encryption.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            String subject = "subject";
            Certificate certificate = new Certificate(publicKey, subject);

            assertEquals(publicKey, certificate.getPublicRSAKey());
            assertEquals(subject, certificate.getSubject());
            assertNotNull(certificate.getSerialNumber());
            //assertNotNull(certificate.getEmissionDate());
            assertNull(certificate.getIssuer());
            assertNull(certificate.getSignature());
        }

        @Test
        @DisplayName("Tests if the correct Issuer is set on the Certificate")
        public void testSetIssuer() throws Exception {

            KeyPair keyPair = Encryption.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            String subject = "subject";
            Certificate certificate = new Certificate(publicKey, subject);

            String issuer = "issuer";
            certificate.setIssuer(issuer);
            assertEquals(issuer, certificate.getIssuer());
        }

        @Test
        @DisplayName("Tests if Certificate was signed")
        public void testSetSignature() throws Exception {

            KeyPair keyPair = Encryption.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            String subject = "subject";
            Certificate certificate = new Certificate(publicKey, subject);

            byte[] signature = new byte[1024]; // specify the size of the byte array
            certificate.setSignature(signature);

            assertArrayEquals(signature, certificate.getSignature());
        }

        @Test
        @DisplayName("Tests if the correct Emission Date was applied to the Certificate")
        public void testSetEmissionDate() throws Exception {

            KeyPair keyPair = Encryption.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            String subject = "subject";
            Certificate certificate = new Certificate(publicKey, subject);

            Date emissionDate = new Date();
            certificate.setEmissionDate(emissionDate);
            assertEquals(emissionDate, certificate.getEmissionDate());
        }


    }

    // Encryption.java related

    @Nested
    @DisplayName(" Test RSA Encryption ")
    class EncryptionTests {

        @Test
        @DisplayName("Tests the RSA Message Encryption")
        public void testMessageEncryption() throws Exception {

            KeyPair keyPair = Encryption.generateKeyPair();
            byte[] message = "message".getBytes();
            byte[] encrypted = Encryption.encryptRSA(message, keyPair.getPublic());
            byte[] decrypted = Encryption.decryptRSA(encrypted, keyPair.getPrivate());

            assertArrayEquals(message, decrypted);
        }
    }

    // Integrity.java related

    @Nested
    @DisplayName(" Test: Integrity.java ")
    class testIntegrity {

        @Test
        @DisplayName("Tests the Message Integrity - SHA-256")
        public void testMessageIntegrity() throws Exception {
            byte[] message = "message".getBytes();
            byte[] digest = Integrity.generateDigest(message);

            assertTrue(Integrity.verifyDigest(digest, Integrity.generateDigest(message)));
        }
    }

    @Nested
    @DisplayName("test: Message.java")
    class testMessage {

        private Message message;

        @Test
        @DisplayName("Test Message method")
        void testMessageValue() {
            byte[] messageByte = "Message".getBytes();
            String recipient = "recipient";
            String sender = "sender";
            MessageTypes messageType = MessageTypes.USER_MESSAGE;
            message = new Message(messageByte, recipient, sender,messageType);
            assertAll(
                    () -> assertEquals(messageByte, message.getMessage()),
                    () -> assertEquals(recipient, message.getRecipient()),
                    () -> assertEquals(sender, message.getSender()),
                    () -> assertEquals(messageType, message.getMessageType())
            );
        }

    }
    @Nested
    @DisplayName("test: DiffieHellman.java")
    class testDiffieHellman{
        @Test
        @DisplayName("Testing generatePrivateKey method")
        public void testGeneratePrivateKey() throws NoSuchAlgorithmException {
            BigInteger privateKey = DiffieHellman.generatePrivateKey();
            assertNotNull(privateKey);
        }

        @Test
        @DisplayName("Testing generatePublicKey method")
        public void testGeneratePublicKey() throws NoSuchAlgorithmException {
            BigInteger privateKey = DiffieHellman.generatePrivateKey();
            BigInteger publicKey = DiffieHellman.generatePublicKey(privateKey);
            assertNotNull(publicKey);
        }

        @Test
        @DisplayName("Testing computeSecret method")
        public void testComputeSecret() throws NoSuchAlgorithmException {
            BigInteger User1PrivateKey = DiffieHellman.generatePrivateKey();
            BigInteger User2PrivateKey = DiffieHellman.generatePrivateKey();

            BigInteger User1PublicKey = DiffieHellman.generatePublicKey(User1PrivateKey);
            BigInteger User2PublicKey = DiffieHellman.generatePublicKey(User2PrivateKey);

            BigInteger User1Secret = DiffieHellman.computeSecret(User2PublicKey, User1PrivateKey);
            BigInteger User2Secret = DiffieHellman.computeSecret(User1PublicKey, User2PrivateKey);

            assertEquals(User1Secret, User2Secret);
        }
    }

}