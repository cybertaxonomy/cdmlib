/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.publication.NomenclaturalReference;
import eu.etaxonomy.cdm.model.common.Source;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.strategy.INameCacheStrategy;

import org.apache.log4j.Logger;
import java.util.*;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;



/**
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:36:15
 */
@XmlRootElement  //JAXP test dummy
@Entity
public class TaxonName extends IdentifiableEntity {
	static Logger logger = Logger.getLogger(TaxonName.class);

	private INameCacheStrategy cacheStrategy;
	private String year;
	
	//TODO make private but then the Aspect class has problems !!
	public TaxonName(){
		
	}
	
	public TaxonName(INameCacheStrategy cacheStrategy){
		setCacheStrategy(cacheStrategy);
	}
	
	//TODO inserted for PROTOTYPE
	public String getYear(){
		return this.year;
	}
	public void setYear(String strYear){
		this.year = strYear;
	}
	
	//TODO for PROTOTYPE
	@Transient
	public INameCacheStrategy getCacheStrategy() {
		return cacheStrategy;
	}
	public void setCacheStrategy(INameCacheStrategy cacheStrategy) {
		this.cacheStrategy = cacheStrategy;
	}
	
	
	//The complete author string and maybe year
	private String authorship;
	private String cultivarName;
	//The full name including author strings and maybe year
	private String fullName;
	//Genus part of a name
	private String genus;
	private boolean hasProblem;
	private String idInSource;
	//Genus subdivision epithet
	private String infragenericEpithet;
	private String infraSpecificEpithet;
	private boolean isAnamorphic;
	//alternative naming: useFullName
	//when not set this flag protects the detailed parsed attributes from being overwritten
	private boolean isAtomised;
	private boolean isBinomHybrid;
	private boolean isCultivarGroup;
	//if set: this name is a hybrid formula (a hybrid that does not have an own name) and no other hybrid flags may be set. A
	//hybrid name  may not have either an authorteam nor other name components. 
	private boolean isHybridFormula;
	private boolean isMonomHybrid;
	private boolean isTrinomHybrid;
	//The name without author strings and year
	private String name;
	private String nameInSource;
	private String nomenclaturalMicroReference;
	//The species epithet
	private String specificEpithet;
	//Name of taxon when rank is above genus
	private String uninomial;
	//Non-atomised addition to a name
	private String unnamedNamePhrase;
	private Rank rank;
	private ArrayList inverseNameRelation;
	private NomenclaturalCode nomenclaturalCode;
	private ArrayList facts;
	private ArrayList nameRelation;
	private ArrayList typeDesignations;
	private NomenclaturalReference nomenclaturalReference;
	private Source source;
	private Team authorTeam;
	private Team exAuthorTeam;

