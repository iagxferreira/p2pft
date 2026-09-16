package com.p2pft.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class FrameCodecTest {
    @Test
    void framesKeepMessageBoundariesAcrossStreams() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        FrameCodec.write(output, "manifest".getBytes(StandardCharsets.UTF_8));
        FrameCodec.write(output, new byte[]{1, 2, 3});

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        assertArrayEquals("manifest".getBytes(StandardCharsets.UTF_8), FrameCodec.read(input));
        assertArrayEquals(new byte[]{1, 2, 3}, FrameCodec.read(input));
    }
}
