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

	/**
	 * raw html code.
	 */
	private String html;

	/**
	 * this stack holds start tag.
	 */
	private Stack<Tag> stack;

	private AtomicInteger depth;

	/**
	 * this map holds sub htmlElements waiting for appending to parent.
	 */
	private Map<Tag, List<HtmlElement>> map;

	private List<HtmlElement> topLevelElemets;

	private List<Tag> tagList;

	public HtmlParser() {
	}

	/**
	 * build method,return a HtmlParser instance.
	 * 
	 * @param html
	 * @return
	 */
	public HtmlParser on(String html) {
		this.html = html.trim();
		this.stack = new Stack<Tag>();
		this.depth = new AtomicInteger(0);
		this.map = Maps.newHashMap();
		this.topLevelElemets = Lists.newArrayList();
		this.tagList = Lists.newArrayList();
		return this;
	}

	/**
	 * parse raw html code to a tree structure.
	 * 
	 * @return
	 */
	public HtmlElement parse() {
		Pattern pattern = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>");
		Matcher matcher = pattern.matcher(html);
		TagBuilder tagBuilder = new TagBuilder();
		while (matcher.find()) {
			Tag tag = tagBuilder.build(matcher.start(), matcher.end(), matcher.group());
			tagList.add(tag);
		}
		for (Tag tag : tagList) {
			tag.apply();
		}
		if (topLevelElemets.size() == 1) {
			return topLevelElemets.get(0);
		} else {
			HtmlElement result = new HtmlElement(new HtmlTag("<html>"), new HtmlTag("</html>"));
			result.appendSubElements(topLevelElemets);
			return result;
		}
	}

	private void putSubElementIntoMap(HtmlElement element) {
		Tag parrentTag = null;
		if (!stack.empty()) {
			parrentTag = stack.peek();
			List<HtmlElement> parrentSubElements = map.get(parrentTag);
			parrentSubElements.add(element);
		} else {
			topLevelElemets.add(element);
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

		@Override
		public String toString() {
			return tagStr;
		}

	}

	class TagBuilder {
		Tag build(int startIndex, int endIndex, String tagStr) {
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

		private Pattern pattern = Pattern.compile("<(/?)(\\w+)(\\s+\\S+)*(/?)>");

		@Override
		void apply() {
			Tag startTempTag = stack.pop();
			while (!startTagMatchEndTag(startTempTag.tagStr)) {
				HtmlTag startTag = new HtmlTag(startTempTag.tagStr);
				HtmlElement element = new HtmlElement(startTag, null);
				setValue(startTempTag, element);
				putSubElementIntoMap(element);
				if (!map.get(startTempTag).isEmpty()) {
					List<HtmlElement> subElements = map.get(startTempTag);
					putSubElementsIntoMap(subElements);
				}
				depth.set(0);
				startTempTag = stack.pop();
			}
			HtmlTag startTag = new HtmlTag(startTempTag.tagStr);
			HtmlTag endTag = new HtmlTag(this.tagStr);
			HtmlElement element = new HtmlElement(startTag, endTag);
			int depthInt = depth.incrementAndGet();
			if (depthInt == 1 && map.get(startTempTag).isEmpty()) {
				String value = StringUtils.substring(html, startTempTag.endIndex + 1, this.startIndex - 1).trim();
				element.setValue(value);
			} else {
				List<HtmlElement> subElements = map.get(startTempTag);
				element.appendSubElements(subElements);
			}
			putSubElementIntoMap(element);
		}

		private void setValue(Tag startTempTag, HtmlElement element) {
			int index = tagList.indexOf(startTempTag);
			Tag followingTag = tagList.get(index + 1);
			String value = StringUtils.substring(html, startTempTag.endIndex + 1, followingTag.startIndex - 1).trim();
			if (StringUtils.isNotBlank(value))
				element.setValue(value);
		}

		private boolean startTagMatchEndTag(String startTagStr) {
			Matcher matcher1 = pattern.matcher(startTagStr);
			matcher1.find();
			String startTagType = matcher1.group(2);
			Matcher matcher2 = pattern.matcher(tagStr);
			matcher2.find();
			String endTagType = matcher2.group(2);
			return StringUtils.equals(startTagType, endTagType);
		}

		private void putSubElementsIntoMap(List<HtmlElement> elements) {
			Tag parrentTag = null;
			if (!stack.empty()) {
				parrentTag = stack.peek();
				List<HtmlElement> parrentSubElements = map.get(parrentTag);
				parrentSubElements.addAll(elements);
			} else {
				topLevelElemets.addAll(elements);
			}
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
