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
        Node document = parser.parse(markdownContent);
        long length = renderer.render(document).trim().length();
        return length > 75
                ? renderer.render(document).trim().substring(0, 75) + "..."
                : renderer.render(document).trim() + "...";
    }
}
