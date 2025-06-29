package com.anduckhmt146.chatbot.Chatbot.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class DataLoader {

    @Autowired
    private VectorStore vectorStore;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(DataLoader.class);

    @PostConstruct
    public void initData() {
        try {
            Resource resource = new ClassPathResource("data.txt");

            if (!resource.exists()) {
                log.error("❌ data.txt not found in classpath");
                return;
            }

            TextReader textReader = new TextReader(resource);
            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> documents = splitter.split(textReader.get());

            log.info("📄 Split input into {} documents", documents.size());

            for (int i = 0; i < documents.size(); i++) {
                Document doc = documents.get(i);
                log.info("📘 Document {} content:\n{}", i + 1, doc.getFormattedContent());
            }

            vectorStore.add(documents);
            log.info("✅ Successfully loaded {} documents into vector store", documents.size());

        } catch (Exception e) {
            log.error("🔥 Data load error", e);
        }
    }
}