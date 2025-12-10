package com.kama.jchatmind.model.request;

import lombok.Data;

@Data
public class CreateDocumentRequest {
    private String kbId;
    private String filename;
    private String filetype;
    private Long size;
}

