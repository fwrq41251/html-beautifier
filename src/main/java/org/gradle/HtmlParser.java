package org.gradle;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

class HtmlParser {

	private String html;

	private Stack<Tag> stack;

	private AtomicInteger depth;

	private Map<Tag, List<HtmlElement>> map;

	private HtmlElement htmlElement;

	public HtmlParser() {
	}

	public HtmlParser on(String html) {
		this.html = html.trim();
		this.stack = new Stack<Tag>();
		this.depth = new AtomicInteger(0);
		this.map = Maps.newHashMap();
		return this;
	}

	public HtmlElement parse() {
		Pattern pattern = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>");
		Matcher matcher = pattern.matcher(html);
		List<Tag> tempTags = Lists.newArrayList();
		while (matcher.find()) {
			TagBuilder tagBuilder = new TagBuilder();
			Tag tag = tagBuilder.getTag(matcher.start(), matcher.end(), matcher.group());
			tempTags.add(tag);
		}
		for (Tag tag : tempTags) {
			tag.apply();
		}
		return htmlElement;
	}

	private void putSubElementIntoMap(HtmlElement element) {
		Tag parrentTag = null;
		if (!stack.empty()) {
			parrentTag = stack.peek();
			List<HtmlElement> parrentSubElements = map.get(parrentTag);
			parrentSubElements.add(element);
		}
	}

	abstract class Tag {

		final int startIndex;

		final int endIndex;

		final String tagStr;

		Tag(int startIndex, int endIndex, String tagStr) {
			super();
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.tagStr = tagStr;
		}

		abstract void apply();

	}

	class TagBuilder {
		Tag getTag(int startIndex, int endIndex, String tagStr) {
			if (isStartTag(tagStr)) {
				return new StartTag(startIndex, endIndex, tagStr);
			} else if (isEndTag(tagStr)) {
				return new EndTag(startIndex, endIndex, tagStr);
			} else if (isSingleTag(tagStr)) {
				return new SingleTag(startIndex, endIndex, tagStr);
			} else {
				throw new IllegalArgumentException("tagStr does not match anything");
			}
		}

		boolean isStartTag(String tagStr) {
			return !StringUtils.startsWith(tagStr, "</") && !StringUtils.endsWith(tagStr, "/>");
		}

		boolean isEndTag(String tagStr) {
			return StringUtils.startsWith(tagStr, "</");
		}

		boolean isSingleTag(String tagStr) {
			return StringUtils.endsWith(tagStr, "/>");
		}
	}

	class StartTag extends Tag {

		StartTag(int startIndex, int endIndex, String tagStr) {
			super(startIndex, endIndex, tagStr);
		}

		@Override
		void apply() {
			stack.push(this);
			map.put(this, Lists.newArrayList());
			depth.set(0);

		}

	}

	class EndTag extends Tag {

		EndTag(int startIndex, int endIndex, String tagStr) {
			super(startIndex, endIndex, tagStr);
		}

		@Override
		void apply() {
			Tag startTempTag = stack.pop();
			HtmlTag startTag = new HtmlTag(startTempTag.tagStr);
			HtmlTag endTag = new HtmlTag(this.tagStr);
			HtmlElement element = new HtmlElement(startTag, endTag);
			int depthInt = depth.incrementAndGet();
			if (depthInt == 1) {
				String value = StringUtils.substring(html, startTempTag.endIndex + 1, this.startIndex - 1).trim();
				element.setValue(value);
			} else if (depthInt >= 2) {
				List<HtmlElement> subElements = map.get(startTempTag);
				element.appendSubElements(subElements);
			}
			putSubElementIntoMap(element);
			htmlElement = element;
		}

	}

	class SingleTag extends Tag {

		SingleTag(int startIndex, int endIndex, String tagStr) {
			super(startIndex, endIndex, tagStr);
		}

		@Override
		void apply() {
			HtmlTag startTag = new HtmlTag(this.tagStr);
			HtmlElement element = new HtmlElement(startTag, null);
			putSubElementIntoMap(element);
		}

	}
}
