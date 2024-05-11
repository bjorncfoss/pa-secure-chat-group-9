import java.io.IOException;

public class MainServer {
    public static void main ( String[] args ) throws Exception {
        Server server = new Server ( 9000 );
        Thread serverThread = new Thread ( server );
        serverThread.start ( );
    }
}