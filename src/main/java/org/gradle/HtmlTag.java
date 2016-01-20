package org.gradle;

class HtmlTag {

	private final String tagStr;


	public HtmlTag(String tagStr) {
		this.tagStr = tagStr;
	}

	public String getTagStr() {
		return tagStr;
	}

	@Override
	public String toString() {
		return tagStr;
	}

}
