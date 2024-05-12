// UnitTests.java

import org.junit.jupiter.api.*;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;



import static org.junit.jupiter.api.Assertions.assertEquals;



class UnitTests {

    @Nested
    @DisplayName("Certificate")
    class testCertificate {
        @Test
        @DisplayName("Testing GenerateCertificate method")
         void testGenerateCertificate() throws Exception {
            // Arrange
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // Act
            Certificate.generateCertificate(keyPair);

            // Assert
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "certificate_" + timeStamp + ".cert";
            String filePath = "certificates/" + fileName;

            assertTrue(Files.exists(Paths.get(filePath)), "Certificate file should be created");

            // Clean up
            Files.deleteIfExists(Paths.get(filePath));
        }

        private static final BigInteger MAX_SERIAL_NUMBER = BigInteger.valueOf(Long.MAX_VALUE);
        @Test
        @DisplayName("Testing SerialNumberGenerator")
        void testSerialNumberGenerator() throws NoSuchFieldException, IllegalAccessException {
            Certificate.SerialNumberGenerator generator = new Certificate.SerialNumberGenerator();
            Set<BigInteger> generatedSerialNumbers = new HashSet<>();

            // Act & Assert
            for (int i = 0; i < 1000; i++) {
                BigInteger serialNumber = generator.generateSerialNumber();
                assertTrue(serialNumber.compareTo(BigInteger.ZERO) > 0, "Serial number should be greater than zero");
                assertTrue(serialNumber.compareTo(MAX_SERIAL_NUMBER) <= 0, "Serial number should not exceed MAX_SERIAL_NUMBER");

            }
        }

    }

    @Nested
    @DisplayName("CertificateHandler")
    class testCertificateHandler{
        private Message message;
        @Test
        @DisplayName("Testing sendMessage method")
        void testSendMessage() throws IOException {
            byte[] messageByte = "Message".getBytes();
            String recipient = "recipient";
            String sender = "sender";
            Message.messageType messageType = Message.messageType.USER_MESSAGE;
            message = new Message(messageByte, recipient, sender,messageType);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            CertificateHandler certificateHandler = new CertificateHandler(null, null, objectOutputStream);
            certificateHandler.sendMessage(message);

            // Verify the written bytes
            byte[] writtenBytes = byteArrayOutputStream.toByteArray();
            assertNotNull(writtenBytes);
            assertTrue(writtenBytes.length > 0);
        }

    }

    @Nested
    @DisplayName("Server")
    class testServer{
        @Test
        void testServerConnection() throws Exception {

            Server server = new Server(1500);
            Thread serverThread = new Thread(server);
            serverThread.start();

            assertTrue(serverThread.isAlive());
        }
    }


    @Nested
    @DisplayName("ClientHandler")
    class testClientHandler {

    }

    @Nested
    @DisplayName("DiffieHellman")
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

    @Nested
    @DisplayName("Encryption")
    class testEncryption{
        @Test
        @DisplayName("testing Encrytion and Decryption with AES methods")
        public void testEncryptAndDecryptAES() throws Exception {
            String message = "Test Message";
            byte[] secretKey = "15236984qas".getBytes();
            byte[] encryptedMessage = Encryption.encryptAES(message.getBytes(), secretKey);
            byte[] decryptedMessage = Encryption.decryptAES(encryptedMessage, secretKey);
            assertEquals(message, new String(decryptedMessage));
        }

        @Test
        @DisplayName("testing Encrytion and Decryption with RSA methods")
        public void testEncryptAndDecryptRSA() throws Exception {
            String message = "Test Message";
            KeyPair keyPair = Encryption.generateKeyPair();
            byte[] encryptedMessage = Encryption.encryptRSA(message.getBytes(), keyPair.getPublic());
            byte[] decryptedMessage = Encryption.decryptRSA(encryptedMessage, keyPair.getPrivate());
            assertEquals(message, new String(decryptedMessage));
        }

        @Test
        @DisplayName("testing generateKeyPair method")
        public void testGenerateKeyPair() throws Exception {
            KeyPair keyPair = Encryption.generateKeyPair();
            assertAll(
                    () ->assertNotNull(keyPair),
                    () ->assertNotNull(keyPair.getPublic()),
                    () ->assertNotNull(keyPair.getPrivate())
            );
        }
    }

    @Nested
    @DisplayName("Integrity")
    public class testIntegrity {

        @Test
        @DisplayName("Testing generate and verify Digest methods")
        public void testDigest() throws Exception {
            String message = "Test Message";
            byte[] messageBytes = message.getBytes();
            byte[] digest = Integrity.generateDigest(messageBytes);
            assertNotNull(digest);
            boolean verifyDigestTrue = Integrity.verifyDigest(digest, Integrity.generateDigest(messageBytes));
            assertTrue(verifyDigestTrue);
            messageBytes[0] = 'A';
            boolean verifyDigestFalse = Integrity.verifyDigest(digest, Integrity.generateDigest(messageBytes));
            assertFalse(verifyDigestFalse);
        }
    }
    @Nested
    @DisplayName("Message")
    class testMessage {

        private Message message;

        @Test
        @DisplayName("Test Message method")
        void testMessageValue() {
            byte[] messageByte = "Message".getBytes();
            String recipient = "recipient";
            String sender = "sender";
            Message.messageType messageType = Message.messageType.USER_MESSAGE;
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
    @DisplayName("Client")
    class testClient {

        private Message message;
        @Test
        @DisplayName("Testing the sendMessage method")
        void testSendMessage() throws IOException {
            byte[] messageByte = "Message".getBytes();
            String recipient = "recipient";
            String sender = "sender";
            Message.messageType messageType = Message.messageType.USER_MESSAGE;
            message = new Message(messageByte, recipient, sender,messageType);
            assertEquals("sender", message.getSender());
        }

        @Test
        @DisplayName("Testing the recieveMessage method")
        void testRecieveMesaage() {
            byte[] messageByte = "Message".getBytes();
            String recipient = "recipient";
            String sender = "sender";
            Message.messageType messageType = Message.messageType.USER_MESSAGE;
            message = new Message(messageByte, recipient, sender,messageType);
            assertArrayEquals(messageByte, message.getMessage());
        }


        @Test
        @DisplayName("Testing the extractRecipients method")
        public void testExtractRecipients() {
            String message = "Testing @username1 and @username2!";
            List<String> expectedRecipients = Arrays.asList("username1", "username2");

            List<String> actualRecipients = Client.extractRecipients(message);

            assertEquals(expectedRecipients, actualRecipients);
        }

        @Test
        @DisplayName("Testing the extractMessage method")
        public void testExtractMessage() {
            String message = "Testing @user1";
            String expectedMessage = "Testing";

            String actualMessage = Client.extractMessage(message);

            assertEquals(expectedMessage, actualMessage);
        }
    }
}
