package com.david.strategy.utils;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.Normalizer;

/**
 * 智能输出标准化工具
 *
 * 目标：最大限度消除打印格式差异带来的误判（如 [0,1] vs [0, 1]、大小写、数字冗余零等），
 * 保留结构与顺序语义，不引入“排序/容差”这类可能改变语义的比较策略。
 *
 * 设计要点：
 * 1) 仅做“无损”标准化：
 *    - 括号/逗号周边多余空格移除
 *    - 布尔统一为小写 true/false
 *    - 数字去除无意义前导/末尾零（-0 -> 0；1.2300 -> 1.23）
 *    - 行内两端空白去除，保留换行，逐行处理
 * 2) 结构感知：在方括号/花括号/小括号的“嵌套区域”内执行更激进的空白清理，避免 [0, 1] 被判不等。
 * 3) 对非结构化文本：仅做温和的修整（trim 每行两端空白，不折叠行内空格、不打平换行）。
 *
 * 注意：此工具仅用于“输出标准化”，不用于“输入标准化”（如输入文件名、路径等）。
 * 也不用于“错误信息标准化”（如异常堆栈、错误消息等）。
 * 该工具的“无损”特性确保了在结构化比较时，不会因为格式差异而引入误判。
 */
public final class SmartOutputNormalizer {

	private SmartOutputNormalizer() {}

	private static final ObjectMapper OM = new ObjectMapper();

	/**
	 * 标准化输出文本。
	 *
	 * @param text 原始输出
	 * @return 规范化后的输出（可直接用于字符串相等判断）
	 */
	public static String normalize(String text) {
		if (text == null || text.isEmpty()) return "";

		// 1) 统一 Unicode 规范形式，去除 BOM 与零宽字符，尽量使用内置标准库
		String pre = Normalizer.normalize(text, Normalizer.Form.NFC)
				.replace("\uFEFF", "") // BOM
				.replaceAll("[\u200B\u200C\u200D\u2060]", ""); // 零宽字符

		// 优先尝试使用内置 JSON 解析，将 JSON/数组/数值/布尔等序列化为紧凑标准字符串
		String trimmed = pre.strip();
		try {
			JsonNode node = OM.readTree(trimmed);
			// 解析成功，直接返回 Jackson 的标准化紧凑表示
			return OM.writeValueAsString(node);
		} catch (Exception ignore) {
			// 非 JSON，继续走结构化与逐行规范化逻辑
		}

		// 尝试“逐行 JSON 规范化”：若每一非空行都可单独解析为 JSON，则对每行做 Jackson 规范化
		String[] maybeJsonLines = trimmed.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
		boolean allJsonLines = true;
		for (String ln : maybeJsonLines) {
			String s = ln.strip();
			if (s.isEmpty()) continue;
			try { OM.readTree(s); } catch (Exception e) { allJsonLines = false; break; }
		}
		if (allJsonLines) {
			StringBuilder b = new StringBuilder(trimmed.length());
			for (int i = 0; i < maybeJsonLines.length; i++) {
				String s = maybeJsonLines[i].strip();
				if (s.isEmpty()) {
					// 保留空行
					if (i < maybeJsonLines.length - 1) b.append('\n');
					continue;
				}
				try {
					JsonNode node = OM.readTree(s);
					b.append(OM.writeValueAsString(node));
				} catch (Exception e) {
					// 理论上不会触发，保底原样输出行
					b.append(s);
				}
				if (i < maybeJsonLines.length - 1) b.append('\n');
			}
			String result = b.toString().replaceAll("\\s+$", "");
			return result;
		}

		// 统一换行
		String unified = pre.replace("\r\n", "\n").replace('\r', '\n');
		StringBuilder out = new StringBuilder(unified.length());

		int depth = 0; // 括号嵌套深度
		boolean inQuotes = false; // 是否在字符串字面量中
		char quoteChar = '\0';

		// 逐字符处理，结构感知地清理空白
		for (int i = 0; i < unified.length(); i++) {
			char c = unified.charAt(i);

			// 引号状态机（保留引号内的内容原样）
			if (inQuotes) {
				out.append(c);
				if (c == quoteChar && !isEscaped(unified, i)) {
					inQuotes = false;
				}
				continue;
			}

			if (c == '\'' || c == '"') {
				inQuotes = true;
				quoteChar = c;
				out.append(c);
				continue;
			}

			// 括号深度跟踪
			if (c == '[' || c == '{' || c == '(') depth++;
			if (c == ']' || c == '}' || c == ')') depth = Math.max(0, depth - 1);

			if (depth > 0) {
				// 在结构内部，去掉逗号、冒号、等号周围和括号紧邻的多余空格
				if (Character.isWhitespace(c)) {
					// 查看前后有无必须的非空格分隔
					char prev = lastNonSpace(out);
					char next = nextNonSpace(unified, i + 1);
					if (prev == '\0' || next == '\0') {
						continue; // 边界空白丢弃
					}
					if (prev == '[' || prev == '{' || prev == '(' || prev == ',' || prev == ':' || prev == '=') {
						continue; // 分隔符后的空白去除
					}
					if (next == ']' || next == '}' || next == ')' || next == ',' || next == ':' || next == '=') {
						continue; // 分隔符前的空白去除
					}
					// 其他位置保留一个空格
					if (out.length() > 0 && out.charAt(out.length() - 1) != ' ') out.append(' ');
					continue;
				}
				// 统一分隔符本体
				if (c == ',') {
					// 确保逗号后不自动补空格（结构化比较不需要）
					out.append(',');
					continue;
				}
				// 直接附加
				out.append(c);
			} else {
				// 非结构内：保留换行；行内仅去首尾空白
				if (c == '\n') {
					trimEnd(out);
					out.append('\n');
					continue;
				}
				out.append(c);
			}
		}

		// 收尾：逐行精修（布尔、数字规范化；行首尾空白剔除）
		String[] lines = out.toString().split("\n", -1);
		StringBuilder finalOut = new StringBuilder(out.length());
		for (int i = 0; i < lines.length; i++) {
			String ln = lines[i].strip();
			ln = normalizeBooleans(ln);
			ln = normalizeNumbersInLine(ln);
			finalOut.append(ln);
			if (i < lines.length - 1) finalOut.append('\n');
		}
		String result = finalOut.toString();
		// 移除结尾处所有空白（包含换行/空格/制表符），避免仅因末尾换行产生误判
		result = result.replaceAll("\\s+$", "");
		return result;
	}

