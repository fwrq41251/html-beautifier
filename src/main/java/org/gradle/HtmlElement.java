package org.gradle;

import java.text.MessageFormat;
import java.util.List;

import com.google.common.collect.Lists;

class HtmlElement {

	private HtmlTag startTag;

	private HtmlTag endTag;

	private List<HtmlElement> subElements = Lists.newArrayList();

	private String value;

	public HtmlElement(HtmlTag startTag, HtmlTag endTag) {
		super();
		this.startTag = startTag;
		this.endTag = endTag;
	}

	public void addSubElement(HtmlElement element) {
		subElements.add(element);
	}

	public void appendSubElements(List<HtmlElement> elements) {
		subElements.addAll(elements);
	}

	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * XXX recursive toString() invoke, bad performance.This may move to the
	 * Parser class.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(MessageFormat.format("{0}", startTag.getTagStr()));
		if (subElements.size() == 0) {
			sb.append(value);
		} else {
			sb.append("\n");
			subElements.forEach(element -> {
				appendIndentation(sb);
				sb.append(element.toString());
			});
		}
		sb.append(MessageFormat.format("{0}\n", endTag.getTagStr()));
		return sb.toString();
	}

	private void appendIndentation(StringBuilder sb) {
		sb.append(" ");
		sb.append(" ");
		sb.append(" ");
		sb.append(" ");
	}

}
