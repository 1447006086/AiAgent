package com.zyk.aiagent.github;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class GitHubConfig {
    
    @Bean
    public List<String> supportedFileExtensions() {
        return Arrays.asList(
            ".md", ".txt", ".java", ".py", ".js", ".ts", 
            ".json", ".yaml", ".yml", ".xml", ".html"
        );
    }
}