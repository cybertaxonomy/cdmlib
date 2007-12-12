package org.bgbm.model;

import java.util.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;

@Entity
public class Record extends Annotatable {
	public Record() {
		super();
	}

	static Logger logger = Logger.getLogger(Record.class);

	private List<Track> tracks = new ArrayList();
	private String title;
	private Calendar publicationDate;
	private Label label;
	
	public Record(String title, Calendar publicationDate, Label label) {
		super();
		this.title = title;
		this.publicationDate = publicationDate;
		this.label = label;
	}

	
	@OneToMany(mappedBy="record")
	@Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE,
          org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
	public List<Track> getTracks() {
		return tracks;
	}
	protected void setTracks(List<Track> tracks) {
		this.tracks = tracks;
	}
	public void addTrack(String title, Band artist, double d){
		Track track=new Track(title,artist,d);
		track.setRecord(this);
		this.tracks.add(track);
	}
	public String getTitle() {
		if (title!=null){			
			return title;
		}else{
			return "";
		}
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
	
	@ManyToOne() // cascade={CascadeType.PERSIST, CascadeType.MERGE}
	@Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE,
          org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
	public Label getLabel() {
		return label;
	}
	public void setLabel(Label label) {
		this.label = label;
	}
	
	public String toString (){
		Integer annos = this.getAnnotations().size();
		Integer songs = this.getTracks().size();
		String result = "RECORD:"+this.getTitle()+" ("+label.getName()+") <#"+annos+"> Songs #"+songs+": ";
		for (Track t : tracks){
			result += t.toString()+" ";
		}
		return result;
	}
}
