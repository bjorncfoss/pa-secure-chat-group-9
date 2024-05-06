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
    }

    @Override
    public void run ( ) {
        try {
            System.out.println(1);
            while ( isConnected ) {
                System.out.println(2);
                client = server.accept ( );
                System.out.println(3);
                in = new ObjectInputStream(client.getInputStream());
                out = new ObjectOutputStream(client.getOutputStream());
                System.out.println(4);
                Message messageObj = (Message) in.readObject();
                String username= new String(messageObj.getMessage());
                System.out.println(5);
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

}