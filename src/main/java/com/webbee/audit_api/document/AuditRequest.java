package com.webbee.audit_api.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.webbee.audit_api.util.Indices;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = Indices.AUDIT_REQUEST_INDEX)
@Mapping(mappingPath = "/mappings/audit_request_mapping.json")
@Setting(settingPath = "/settings/audit_request_settings.json")
public class AuditRequest {

    @Id
    private String id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime timestamp;

    private String requestType;

    private String method;

    private String statusCode;

    private String path;

    private String requestBody;

    private String responseBody;

}
