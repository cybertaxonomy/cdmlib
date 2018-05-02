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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.excel.common.ExcelRowBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @since 08.04.2011
 * @version 1.0
 */
public class SpecimenRow extends ExcelRowBase{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SpecimenRow.class);

	private String basisOfRecord;

	private String country;
	private String isoCountry;
	private String locality;
	private String latitude;
	private String longitude;
	private String referenceSystem;
	private String errorRadius;
	private String altitude;
	private String altitudeMax;
	private String fieldNotes;
	private String collectingDate;
	private String collectingDateEnd;
	private String collectorsNumber;
	private String primaryCollector;
	private String language;

	private String sex;

	private String accessionNumber;
	private String barcode;

	//	private String author;

//	private String family;
//	private String genus;
//	private String specificEpithet;
	private String collectionCode;
	private String collection;


	private TreeMap<Integer, IdentifiableSource> sources = new TreeMap<Integer, IdentifiableSource>();
	private TreeMap<Integer, String> collectors = new TreeMap<Integer, String>();
	private TreeMap<Integer, String> unitNotes = new TreeMap<Integer, String>();
	private TreeMap<Integer, SpecimenTypeDesignation> types = new TreeMap<Integer, SpecimenTypeDesignation>();
	private TreeMap<Integer, DeterminationLight> determinations = new TreeMap<Integer, DeterminationLight>();
	private List<PostfixTerm> levels  = new ArrayList<PostfixTerm>();



	public SpecimenRow() {
	}



	//	may be public if necessary
	protected class DeterminationLight{
		String taxonUuid;
		String family;
		String genus;
		String rank;
		String fullName;
		String speciesEpi;
		String infraSpeciesEpi;
		String author;
		String modifier;
		String determinedBy;
		String determinedWhen;
		String notes;

		public boolean hasTaxonInformation() {
			boolean result = StringUtils.isNotBlank(taxonUuid)
			 	|| StringUtils.isNotBlank(family)
			 	|| StringUtils.isNotBlank(genus)
			 	|| StringUtils.isNotBlank(rank)
			 	|| StringUtils.isNotBlank(fullName)
			 	|| StringUtils.isNotBlank(speciesEpi)
				|| StringUtils.isNotBlank(author)
				|| StringUtils.isNotBlank(infraSpeciesEpi);
			return result;
		}

	}


// **************************** GETTER / SETTER *********************************/

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


	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
