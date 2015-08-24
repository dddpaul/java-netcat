package com.github.dddpaul.netcat;

import java.io.*;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.Callable;

import static com.github.dddpaul.netcat.NetCat.*;

public class DatagramSender implements Callable<Long> {

    private ReadableByteChannel input;
    private DatagramChannel output;
    private SocketAddress remoteAddress;
    private int timeout; // ms

    public DatagramSender(InputStream input, DatagramChannel output, SocketAddress remoteAddress) {
        this.input = Channels.newChannel(input);
        this.output = output;
        this.remoteAddress = remoteAddress;
    }

    public DatagramSender(InputStream input, DatagramChannel output, SocketAddress remoteAddress, int timeout) {
        this(input, output, remoteAddress);
        this.timeout = timeout;
    }

    @Override
    public Long call() {
        long total = 0;
        ByteBuffer buf = ByteBuffer.allocateDirect(BUFFER_LIMIT);
        try {
            while (input.read(buf) != -1) {
                buf.flip();
                total += output.send(buf, remoteAddress);
                if (buf.hasRemaining()) {
                    buf.compact();
                } else {
                    buf.clear();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (timeout > 0) {
                try {
                    long start = System.currentTimeMillis();
                    while (System.currentTimeMillis() - start < timeout) {
                        Thread.sleep(100);
                    }
                    output.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return total;
    }
}
