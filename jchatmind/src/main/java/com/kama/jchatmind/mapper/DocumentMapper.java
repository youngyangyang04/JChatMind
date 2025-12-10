package com.kama.jchatmind.mapper;

import com.kama.jchatmind.model.entity.Document;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author charon
 * @description 针对表【document】的数据库操作Mapper
 * @createDate 2025-12-02 15:42:18
 * @Entity com.kama.jchatmind.model.entity.Document
 */
@Mapper
public interface DocumentMapper {
    int insert(Document document);

    Document selectById(String id);

    List<Document> selectAll();

    List<Document> selectByKbId(String kbId);

    int deleteById(String id);

    int updateById(Document document);
}
