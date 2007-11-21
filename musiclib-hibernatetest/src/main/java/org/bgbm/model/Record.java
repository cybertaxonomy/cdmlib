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
	
	public Record(String title, Calendar publicationDate, Label label) {
		super();
		this.title = title;
		this.publicationDate = publicationDate;
		this.label = label;
	}

	
	@OneToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE}, mappedBy="record")
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
	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE})
	public Label getLabel() {
		return label;
	}
	public void setLabel(Label label) {
		this.label = label;
	}
	
	public String toString (){
		String result = "RECORD:"+this.title+" [";
		for (Track t : tracks){
			result += t.getName()+"("+t.getArtist().getName()+") ";
		}
		result += "]";
		return result;
	}
}
