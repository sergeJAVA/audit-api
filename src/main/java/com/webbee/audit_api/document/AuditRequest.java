package com.webbee.audit_api.document;

import com.webbee.audit_api.util.Indices;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = Indices.AUDIT_REQUEST_INDEX)
public class AuditRequest {

    @Id
    private String id;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime timestamp;

    @Field(type = FieldType.Keyword)
    private String requestType;

    @Field(type = FieldType.Keyword)
    private String method;

    @Field(type = FieldType.Keyword)
    private String statusCode;

    @Field(type = FieldType.Text)
    private String path;

    @Field(type = FieldType.Text)
    private String requestBody;

    @Field(type = FieldType.Text)
    private String responseBody;

}
