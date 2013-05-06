package org.jahia.services.render.filter.cache;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.*;

/**
 * Class of utility method for compressing/decompressing cache key.
 */

public class KeyCompressor {
    protected transient static Logger logger = org.slf4j.LoggerFactory.getLogger(KeyCompressor.class);

    /**
     * Encode a cacheKey.
     * @param inputString The key to encode
     * @return the encoded key as a Base-64 url safe string
     */
    public static String encodeKey(String inputString) {
        if (StringUtils.isEmpty(inputString)) {
            return inputString;
        }
        // Compress the bytes
        byte[] output = new byte[4096];
        Deflater compresser = new Deflater(Deflater.BEST_SPEED);
        try {
            compresser.setInput(inputString.getBytes("UTF-8"));
            compresser.finish();
            int compressedDataLength = compresser.deflate(output);
            byte[] copy = new byte[compressedDataLength];
            System.arraycopy(output, 0, copy, 0, Math.min(output.length, compressedDataLength));
            return Base64.encodeBase64URLSafeString(copy);
        } catch (UnsupportedEncodingException e) {
            logger.warn("Not able to encode dependency: " + inputString, e);
        } finally {
            compresser.end();
        }

        return inputString;
    }

    /**
     * Decode a base-64 url safe cache key.
     *
     * @param inputString encoded key.
     * @return decoded key
     */
    public static String decodeKey(String inputString) {
        if (StringUtils.isEmpty(inputString)) {
            return inputString;
        }
        byte[] input = Base64.decodeBase64(inputString);
        // Decompress the bytes
        StringBuilder outputString = new StringBuilder();
        Inflater inf = new Inflater();
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2048);
            byte[] buf = new byte[512];
            int len = input.length;
            int off = 0;
            for (;;) {
                int n;

                // Fill the decompressor buffer with output data
                if (inf.needsInput()) {
                    int part;

                    if (len < 1) {
                        break;
                    }

                    part = (len < 512 ? len : 512);
                    inf.setInput(input, off, part);
                    off += part;
                    len -= part;
                }

                // Decompress and write blocks of output data
                do {
                    n = inf.inflate(buf, 0, buf.length);
                    if (n > 0) {
                        outputStream.write(buf, 0, n);
                    }
                } while (n > 0);

                // Check the decompressor
                if (inf.finished()) {
                    break;
                }
                if (inf.needsDictionary()) {
                    throw new ZipException("ZLIB dictionary missing");
                }
            }
            outputString.append(outputStream.toString("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.warn("Not able to decode dependency: " + inputString, e);
        } catch (IOException e) {
            logger.warn("Not able to decode dependency: " + inputString, e);
        } catch (DataFormatException e) {
            logger.warn("Not able to decode dependency: " + inputString, e);
        } finally {
            inf.end();
        }
        return outputString.toString();
    }
}