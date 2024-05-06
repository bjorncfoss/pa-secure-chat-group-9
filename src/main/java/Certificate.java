import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class Certificate implements Serializable {

    // attributes
    private final String id;
    private final BigInteger publicKey;
    private final BigInteger privateKey;

    private String certificateContent;

    // constructor
    public Certificate(String id, BigInteger publicKey, BigInteger privateKey)
    {
        this.id = id;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        setContent();
    }

    // shows the certificate for public and private key
    public void setContent ()
    {
        StringBuilder sb = new StringBuilder("-----BEGIN CERTIFICATE-----\n");
        sb.append(privateKey + "\n");
        sb.append("-----END CERTIFICATE-----\n");
        this.certificateContent = sb.toString();

        StringBuilder sb2 = new StringBuilder("-----BEGIN CERTIFICATE-----\n");
        sb.append(publicKey + "\n");
        sb.append("-----END CERTIFICATE-----\n");
        this.certificateContent += sb2.toString();
    }

}