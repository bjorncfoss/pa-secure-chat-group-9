import java.io.ObjectOutputStream;

public class User {
    /** The name of the user. */
    private String name;

    /** The output stream for communicating with the user. */
    private ObjectOutputStream out;

    /** The certificate associated with the user. */
    private String certificate;

    /**
     * Constructs a User object with the specified name, output stream, and certificate.
     *
     * @param name the name of the user
     * @param out the output stream for communicating with the user
     * @param certificate the certificate associated with the user
     */
    public User(String name, ObjectOutputStream out, String certificate) {
        this.name = name;
        this.out = out;
        this.certificate = certificate;
    }

    /**
     * Gets the name of the user.
     *
     * @return the name of the user
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the output stream for communicating with the user.
     *
     * @return the output stream for communicating with the user
     */
    public ObjectOutputStream getOut() {
        return out;
    }

    /**
     * Gets the certificate associated with the user.
     *
     * @return the certificate associated with the user
     */
    public String getCertificate() {
        return certificate;
    }
}
