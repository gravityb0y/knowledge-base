package ru.urfu.knowledge.service;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.urfu.knowledge.config.DocsConfiguration;
import ru.urfu.knowledge.entity.KnowledgeChunk;
import ru.urfu.knowledge.util.Chunker;
import ru.urfu.knowledge.util.FileUtils;
import ru.urfu.knowledge.util.StringPipeline;

import java.io.File;
import java.util.*;

@Service
public class FileProcessorService implements ProcessorService {

    private static final Logger logger = LoggerFactory.getLogger(FileProcessorService.class);

    private final Tika tika;
    private final DocsConfiguration docsConfiguration;

    @Autowired
    public FileProcessorService(Tika tika, DocsConfiguration docsConfiguration) {
        this.tika = tika;
        this.docsConfiguration = docsConfiguration;
    }

    @Override
    public List<KnowledgeChunk> process() {
        File folder = new File(docsConfiguration.getPath());
        if (!folder.exists() || !folder.isDirectory()) {
            logger.error("Папка с документами не найдена: {}", folder.getAbsolutePath());
            return Collections.emptyList();
        }

        File[] files = folder.listFiles();
        if (files == null)  {
            logger.warn("В указанной папке не найдены файлы");
            return Collections.emptyList();
        }

        return Arrays.stream(files)
                .filter(file -> file.isFile() && isSupported(file))
                .map(this::processFile)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .toList();
    }

    private List<KnowledgeChunk> processFile(File file) {
        try {
            String rawText = tika.parseToString(file);
            String title = extractTitle(rawText);
            String fileName = file.getName();
            String text = StringPipeline.of(rawText)
                    .then(this::removeGarbageSections)
                    .then(this::removeContentHeader)
                    .then(this::removeTableOfContents)
                    .then(this::normalizeText)
                    .build();
            String hash = FileUtils.calculateFileHash(file);
            logger.info("Файл с названием: {}, Содержимое: {}", fileName, text.substring(0, Math.min(500, text.length())));
            List<String> chunks = Chunker.chunkText(text, 150, 2)
                    .stream()
                    .filter(this::isValidChunk)
                    .toList();
            List<KnowledgeChunk> result = new LinkedList<>();

            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);
                result.add(KnowledgeChunk.builder()
                        .chunkIndex(i)
                        .content(chunk)
                        .title(title)
                        .sourceName("docs")
                        .sourceId(fileName)
                        .sourceType(getFileExtension(file))
                        .sourceHash(hash)
                        .build());

            }
            return result;
        } catch (Exception e) {
            logger.error("Ошибка при обработке файла: {}", file.getName(), e);
            return null;
        }

    }
    private boolean isSupported(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".doc")
                || name.endsWith(".docx")
                || name.endsWith(".pdf")
                || name.endsWith(".ppt")
                || name.endsWith(".pps");
    }

    private String normalizeText(String text) {
        return text
                .replaceAll("-\\n", "")
                .replaceAll("\\n+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String removeGarbageSections(String text) {
        text = text.replaceAll("(?is)(содержание|оглавление).*?(введение)", "Введение");
        text = text.replaceAll("(?is)история изменений.*?(введение)", "Введение");
        return text;
    }

    private String removeTableOfContents(String text) {
        return text.replaceAll("(?m)^.*\\.{3,}\\s*\\d+.*$", "");
    }

    private String removeContentHeader(String text) {
        return text.replaceAll("(?i)содержание", "");
    }

    private boolean isValidChunk(String chunk) {
        String lower = chunk.toLowerCase();
        return lower.length() > 50  // не слишком маленький
                && !lower.contains("содержание")
                && !lower.contains("история изменений");
    }

    private String extractTitle(String text) {
        String[] lines = text.split("\\n");

        for (String line : lines) {
            line = line.trim();

            if (line.length() > 10 && line.length() < 200) {
                return line;
            }
        }

        return "Без названия";
    }

    private String getFileExtension(File file) {
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf('.');

        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }

        return "";
    }
}
