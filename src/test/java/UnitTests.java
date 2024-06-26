
// *------------------------*
// *     UnitTests.java     *
// *------------------------*

// Include necessary imports
import org.junit.jupiter.api.*;

import java.io.*;
import java.math.BigInteger;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.security.SecureRandom;

// Includes all type of Asserts
import static org.junit.jupiter.api.Assertions.*;

public class UnitTests
{

    @Nested
    @DisplayName("Test: MainServer.java ")
    class testMainServer {

        @Test
        @DisplayName("Tests the Main Server Connection!")
        public void testMainServerConnection() {
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
    }


    @Nested
    @DisplayName("Test: Certificate.java ")
    class CertificateTest {

        @Test
        @DisplayName("Tests the Certificate Creation")
        public void testCertificate() throws Exception {

            KeyPair keyPair = Encryption.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            String subject = "subject";
            Certificate certificate = new Certificate(publicKey, subject);

            assertAll(
                    () -> assertEquals(publicKey, certificate.getPublicRSAKey()),
                    () -> assertEquals(subject, certificate.getSubject()),
                    () -> assertNull(certificate.getIssuer()),
                    () -> assertNull(certificate.getSignature())
            );
        }

        @Test
        @DisplayName("Verifies that the getCertificateContent method returns the expected certificate content.")
        void testGetCertificateContent() {
            String expectedContent = "Test Certificate Content";
            Certificate.setCertificateContent(expectedContent);

            assertAll(
                    () -> assertEquals(expectedContent, Certificate.getCertificateContent())
            );
        }

        @Test
        @DisplayName("Verifies that the setCertificateContent method sets the certificate content correctly.")
        void testSetCertificateContent() {
            String newContent = "New Certificate Content";
            Certificate.setCertificateContent(newContent);

            assertAll(
                    () -> assertEquals(newContent, Certificate.getCertificateContent())
            );
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

            assertAll(
                    () -> assertEquals(issuer, certificate.getIssuer())
            );
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

            assertAll(
                    () -> assertArrayEquals(signature, certificate.getSignature())
            );
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

            assertAll(
                    () -> assertEquals(emissionDate, certificate.getEmissionDate())
            );
        }

        @Test
        void testSetSubject() throws Exception {

            KeyPair keyPair = Encryption.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            String subject = "subject";
            Certificate certificate = new Certificate(publicKey, subject);

            String newSubject = "New Subject";
            certificate.setSubject(newSubject);

            assertAll(
                    () -> assertEquals(newSubject, certificate.getSubject())
            );
        }

        @Test
        @DisplayName("Test equality when creating 2 certificates")
        public void testEquals() throws Exception {

            KeyPair keyPair = Encryption.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            String subject = "subject";

            Certificate certificate1 = new Certificate(publicKey, subject);
            Certificate certificate2 = new Certificate(publicKey, subject);

            assertAll(
                    () -> assertNotEquals(certificate1, certificate2),
                    () -> assertNotEquals(null, certificate1),
                    () -> assertNotEquals(certificate1, new Object())
            );
        }

        @Test
        @DisplayName("Testing Hash Code Method")
        public void testHashCode() throws Exception {

            KeyPair keyPair = Encryption.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            String subject = "subject";

            Certificate certificate1 = new Certificate(publicKey, subject);
            Certificate certificate2 = new Certificate(publicKey, subject);

            assertAll(
                    () -> assertNotEquals(certificate1.hashCode(), certificate2.hashCode())
            );
        }
    }


    @Nested
    @DisplayName("Test: CertificateEncoder.java")
    class testCertificateEncoder {

        private CertificateEncoder encoder;
        private static final String HEADER = "-----BEGIN CERTIFICATE-----";
        private static final String FOOTER = "-----END CERTIFICATE-----";

        @BeforeEach
        void setup() {
            encoder = new CertificateEncoder();
        }

        @Test
        @DisplayName("Test Certificate Encoding")
        public void testEncode() throws Exception {

            KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            Certificate certificate = new Certificate(publicKey, " subject");
            String pemData = encoder.encode(certificate);

            assertNotNull(pemData);
            String base64Encoded = pemData.replace(HEADER, "").replace(FOOTER, "").trim();

            assertAll(
                    ()-> assertFalse(pemData.startsWith(HEADER)),
                    ()-> assertFalse(pemData.endsWith(FOOTER))
            );
        }

        @Test
        @DisplayName("Test Certificate Decoding")
        public void testDecode() throws Exception {
            KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            Certificate certificate = new Certificate(publicKey, "Test Subject");
            String pemData = encoder.encode(certificate);

            Certificate decodedCertificate = encoder.decode(pemData);

            assertAll(
                    () -> assertEquals(certificate, decodedCertificate)
            );
        }
    }


