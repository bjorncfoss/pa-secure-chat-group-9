public class CertificateServer {

    public static void main ( String[] args ) throws Exception {

        // Server to Generate Certificate
        Server server = new Server ( 8080 );
        Thread serverThread = new Thread ( server );
        serverThread.start ( );
    }

}