	// 将 True/FALSE 等统一为小写 true/false（仅替换独立词）
	private static String normalizeBooleans(String s) {
		return s
				.replaceAll("(?i)\\bTRUE\\b", "true")
				.replaceAll("(?i)\\bFALSE\\b", "false");
	}

	// 规范化数字表示：移除无意义的前导/末尾零，将 -0 归一为 0；保持科学计数法为常规字符串
	private static String normalizeNumbersInLine(String s) {
		// 匹配独立数字（含可选正负号、小数、科学计数法），避免命中变量名/键名
		final Pattern NUMBER = Pattern.compile("(?<![A-Za-z0-9_])([+-]?((?:\\d+\\.\\d+)|(?:\\d+\\.)|(?:\\.\\d+)|(?:\\d+))(?:[eE][+-]?\\d+)?)");
		Matcher m = NUMBER.matcher(s);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String token = m.group(1);
			String replacement;
			try {
				BigDecimal bd = new BigDecimal(token);
				if (bd.compareTo(BigDecimal.ZERO) == 0) {
					replacement = "0";
				} else {
					replacement = stripTrailingZerosToPlainString(bd);
				}
			} catch (Exception ignore) {
				replacement = token; // 无法解析则原样返回
			}
			// 使用 quoteReplacement 以避免反斜杠等转义问题
			m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
		}
		m.appendTail(sb);
		return sb.toString();
	}

	private static String stripTrailingZerosToPlainString(BigDecimal bd) {
		bd = bd.stripTrailingZeros();
		String plain = bd.toPlainString();
		// 处理形如 1. 或 .5 -> 1 与 0.5
		if (plain.endsWith(".")) plain = plain.substring(0, plain.length() - 1);
		if (plain.startsWith(".")) plain = "0" + plain;
		return plain;
	}

	private static boolean isEscaped(String s, int pos) {
		int backslashes = 0;
		for (int i = pos - 1; i >= 0 && s.charAt(i) == '\\'; i--) backslashes++;
		return (backslashes % 2) == 1;
	}

	private static char lastNonSpace(CharSequence sb) {
		for (int i = sb.length() - 1; i >= 0; i--) {
			char c = sb.charAt(i);
			if (!Character.isWhitespace(c)) return c;
		}
		return '\0';
	}

	private static char nextNonSpace(CharSequence s, int start) {
		for (int i = start; i < s.length(); i++) {
			char c = s.charAt(i);
			if (!Character.isWhitespace(c)) return c;
		}
		return '\0';
	}

	private static void trimEnd(StringBuilder sb) {
		int i = sb.length() - 1;
		while (i >= 0 && Character.isWhitespace(sb.charAt(i)) && sb.charAt(i) != '\n') i--;
		if (i < sb.length() - 1) sb.setLength(i + 1);
	}
}
