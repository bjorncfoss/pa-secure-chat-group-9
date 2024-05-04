import java.util.Scanner;

public class MainClient {
    /**
     * The main method of the client.
     *
     * @param args The command line arguments
     */
    public static void main ( String[] args ) throws Exception {
        Scanner usrInput = new Scanner ( System.in );
        System.out.println ( "Insert username");
        String nickname = usrInput.nextLine();
        Client client = new Client ( 8000 , nickname);
        client.execute ( );
    }
}