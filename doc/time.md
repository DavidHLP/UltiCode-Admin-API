```java
	/**
	 * 首次浏览时间
	 */
	@Builder.Default
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	@TableField(value = "created_at", fill = FieldFill.INSERT)
	private LocalDateTime createdAt = LocalDateTime.now();
```