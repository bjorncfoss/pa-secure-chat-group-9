import java.math.BigInteger;
import java.security.SecureRandom;

public class SerialNumberGenerator {
    private final BigInteger MAX_SERIAL_NUMBER = BigInteger.valueOf(Long.MAX_VALUE);
    private BigInteger lastSerialNumber;

    public SerialNumberGenerator() {
        // Initialize last serial number to a random value
        lastSerialNumber = new BigInteger(MAX_SERIAL_NUMBER.bitLength(), new SecureRandom());
    }

    public BigInteger generateSerialNumber() {
        // Increment the last serial number
        lastSerialNumber = lastSerialNumber.add(BigInteger.ONE);

        // Check if the new serial number exceeds the maximum value
        if (lastSerialNumber.compareTo(MAX_SERIAL_NUMBER) > 0) {
            // Reset the serial number to a random value if it exceeds the maximum
            lastSerialNumber = new BigInteger(MAX_SERIAL_NUMBER.bitLength(), new SecureRandom());
        }

        return lastSerialNumber;
    }

}