    @Nested
    @DisplayName("Test: CertificateHandler.java")
    class testCertificateHandler {

        private ServerSocket serverSocket;
        private Socket clientSocket;
        private CertificateHandler certificateHandler;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private KeyPair keyPair;
        private ExecutorService executorService;

        @BeforeEach
        public void setUp() throws Exception {
            KeyPairGenerator pairGenerator = KeyPairGenerator.getInstance("RSA");
            pairGenerator.initialize(2048);
            keyPair = pairGenerator.generateKeyPair();
            serverSocket = new ServerSocket(0); // Use 0 to get a free port
            int port = serverSocket.getLocalPort();
            clientSocket = new Socket("localhost", port);
            executorService = Executors.newSingleThreadExecutor();
            executorService.submit(() -> {
                try {
                    Socket socket = serverSocket.accept();
                    certificateHandler = new CertificateHandler(socket, keyPair);
                    certificateHandler.run();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
        }

        @AfterEach
        public void tearDown() throws Exception {
            clientSocket.close();
            serverSocket.close();
            executorService.shutdown();
        }

        @Test
        @DisplayName("Testing the Process method")
        public void testProcess() throws Exception {
            Message userMessage = new Message("Test message".getBytes(), "recipient", "sender", MessageTypes.USER_MESSAGE);
            out.writeObject(userMessage);
            out.flush();
            Message message = (Message) in.readObject();
            assertAll(
                    ()->assertEquals(userMessage.getMessageType(), message.getMessageType()),
                    ()->assertArrayEquals(userMessage.getMessage(), message.getMessage())
            );

        }

        @Test
        @DisplayName("Testing the validateCertificate method")
        public void testValidateCertificate() throws Exception {
            Message message = new Message(new byte[0], "recipient", "sender", MessageTypes.CERTIFICATE_VALIDATION);
            out.writeObject(message);
            out.flush();
        }

        @Test
        @DisplayName("Testing the SendMessage method")
        public void testSendMessage() throws IOException, ClassNotFoundException {
            Message message = new Message("Test".getBytes(), "recipient", "sender", MessageTypes.USER_MESSAGE);
            out.writeObject(message);
            out.flush();
            Message receivedMessage = (Message) in.readObject();

            assertAll(
                    ()->assertEquals(message.getMessageType(), receivedMessage.getMessageType()),
                    ()->assertArrayEquals(message.getMessage(), receivedMessage.getMessage())
            );
        }

    }


    @Nested
    @DisplayName("Test: CertificateServer.java ")
    class CertificateServerTest {

        @Test
        @DisplayName("Test the Certificate Server Connection!")
        public void testMain() throws Exception {
            ServerSocket serverSocket = new ServerSocket(8100);
            KeyPair keyPair = Encryption.generateKeyPair();
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(() -> {
                try {
                    // Call the main method using the class name
                    CertificateServer.main(new String[] {});
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            Socket client = new Socket("localhost", 8100);

            assertAll(
                    () -> assertNotNull(client),
                    () -> assertEquals("localhost/127.0.0.1:8100", client.getRemoteSocketAddress().toString())
            );

            client.close();
            serverSocket.close();
        }
    }

    @Nested
    @DisplayName("Test: Client.java")
    class testClient {


        @Test
        @DisplayName("Tests the extractRecipients method to ensure it correctly extracts usernames from a message.")
        void testExtractRecipients() {
            String message = "@user1 Hello @user2, how are you @user3?";
            List<String> recipients = Client.extractRecipients(message);

            assertAll(
                    () -> assertEquals(3, recipients.size()),
                    () -> assertTrue(recipients.contains("user1")),
                    () -> assertTrue(recipients.contains("user2")),
                    () -> assertTrue(recipients.contains("user3"))
            );
        }

        @Test
        @DisplayName("Tests KeyPair Generator (Public & Private Keys)")
        void testGenerateKeyPair() throws Exception {
            KeyPair keyPair = Encryption.generateKeyPair();

            assertAll(
                    () -> assertNotNull(keyPair),
                    () -> assertNotNull(keyPair.getPrivate()),
                    () -> assertNotNull(keyPair.getPublic())
            );
        }

        @Test
        @DisplayName("Test to verify extracted message")
        void testExtractMessage() {
            String message = "@user1 Hello @user2, how are you @user3?";
            String extractedMessage = Client.extractMessage(message);

            assertAll(
                    () -> assertEquals("Hello , how are you ?", extractedMessage)
            );
        }

        @Test
        void testCAPublicKey() throws Exception {

            // Generate a key pair for the test
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();
            PublicKey expectedPublicKey = keyPair.getPublic();
        }

    }

    @Nested
    @DisplayName("Test: ClientHandler.java")
    class testClientHandler {

    }


    @Nested
    @DisplayName("test: DiffieHellman.java")
    class testDiffieHellman {

        @Test
        @DisplayName("Testing generatePrivateKey method")
        public void testGeneratePrivateKey() throws NoSuchAlgorithmException {
            BigInteger privateKey = DiffieHellman.generatePrivateKey();

            assertAll(
                    () -> assertNotNull(privateKey)
            );
        }

        @Test
        @DisplayName("Testing generatePublicKey method")
        public void testGeneratePublicKey() throws NoSuchAlgorithmException {
            BigInteger privateKey = DiffieHellman.generatePrivateKey();
            BigInteger publicKey = DiffieHellman.generatePublicKey(privateKey);

            assertAll(
                    () -> assertNotNull(publicKey)
            );
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

            assertAll(
                    () -> assertEquals(User1Secret, User2Secret)
            );
        }
    }


    @Nested
    @DisplayName(" Test: Encryption ")
    class testEncryption {

        @Test
        @DisplayName("Tests the RSA Message Encryption")
        public void testMessageEncryption() throws Exception {

            KeyPair keyPair = Encryption.generateKeyPair();
            byte[] message = "message".getBytes();
            byte[] encrypted = Encryption.encryptRSA(message, keyPair.getPublic());
            byte[] decrypted = Encryption.decryptRSA(encrypted, keyPair.getPrivate());

            assertAll(
                    () -> assertArrayEquals(message, decrypted)
            );
        }

        @Test
        @DisplayName("Test AES Encryption")
        void testEncryptAES() throws Exception {
            byte[] message = "Hello, World!".getBytes();
            byte[] secretKey = "mySecretKey".getBytes();
            byte[] encryptedMessage = Encryption.encryptAES(message, secretKey);

            // Verify that the encrypted message is not equal to the original message
            assertAll(
                    () -> assertNotEquals(message, encryptedMessage)
            );

        }

        @Test
        @DisplayName("Test AES Decryption")
        void testDecryptAES() throws Exception {
            byte[] message = "Hello, World!".getBytes();
            byte[] secretKey = "mySecretKey".getBytes();
            byte[] encryptedMessage = Encryption.encryptAES(message, secretKey);
            byte[] decryptedMessage = Encryption.decryptAES(encryptedMessage, secretKey);

            // Verify that the decrypted message is equal to the original message
            assertAll(
                    () -> assertArrayEquals(message, decryptedMessage)
            );
        }
    }


    @Nested
    @DisplayName(" Test: Integrity.java ")
    class testIntegrity {

        @Test
        @DisplayName("Tests the Message Integrity - SHA-256")
        public void testMessageIntegrity() throws Exception {
            byte[] message = "message".getBytes();
            byte[] digest = Integrity.generateDigest(message);

            assertAll(
                    () -> assertTrue(Integrity.verifyDigest(digest, Integrity.generateDigest(message)))
            );
        }
    }


    @Nested
    @DisplayName("Test: KeyMessage.java")
    class testKeyMessage{
        @Test
        @DisplayName("Testing KeyPair Equality")
        public void testingKeyMessage() throws Exception {
            KeyPair keyPair = Encryption.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();

            KeyMessage keyMessage = new KeyMessage(publicKey, "recipient", "sender");

            assertAll(
                    () -> assertEquals(MessageTypes.KEY_MESSAGE, keyMessage.getMessageType()),
                    () -> assertEquals(publicKey, keyMessage.getPublicKey())
            );
        }
    }


    @Nested
    @DisplayName("Test: Message.java")
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
    @DisplayName("Test: SerialNumberGenerator.java")
    class testSerialNumberGenerator {

        @Test
        @DisplayName(" Test Serial Number Generator ")
        void testGenerateSerialNumber() {
            SerialNumberGenerator generator = new SerialNumberGenerator();
            BigInteger initialSerialNumber = generator.generateSerialNumber();
            BigInteger newSerialNumber = generator.generateSerialNumber();

            // Verify that the new serial number is one more than the initial serial number
            assertAll(
                    () -> assertEquals(initialSerialNumber.add(BigInteger.ONE), newSerialNumber)
            );
        }

    }

    @Nested
    @DisplayName("Test: User.java")
    class testUser{

        @Test
        @DisplayName("testing User equality")
        void testUserEquals() throws Exception {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);

            User user = new User("Rick Grimes", out, "Negan");

            assertAll(
                    () -> assertEquals("Rick Grimes", user.getName()),
                    () -> assertEquals(out, user.getOut()),
                    () -> assertEquals("Negan", user.getCertificate())
            );
        }
    }

}