	public String getAuthorship(){
		return authorship;
	}

	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}  )
	public Team getAuthorTeam(){
		return authorTeam;
	}

	@XmlElement
	public String getCultivarName(){
		return cultivarName;
	}

	@ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE} )
	public Team getExAuthorTeam(){
		return exAuthorTeam;
	}

	public ArrayList getFacts(){
		return facts;
	}

	public String getFullName(){
		if (name == null){ 
			return cacheStrategy.getFullNameCache(this);
		}else{
			return fullName;
		}
	}

	public String getGenus(){
		return genus;
	}

	public boolean getHasProblem(){
		return hasProblem;
	}

	public String getIdInSource(){
		return idInSource;
	}

	public String getInfragenericEpithet(){
		return infragenericEpithet;
	}

	public String getInfraSpecificEpithet(){
		return infraSpecificEpithet;
	}

	public ArrayList getInverseNameRelation(){
		return inverseNameRelation;
	}

	public String getName(){
		if (name == null){ 
			return cacheStrategy.getNameCache(this);
		}else{
			return name;
		}
	}

	public String getNameInSource(){
		return nameInSource;
	}

	public ArrayList getNameRelation(){
		return nameRelation;
	}

	public NomenclaturalCode getNomenclaturalCode(){
		return nomenclaturalCode;
	}

	public String getNomenclaturalMicroReference(){
		return nomenclaturalMicroReference;
	}

	@Transient
	public NomenclaturalReference getNomenclaturalReference(){
		return nomenclaturalReference;
	}

	@Transient
	public String[] getProblems(){
		return null;
	}

	public Rank getRank(){
		return rank;
	}

	@Transient
	public Source getSource(){
		return source;
	}

	public String getSpecificEpithet(){
		return specificEpithet;
	}

	public ArrayList getTypeDesignations(){
		return typeDesignations;
	}

	public String getUninomial(){
		return uninomial;
	}

	public String getUnnamedNamePhrase(){
		return unnamedNamePhrase;
	}
	

	public boolean isAnamorphic(){
		return isAnamorphic;
	}

	public boolean isAtomised(){
		return isAtomised;
	}

	public boolean isBinomHybrid(){
		return isBinomHybrid;
	}

	public boolean isCultivarGroup(){
		return isCultivarGroup;
	}

	public boolean isHybridFormula(){
		return isHybridFormula;
	}

	public boolean isMonomHybrid(){
		return isMonomHybrid;
	}

	public boolean isTrinomHybrid(){
		return isTrinomHybrid;
	}

	/**
	 * 
	 * @param fullname
	 */
	public boolean parseName(String fullname){
		return false;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setAnamorphic(boolean newVal){
		super.changeSupport.firePropertyChange("isAnamorphic", isAnamorphic, newVal);
		isAnamorphic = newVal;
		
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setAtomised(boolean newVal){
		super.changeSupport.firePropertyChange("isAtomised", isAtomised, newVal);
		isAtomised = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setAuthorship(String newVal){
		super.changeSupport.firePropertyChange("authorship", authorship, newVal);
		authorship = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setAuthorTeam(Team newVal){
		super.changeSupport.firePropertyChange("authorTeam", authorTeam, newVal);
		authorTeam = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setBinomHybrid(boolean newVal){
		super.changeSupport.firePropertyChange("isBinomHybrid", isBinomHybrid, newVal);
		isBinomHybrid = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCultivarGroup(boolean newVal){
		super.changeSupport.firePropertyChange("isCultivarGroup", isCultivarGroup, newVal);
		isCultivarGroup = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCultivarName(String newVal){
		super.changeSupport.firePropertyChange("cultivarName", cultivarName, newVal);
		cultivarName = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setExAuthorTeam(Team newVal){
		super.changeSupport.firePropertyChange("exAuthorTeam", exAuthorTeam, newVal);
		exAuthorTeam = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setFacts(ArrayList newVal){
		facts = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setFullName(String newVal){
		super.changeSupport.firePropertyChange("fullName", fullName, newVal);
		fullName = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setGenus(String newVal){
		super.changeSupport.firePropertyChange("genus", genus, newVal);
		genus = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setHasProblem(boolean newVal){
		super.changeSupport.firePropertyChange("hasProblem", hasProblem, newVal);
		hasProblem = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setHybridFormula(boolean newVal){
		super.changeSupport.firePropertyChange("isHybridFormula", isHybridFormula, newVal);
		isHybridFormula = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setIdInSource(String newVal){
		super.changeSupport.firePropertyChange("idInSource", idInSource, newVal);
		idInSource = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setInfragenericEpithet(String newVal){
		super.changeSupport.firePropertyChange("infragenericEpithet", infragenericEpithet, newVal);
		infragenericEpithet = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setInfraSpecificEpithet(String newVal){
		super.changeSupport.firePropertyChange("infraSpecificEpithet", infraSpecificEpithet, newVal);
		infraSpecificEpithet = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setInverseNameRelation(ArrayList newVal){
		super.changeSupport.firePropertyChange("inverseNameRelation", inverseNameRelation, newVal);
		inverseNameRelation = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setMonomHybrid(boolean newVal){
		super.changeSupport.firePropertyChange("isMonomHybrid", isMonomHybrid, newVal);
		isMonomHybrid = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setName(String newVal){
		super.changeSupport.firePropertyChange("name", name, newVal);
		name = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setNameInSource(String newVal){
		super.changeSupport.firePropertyChange("nameInSource", nameInSource, newVal);
		nameInSource = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setNameRelation(ArrayList newVal){
		super.changeSupport.firePropertyChange("nameRelation", nameRelation, newVal);
		nameRelation = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setNomenclaturalCode(NomenclaturalCode newVal){
		super.changeSupport.firePropertyChange("nomenclaturalCode", nomenclaturalCode, newVal);
		nomenclaturalCode = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setNomenclaturalMicroReference(String newVal){
		super.changeSupport.firePropertyChange("nomenclaturalMicroReference", nomenclaturalMicroReference, newVal);
		nomenclaturalMicroReference = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setNomenclaturalReference(NomenclaturalReference newVal){
		super.changeSupport.firePropertyChange("nomenclaturalReference", nomenclaturalReference, newVal);
		nomenclaturalReference = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setRank(Rank newVal){
		super.changeSupport.firePropertyChange("rank", rank, newVal);
		rank = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSource(Source newVal){
		super.changeSupport.firePropertyChange("source", source, newVal);
		source = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSpecificEpithet(String newVal){
		super.changeSupport.firePropertyChange("specificEpithet", specificEpithet, newVal);
		specificEpithet = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setTrinomHybrid(boolean newVal){
		super.changeSupport.firePropertyChange("isTrinomHybrid", isTrinomHybrid, newVal);
		isTrinomHybrid = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setTypeDesignations(ArrayList newVal){
		//super.changeSupport.firePropertyChange("typeDesignations", typeDesignations, newVal);
		typeDesignations = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setUninomial(String newVal){
		super.changeSupport.firePropertyChange("uninomial", uninomial, newVal);
		uninomial = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setUnnamedNamePhrase(String newVal){
		super.changeSupport.firePropertyChange("unnamedNamePhrase", unnamedNamePhrase, newVal);
		unnamedNamePhrase = newVal;
	}
	
}