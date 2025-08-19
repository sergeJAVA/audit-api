package com.webbee.audit_api.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.webbee.audit_api.util.Indices;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = Indices.AUDIT_METHOD_INDEX)
public class AuditMethod {

    @Id
    private String id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime timestamp;

    @Field(type = FieldType.Keyword)
    private String logLevel;

    @Field(type = FieldType.Text)
    private String correlationId ;

    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = { @InnerField(suffix = "keyword", type = FieldType.Keyword) }
    )
    private String methodName;

    @Field(type = FieldType.Text)
    private String args;

    @Field(type = FieldType.Text)
    private String logType;

    @Field(type = FieldType.Text)
    private String result;

    @Field(type = FieldType.Text)
    private String exceptionMessage;

}
