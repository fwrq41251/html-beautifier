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
		HtmlElement result = null;
		Pattern pattern = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>");
		Matcher matcher = pattern.matcher(html);
		List<Tag> tempTags = Lists.newArrayList();
		while (matcher.find()) {
			Tag tag = new Tag(matcher.start(), matcher.end(), matcher.group());
			tempTags.add(tag);
		}
		for (Tag tag : tempTags) {
			if (tag.isStartTag()) {
				stack.push(tag);
				map.put(tag, Lists.newArrayList());
				depth.set(0);
			} else if (tag.isEndTag()) {
				Tag startTempTag = stack.pop();
				HtmlTag startTag = new HtmlTag(startTempTag.tagStr);
				HtmlTag endTag = new HtmlTag(tag.tagStr);
				HtmlElement element = new HtmlElement(startTag, endTag);
				int depthInt = depth.incrementAndGet();
				Tag parrentTag = null;
				if (!stack.empty()) {
					parrentTag = stack.peek();
				}
				if (depthInt == 1) {
					String value = StringUtils.substring(html, startTempTag.endIndex + 1, tag.startIndex - 1).trim();
					element.setValue(value);
					if (null != parrentTag) {
						List<HtmlElement> parrentSubElements = map.get(parrentTag);
						parrentSubElements.add(element);
					}
				} else if (depthInt >= 2) {
					List<HtmlElement> subElements = map.get(startTempTag);
					element.appendSubElements(subElements);
					if (null != parrentTag) {
						List<HtmlElement> parrentSubElements = map.get(parrentTag);
						parrentSubElements.add(element);
					}
				}
				result = element;
			} else if (tag.isSingleTag()) {
				HtmlTag startTag = new HtmlTag(tag.tagStr);
				HtmlElement element = new HtmlElement(startTag, null);
			}
		}
		return result;
	}

	static class Tag {

		final int startIndex;

		final int endIndex;

		final String tagStr;

		Tag(int startIndex, int endIndex, String tagStr) {
			super();
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.tagStr = tagStr;
		}

		boolean isStartTag() {
			return !StringUtils.startsWith(tagStr, "</") && !StringUtils.endsWith(tagStr, "/>");
		}

		boolean isEndTag() {
			return StringUtils.startsWith(tagStr, "</");
		}

		boolean isSingleTag() {
			return StringUtils.endsWith(tagStr, "/>");
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + endIndex;
			result = prime * result + startIndex;
			result = prime * result + ((tagStr == null) ? 0 : tagStr.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Tag other = (Tag) obj;
			if (endIndex != other.endIndex)
				return false;
			if (startIndex != other.startIndex)
				return false;
			if (tagStr == null) {
				if (other.tagStr != null)
					return false;
			} else if (!tagStr.equals(other.tagStr))
				return false;
			return true;
		}

	}
}
