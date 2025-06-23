package com.zyk.imagesearchmcpserver.tools;



import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 优化的图片搜索工具类
 * 使用多种搜索策略来提高搜索结果的准确性
 */
@Service
public class ImageSearchTool {

    private static final Log log = LogFactory.get();
    private static final String API_SEARCH_URL = "https://api.pexels.com/v1/search";

    private final String apiKey="0yG3Mwb1uRlJY8SoQc185Uz3rlyigTYwfdB1mlnFaqyNgB2ZwvQYJgox";



    /**
     * 智能搜索图片 - 使用多种策略提高匹配度
     *
     * @param query 用户输入的搜索关键词
     * @param maxResults 最大返回结果数
     * @return 优化后的搜索结果
     */
    public String smartSearch(String query, int maxResults) {
        log.info("开始智能搜索: '{}'", query);

        if (StrUtil.isBlank(query)) {
            return "搜索关键词不能为空";
        }

        // 1. 生成多个搜索变体
        List<String> searchVariants = generateSearchVariants(query);
        log.info("生成搜索变体: {}", searchVariants);

        // 2. 执行多次搜索
        List<JSONObject> allPhotos = new ArrayList<>();
        Set<String> seenPhotoIds = new HashSet<>(); // 去重

        for (String variant : searchVariants) {
            if (allPhotos.size() >= maxResults) break;

            JSONObject result = searchImages(variant, Math.min(10, maxResults), 1);
            if (result != null) {
                JSONArray photos = result.getJSONArray("photos");
                if (photos != null) {
                    for (int i = 0; i < photos.size(); i++) {
                        JSONObject photo = photos.getJSONObject(i);
                        String photoId = photo.getStr("id");

                        // 去重
                        if (!seenPhotoIds.contains(photoId)) {
                            seenPhotoIds.add(photoId);
                            allPhotos.add(photo);

                            if (allPhotos.size() >= maxResults) break;
                        }
                    }
                }
            }

            // 避免请求过于频繁
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // 3. 按相关性排序
        allPhotos.sort((a, b) -> {
            double scoreA = calculateRelevanceScore(a, query);
            double scoreB = calculateRelevanceScore(b, query);
            return Double.compare(scoreB, scoreA); // 降序排列
        });

        // 4. 格式化输出结果
        return formatSearchResults(allPhotos, query);
    }

    /**
     * 生成搜索变体 - 提高搜索覆盖度
     */
    private List<String> generateSearchVariants(String originalQuery) {
        List<String> variants = new ArrayList<>();

        // 原始查询
        variants.add(originalQuery);

        // 英文变体（如果是中文）
        if (isChinese(originalQuery)) {
            variants.addAll(getEnglishVariants(originalQuery));
        }

        // 同义词扩展
        variants.addAll(getSynonyms(originalQuery));

        // 相关词汇
        variants.addAll(getRelatedTerms(originalQuery));

        // 去重并限制数量
        return CollUtil.distinct(variants).subList(0, Math.min(variants.size(), 5));
    }

    /**
     * 判断是否为中文
     */
    private boolean isChinese(String text) {
        return text.matches(".*[\\u4e00-\\u9fa5].*");
    }

    /**
     * 获取英文变体
     */
    private List<String> getEnglishVariants(String chineseQuery) {
        Map<String, String> translations = new HashMap<>();
        translations.put("城市", "city");
        translations.put("夜景", "night city");
        translations.put("建筑", "architecture");
        translations.put("自然", "nature");
        translations.put("风景", "landscape");
        translations.put("人物", "people");
        translations.put("动物", "animals");
        translations.put("科技", "technology");
        translations.put("未来", "futuristic");
        translations.put("现代", "modern");
        translations.put("传统", "traditional");
        translations.put("艺术", "art");
        translations.put("设计", "design");
        translations.put("工作", "work");
        translations.put("生活", "lifestyle");
        translations.put("美食", "food");
        translations.put("旅行", "travel");
        translations.put("运动", "sports");
        translations.put("音乐", "music");
        translations.put("电影", "movie");

        List<String> variants = new ArrayList<>();
        for (Map.Entry<String, String> entry : translations.entrySet()) {
            if (chineseQuery.contains(entry.getKey())) {
                variants.add(chineseQuery.replace(entry.getKey(), entry.getValue()));
                variants.add(entry.getValue()); // 只搜索英文关键词
            }
        }

        return variants;
    }

    /**
     * 获取同义词
     */
    private List<String> getSynonyms(String query) {
        Map<String, List<String>> synonyms = new HashMap<>();
        synonyms.put("城市", Arrays.asList("urban", "metropolitan", "downtown"));
        synonyms.put("夜景", Arrays.asList("night", "evening", "dark"));
        synonyms.put("建筑", Arrays.asList("building", "structure", "construction"));
        synonyms.put("自然", Arrays.asList("nature", "wild", "outdoor"));
        synonyms.put("风景", Arrays.asList("landscape", "scenery", "view"));
        synonyms.put("科技", Arrays.asList("technology", "digital", "modern"));
        synonyms.put("未来", Arrays.asList("futuristic", "sci-fi", "advanced"));

        List<String> variants = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : synonyms.entrySet()) {
            if (query.contains(entry.getKey())) {
                variants.addAll(entry.getValue());
            }
        }

        return variants;
    }

