import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
        try {
            process();
        } catch (Exception e) {
            System.out.println("");
        }
    }

    private void process() throws IOException, ClassNotFoundException {
        Message message;
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

    private void sendMessage(Message messageObj) throws IOException {
        out.writeObject(messageObj);
    }

    private void validateCertificate(Message messageObj) throws IOException {
        Certificate certificate = messageObj.getCertificate();
        boolean isValid = isCertificateValid(certificate);
    }

    private boolean isCertificateValid(Certificate certificate) {
        // Implement certificate validation logic here
        return true;
    }

    private void closeConnection() throws Exception {
        client.close();
        out.close();
        in.close();
    }

}
