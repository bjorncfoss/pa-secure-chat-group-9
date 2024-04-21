import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class represents a server that receives a message from the client. The server is implemented as a thread.
 */
public class Receiver implements Runnable {

    private final ServerSocket server;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket client;

    /**
     * Constructs a Receiver object by specifying the port number. The server will be then created on the specified
     * port. The Receiver will be accepting connections from all local addresses.
     *
     * @param port the port number
     *
     * @throws IOException if an I/O error occurs when opening the socket
     */
    public Receiver ( int port ) throws IOException {
        server = new ServerSocket ( port );
    }

    @Override
    public void run ( ) {
        try {
            client = server.accept ( );
            in = new ObjectInputStream ( client.getInputStream ( ) );
            out = new ObjectOutputStream ( client.getOutputStream ( ) );
            // Process the request
            process ( in );
            // Close connection
            closeConnection ( );
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
    private void process ( ObjectInputStream in ) throws IOException, ClassNotFoundException {
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