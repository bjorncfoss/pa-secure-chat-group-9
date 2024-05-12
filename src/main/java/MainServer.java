import java.io.IOException;

// Javadoc: http://localhost:63342/pa-secure-chat-group-9/target/apidocs/package-summary.html

/**
 * A class representing the main server application.
 */
public class MainServer {

    /**
     * The main method to start the server on the specified port.
     *
     * @param args The command-line arguments (not used).
     * @throws Exception If an error occurs while starting the server.
     */
    public static void main ( String[] args ) throws Exception {
        Server server = new Server ( 9000 );
        Thread serverThread = new Thread ( server );
        serverThread.start ( );
    }
}