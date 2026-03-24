package ru.urfu.knowledge.util;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;

public class FileUtils {

    public static String calculateFileHash(File file) {
        try {
            byte[] content = Files.readAllBytes(file.toPath());
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(content);

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
