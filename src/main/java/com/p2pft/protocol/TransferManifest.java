package com.p2pft.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

public record TransferManifest(String fileId, String fileName, long size, int chunkSize, int chunkCount,
                               String fileHash) {
    private static final int VERSION = 1;

    public void write(OutputStream output) throws IOException {
        var data = new DataOutputStream(output);
        data.writeInt(VERSION);
        writeString(data, fileId);
        writeString(data, fileName);
        data.writeLong(size);
        data.writeInt(chunkSize);
        data.writeInt(chunkCount);
        writeString(data, fileHash);
    }

    public static TransferManifest read(InputStream input) throws IOException {
        var data = new DataInputStream(input);
        if (data.readInt() != VERSION) throw new IOException("unsupported manifest version");
        return new TransferManifest(readString(data), readString(data), data.readLong(), data.readInt(), data.readInt(), readString(data));
    }

    private static void writeString(DataOutputStream output, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        output.writeInt(bytes.length);
        output.write(bytes);
    }

    private static String readString(DataInputStream input) throws IOException {
        int length = input.readInt();
        if (length < 0 || length > 1024 * 1024) throw new IOException("invalid manifest string");
        return new String(input.readNBytes(length), StandardCharsets.UTF_8);
    }
}
