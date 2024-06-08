import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class represents a server that receives a message from the client. The server is implemented as a thread.
 */
public class Server implements Runnable {

    private final ServerSocket server;
    private ConcurrentHashMap<String, User> clients = new ConcurrentHashMap<>();
    private boolean isConnected = false;
    private ArrayList<String > registeredNames= new ArrayList<>();
    private Socket client;
    private ReentrantLock clientsLock = new ReentrantLock();

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
            while ( isConnected ) {
                client = server.accept ( );
                // Process the request
                Thread clientThread = new Thread(new ClientHandler(client, clients, registeredNames,clientsLock));
                clientThread.start();
            }
        } catch ( Exception e ) {
            throw new RuntimeException ( e );
        }
    }

}