package org.bgbm.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;

@Entity
public class Track extends MetaBase {
	public Track() {
		super();
		// TODO Auto-generated constructor stub
	}
	static Logger logger = Logger.getLogger(Track.class);
	private String name;
	private double duration;
	private Record record;
	private Band artist;
	
	public Track(String title, Band artist2, double duration) {
		this.name=title;
		this.artist=artist2;
		this.duration=duration;
	}
	
	
	public String getName() {
		if (name!=null){			
			return name;
		}else{
			return "";
		}
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getDuration() {
		return duration;
	}
	public void setDuration(double duration) {
		this.duration = duration;
	}
	
	@ManyToOne
	public Record getRecord() {
		return record;
	}
	public void setRecord(Record record) {
		this.record = record;
	}
	@ManyToOne
	@Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE,
        org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
	public Band getArtist() {
		return artist;
	}
	public void setArtist(Band artist) {
		this.artist = artist;
	}
	public String toString(){
		String art = "";
		if (artist!=null){
			art = artist.toString(); 
		}
		return getName()+"("+art+")";
	}
}
