package com.zyk.aiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import com.zyk.aiagent.constent.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 文件操作工具类
 */
@Component
public class FileOperationTool {

    private final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/file";

    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = "Name of a file to read") String fileName) {
        String filePath = FILE_DIR + "/" + fileName;
        try {
            return FileUtil.readUtf8String(filePath);
        } catch (IORuntimeException e) {
            return "文件读取失败：" + e.getMessage();
        }
    }

    @Tool(description = "write content from a file")
    public String writeFile(@ToolParam(description = "Name of a file to write") String fileName,
                            @ToolParam(description = "Content to write to the file") String content) {
        String filePath = FILE_DIR + "/" + fileName;
        try {
            FileUtil.mkdir(filePath);
            FileUtil.writeUtf8String(content,filePath );
            return "写入成功" + fileName;
        } catch (Exception e) {
            return "文件写入失败：" + e.getMessage();
        }
    }
}
