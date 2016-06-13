package dasolma.com.asaplib.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Created by dasolma on 30/12/14.
 */
public class Bytes {

    public static byte[] compress(byte[] input) {

        // Compressor with highest level of compression
        Deflater compressor = new Deflater();
        compressor.setLevel(Deflater.BEST_COMPRESSION);

        // Give the compressor the data to compress
        compressor.setInput(input);
        compressor.finish();

        // Create an expandable byte array to hold the compressed data.
        // It is not necessary that the compressed data will be smaller than
        // the uncompressed data.
        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);

        // Compress the data
        byte[] buf = new byte[1024];
        while (!compressor.finished()) {
            int count = compressor.deflate(buf);
            bos.write(buf, 0, count);

        }
        try {
            bos.close();
        } catch (IOException e) {
        }

        // Get the compressed data
        return bos.toByteArray();
    }

    public static byte[] decompress(byte[] data) throws IOException, DataFormatException {

        Inflater inflater = new Inflater();
        inflater.setInput(data);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }

        outputStream.close();
        byte[] output = outputStream.toByteArray();


        return output;
    }

    public static byte[] getBytes(Boolean[] data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        writeBooleans(out, data);

        return out.toByteArray();
    }

    public static boolean[] getBooleans(byte[] data) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        List<Boolean>  booleans = readBooleans(in, data.length);

        return toPrimitiveArray(booleans);

    }

    private  static boolean[] toPrimitiveArray(final List<Boolean> booleanList) {
        final boolean[] primitives = new boolean[booleanList.size()];
        int index = 0;
        for (Boolean object : booleanList) {
            primitives[index++] = object;
        }
        return primitives;
    }

    private static void writeBooleans(OutputStream out, Boolean[] ar) throws IOException {
        for (int i = 0; i < ar.length; i += 8) {
            int b = 0;
            for (int j = Math.min(i + 7, ar.length-1); j >= i; j--) {
                b = (b << 1) | (ar[j] ? 1 : 0);
            }
            out.write(b);
        }
    }

    private static List<Boolean> readBooleans(InputStream in, int size) throws IOException {
        List<Boolean> result = new ArrayList<Boolean>();

        for (int i = 0; i < size; i++) {
            int b = in.read();
            if (b < 0) throw new EOFException();
            for (int j = i; j < i + 8; j++) {
                result.add( (b & 1) != 0 );
                b >>>= 1;
            }
        }

        return result;


    }

    public static byte[] getBytes(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public static byte[] getBytes(double value) {
        return ByteBuffer.allocate(8).putDouble(value).array();
    }

    public static int getInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static double getDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
    }


    public static byte[] removeFirst( byte[] data ) {
        byte[] result = new byte[data.length -1];
        System.arraycopy(data, 1, result, 0, data.length -1);
        return result;
    }

}
