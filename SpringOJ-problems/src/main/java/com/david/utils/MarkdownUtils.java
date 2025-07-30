package com.david.utils;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;

public class MarkdownUtils {

	// 创建解析器和渲染器的实例，可以作为静态变量以提高性能
	private static final Parser parser = Parser.builder().build();
	private static final TextContentRenderer renderer = TextContentRenderer.builder().build();

	/**
	 * 将 Markdown 格式的字符串转换为纯文本
	 *
	 * @param markdownContent 包含 Markdown 格式的字符串
	 * @return 只包含纯文本的字符串
	 */
	public static String toPlainText(String markdownContent) {
		if (markdownContent == null || markdownContent.isEmpty()) {
			return "";
		}
		// 1. 使用解析器将 Markdown 文本解析成一个 Node 对象（抽象语法树）
		Node document = parser.parse(markdownContent);

		// 2. 使用 TextContentRenderer 将 Node 对象渲染成纯文本
		return renderer.render(document).trim();
	}
}
