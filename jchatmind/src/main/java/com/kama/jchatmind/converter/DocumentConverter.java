package com.kama.jchatmind.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kama.jchatmind.model.dto.DocumentDTO;
import com.kama.jchatmind.model.entity.Document;
import com.kama.jchatmind.model.request.CreateDocumentRequest;
import com.kama.jchatmind.model.request.UpdateDocumentRequest;
import com.kama.jchatmind.model.vo.DocumentVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@AllArgsConstructor
public class DocumentConverter {

    private final ObjectMapper objectMapper;

    public Document toEntity(DocumentDTO documentDTO) throws JsonProcessingException {
        Assert.notNull(documentDTO, "DocumentDTO cannot be null");

        return Document.builder()
                .id(documentDTO.getId())
                .kbId(documentDTO.getKbId())
                .filename(documentDTO.getFilename())
                .filetype(documentDTO.getFiletype())
                .size(documentDTO.getSize())
                .metadata(documentDTO.getMetadata() != null 
                        ? objectMapper.writeValueAsString(documentDTO.getMetadata()) 
                        : null)
                .createdAt(documentDTO.getCreatedAt())
                .updatedAt(documentDTO.getUpdatedAt())
                .build();
    }

    public DocumentDTO toDTO(Document document) throws JsonProcessingException {
        Assert.notNull(document, "Document cannot be null");

        return DocumentDTO.builder()
                .id(document.getId())
                .kbId(document.getKbId())
                .filename(document.getFilename())
                .filetype(document.getFiletype())
                .size(document.getSize())
                .metadata(document.getMetadata() != null 
                        ? objectMapper.readValue(document.getMetadata(), DocumentDTO.MetaData.class) 
                        : null)
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    public DocumentVO toVO(DocumentDTO dto) {
        return DocumentVO.builder()
                .id(dto.getId())
                .kbId(dto.getKbId())
                .filename(dto.getFilename())
                .filetype(dto.getFiletype())
                .size(dto.getSize())
                .build();
    }

    public DocumentVO toVO(Document document) throws JsonProcessingException {
        return toVO(toDTO(document));
    }

    public DocumentDTO toDTO(CreateDocumentRequest request) {
        Assert.notNull(request, "CreateDocumentRequest cannot be null");
        Assert.notNull(request.getKbId(), "KbId cannot be null");

        return DocumentDTO.builder()
                .kbId(request.getKbId())
                .filename(request.getFilename())
                .filetype(request.getFiletype())
                .size(request.getSize())
                .build();
    }

    public void updateDTOFromRequest(DocumentDTO dto, UpdateDocumentRequest request) {
        Assert.notNull(dto, "DocumentDTO cannot be null");
        Assert.notNull(request, "UpdateDocumentRequest cannot be null");

        if (request.getFilename() != null) {
            dto.setFilename(request.getFilename());
        }
        if (request.getFiletype() != null) {
            dto.setFiletype(request.getFiletype());
        }
        if (request.getSize() != null) {
            dto.setSize(request.getSize());
        }
    }
}
