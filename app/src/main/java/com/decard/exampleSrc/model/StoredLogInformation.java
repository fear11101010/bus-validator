package com.decard.exampleSrc.model;

import lombok.Data;
import java.nio.ByteBuffer;

@Data
public class StoredLogInformation {

    private byte equipmentClassificationCode; // BIN 1
    private byte serviceClassificationCode;   // BIN 1
    private byte contextCode;                 // BIN 1
    private byte paymentMethodCode;           // BIN 1

    private byte[] date;                       // BIN 2
    private byte time;                        // BIN 1

    private byte[] place1;                     // BIN 2
    private byte[] place2;                     // BIN 2

    private byte[] cardBalance;                  // BIN 3 (handled as 3 bytes)

    private byte[] storedValueLogId;           // BIN 2

    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(16); // Total bytes = 16 (1+1+1+1+2+1+2+2+3+2)

        buffer.put(equipmentClassificationCode);   // BIN 1
        buffer.put(serviceClassificationCode);     // BIN 1
        buffer.put(contextCode);                   // BIN 1
        buffer.put(paymentMethodCode);             // BIN 1

        buffer.put(date);                     // BIN 2
        buffer.put(time);                          // BIN 1

        buffer.put(place1);                   // BIN 2
        buffer.put(place2);                   // BIN 2

        // Put 3 bytes of cardBalance manually
        buffer.put(cardBalance);            // Least significant byte

        buffer.put(storedValueLogId);         // BIN 2

        return buffer.array();
    }
}
