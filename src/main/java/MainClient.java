import java.util.Scanner;

/**
 * A class representing the main client application.
 */
public class MainClient {
    /**
     * The main method of the client.
     *
     * @param args The command line arguments
     */
    public static void main ( String[] args ) throws Exception {
        Client client = new Client ( 9000 , 8100 );
        client.execute ( );
    }
}