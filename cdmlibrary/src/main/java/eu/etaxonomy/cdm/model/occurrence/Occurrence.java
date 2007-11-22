/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;


import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Media;
import eu.etaxonomy.cdm.model.common.Taxon;
import eu.etaxonomy.cdm.model.description.Sex;
import eu.etaxonomy.cdm.model.description.Stage;

import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * type figures are observations with at least a figure object in media
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:41
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class Occurrence extends Taxon {
	static Logger logger = Logger.getLogger(Occurrence.class);
	//Locality name (as free text) where this occurrence happened
	private String locality;
	//Date on which this occurrence happened
	private Calendar eventDate;
	private Set<Media> media;
	private Point exactLocation;
	private NamedArea namedArea;
	private Agent collector;
	private Collection collection;
	private String catalogNumber;
	private String fieldNumber;
	private String fieldNotes;
	private String collectingMethod;
	private Sex sex;
	private Stage lifeStage;
	private Integer individualCount;
	// meter above/below sea level of the surface
	private Integer absoluteElevation;
	// distance in meter from the surface when colecting. E.g. 10m below the ground or lake surface or 20m in the canope
	private Integer relativeElevation;
	// the verbatim description of this occurrence. Free text usable when no atomised data is available.
	// in conjunction with titleCache which serves as the "citation" string for this object
	private String description;

	public Collection getCollection(){
		return this.collection;
	}

	/**
	 * 
	 * @param collection    collection
	 */
	public void setCollection(Collection collection){
		this.collection = collection;
	}

	public Point getExactLocation(){
		return this.exactLocation;
	}

	/**
	 * 
	 * @param exactLocation    exactLocation
	 */
	public void setExactLocation(Point exactLocation){
		this.exactLocation = exactLocation;
	}

	

	@OneToMany
	public Set<Media> getMedia() {
		return media;
	}
	protected void setMedia(Set<Media> media) {
		this.media = media;
	}
	public void addMedia(Media media) {
		this.media.add(media);
	}
	public void removeMedia(Media media) {
		this.media.remove(media);
	}

	
	public Agent getCollector(){
		return this.collector;
	}

	/**
	 * 
	 * @param collector    collector
	 */
	public void setCollector(Agent collector){
		this.collector = collector;
	}

	public NamedArea getNamedArea(){
		return this.namedArea;
	}

	/**
	 * 
	 * @param namedArea    namedArea
	 */
	public void setNamedArea(NamedArea namedArea){
		this.namedArea = namedArea;
	}

	public String getLocality(){
		return this.locality;
	}

	/**
	 * 
	 * @param locality    locality
	 */
	public void setLocality(String locality){
		this.locality = locality;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Calendar getEventDate(){
		return this.eventDate;
	}

	/**
	 * 
	 * @param eventDate    eventDate
	 */
	public void setEventDate(Calendar eventDate){
		this.eventDate = eventDate;
	}

	public String generateTitle(){
		return "";
	}

	public String getCatalogNumber() {
		return catalogNumber;
	}

	public void setCatalogNumber(String catalogNumber) {
		this.catalogNumber = catalogNumber;
	}

	public String getFieldNumber() {
		return fieldNumber;
	}

	public void setFieldNumber(String fieldNumber) {
		this.fieldNumber = fieldNumber;
	}

	public String getFieldNotes() {
		return fieldNotes;
	}

	public void setFieldNotes(String fieldNotes) {
		this.fieldNotes = fieldNotes;
	}

	public String getCollectingMethod() {
		return collectingMethod;
	}

	public void setCollectingMethod(String collectingMethod) {
		this.collectingMethod = collectingMethod;
	}

	public Sex getSex() {
		return sex;
	}

	public void setSex(Sex sex) {
		this.sex = sex;
	}

	public Stage getLifeStage() {
		return lifeStage;
	}

	public void setLifeStage(Stage lifeStage) {
		this.lifeStage = lifeStage;
	}

	public Integer getIndividualCount() {
		return individualCount;
	}

	public void setIndividualCount(Integer individualCount) {
		this.individualCount = individualCount;
	}

	public Integer getAbsoluteElevation() {
		return absoluteElevation;
	}

	public void setAbsoluteElevation(Integer absoluteElevation) {
		this.absoluteElevation = absoluteElevation;
	}

	public Integer getRelativeElevation() {
		return relativeElevation;
	}

	public void setRelativeElevation(Integer relativeElevation) {
		this.relativeElevation = relativeElevation;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}