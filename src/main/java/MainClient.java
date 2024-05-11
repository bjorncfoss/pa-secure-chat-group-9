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
        Scanner usrInput = new Scanner ( System.in );

        // Initial message when opening the client window
        System.out.println ( "Insert new @username: ");
        String nickname = usrInput.nextLine();
        Client client = new Client ( 9000 , nickname);
        client.execute ( );
    }
}