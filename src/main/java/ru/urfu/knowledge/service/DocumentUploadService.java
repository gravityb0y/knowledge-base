package ru.urfu.knowledge.service;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.urfu.knowledge.dto.DocumentUploadResponse;
import ru.urfu.knowledge.dto.KnowledgeChunk;
import ru.urfu.knowledge.util.ChunkUtils;
import ru.urfu.knowledge.util.Chunker;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class DocumentUploadService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "docx", "pptx");
    private static final String SOURCE_TYPE = "document";

    private final Tika tika;
    private final WeaviateService weaviateService;

    public DocumentUploadService(WeaviateService weaviateService) {
        this.weaviateService = weaviateService;
        this.tika = new Tika();
    }

    public DocumentUploadResponse upload(MultipartFile file, String title) {
        validateFile(file);

        String originalFileName = sanitizeFileName(file.getOriginalFilename());
        String resolvedTitle = resolveTitle(title, originalFileName);

        String extractedText = extractText(file);
        String normalizedText = ChunkUtils.normalizeText(extractedText);

        if (normalizedText == null || normalizedText.isBlank()) {
            throw new IllegalArgumentException("Не удалось извлечь текст из документа");
        }

        String contentHash = ChunkUtils.getContentHash(normalizedText);
        String sourceId = buildSourceId(originalFileName, contentHash);

        List<String> chunks = Chunker.chunkText(normalizedText, 150, 2)
                .stream()
                .filter(ChunkUtils::isValidChunk)
                .toList();

        if (chunks.isEmpty()) {
            throw new IllegalArgumentException("После обработки документа не осталось валидных фрагментов");
        }

        List<KnowledgeChunk> knowledgeChunks = buildKnowledgeChunks(
                chunks,
                resolvedTitle,
                originalFileName,
                sourceId,
                contentHash
        );

        weaviateService.deleteBySourceId(sourceId);
        weaviateService.saveChunks(knowledgeChunks);

        return DocumentUploadResponse.builder()
                .sourceId(sourceId)
                .fileName(originalFileName)
                .title(resolvedTitle)
                .sourceType(SOURCE_TYPE)
                .chunksCount(knowledgeChunks.size())
                .message("Документ успешно добавлен в базу знаний")
                .build();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл не должен быть пустым");
        }

        String fileName = file.getOriginalFilename();
        String extension = getExtension(fileName);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Неподдерживаемый формат файла. Разрешены только: pdf, docx, pptx"
            );
        }
    }

    private String extractText(MultipartFile file) {
        try {
            return tika.parseToString(file.getInputStream());
        } catch (IOException | TikaException exception) {
            throw new IllegalArgumentException("Ошибка чтения файла", exception);
        }
    }

    private List<KnowledgeChunk> buildKnowledgeChunks(
            List<String> chunks,
            String title,
            String fileName,
            String sourceId,
            String contentHash
    ) {
        List<KnowledgeChunk> result = new LinkedList<>();

        for (int i = 0; i < chunks.size(); i++) {
            result.add(KnowledgeChunk.builder()
                    .title(title)
                    .content(chunks.get(i))
                    .sourceName(fileName)
                    .sourceId(sourceId)
                    .sourceType(SOURCE_TYPE)
                    .url(null)
                    .chunkIndex(i)
                    .sourceHash(contentHash)
                    .build());
        }

        return result;
    }

    private String resolveTitle(String title, String fileName) {
        if (title != null && !title.isBlank()) {
            return title.trim();
        }

        return removeExtension(fileName);
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "uploaded-document";
        }

        return fileName
                .replace("\\", "/")
                .substring(fileName.replace("\\", "/").lastIndexOf("/") + 1)
                .trim();
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }

        return fileName.substring(fileName.lastIndexOf(".") + 1)
                .toLowerCase(Locale.ROOT);
    }

    private String removeExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return fileName;
        }

        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    private String buildSourceId(String fileName, String contentHash) {
        return sha256(fileName.toLowerCase(Locale.ROOT) + ":" + contentHash);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось сформировать идентификатор документа", exception);
        }
    }
}