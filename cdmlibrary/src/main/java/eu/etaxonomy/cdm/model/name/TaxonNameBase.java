/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IReferencedEntity;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * The upmost (abstract) class for scientific taxon names regardless of the any
 * particular nomenclatural code. The scientific name including author strings and
 * maybe year is stored in IdentifiableEntity.titleCache
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:57
 */
@Entity
public abstract class TaxonNameBase extends IdentifiableEntity implements IReferencedEntity {
	static Logger logger = Logger.getLogger(TaxonNameBase.class);
	//The scientific name without author strings and year
	private String name;
	//Non-atomised addition to a name not ruled by a nomenclatural code
	private String appendedPhrase;
	//Details of the nomenclatural reference (protologue). These are mostly (implicitly) pages but can also be figures or
	//tables or any other element of a publication. {only if a nomenclatural reference exists}
	private String nomenclaturalMicroReference;
	//this flag will be set to true if the parseName method was unable to successfully parse the name
	private boolean hasProblem = false;
	private ArrayList typeDesignations;
	private ArrayList nameInSource;
	private ArrayList nameRelations;
	private ArrayList status;
	private Rank rank;
	//if set, the Reference.isNomenclaturallyRelevant flag should be set to true!
	private INomenclaturalReference nomenclaturalReference;
	private ArrayList newCombinations;
	private TaxonNameBase basionym;
	private ArrayList inverseNameRelations;

	public TaxonNameBase(Rank rank) {
		this.setRank(rank);
	}

	public Rank getRank(){
		return this.rank;
	}

	/**
	 * 
	 * @param rank    rank
	 */
	public void setRank(Rank rank){
		this.rank = rank;
	}

	public ArrayList getNameRelations(){
		return this.nameRelations;
	}

	/**
	 * 
	 * @param nameRelations    nameRelations
	 */
	public void setNameRelations(ArrayList nameRelations){
		this.nameRelations = nameRelations;
	}

	public ArrayList getInverseNameRelations(){
		return this.inverseNameRelations;
	}

	/**
	 * 
	 * @param inverseNameRelations    inverseNameRelations
	 */
	public void setInverseNameRelations(ArrayList inverseNameRelations){
		this.inverseNameRelations = inverseNameRelations;
	}

	public ArrayList getTypeDesignations(){
		return this.typeDesignations;
	}

	/**
	 * 
	 * @param typeDesignations    typeDesignations
	 */
	public void setTypeDesignations(ArrayList typeDesignations){
		this.typeDesignations = typeDesignations;
	}

	public ArrayList getStatus(){
		return this.status;
	}

	/**
	 * 
	 * @param status    status
	 */
	public void setStatus(ArrayList status){
		this.status = status;
	}

	public INomenclaturalReference getNomenclaturalReference(){
		return this.nomenclaturalReference;
	}

	/**
	 * 
	 * @param nomenclaturalReference    nomenclaturalReference
	 */
	public void setNomenclaturalReference(INomenclaturalReference nomenclaturalReference){
		this.nomenclaturalReference = nomenclaturalReference;
	}

	public ArrayList getNameInSource(){
		return this.nameInSource;
	}

	/**
	 * 
	 * @param nameInSource    nameInSource
	 */
	public void setNameInSource(ArrayList nameInSource){
		this.nameInSource = nameInSource;
	}

	public ArrayList getNewCombinations(){
		return this.newCombinations;
	}

	/**
	 * 
	 * @param newCombinations    newCombinations
	 */
	public void setNewCombinations(ArrayList newCombinations){
		this.newCombinations = newCombinations;
	}

	public TaxonNameBase getBasionym(){
		return this.basionym;
	}

	/**
	 * 
	 * @param basionym    basionym
	 */
	public void setBasionym(TaxonNameBase basionym){
		this.basionym = basionym;
	}

	public String getName(){
		return this.name;
	}

	/**
	 * 
	 * @param name    name
	 */
	public void setName(String name){
		this.name = name;
	}

	public String getAppendedPhrase(){
		return this.appendedPhrase;
	}

	/**
	 * 
	 * @param appendedPhrase    appendedPhrase
	 */
	public void setAppendedPhrase(String appendedPhrase){
		this.appendedPhrase = appendedPhrase;
	}

	public String getNomenclaturalMicroReference(){
		return this.nomenclaturalMicroReference;
	}

	/**
	 * 
	 * @param nomenclaturalMicroReference    nomenclaturalMicroReference
	 */
	public void setNomenclaturalMicroReference(String nomenclaturalMicroReference){
		this.nomenclaturalMicroReference = nomenclaturalMicroReference;
	}

	public boolean getHasProblem(){
		return this.hasProblem;
	}

	/**
	 * 
	 * @param hasProblem    hasProblem
	 */
	public void setHasProblem(boolean hasProblem){
		this.hasProblem = hasProblem;
	}

	@Transient
	public StrictReferenceBase getCitation(){
		return null;
	}

	@Transient
	public String getCitationString(){
		return null;
	}

	@Transient
	public String[] getProblems(){
		return null;
	}

	/**
	 * returns year of according nomenclatural reference, null if nomenclatural
	 * reference does not exist
	 */
	@Transient
	public String getYear(){
		return "";
	}

	/**
	 * 
	 * @param fullname    fullname
	 */
	public boolean parseName(String fullname){
		return false;
	}

}