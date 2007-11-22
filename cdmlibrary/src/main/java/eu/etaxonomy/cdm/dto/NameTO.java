package eu.etaxonomy.cdm.dto;

import java.util.ArrayList;
import java.util.List;

public class NameTO {

	private String fullname;
	private List<TaggedText> taggedName = new ArrayList();

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public List<TaggedText> getTaggedName() {
		return taggedName;
	}

	protected void addNameToken(TaggedText token) {
		this.taggedName.add(token);
	}

}
