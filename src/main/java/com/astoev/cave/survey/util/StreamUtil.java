package com.astoev.cave.survey.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by astoev on 11/13/15.
 */
public class StreamUtil {

    // same as IOUtils.closeQuietly()
    public static void closeQuietly(Closeable aStream) {

        try {
            if (aStream != null) {
                aStream.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    public static byte[] read(InputStream in) throws IOException {
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        byte [] chunk = new byte[1024];
        int len;
        while ((len = in.read(chunk)) > 0) {
            buff.write(chunk, 0, len);
        }
        return buff.toByteArray();
    }

    public static void copy(InputStream anInput, OutputStream anOutput) throws IOException {
        byte [] buff = new byte[1024];
        int len;
        while ((len = anInput.read(buff)) > 0) {
            anOutput.write(buff, 0, len);
        }
        anOutput.flush();
    }

    public static void copyAndClose(InputStream anInput, OutputStream anOutput) throws IOException {
        try {
            copy(anInput, anOutput);
        } finally {
            closeQuietly(anInput);
            closeQuietly(anOutput);
        }
    }

}
