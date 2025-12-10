package com.kama.jchatmind.agent.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class FileSystemTools implements Tool {

    // 允许访问的基础目录，防止路径遍历攻击
    private static final String BASE_DIRECTORY = System.getProperty("user.dir");

    @Override
    public String getName() {
        return "fileSystemTool";
    }

    @Override
    public String getDescription() {
        return "提供文件系统操作的工具，包括读取文件、写入文件、列出目录等功能";
    }

    @Override
    public ToolType getType() {
        return ToolType.OPTIONAL;
    }

    /**
     * 读取文件内容
     *
     * @param filePath 文件路径（相对于工作目录）
     * @return 文件内容
     */
    @org.springframework.ai.tool.annotation.Tool(
            name = "readFile",
            description = "读取指定文件的内容。参数：filePath - 文件路径（相对于工作目录）"
    )
    public String readFile(String filePath) {
        try {
            Path path = validateAndResolvePath(filePath);

            if (!Files.exists(path)) {
                return "错误：文件不存在 - " + filePath;
            }

            if (!Files.isRegularFile(path)) {
                return "错误：路径不是文件 - " + filePath;
            }

            String content = Files.readString(path);
            log.info("成功读取文件: {}", filePath);
            return "文件内容:\n" + content;

        } catch (SecurityException e) {
            log.error("安全错误：{}", e.getMessage());
            return "错误：访问被拒绝 - " + e.getMessage();
        } catch (IOException e) {
            log.error("读取文件失败: {}", filePath, e);
            return "错误：读取文件失败 - " + e.getMessage();
        } catch (Exception e) {
            log.error("未知错误: {}", e.getMessage(), e);
            return "错误：操作失败 - " + e.getMessage();
        }
    }

    /**
     * 写入文件内容
     *
     * @param filePath 文件路径（相对于工作目录）
     * @param content  要写入的内容
     * @return 操作结果
     */
    @org.springframework.ai.tool.annotation.Tool(
            name = "writeFile",
            description = "将内容写入指定文件。如果文件不存在则创建，如果文件存在则覆盖。参数：filePath - 文件路径（相对于工作目录），content - 要写入的内容"
    )
    public String writeFile(String filePath, String content) {
        try {
            Path path = validateAndResolvePath(filePath);

            // 确保父目录存在
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
                log.info("创建目录: {}", parent);
            }

            Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            log.info("成功写入文件: {}", filePath);
            return "成功写入文件: " + filePath;

        } catch (SecurityException e) {
            log.error("安全错误：{}", e.getMessage());
            return "错误：访问被拒绝 - " + e.getMessage();
        } catch (IOException e) {
            log.error("写入文件失败: {}", filePath, e);
            return "错误：写入文件失败 - " + e.getMessage();
        } catch (Exception e) {
            log.error("未知错误: {}", e.getMessage(), e);
            return "错误：操作失败 - " + e.getMessage();
        }
    }

    /**
     * 追加内容到文件
     *
     * @param filePath 文件路径（相对于工作目录）
     * @param content  要追加的内容
     * @return 操作结果
     */
    @org.springframework.ai.tool.annotation.Tool(
            name = "appendToFile",
            description = "将内容追加到指定文件的末尾。如果文件不存在则创建。参数：filePath - 文件路径（相对于工作目录），content - 要追加的内容"
    )
    public String appendToFile(String filePath, String content) {
        try {
            Path path = validateAndResolvePath(filePath);

            // 确保父目录存在
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
                log.info("创建目录: {}", parent);
            }

            Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            log.info("成功追加内容到文件: {}", filePath);
            return "成功追加内容到文件: " + filePath;

        } catch (SecurityException e) {
            log.error("安全错误：{}", e.getMessage());
            return "错误：访问被拒绝 - " + e.getMessage();
        } catch (IOException e) {
            log.error("追加内容到文件失败: {}", filePath, e);
            return "错误：追加内容失败 - " + e.getMessage();
        } catch (Exception e) {
            log.error("未知错误: {}", e.getMessage(), e);
            return "错误：操作失败 - " + e.getMessage();
        }
    }

    /**
     * 列出目录中的文件和子目录
     *
     * @param directoryPath 目录路径（相对于工作目录），如果为空则列出当前目录
     * @return 目录内容列表
     */
    @org.springframework.ai.tool.annotation.Tool(
            name = "listFiles",
            description = "列出指定目录中的文件和子目录。参数：directoryPath - 目录路径（相对于工作目录），如果为空则列出当前目录"
    )
    public String listFiles(String directoryPath) {
        try {
            Path path;
            if (directoryPath == null || directoryPath.trim().isEmpty()) {
                path = Paths.get(BASE_DIRECTORY);
            } else {
                path = validateAndResolvePath(directoryPath);
            }

            if (!Files.exists(path)) {
                return "错误：目录不存在 - " + directoryPath;
            }

            if (!Files.isDirectory(path)) {
                return "错误：路径不是目录 - " + directoryPath;
            }

            List<String> items = Files.list(path)
                    .map(p -> {
                        String name = p.getFileName().toString();
                        if (Files.isDirectory(p)) {
                            return "[DIR] " + name;
                        } else {
                            try {
                                long size = Files.size(p);
                                return "[FILE] " + name + " (" + formatFileSize(size) + ")";
                            } catch (IOException e) {
                                return "[FILE] " + name;
                            }
                        }
                    })
                    .sorted()
                    .collect(Collectors.toList());

            if (items.isEmpty()) {
                return "目录为空: " + directoryPath;
            }

            log.info("成功列出目录内容: {}", directoryPath);
            return "目录内容 (" + directoryPath + "):\n" + String.join("\n", items);

        } catch (SecurityException e) {
            log.error("安全错误：{}", e.getMessage());
            return "错误：访问被拒绝 - " + e.getMessage();
        } catch (IOException e) {
            log.error("列出目录失败: {}", directoryPath, e);
            return "错误：列出目录失败 - " + e.getMessage();
        } catch (Exception e) {
            log.error("未知错误: {}", e.getMessage(), e);
            return "错误：操作失败 - " + e.getMessage();
        }
    }

    /**
     * 删除文件或目录
     *
     * @param path 文件或目录路径（相对于工作目录）
     * @return 操作结果
     */
    @org.springframework.ai.tool.annotation.Tool(
            name = "deleteFile",
            description = "删除指定的文件或目录。参数：path - 文件或目录路径（相对于工作目录）"
    )
    public String deleteFile(String path) {
        try {
            Path filePath = validateAndResolvePath(path);

            if (!Files.exists(filePath)) {
                return "错误：文件或目录不存在 - " + path;
            }

            if (Files.isDirectory(filePath)) {
                // 递归删除目录
                try (Stream<Path> paths = Files.walk(filePath)) {
                    paths.sorted((a, b) -> b.compareTo(a)) // 先删除文件，再删除目录
                            .forEach(p -> {
                                try {
                                    Files.delete(p);
                                } catch (IOException e) {
                                    log.warn("删除失败: {}", p, e);
                                }
                            });
                }
                log.info("成功删除目录: {}", path);
                return "成功删除目录: " + path;
            } else {
                Files.delete(filePath);
                log.info("成功删除文件: {}", path);
                return "成功删除文件: " + path;
            }

        } catch (SecurityException e) {
            log.error("安全错误：{}", e.getMessage());
            return "错误：访问被拒绝 - " + e.getMessage();
        } catch (IOException e) {
            log.error("删除文件失败: {}", path, e);
            return "错误：删除失败 - " + e.getMessage();
        } catch (Exception e) {
            log.error("未知错误: {}", e.getMessage(), e);
            return "错误：操作失败 - " + e.getMessage();
        }
    }

    /**
     * 创建目录
     *
     * @param directoryPath 目录路径（相对于工作目录）
     * @return 操作结果
     */
    @org.springframework.ai.tool.annotation.Tool(
            name = "createDirectory",
            description = "创建指定目录，如果父目录不存在则一并创建。参数：directoryPath - 目录路径（相对于工作目录）"
    )
    public String createDirectory(String directoryPath) {
        try {
            Path path = validateAndResolvePath(directoryPath);

            if (Files.exists(path)) {
                if (Files.isDirectory(path)) {
                    return "目录已存在: " + directoryPath;
                } else {
                    return "错误：路径已存在但不是目录 - " + directoryPath;
                }
            }

            Files.createDirectories(path);
            log.info("成功创建目录: {}", directoryPath);
            return "成功创建目录: " + directoryPath;

        } catch (SecurityException e) {
            log.error("安全错误：{}", e.getMessage());
            return "错误：访问被拒绝 - " + e.getMessage();
        } catch (IOException e) {
            log.error("创建目录失败: {}", directoryPath, e);
            return "错误：创建目录失败 - " + e.getMessage();
        } catch (Exception e) {
            log.error("未知错误: {}", e.getMessage(), e);
            return "错误：操作失败 - " + e.getMessage();
        }
    }

    /**
     * 验证路径并解析为绝对路径，防止路径遍历攻击
     *
     * @param filePath 文件路径
     * @return 解析后的绝对路径
     * @throws SecurityException 如果路径不安全
     */
    private Path validateAndResolvePath(String filePath) throws SecurityException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        Path basePath = Paths.get(BASE_DIRECTORY).toAbsolutePath().normalize();
        Path resolvedPath = basePath.resolve(filePath).toAbsolutePath().normalize();

        // 检查解析后的路径是否在基础目录内
        if (!resolvedPath.startsWith(basePath)) {
            throw new SecurityException("路径遍历攻击被阻止: " + filePath);
        }

        return resolvedPath;
    }

    /**
     * 格式化文件大小
     *
     * @param size 文件大小（字节）
     * @return 格式化后的文件大小字符串
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
