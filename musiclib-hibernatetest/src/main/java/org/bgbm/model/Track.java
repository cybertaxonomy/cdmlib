package org.bgbm.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.apache.log4j.Logger;

@Entity
public class Track extends MetaBase {
	static Logger logger = Logger.getLogger(Track.class);
	private String name;
	private double duration;
	private Record record;
	private Artist artist;
	
	public Track(String title, Artist artist2, double duration) {
		this.name=title;
		this.artist=artist2;
		this.duration=duration;
	}
	
	
	public String getName() {
		return name;
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
	@ManyToOne(cascade=CascadeType.PERSIST)
	public Record getRecord() {
		return record;
	}
	public void setRecord(Record record) {
		this.record = record;
	}
	@ManyToOne(cascade=CascadeType.PERSIST)
	public Artist getArtist() {
		return artist;
	}
	public void setArtist(Artist artist) {
		this.artist = artist;
	}
	
}
