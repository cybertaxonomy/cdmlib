package org.bgbm.model;

import javax.persistence.Entity;

import org.apache.log4j.Logger;

@Entity
public class Track extends MetaBase {
	static Logger logger = Logger.getLogger(Track.class);
	private String name;
	private float duration;
	private Record record;
	private Artist artist;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public float getDuration() {
		return duration;
	}
	public void setDuration(float duration) {
		this.duration = duration;
	}
	public Record getRecord() {
		return record;
	}
	public void setRecord(Record record) {
		this.record = record;
	}
	public Artist getArtist() {
		return artist;
	}
	public void setArtist(Artist artist) {
		this.artist = artist;
	}
	
}
