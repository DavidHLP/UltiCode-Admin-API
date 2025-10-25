package com.david.judge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("files")
public class FileRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("storage_key")
    private String storageKey;

    private String sha256;

    @TableField("mime_type")
    private String mimeType;

    @TableField("size_bytes")
    private Long sizeBytes;

    @TableField("created_by")
    private Long createdBy;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
