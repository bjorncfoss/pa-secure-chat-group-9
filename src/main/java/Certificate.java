import java.io.*;
import java.math.BigInteger;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.security.spec.RSAPublicKeySpec;
import java.util.Objects;

/**
 * Represents a certificate.
 */
public class Certificate implements Serializable {

    // attributes
    private PublicKey publicRSAKey;
    private static String certificateContent;

    // Serial Number
    private BigInteger serialNumber;
    private String subject;
    private String issuer;
    private byte[] signature;
    private Date emissionDate;

    /**
     * Constructs a Certificate object.
     *
     * @param publicRSAKey The public RSA key.
     * @param Subject      The subject of the certificate.
     */
    public Certificate(PublicKey publicRSAKey, String Subject){
        this.publicRSAKey = publicRSAKey;
        this.serialNumber = new SerialNumberGenerator().generateSerialNumber();
        this.subject=Subject;
    }


    public byte[] getCertificateData() { return (serialNumber + issuer + subject + publicRSAKey.toString() + emissionDate.toString()).getBytes(); }

    /**
     * Gets the public RSA key.
     *
     * @return The public RSA key.
     */
    public PublicKey getPublicRSAKey() {
        return publicRSAKey;
    }

    /**
     * Gets the certificate content.
     *
     * @return The certificate content.
     */
    public static String getCertificateContent() {
        return certificateContent;
    }

    /**
     * Sets the certificate content.
     *
     * @param certificateContent The certificate content to set.
     */
    public static void setCertificateContent(String certificateContent) {
        Certificate.certificateContent = certificateContent;
    }

    /**
     * Gets the serial number.
     *
     * @return The serial number.
     */
    public BigInteger getSerialNumber() {
        return serialNumber;
    }

    /**
     * Sets the serial number.
     *
     * @param serialNumber The serial number to set.
     */
    public void setSerialNumber(BigInteger serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * Gets the subject of the certificate.
     *
     * @return The subject of the certificate.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the subject of the certificate.
     *
     * @param subject The subject to set.
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Gets the issuer of the certificate.
     *
     * @return The issuer of the certificate.
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * Sets the issuer of the certificate.
     *
     * @param issuer The issuer to set.
     */
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    /**
     * Gets the signature of the certificate.
     *
     * @return The signature of the certificate.
     */
    public byte[] getSignature() {
        return signature;
    }

    /**
     * Sets the signature of the certificate.
     *
     * @param signature The signature to set.
     */
    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    /**
     * Gets the emission date of the certificate.
     *
     * @return The emission date of the certificate.
     */
    public Date getEmissionDate() {
        return emissionDate;
    }

    /**
     * Sets the emission date of the certificate.
     *
     * @param emissionDate The emission date to set.
     */
    public void setEmissionDate(Date emissionDate) {
        this.emissionDate = emissionDate;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param o the object to be compared for equality
     * @return true if the specified object is equal to this certificate, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Certificate that = (Certificate) o;
        return Objects.equals(publicRSAKey, that.publicRSAKey)
                && Objects.equals(serialNumber, that.serialNumber)
                && Objects.equals(subject, that.subject)
                && Objects.equals(issuer, that.issuer)
                && Objects.deepEquals(signature, that.signature)
                && Objects.equals(emissionDate, that.emissionDate);
    }

    /**
     * Returns a hash code value for the certificate.
     *
     * @return a hash code value for this certificate
     */
    @Override
    public int hashCode() {
        return Objects.hash(publicRSAKey, serialNumber, subject, issuer, Arrays.hashCode(signature), emissionDate);
    }
}