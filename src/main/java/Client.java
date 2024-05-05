import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

/**
 * This class represents the sender of the message. It sends the message to the receiver by means of a socket. The use
 * of Object streams enables the sender to send any kind of object.
 */
public class Client {

    private static final String HOST = "0.0.0.0";


    private final Socket client;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    private final PublicKey publicRSAKey;
    private final PrivateKey privateRSAKey;

    private final BigInteger privateDHKey;
    private final BigInteger publicDHKey;
    private final PublicKey receiverPublicRSAKey;

    private String username;
    private boolean isConnected;

    // Public Key Construct
    public PublicKey getPublicKey() {
        return publicRSAKey;
    }

    public PrivateKey getPrivateKey() {
        return privateRSAKey;
    }


    /**
     * Constructs a Sender object by specifying the port to connect to. The socket must be created before the sender can
     * send a message.
     *
     * @param port the port to connect to
     *
     * @throws IOException when an I/O error occurs when creating the socket
     */
    public Client (int port, String nickname) throws Exception {
        client = new Socket ( HOST , port );
        out = new ObjectOutputStream ( client.getOutputStream ( ) );
        in = new ObjectInputStream ( client.getInputStream ( ) );
        this.username = nickname;
        isConnected = true;
        KeyPair keyPair = Encryption.generateKeyPair ( );
        privateRSAKey = keyPair.getPrivate ( );
        publicRSAKey = keyPair.getPublic ( );

        this.privateDHKey = DiffieHellman.generatePrivateKey ( );
        this.publicDHKey = DiffieHellman.generatePublicKey ( this.privateDHKey );

        // Performs the RSA key distribution
        receiverPublicRSAKey = rsaKeyDistribution ( );
    }


    public void execute() throws IOException {
        Scanner usrInput = new Scanner(System.in);
        try {
            if (isConnected) {
                System.out.println("Username:" + username);
                sendMessage(username);
            }
            // Thread for receiving messages
            Thread receiveThread = new Thread(() -> {
                try {
                    while (isConnected) {
                        receiveMessage();
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
            receiveThread.start();
            while (isConnected) {
                String message = usrInput.nextLine();
                sendMessage(message);
            }
        } finally {
            closeConnection();
        }
    }

    /**
     * Sends a message to the receiver using the OutputStream of the socket. The message is sent as an object of the
     * {@link Message} class.
     *
     * @param message the message to send
     *
     * @throws IOException when the encryption or the integrity generation fails
     */
    public void sendMessage ( String message ) throws IOException {
        List<String>recipients = extractRecipients ( message );
        String userMessage= extractMessage(message);
        // Creates the message object
        Message messageObj = new Message ( userMessage.getBytes ( ), recipients, username );
        // Sends the message
        out.writeObject ( messageObj );
        out.flush();
    }

    private BigInteger agreeOnSharedSecret() throws Exception
    {
        BigInteger privateKey = DiffieHellman.generatePrivateKey();
        BigInteger publicKey = DiffieHellman.generatePublicKey(privateKey);
        BigInteger clientPublicKeyEncrypted = new BigInteger((byte[]) in.readObject());

        // Decrypts the client's public key
        BigInteger clientPublicKey = new BigInteger ( Encryption.decryptRSA ( clientPublicKeyEncrypted.toByteArray ( ) , receiverPublicRSAKey ) );

        // Sends the server's public key to the client
        sendPublicKey ( publicKey );

        // Computes the shared secret
        return DiffieHellman.computeSecret ( clientPublicKey , privateKey );
    }

    /**
     * Sends the public key to the client.
     *
     * @param publicKey the public key to send
     *
     * @throws IOException if an I/O error occurs when sending the public key
     */
    private void sendPublicKey ( BigInteger publicKey ) throws Exception {
        out.writeObject ( Encryption.encryptRSA ( publicKey.toByteArray ( ) , this.privateRSAKey ) );
    }

    /**
     * Executes the key distribution protocol. The sender sends its public key to the receiver and receives the public
     * key of the receiver.
     *
     * @return the public key of the sender
     *
     * @throws Exception when the key distribution protocol fails
     */
    private PublicKey rsaKeyDistribution () throws Exception
    {
        sendPublicRSAKey();
        return (PublicKey) in.readObject();
    }

    /**
     * Sends the public key of the sender to the receiver.
     *
     * @throws IOException when an I/O error occurs when sending the public key
     */
    private void sendPublicRSAKey ( ) throws IOException {
        out.writeObject ( publicRSAKey );
        out.flush ( );
    }

    public void receiveMessage () throws IOException, ClassNotFoundException {
        Message messageObj = (Message) in.readObject();
        System.out.println(messageObj.getSender()+": "+ new  String(messageObj.getMessage()));
    }

    public static List<String> extractRecipients(String message) {
        List<String> recipients = new ArrayList<>();
        Pattern pattern = Pattern.compile("@(\\w+)");
        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            recipients.add(matcher.group(1));
        }
        return recipients;
    }

    public static String extractMessage(String message) {
        String messagem = message.replaceAll("@\\w+(,\\s*@\\w+)*", "").trim();
        return messagem;
    }
    /**
     * Closes the connection by closing the socket and the streams.
     *
     * @throws IOException when an I/O error occurs when closing the connection
     */
    private void closeConnection ( ) throws IOException {
        client.close ( );
        out.close ( );
        in.close ( );
    }
}
