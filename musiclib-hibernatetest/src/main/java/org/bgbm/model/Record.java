package org.bgbm.model;

import java.util.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.apache.log4j.Logger;

@Entity
public class Record extends MetaBase {
	static Logger logger = Logger.getLogger(Record.class);

	private List<Track> tracks = new ArrayList();
	private String title;
	private Calendar publicationDate;
	private Label label;
	
	@OneToMany(cascade=CascadeType.PERSIST, mappedBy="record")
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
	@ManyToOne(cascade=CascadeType.PERSIST)
	public Label getLabel() {
		return label;
	}
	public void setLabel(Label label) {
		this.label = label;
	}
}
