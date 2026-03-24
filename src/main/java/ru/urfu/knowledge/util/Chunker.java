package ru.urfu.knowledge.util;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Chunker {

    public static List<String> chunkText(String text, int wordsPerChunk, int overlap) {
        List<String> chunks = new ArrayList<>();
        List<String> sentences = splitToSentences(text);

        List<String> currentChunk = new ArrayList<>();
        int currentWords = 0;

        for (String sentence : sentences) {
            String[] words = sentence.split("\\s+");
            currentChunk.add(sentence);
            currentWords += words.length;

            if (currentWords >= wordsPerChunk) {
                // объединяем предложения в один чанк
                chunks.add(String.join(" ", currentChunk));

                // оставляем overlap предложений
                if (overlap > 0) {
                    List<String> newChunk = new ArrayList<>();
                    int count = 0;
                    for (int i = currentChunk.size() - 1; i >= 0 && count < overlap; i--) {
                        newChunk.add(0, currentChunk.get(i));
                        count++;
                    }
                    currentChunk = newChunk;
                    currentWords = currentChunk.stream().mapToInt(s -> s.split("\\s+").length).sum();
                } else {
                    currentChunk = new ArrayList<>();
                    currentWords = 0;
                }
            }
        }

        if (!currentChunk.isEmpty()) {
            chunks.add(String.join(" ", currentChunk));
        }

        return chunks;
    }

    private static List<String> splitToSentences(String text) {
        List<String> sentences = new ArrayList<>();
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.of("ru", "RU"));
        iterator.setText(text);
        int start = iterator.first();
        int end = iterator.next();
        while (end != BreakIterator.DONE) {
            String sentence = text.substring(start, end).trim();
            if (sentence.length() > 20 && !sentence.contains(".....")) sentences.add(sentence);
            start = end;
            end = iterator.next();
        }
        return sentences;
    }
}
