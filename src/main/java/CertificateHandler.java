import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;

public class CertificateHandler implements Runnable {

    private final Socket client;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    public CertificateHandler(Socket client, ObjectInputStream in, ObjectOutputStream out) {
        this.client = client;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {

        // Creates thread to display messages
        new Thread(() -> {
            try {
                Message message;
                while((message = (Message) in.readObject()) != null)
                {
                    process(message);
                }
                //process(in, out);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    closeConnection();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void process(Message message) throws IOException, ClassNotFoundException {
        while ((message = (Message) in.readObject()) != null) {
            switch (message.getMessageType()) {
                case USER_MESSAGE:
                    sendMessage(message);
                    break;
                case CERTIFICATE_VALIDATION:
                    validateCertificate(message);
                    break;
            }
        }
    }

    protected void sendMessage(Message messageObj) throws IOException {
        out.writeObject(messageObj);
        out.flush();
    }

    private void validateCertificate(Message messageObj) throws IOException {

        String username = messageObj.getSender();
        //Certificate certificate = messageObj.getCertificate();
    }

    private boolean isCertificateValid(Certificate certificate) {
        // TODO: Implement certificate validation here
        return true;
    }

    private void closeConnection() throws Exception {
        client.close();
        out.close();
        in.close();
    }

}
