package com.p2pft.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class FrameCodec {
    private static final int MAX_FRAME = 16 * 1024 * 1024;
    private FrameCodec() {}

    public static void write(OutputStream output, byte[] payload) throws IOException {
        if (payload.length > MAX_FRAME) throw new IllegalArgumentException("frame too large");
        var stream = new DataOutputStream(output);
        stream.writeInt(payload.length);
        stream.write(payload);
        stream.flush();
    }

    public static byte[] read(InputStream input) throws IOException {
        var stream = new DataInputStream(input);
        int length = stream.readInt();
        if (length < 0 || length > MAX_FRAME) throw new IOException("invalid frame length");
        byte[] payload = stream.readNBytes(length);
        if (payload.length != length) throw new IOException("truncated frame");
        return payload;
    }
}
