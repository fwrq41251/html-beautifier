package org.gradle;

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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int depth = -1;
		append(sb, depth);
		return sb.toString();
	}

	private void append(StringBuilder sb, int depth) {
		depth = depth + 1;
		if (null != value) {
			appendIndentation(sb, depth);
			sb.append(startTag.getTagStr() + value + endTag.getTagStr() + "\n");
		} else {
			appendIndentation(sb, depth);
			sb.append(startTag.getTagStr() + "\n");
			for (HtmlElement element : subElements) {
				element.append(sb, depth);
			}
			appendIndentation(sb, depth);
			sb.append(endTag.getTagStr() + "\n");
		}
	}

	private void appendIndentation(StringBuilder sb, int depth) {
		for (int i = 0; i < 4 * depth; i++) {
			sb.append(" ");
		}
	}

}
