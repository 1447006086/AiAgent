package com.zyk.aiagent.github;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GitHubDocumentService {
    
    @Value("${github.access-token}")
    private String accessToken;
    
    public List<Document> loadRepositoryDocuments(String owner, String repo, String branch) {
        try {
            GitHub github = GitHub.connectUsingOAuth(accessToken);
            GHRepository repository = github.getRepository(owner + "/" + repo);
            
            List<Document> documents = new ArrayList<>();
            
            // 获取仓库内容
            List<GHContent> contents = repository.getDirectoryContent("", branch);
            processContents(contents, documents, repository, branch);
            
            return documents;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load GitHub documents", e);
        }
    }
    
    private void processContents(List<GHContent> contents, List<Document> documents, 
                               GHRepository repository, String branch) throws IOException {
        for (GHContent content : contents) {
            if (content.isFile()) {
                // 读取文件内容
                String fileContent = content.getContent();
                String filePath = content.getPath();
                
                // 创建文档
                Document document = new Document(fileContent, Map.of(
                    "source", "github",
                    "repository", repository.getFullName(),
                    "path", filePath,
                    "branch", branch,
                    "url", content.getHtmlUrl()
                ));
                
                documents.add(document);
            } else if (content.isDirectory()) {
                // 递归处理目录
                List<GHContent> subContents = repository.getDirectoryContent(content.getPath(), branch);
                processContents(subContents, documents, repository, branch);
            }
        }
    }
}