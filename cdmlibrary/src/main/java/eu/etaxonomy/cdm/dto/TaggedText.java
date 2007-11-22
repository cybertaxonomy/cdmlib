package eu.etaxonomy.cdm.dto;

public class TaggedText {
	private String text;
	private TagEnum type;
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public TagEnum getType() {
		return type;
	}
	public void setType(TagEnum type) {
		this.type = type;
	}
	
}
