public class MainClient {
    /**
     * The main method of the client.
     *
     * @param args The command line arguments
     */
    public static void main ( String[] args ) throws Exception {
        Client client = new Client ( 8000 );
        client.execute ( );
    }
}