    /**
     * 获取相关词汇
     */
    private List<String> getRelatedTerms(String query) {
        Map<String, List<String>> related = new HashMap<>();
        related.put("城市", Arrays.asList("skyline", "street", "traffic"));
        related.put("夜景", Arrays.asList("lights", "neon", "reflection"));
        related.put("建筑", Arrays.asList("glass", "steel", "concrete"));
        related.put("科技", Arrays.asList("computer", "screen", "data"));
        related.put("未来", Arrays.asList("robot", "space", "hologram"));

        List<String> variants = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : related.entrySet()) {
            if (query.contains(entry.getKey())) {
                variants.addAll(entry.getValue());
            }
        }

        return variants;
    }

    /**
     * 计算图片与查询的相关性分数
     */
    private double calculateRelevanceScore(JSONObject photo, String originalQuery) {
        double score = 0.0;

        // 1. 标题匹配度
        String alt = photo.getStr("alt", "").toLowerCase();
        String query = originalQuery.toLowerCase();

        if (alt.contains(query)) {
            score += 10.0;
        }

        // 2. 关键词匹配
        String[] queryWords = query.split("\\s+");
        for (String word : queryWords) {
            if (alt.contains(word)) {
                score += 5.0;
            }
        }

        // 3. 图片质量（基于尺寸）
        int width = photo.getInt("width", 0);
        int height = photo.getInt("height", 0);
        if (width > 2000 && height > 2000) {
            score += 2.0; // 高质量图片加分
        }

        // 4. 摄影师知名度（简单判断）
        String photographer = photo.getStr("photographer", "");
        if (photographer.length() > 0) {
            score += 1.0;
        }

        return score;
    }

    /**
     * 格式化搜索结果
     */
    private String formatSearchResults(List<JSONObject> photos, String originalQuery) {
        if (photos.isEmpty()) {
            return String.format("抱歉，没有找到与 '%s' 相关的图片。请尝试使用不同的关键词。", originalQuery);
        }

        StringBuilder result = new StringBuilder();
        result.append(String.format("为 '%s' 找到了 %d 张相关图片：\n\n", originalQuery, photos.size()));

        result.append("提示：如果结果不够准确，请尝试使用更具体的关键词，或者用英文搜索。");

        return result.toString();
    }

    /**
     * 基础搜索方法
     */
    private JSONObject searchImages(String query, int perPage, int page) {
        try {
            cn.hutool.http.HttpRequest request = HttpUtil.createGet(API_SEARCH_URL)
                    .header(Header.AUTHORIZATION, this.apiKey)
                    .form("query", query, "per_page", perPage, "page", page);

            String responseBody = request.execute().body();

            if (StrUtil.isBlank(responseBody)) {
                return null;
            }

            return JSONUtil.parseObj(responseBody);

        } catch (Exception e) {
            log.error("搜索失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 搜索Pexels图片并返回前五条结果
     * 使用Hutool工具类处理JSON响应
     *
     * @param query 搜索关键词
     * @return 格式化的搜索结果字符串，包含前五条图片信息
     */
    public String searchPexelsImages(String query) {
        log.info("开始搜索Pexels图片: '{}'", query);

        if (StrUtil.isBlank(query)) {
            return "搜索关键词不能为空";
        }

        try {
            // 使用Hutool创建HTTP请求
            HttpRequest request = HttpUtil.createGet(API_SEARCH_URL)
                    .header(Header.AUTHORIZATION, this.apiKey)
                    .form("query", query, "per_page", 5, "page", 1);

            log.info("请求URL: {}", request.getUrl());

            // 执行请求并获取响应
            String responseBody = request.execute().body();

            if (StrUtil.isBlank(responseBody)) {
                return "API响应为空，请检查网络连接或API Key";
            }

            // 使用Hutool解析JSON响应
            JSONObject response = JSONUtil.parseObj(responseBody);

            // 提取关键信息
            int totalResults = response.getInt("total_results", 0);
            JSONArray photos = response.getJSONArray("photos");

            if (photos == null || photos.isEmpty()) {
                return String.format("没有找到与 '%s' 相关的图片", query);
            }

            // 构建结果字符串
            StringBuilder result = new StringBuilder();
            result.append(String.format("搜索关键词: %s\n", query));
            result.append(String.format("总结果数: %d\n", totalResults));
            result.append(String.format("返回前 %d 条结果:\n\n", photos.size()));

            // 遍历前五条结果
            for (int i = 0; i < photos.size(); i++) {
                JSONObject photo = photos.getJSONObject(i);

                // 提取图片信息
                int id = photo.getInt("id", 0);
                String alt = photo.getStr("alt", "无描述");
                String photographer = photo.getStr("photographer", "未知摄影师");
                String photographerUrl = photo.getStr("photographer_url", "");
                String photoUrl = photo.getStr("url", "");
                int width = photo.getInt("width", 0);
                int height = photo.getInt("height", 0);
                String avgColor = photo.getStr("avg_color", "");

                // 提取不同尺寸的图片链接
                JSONObject src = photo.getJSONObject("src");
                String originalUrl = src != null ? src.getStr("original", "") : "";
                String largeUrl = src != null ? src.getStr("large", "") : "";
                String mediumUrl = src != null ? src.getStr("medium", "") : "";
                String smallUrl = src != null ? src.getStr("small", "") : "";

                // 格式化输出
                result.append(String.format("%d. 【%s】\n", i + 1, alt));
                result.append(String.format("   ID: %d\n", id));
                result.append(String.format("   摄影师: %s\n", photographer));
                result.append(String.format("   摄影师主页: %s\n", photographerUrl));
                result.append(String.format("   图片尺寸: %dx%d\n", width, height));
                result.append(String.format("   主色调: %s\n", avgColor));
                result.append(String.format("   详情页: %s\n", photoUrl));
                result.append(String.format("   原图链接: %s\n", originalUrl));
                result.append(String.format("   大图链接: %s\n", largeUrl));
                result.append(String.format("   中图链接: %s\n", mediumUrl));
                result.append(String.format("   小图链接: %s\n", smallUrl));
                result.append("\n");
            }

            // 添加下一页信息
            String nextPage = response.getStr("next_page", "");
            if (StrUtil.isNotBlank(nextPage)) {
                result.append("下一页链接: ").append(nextPage).append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("搜索Pexels图片失败: {}", e.getMessage(), e);
            return String.format("搜索失败: %s", e.getMessage());
        }
    }

    @Tool(description = "搜索网络图片")
    public String search(@ToolParam(description = "通过关键字搜索图片")String query){
        return searchPexelsImages(query);
    }
}