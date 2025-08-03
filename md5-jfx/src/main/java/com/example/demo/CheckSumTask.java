package com.example.demo;

import javafx.concurrent.Task;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CheckSumTask extends Task<String> {

    private static final int BSIZE = 2048;

    private File file;

    public CheckSumTask(File file) {
        this.file = file;
    }

    @Override
    protected String call() throws Exception {
        if (file == null || !file.exists()) {
            return "";
        }
        long max = file.length();
        System.out.println(max);
        try(FileInputStream input = new FileInputStream(file))  {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            FileChannel channel = input.getChannel();
            ByteBuffer buf = ByteBuffer.allocate(BSIZE);

            long size = 0;
            while (true) {
                int read = channel.read(buf);
                if (read == -1) {
                    updateProgress(max, max);
                    break;
                }
                size += read;
                updateProgress(size, max);

                buf.flip();
                md5.update(buf);
                buf.clear();
            }

            return new BigInteger(1, md5.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
