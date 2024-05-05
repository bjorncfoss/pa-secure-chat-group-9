import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;

public class GenerateCertificate implements Serializable {

    // attributes
    private final String id;
    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    // constructor
    public GenerateCertificate (String id, PublicKey publicKey, PrivateKey privateKey)
    {
        this.id = id;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    // shows the certificate for public and private keys
    public void WriteCertificate(String filename) throws IOException
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename)))
        {
            // -----------------------------------------------
            // Private Key
            writer.write("-----BEGIN CERTIFICATE-----\n");
            writer.write(java.util.Base64.getEncoder().encodeToString(privateKey.getEncoded()) + "\n");
            writer.write("-----END CERTIFICATE-----\n");
            // -----------------------------------------------
            // Public Key
            writer.write("-----BEGIN CERTIFICATE-----\n");
            writer.write(java.util.Base64.getEncoder().encodeToString(publicKey.getEncoded()) + "\n");
            writer.write("-----END CERTIFICATE-----\n");
            // -----------------------------------------------
        }
    }
}