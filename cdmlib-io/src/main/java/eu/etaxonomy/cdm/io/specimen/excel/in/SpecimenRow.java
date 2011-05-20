/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen.excel.in;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @created 08.04.2011
 * @version 1.0
 */
public class SpecimenRow {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SpecimenRow.class);

	private UUID uuid = null; 
	private String basisOfRecord;
	private String country;
	private String isoCountry;
	private String locality;
	private String latitude;
	private String longitude;
	private String fieldNotes;
	private String fieldNumber;
	private String accessionNumber;
	private String barcode;
	private String author;
	private String absoluteElevation;
	private String family;
	private String genus;
	private String specificEpithet;
	private String sex;
	private String ecology;
	private String plantDescription;
	private String collectionCode;
	private String collection;
	private String collectingDate;
	private String collectingDateEnd;
	private String collectorsNumber;
	private String referenceSystem;
	private String errorRadius;
	
	private TreeMap<Integer, IdentifiableSource> sources = new TreeMap<Integer, IdentifiableSource>();
	private TreeMap<Integer, String> collectors = new TreeMap<Integer, String>();
	

	
	public SpecimenRow() {

//		commonNames = new HashMap<String, List<String>>();
	}

	
// **************************** GETTER / SETTER *********************************/	
	

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}


	public UUID getUuid() {
		return uuid;
	}


	public void setBasisOfRecord(String basisOfRecord) {
		this.basisOfRecord = basisOfRecord;
	}


	public String getBasisOfRecord() {
		return basisOfRecord;
	}


	public void setCountry(String country) {
		this.country = country;
	}


	public String getCountry() {
		return country;
	}


	public void setIsoCountry(String isoCountry) {
		this.isoCountry = isoCountry;
	}


	public String getIsoCountry() {
		return isoCountry;
	}


	public void setLocality(String locality) {
		this.locality = locality;
	}


	public String getLocality() {
		return locality;
	}


	public void setFieldNotes(String fieldNotes) {
		this.fieldNotes = fieldNotes;
	}


	public String getFieldNotes() {
		return fieldNotes;
	}


	public void setAccessionNumber(String accessionNumber) {
		this.accessionNumber = accessionNumber;
	}


	public String getAccessionNumber() {
		return accessionNumber;
	}


	public void setFieldNumber(String fieldNumber) {
		this.fieldNumber = fieldNumber;
	}


	public String getFieldNumber() {
		return fieldNumber;
	}


	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	
	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}


	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}


	/**
	 * @return the absoluteElevation
	 */
	public String getAbsoluteElevation() {
		return absoluteElevation;
	}


	/**
	 * @param absoluteElevation the absoluteElevation to set
	 */
	public void setAbsoluteElevation(String absoluteElevation) {
		this.absoluteElevation = absoluteElevation;
	}


	/**
	 * @return the family
	 */
	public String getFamily() {
		return family;
	}


	/**
	 * @param family the family to set
	 */
	public void setFamily(String family) {
		this.family = family;
	}


	/**
	 * @return the genus
	 */
	public String getGenus() {
		return genus;
	}


	/**
	 * @param genus the genus to set
	 */
	public void setGenus(String genus) {
		this.genus = genus;
	}


	/**
	 * @return the specificEpithet
	 */
	public String getSpecificEpithet() {
		return specificEpithet;
	}


	/**
	 * @param specificEpithet the specificEpithet to set
	 */
	public void setSpecificEpithet(String specificEpithet) {
		this.specificEpithet = specificEpithet;
	}


	/**
	 * @return the ecology
	 */
	public String getEcology() {
		return ecology;
	}


	/**
	 * @param ecology the ecology to set
	 */
	public void setEcology(String ecology) {
		this.ecology = ecology;
	}


	/**
	 * @return the plantDescription
	 */
	public String getPlantDescription() {
		return plantDescription;
	}


	/**
	 * @param plantDescription the plantDescription to set
	 */
	public void setPlantDescription(String plantDescription) {
		this.plantDescription = plantDescription;
	}


	/**
	 * @return the collectionCode
	 */
	public String getCollectionCode() {
		return collectionCode;
	}


	/**
	 * @param collectionCode the collectionCode to set
	 */
	public void setCollectionCode(String collectionCode) {
		this.collectionCode = collectionCode;
	}


	/**
	 * @return the collectingDate
	 */
	public String getCollectingDate() {
		return collectingDate;
	}


	/**
	 * @param collectingDate the collectingDate to set
	 */
	public void setCollectingDate(String collectingDate) {
		this.collectingDate = collectingDate;
	}


	/**
	 * @return the collectorsNumber
	 */
	public String getCollectorsNumber() {
		return collectorsNumber;
	}


	/**
	 * @param collectorsNumber the collectorsNumber to set
	 */
	public void setCollectorsNumber(String collectorsNumber) {
		this.collectorsNumber = collectorsNumber;
	}


	/**
	 * @return the barcode
	 */
	public String getBarcode() {
		return barcode;
	}


	/**
	 * @return the collectingDateEnd
	 */
	public String getCollectingDateEnd() {
		return collectingDateEnd;
	}


	/**
	 * @param collectingDateEnd the collectingDateEnd to set
	 */
	public void setCollectingDateEnd(String collectingDateEnd) {
		this.collectingDateEnd = collectingDateEnd;
	}


	/**
	 * @return the latitude
	 */
	public String getLatitude() {
		return latitude;
	}


	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}


	/**
	 * @return the longitude
	 */
	public String getLongitude() {
		return longitude;
	}


	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}


	public void putIdInSource(int key, String id){
		IdentifiableSource source = getOrMakeSource(key);
		source.setIdInSource(id);
	}
	public void putSourceReference(int key, Reference<?> reference){
		IdentifiableSource source = getOrMakeSource(key);
		source.setCitation(reference);
	}

	public List<IdentifiableSource> getSources() {
		return getOrdered(sources);
	}

	public void putCollector(int key, String collector){
		this.collectors.put(key, collector);
	}

	public List<String> getCollectors() {
		return getOrdered(collectors);
	}
	
	/**
	 * @param key
	 * @return
	 */
	private IdentifiableSource getOrMakeSource(int key) {
		IdentifiableSource  source = sources.get(key);
		if (source == null){
			source = IdentifiableSource.NewInstance();
			sources.put(key, source);
		}
		return source;
	}
	
	private<T extends Object> List<T> getOrdered(TreeMap<Integer, T> tree) {
		List<T> result = new ArrayList<T>();
		for (T value : tree.values()){
			result.add(value);
		}
		return result;
	}
	
	public void setSex(String sex) {
		this.sex = sex;
	}


	public String getSex() {
		return sex;
	}


	/**
	 * @return the referenceSystem
	 */
	public String getReferenceSystem() {
		return referenceSystem;
	}


	/**
	 * @param referenceSystem the referenceSystem to set
	 */
	public void setReferenceSystem(String referenceSystem) {
		this.referenceSystem = referenceSystem;
	}


	/**
	 * @return the errorRadius
	 */
	public String getErrorRadius() {
		return errorRadius;
	}


	/**
	 * @param errorRadius the errorRadius to set
	 */
	public void setErrorRadius(String errorRadius) {
		this.errorRadius = errorRadius;
	}


	public void setCollection(String collection) {
		this.collection = collection;
	}


	public String getCollection() {
		return collection;
	}
	
	


	
}
