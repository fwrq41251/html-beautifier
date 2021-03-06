package org.gradle;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlParserTest {

	@Test
	public void parseTest() throws IOException {
		String rawHtml = Files.toString(new File("to_beautified"), Charset.defaultCharset());
		HtmlElement html = HtmlParser.on(rawHtml).parse();
		System.out.println(html);
		// String formatted = html.toString();
		// System.out.println(formatted);
	}

	@Test
	public void regexTest() throws IOException {
		String rawHtml = Files.toString(new File("to_beautified"), Charset.defaultCharset());
		Pattern pattern = Pattern.compile("</?.+/?>");
		Matcher matcher = pattern.matcher(rawHtml);
		List<String> result = Lists.newArrayList();
		while (matcher.find()) {
			String tag = matcher.group();
			System.out.println(tag);
			result.add(tag);
		}
		System.out.println(matcher);
	}

	@Test
	public void regexTest2() {
		String raw = "</table>";
		Pattern pattern = Pattern.compile("(</.+>)");
		Matcher matcher = pattern.matcher(raw);
		int group = matcher.groupCount();
		System.out.println(group); 		// "1"
		boolean bool = matcher.find();
		System.out.println(bool); 		// "true"
		int start = matcher.start();
		System.out.println(start); 		// "0"
		int end = matcher.end();
		System.out.println(end); 		// "8"
	}

	@Test
	public void regexTest3() {
		String raw = "</table class='simple-table' width='100%' border='1' cellspacing='0'>";
		Pattern pattern = Pattern.compile("<(/?)(\\w+)(\\s+\\S+)*(/?)>");
		Matcher matcher = pattern.matcher(raw);
		matcher.find();
		System.out.println(matcher.group(1)); // "/"
		System.out.println(matcher.group(2)); // "table"
		System.out.println(matcher.group(4)); // ""
	}

}
