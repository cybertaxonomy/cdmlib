package org.bgbm.model;

import java.util.*;

import javax.persistence.Entity;

import org.apache.log4j.Logger;

@Entity
public class Record extends MetaBase {
	static Logger logger = Logger.getLogger(Record.class);

	private List<Track> tracks new ArrayList();
	private String title;
	private Calendar publicationDate;
	private Label label;
	
	public List<Track> getTracks() {
		return tracks;
	}
	public void setTracks(List<Track> tracks) {
		this.tracks = tracks;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Calendar getPublicationDate() {
		return publicationDate;
	}
	public void setPublicationDate(Calendar publicationDate) {
		this.publicationDate = publicationDate;
	}
	public Label getLabel() {
		return label;
	}
	public void setLabel(Label label) {
		this.label = label;
	}
}
