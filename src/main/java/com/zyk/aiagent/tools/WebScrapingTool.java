package com.zyk.aiagent.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

/**
 * 网易抓取工具
 */
public class WebScrapingTool {
    @Tool(description = "抓取一个网页内容")
    public String WebScrapingPage(@ToolParam(description = "要抓取的网页地址") String url) {
        try {
            Document elements = Jsoup.connect(url).get();
            return elements.html();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
