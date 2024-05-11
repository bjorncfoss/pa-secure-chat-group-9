// UnitTests.java

import org.junit.jupiter.api.*;
import java.io.*;
import java.net.Socket;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


class UnitTests

{
    @Nested
    @DisplayName("ClientHandler")
    class testClientHandler {
        private ClientHandler clientHandler;
        private Socket client;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private HashMap<String, ObjectOutputStream> clientsList;

        @BeforeEach
        void setUp() {
            client = new Socket();
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                out = new ObjectOutputStream(byteArrayOutputStream);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                in = new ObjectInputStream(byteArrayInputStream);
                clientsList = new HashMap<>();
                clientHandler = new ClientHandler(client, in, out, clientsList);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Test
        @DisplayName("Test ClientHandler connection")
        void testClientHandlerConnection(){
            assertAll(
                    () -> assertNotNull(clientHandler)
            );
        }

        @Test
        @DisplayName("Test process method")
        void testProcess() {

            Message messageObj = new Message("Test Message".getBytes(), List.of("teste recipient"), "sender");


            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

                objectOutputStream.writeObject(messageObj);
                objectOutputStream.flush();

                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());


                try (ObjectInputStream newIn = new ObjectInputStream(byteArrayInputStream)) {

                    Message receivedMessage = (Message) newIn.readObject();

                    assertAll(
                            () -> assertArrayEquals(messageObj.getMessage(), receivedMessage.getMessage()),
                            () -> assertEquals(messageObj.getRecipients(), receivedMessage.getRecipients()),
                            () -> assertEquals(messageObj.getSender(), receivedMessage.getSender())
                    );
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        @Test
        @DisplayName("Test sendMessage method")
        void testSendMessage(){

            Message messageObj = new Message("Test Message".getBytes(), List.of("teste recipient"), "sender");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream ObjectOutputStream = null;
            try {
                ObjectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                clientsList.put("teste recipient", ObjectOutputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            assertDoesNotThrow(() -> clientHandler.sendMessage(messageObj));

            try {
                assert ObjectOutputStream != null;
                ObjectOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
                Message receivedMessage = (Message) objectInputStream.readObject();

                assertArrayEquals(messageObj.getMessage(), receivedMessage.getMessage());
                assertEquals(messageObj.getRecipients(), receivedMessage.getRecipients());
                assertEquals(messageObj.getSender(), receivedMessage.getSender());
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }


    }

    @Nested
    @DisplayName("Message")
    class testMessage{

        private Message message;

        @Test
        @DisplayName("Test Message method")
        void testMessageValue(){
            byte[] messageByte = "Message".getBytes();
            List<String> recipients = Arrays.asList("teste1","teste2");
            String sender = "sender";
            message = new Message(messageByte,recipients,sender);
            assertAll(
                    () -> assertEquals(messageByte,message.getMessage()),
                    () -> assertEquals(recipients,message.getRecipients()),
                    () -> assertEquals(sender,message.getSender())
            );
        }

    }

    @Nested
    @DisplayName("Server")
    class testServer{
        private Server server;
        @Test
        @DisplayName("Testing the Server's port")
        void testServerValue() throws IOException{
            int port = 9000;
            server = new Server(port);
            assertNotNull(server);
        }

    }

    @Nested
    @DisplayName("MainServer")
    class testMainServer{

        @Test
        @DisplayName("Testing the mainserver connection")
        void testMainServerConnection() throws IOException {
            Server server = new Server(8000);
            Thread serverThread = new Thread(server);
            serverThread.start();

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try (Socket socket = new Socket("localhost", 8000)) {
                assertTrue(socket.isConnected());
            } catch (IOException e) {
                fail("Failed to connect to the server: " + e.getMessage());
            }
            serverThread.interrupt();
        }
    }

}


/*
    @Nested
    @DisplayName("Encryption")
    class testEncryption{
        @Test
        @DisplayName("Test encrypt and decrypt with AES")
        void testEncryptAndDecryptWithAES() throws IOException{
            String message = "Test";
            byte[] secretKey = "thisIsASecretKey".getBytes();
            byte[] encryptedMessage = Encryption.encryptMessage(message.getBytes(), secretKey);
            byte[] decryptedMessage = Encryption.decryptMessage(encryptedMessage, secretKey);
            assertArrayEquals(message.getBytes(), decryptedMessage, "Decrypted message does not match original message");
        }

        @Test
        @DisplayName("Test encrypt and decrypt with RSA")
        void testEncryptAndDecryptWithRSA() throws Exception {
            String message = "Hello, World!";
            KeyPair keyPair = Encryption.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();
            byte[] encryptedMessage = Encryption.encryptRSA(message.getBytes(), publicKey);
            byte[] decryptedMessage = Encryption.decryptRSA(encryptedMessage, privateKey);
            assertArrayEquals(message.getBytes(), decryptedMessage, "Decrypted message does not match original message");
        }

    }
    */