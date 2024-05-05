import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

/**
 * This class represents a server that receives a message from the client. The server is implemented as a thread.
 */
public class Server implements Runnable {

    private final ServerSocket server;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private HashMap<String, ObjectOutputStream> clients = new HashMap<>();
    private boolean isConnected = false;

    private Socket client;

    private final PrivateKey privateRSAKey;
    private final PublicKey publicRSAKey;


    /**
     * Constructs a Receiver object by specifying the port number. The server will be then created on the specified
     * port. The Receiver will be accepting connections from all local addresses.
     *
     * @param port the port number
     *
     * @throws IOException if an I/O error occurs when opening the socket
     */
    public Server ( int port ) throws Exception {
        server = new ServerSocket ( port );
        isConnected = true;

        KeyPair keyPair = Encryption.generateKeyPair();
        this.privateRSAKey = keyPair.getPrivate();
        this.publicRSAKey = keyPair.getPublic();
    }

    @Override
    public void run ( ) {
        try {
            while ( isConnected ) {
                client = server.accept ( );
                in = new ObjectInputStream(client.getInputStream());
                out = new ObjectOutputStream(client.getOutputStream());

                Message messageObj = (Message) in.readObject();
                String username= new String(messageObj.getMessage());

                // Check if the username already exists
                if (clients.containsKey(username)) {
                    // If the username already exists, inform the client and close the connection
                    out.writeObject("Username already exists. Please choose another username.");
                    out.flush();
                    client.close();
                    continue; // Continue to accept new connections
                }

                // Add the client to the HashMap along with its output stream
                clients.put(username, out);
                // Process the request
                Thread clientThread = new Thread(new ClientHandler(client, in , out , clients));
                clientThread.start();

            }
        } catch ( Exception e ) {
            throw new RuntimeException ( e );
        }
    }

    /**
     * Executes the key distribution protocol. The receiver will receive the public key of the sender and will send its
     * own public key.
     *
     * @param in the input stream
     *
     * @return the public key of the sender
     *
     * @throws Exception when the key distribution protocol fails
     */
    private PublicKey rsaKeyDistribution (ObjectInputStream in) throws Exception
    {
        PublicKey senderPublicRSAKey = (PublicKey) in.readObject();
        sendPublicRSAKey();
        return senderPublicRSAKey;
    }

    private void process (ObjectInputStream in) throws Exception
    {
        PublicKey senderPublicRSAKey = rsaKeyDistribution(in);
        BigInteger sharedSecret = agreeOnSharedSecret(senderPublicRSAKey);
        Message messageObj = (Message) in.readObject();

        byte[] decryptedMessage = Encryption.decryptAES(messageObj.getMessage(), sharedSecret.toByteArray());

        /*
        byte[] decryptedSignature = Encryption.decryptRSA(messageObj.getSignature(), senderPublicRSAKey);

        if ( !Integrity.verifyDigest(decryptedSignature, Integrity.generateDigest(decryptedMessage)))
        {
            throw new RuntimeException("The message has been tampered with.");
        }
        */

        System.out.println(new String (decryptedMessage));
    }

    /**
     * Agrees on a shared secret with the receiver using the Diffie-Hellman key exchange algorithm.
     *
     * @param senderPublicRSAKey the public key of the sender
     *
     * @return the shared secret
     *
     * @throws NoSuchAlgorithmException if the algorithm used to generate the key is not available
     * @throws IOException              if an I/O error occurs when sending the public key
     * @throws ClassNotFoundException   if the class of a serialized object cannot be found
     */
    private BigInteger agreeOnSharedSecret (PublicKey senderPublicRSAKey) throws Exception
    {
        BigInteger privateKey = DiffieHellman.generatePrivateKey();
        BigInteger publicKey = DiffieHellman.generatePublicKey(privateKey);

        sendPublicKey(publicKey);

        // Waits for the receiver to send its public key
        byte[] receiverPublicKeyEncrypted = ( byte[] ) in.readObject ( );
        byte[] receiverPublicKey = Encryption.decryptRSA ( receiverPublicKeyEncrypted , senderPublicRSAKey );

        // Computes the shared secret
        return DiffieHellman.computeSecret ( new BigInteger ( receiverPublicKey ) , privateKey );
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
     * Sends the public key of the receiver to the sender.
     *
     * @throws IOException when an I/O error occurs when sending the public key
     */
    private void sendPublicRSAKey ( ) throws IOException {
        out.writeObject ( publicRSAKey );
        out.flush ( );
    }
}