//
//	/**
//	 * @return the author
//	 */
//	public String getAuthor() {
//		return author;
//	}
//
//
//	/**
//	 * @param author the author to set
//	 */
//	public void setAuthor(String author) {
//		this.author = author;
//	}


	/**
	 * @return the absoluteElevation
	 */
	public String getAltitude() {
		return altitude;
	}


	/**
	 * @param absoluteElevation the absoluteElevation to set
	 */
	public void setAltitude(String altitude) {
		this.altitude = altitude;
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


	@Override
    public void putIdInSource(int key, String id){
		IdentifiableSource source = getOrMakeSource(key, OriginalSourceType.Import);
		source.setIdInSource(id);
	}
	@Override
    public void putSourceReference(int key, Reference reference){
		IdentifiableSource source = getOrMakeSource(key, OriginalSourceType.Unknown);
		source.setCitation(reference);
	}

	@Override
    public List<IdentifiableSource> getSources() {
		return getOrdered(sources);
	}


	public void setCollectors(String value) {
		//TODO better parse somewhere else? Quick and dirty implementation
		List<String> authors = new ArrayList<String>();
		String[] splits = value.split("&");
		for (String split: splits){
			split = split.trim();
			authors.add(split);
		}
		int index = 1;
		for (int i = 0; i < authors.size() ; i++){
			String author = authors.get(i);
			if (i < authors.size()-1){
				String[] internalSplits = author.split(",");
				for (String internal : internalSplits){
					internal = internal.trim();
					this.collectors.put(index++, internal);
				}
			}else{
				this.collectors.put(index++, author);
			}
		}
	}

	public void putCollector(int key, String collector){
		this.collectors.put(key, collector);
	}

	public List<String> getCollectors() {
		return getOrdered(collectors);
	}

	public void putUnitNote(int key, String unitNote){
		this.unitNotes.put(key, unitNote);
	}

	public List<String> getUnitNotes() {
		return getOrdered(unitNotes);
	}



	/**
	 * @param key
	 * @return
	 */
	private IdentifiableSource getOrMakeSource(int key, OriginalSourceType type) {
		IdentifiableSource  source = sources.get(key);
		if (source == null){
			source = IdentifiableSource.NewInstance(type);
			sources.put(key, source);
		}
		return source;
	}


	@Override
    public void putTypeCategory(int key, SpecimenTypeDesignationStatus status){
		SpecimenTypeDesignation designation = getOrMakeTypeDesignation(key);
		designation.setTypeStatus(status);
	}
	@Override
    public void putTypifiedName(int key, TaxonName name){
		if (name != null){
			SpecimenTypeDesignation designation = getOrMakeTypeDesignation(key);
			name.addTypeDesignation(designation, false);
		}
	}

	@Override
    public List<SpecimenTypeDesignation> getTypeDesignations() {
		return getOrdered(types);
	}


	private SpecimenTypeDesignation getOrMakeTypeDesignation(int key) {
		SpecimenTypeDesignation designation = types.get(key);
		if (designation == null){
			designation = SpecimenTypeDesignation.NewInstance();
			types.put(key, designation);
		}
		return designation;
	}

	public void putDeterminationFamily(int key, String family){
		DeterminationLight determinationEvent = getOrMakeDetermination(key);
		determinationEvent.family = family;
	}

	public void putDeterminationFullName(int key, String fullName){
		DeterminationLight determinationEvent = getOrMakeDetermination(key);
		determinationEvent.fullName = fullName;
	}

	public void putDeterminationTaxonUuid(int key, String taxonUuid){
		DeterminationLight determinationEvent = getOrMakeDetermination(key);
		determinationEvent.taxonUuid = taxonUuid;
	}

	public void putDeterminationRank(int key, String rank){
		DeterminationLight determinationEvent = getOrMakeDetermination(key);
		determinationEvent.rank = rank;
	}

	public void putDeterminationGenus(int key, String genus){
		DeterminationLight determinationEvent = getOrMakeDetermination(key);
		determinationEvent.genus = genus;
	}

	public void putDeterminationSpeciesEpi(int key, String speciesEpi){
		DeterminationLight determinationEvent = getOrMakeDetermination(key);
		determinationEvent.speciesEpi = speciesEpi;
	}

	public void putDeterminationInfraSpeciesEpi(int key, String infraSpeciesEpi){
		DeterminationLight determinationEvent = getOrMakeDetermination(key);
		determinationEvent.infraSpeciesEpi = infraSpeciesEpi;
	}

	public void putDeterminationAuthor(int key, String author){
		DeterminationLight determinationEvent = getOrMakeDetermination(key);
		determinationEvent.author = author;
	}

	public void putDeterminationDeterminedBy(int key, String determinedBy){
		DeterminationLight determinationEvent = getOrMakeDetermination(key);
		determinationEvent.determinedBy = determinedBy;
	}

	public void putDeterminationDeterminedWhen(int key, String determinedWhen){
		DeterminationLight determinationEvent = getOrMakeDetermination(key);
		determinationEvent.determinedWhen = determinedWhen;
	}

	public void putDeterminationDeterminationNotes(int key, String notes){
		DeterminationLight determinationEvent = getOrMakeDetermination(key);
		determinationEvent.notes = notes;
	}

	public void putDeterminationDeterminationModifier(int key, String modifier){
		DeterminationLight determinationEvent = getOrMakeDetermination(key);
		determinationEvent.modifier = modifier;
	}


	public List<DeterminationLight> getDetermination() {
		List<DeterminationLight> result = getOrdered(determinations);
		if (determinations.size() > 1 && getCommonDetermination()!= null ){
			result.remove(getCommonDetermination());
		}
		return result;
	}

	/**
	 * Returns the determination with key "0".
	 * @return
	 */
	public DeterminationLight getCommonDetermination() {
		if (determinations.get(0) != null){
			return determinations.get(0);
		}
		return null;
	}



	private DeterminationLight getOrMakeDetermination(int key) {
		DeterminationLight determination = this.determinations.get(key);
		if (determination == null){
			determination = new DeterminationLight();
			this.determinations.put(key, determination);
		}
		return determination;
	}


	private<T extends Object> List<T> getOrdered(TreeMap<?, T> tree) {
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


	public void addLeveledArea(String levelPostfix, String value) {
		PostfixTerm area = new PostfixTerm();
		area.term = value;
		area.postfix = levelPostfix;
		this.levels.add(area);
	}

	public List<PostfixTerm> getLeveledAreas(){
		return levels;
	}

	public void setAltitudeMax(String altitudeMax) {
		this.altitudeMax = altitudeMax;
	}


	public String getAltitudeMax() {
		return altitudeMax;
	}


	/**
	 * @param primaryCollector the primaryCollector to set
	 */
	public void setPrimaryCollector(String primaryCollector) {
		this.primaryCollector = primaryCollector;
	}


	/**
	 * @return the primaryCollector
	 */
	public String getPrimaryCollector() {
		return primaryCollector;
	}


	public String getLanguage() {
		return language;
	}


	public void setLanguage(String language) {
		this.language = language;
	}

}
