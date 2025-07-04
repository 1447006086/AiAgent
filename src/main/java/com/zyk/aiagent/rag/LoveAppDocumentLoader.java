package com.zyk.aiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class LoveAppDocumentLoader  {

    private final ResourcePatternResolver resolver;

    public LoveAppDocumentLoader(ResourcePatternResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * 加载多个markdown资源
     * @return
     */

    public List<Document> loadMarkdowns(){
        List<Document> allDocument = new ArrayList<>();
        try {
            Resource[] resources = resolver.getResources("classpath:document/*.md");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                MarkdownDocumentReaderConfig config=MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("fileName",filename)
                        .build();
                MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource,config);
                allDocument.addAll(markdownDocumentReader.get());
            }
        } catch (IOException e) {
            log.info("文档加载失败：",e);
        }
        return allDocument;
    }

}
