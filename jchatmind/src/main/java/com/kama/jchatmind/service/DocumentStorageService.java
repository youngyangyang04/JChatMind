package com.kama.jchatmind.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

/**
 * 文档存储服务接口
 */
public interface DocumentStorageService {
    /**
     * 保存上传的文件
     *
     * @param kbId       知识库ID
     * @param documentId 文档ID
     * @param file       上传的文件
     * @return 保存的文件路径
     * @throws IOException 文件保存失败
     */
    String saveFile(String kbId, String documentId, MultipartFile file) throws IOException;

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @throws IOException 文件删除失败
     */
    void deleteFile(String filePath) throws IOException;

    /**
     * 获取文件的完整路径
     *
     * @param filePath 相对文件路径
     * @return 完整文件路径
     */
    Path getFilePath(String filePath);

    /**
     * 检查文件是否存在
     *
     * @param filePath 文件路径
     * @return 文件是否存在
     */
    boolean fileExists(String filePath);
}
