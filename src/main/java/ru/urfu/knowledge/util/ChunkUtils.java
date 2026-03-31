package ru.urfu.knowledge.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class ChunkUtils {

    public static String normalizeText(String text) {
        return text
                .replaceAll("-\\n", "")
                .replaceAll("\\n+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static String removeGarbageSections(String text) {
        text = text.replaceAll("(?is)(содержание|оглавление).*?(введение)", "Введение");
        text = text.replaceAll("(?is)история изменений.*?(введение)", "Введение");
        return text;
    }

    public static String removeTableOfContents(String text) {
        return text.replaceAll("(?m)^.*\\.{3,}\\s*\\d+.*$", "");
    }

    public static String removeContentHeader(String text) {
        return text.replaceAll("(?i)содержание", "");
    }

    public static boolean isValidChunk(String chunk) {
        String lower = chunk.toLowerCase();
        return lower.length() > 50  // не слишком маленький
                && !lower.contains("содержание")
                && !lower.contains("история изменений");
    }

    public static String extractTitle(String text) {
        String[] lines = text.split("\\n");

        for (String line : lines) {
            line = line.trim();

            if (line.length() > 10 && line.length() < 200) {
                return line;
            }
        }

        return "Без названия";
    }

    public static String getContentHash(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hexBytes = md.digest(content.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hexBytes) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    
}
