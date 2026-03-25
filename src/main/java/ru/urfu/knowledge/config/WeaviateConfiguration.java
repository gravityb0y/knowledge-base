package ru.urfu.knowledge.config;

import io.weaviate.client6.v1.api.Config;
import io.weaviate.client6.v1.api.collections.VectorConfig;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.Property;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class WeaviateConfiguration {

    @Value("${weaviate.collection.name}")
    private String collectionName;

    @Value("${ollama.api-endpoint}")
    private String apiEndpoint;

    @Value("${ollama.vector-model}")
    private String vectorModel;

    @Bean
    public WeaviateClient client() throws IOException {
        Config config = Config.of(fn -> fn
                .scheme("http")
                .httpHost("192.168.1.118")
                .httpPort(8080)
                .grpcHost("192.168.1.118")
                .grpcPort(50051)
        );

        WeaviateClient client = new WeaviateClient(config);
        if (!client.collections.exists(collectionName)) {
            client.collections.create(collectionName, col ->
                    col.vectorConfig(VectorConfig.text2vecOllama(v -> v
                                    .model(vectorModel)
                                    .apiEndpoint(apiEndpoint)))
                            .properties(
                                    Property.text("content"),
                                    Property.text("sourceType"),
                                    Property.text("sourceId"),
                                    Property.text("sourceName"),
                                    Property.text("title"),
                                    Property.text("url"),
                                    Property.integer("chunkIndex"),
                                    Property.text("sourceHash")));
        }
        return client;
    }

}
