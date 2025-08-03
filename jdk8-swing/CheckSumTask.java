package org.example;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.concurrent.ExecutionException;

public class CheckSumTask extends SwingWorker<String, File> {

    private File file;

    private JTextField tMD5;

    static final int BSIZE = 2048;

    public CheckSumTask(File file, JTextField tMD5) {
        this.file = file;
        this.tMD5 = tMD5;
    }

    @Override
    protected String doInBackground() throws Exception {
        if (file == null || !file.exists()) {
            return "File Not Exist...";
        }
        if (isCancelled()) {
            return "Task Cancelled...";
        }
        long chunks = 1;
        if (file.length() != BSIZE) {
            chunks = Math.floorDiv(file.length(), BSIZE) + 1;
        }
        System.out.println("FileSize:" + file.length());
        System.out.println("FileChunk:" + chunks);


        try(FileInputStream input = new FileInputStream(file)) {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            FileChannel channel = input.getChannel();
            ByteBuffer buf = ByteBuffer.allocate(BSIZE);

            long count = 0;
            while (true) {
                int read = channel.read(buf);
                if (read == -1) {
                    setProgress(100);
                    break;
                }
                count++;
                System.out.println(count + "," + chunks);
                System.out.println("Progress:" + (count * 100.00 / chunks));
                setProgress((int)((count * 100.00 / chunks)));

                buf.flip();
                md5.update(buf);
                buf.clear();
            }
            return new BigInteger(1, md5.digest()).toString(16);
        }
    }


    @Override
    protected void done() {
        try {
            String ret = get();
            System.out.println(ret);
            tMD5.setText(ret);
        } catch (Exception e) {
            System.out.println("Task Failed: " + e.getMessage());
        }
    }
}
