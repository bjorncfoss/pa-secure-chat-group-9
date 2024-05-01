import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * This class represents a server that receives a message from the client. The server is implemented as a thread.
 */
public class Server implements Runnable {

    private final ServerSocket server;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private HashMap<String, Integer> clients = new HashMap<>();
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
    public Server ( int port ) throws IOException {
        server = new ServerSocket ( port );
        isConnected = true;
    }

    @Override
    public void run ( ) {
        try {
            while ( isConnected ) {
                client = server.accept ( );
                in = new ObjectInputStream(client.getInputStream());
                out = new ObjectOutputStream(client.getOutputStream());
                // Process the request
                Thread clientThread = new Thread(new ClientHandler(client, in , out , clients));
                clientThread.start();

            }
        } catch ( Exception e ) {
            throw new RuntimeException ( e );
        }
    }

    /**
     * Processes the request from the client.
     *
     * @param in the input stream
     *
     * @throws IOException            if an I/O error occurs when reading stream header
     * @throws ClassNotFoundException if the class of a serialized object cannot be found
     */
    private void process ( ObjectInputStream in) throws IOException, ClassNotFoundException {
        // Reads the message object
        Message messageObj = ( Message ) in.readObject ( );
        System.out.println ( new String ( messageObj.getMessage ( ) ) );


    }

    /**
     * Closes the connection and the associated streams.
     *
     * @throws IOException if an I/O error occurs when closing the socket
     */
    private void closeConnection ( ) throws IOException {
        client.close ( );
        out.close ( );
        in.close ( );
    }

}