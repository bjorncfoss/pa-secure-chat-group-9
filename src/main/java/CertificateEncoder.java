import java.io.*;
import java.util.Base64;

public class CertificateEncoder {
    protected static final String HEADER = "-----BEGIN CUSTOM CERTIFICATE-----";
    protected static final String FOOTER = "-----END CUSTOM CERTIFICATE-----";

    /**
     * Encodes the specified {@link Certificate} into a custom PEM format string.
     *
     * @param certificate the {@link Certificate} to encode; must not be null.
     * @return a string representing the PEM encoded certificate.
     * @throws IOException if an I/O error occurs during the encoding process.
     * @throws IllegalArgumentException if the provided certificate is null.
     */

    public String encode(Certificate certificate) throws IOException {

        if (certificate == null) {
            throw new IllegalArgumentException("Certificate cannot be null.");
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {

            objectOutputStream.writeObject(certificate);
            objectOutputStream.flush();
            String base64Encoded = Base64.getEncoder().encodeToString(outputStream.toByteArray());

            return formatPEM(base64Encoded);
        }

    }

    /**
     * Decodes a PEM formatted string into a {@link Certificate}.
     *
     * @param pemData the PEM formatted string to decode; must not be null and must contain the custom headers and footers.
     * @return the decoded {@link Certificate}.
     * @throws IOException if an I/O error occurs during the decoding process.
     * @throws ClassNotFoundException if the class of a serialized object cannot be found during the decoding process.
     * @throws IllegalArgumentException if the provided PEM data is null, or does not contain the proper headers and footers.
     */

    public Certificate decode(String pemData) throws IOException, ClassNotFoundException {
        if (pemData == null || !pemData.contains(HEADER) || !pemData.contains(FOOTER)) {
            throw new IllegalArgumentException("Invalid PEM data provided.");
        }

        String base64Encoded = extractBase64Data(pemData);
        byte[] certBytes = Base64.getDecoder().decode(base64Encoded);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(certBytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {

            return (Certificate) objectInputStream.readObject();
        }
    }

    /**
     * Formats a Base64 encoded string into a custom PEM format with headers and footers.
     *
     * @param base64Encoded the Base64 encoded string to format.
     * @return the formatted PEM string.
     */
    private String formatPEM(String base64Encoded) {
        return HEADER + "\n" + base64Encoded + "\n" + FOOTER;
    }

    /**
     * Extracts the Base64 encoded data from a custom PEM formatted string, removing headers and footers.
     *
     * @param pemData the PEM formatted string.
     * @return the Base64 encoded data string.
     */
    private String extractBase64Data(String pemData) {
        return pemData.replace(HEADER, "").replace(FOOTER, "").trim();
    }

}
