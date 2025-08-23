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
@Document(indexName = Indices.AUDIT_METHOD_INDEX)
@Mapping(mappingPath = "/mappings/audit_method_mapping.json")
@Setting(settingPath = "/settings/audit_method_settings.json")
public class AuditMethod {

    @Id
    private String id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime timestamp;

    private String logLevel;

    private String correlationId;

    private String methodName;

    private String args;

    private String logType;

    private String result;

    private String exceptionMessage;

}
