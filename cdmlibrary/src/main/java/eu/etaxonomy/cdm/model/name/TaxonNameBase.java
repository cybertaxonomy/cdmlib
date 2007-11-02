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

/**
 * The upmost (abstract) class for scientific taxon names regardless of the any
 * particular nomenclatural code. The scientific name including author strings and
 * maybe year is stored in IdentifiableEntity.titleCache
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:43
 */
public abstract class TaxonNameBase extends IdentifiableEntity implements IReferencedEntity {
	static Logger logger = Logger.getLogger(TaxonNameBase.class);

	//The scientific name without author strings and year
	@Description("The scientific name without author strings and year")
	private String name;
	//Non-atomised addition to a name not ruled by a nomenclatural code
	@Description("Non-atomised addition to a name not ruled by a nomenclatural code")
	private String appendedPhrase;
	//Details of the nomenclatural reference (protologue). These are mostly (implicitly) pages but can also be figures or
	//tables or any other element of a publication.
	//{only if a nomenclatural reference exists}
	@Description("Details of the nomenclatural reference (protologue). These are mostly (implicitly) pages but can also be figures or tables or any other element of a publication.
	{only if a nomenclatural reference exists}")
	private String nomenclaturalMicroReference;
	//this flag will be set to true if the parseName method was unable to successfully parse the name
	@Description("this flag will be set to true if the parseName method was unable to successfully parse the name")
	private boolean hasProblem = False;
	private ArrayList typeDesignations;
	private ArrayList nameInSource;
	private ArrayList nameRelations;
	private ArrayList status;
	private Rank rank;
	/**
	 * if set, the Reference.isNomenclaturallyRelevant flag should be set to true!
	 */
	private INomenclaturalReference nomenclaturalReference;
	private ArrayList newCombinations;
	private TaxonNameBase basionym;
	private ArrayList inverseNameRelations;

	public Rank getRank(){
		return rank;
	}

	/**
	 * 
	 * @param rank
	 */
	public void setRank(Rank rank){
		;
	}

	public ArrayList getNameRelations(){
		return nameRelations;
	}

	/**
	 * 
	 * @param nameRelations
	 */
	public void setNameRelations(ArrayList nameRelations){
		;
	}

	public ArrayList getInverseNameRelations(){
		return inverseNameRelations;
	}

	/**
	 * 
	 * @param inverseNameRelations
	 */
	public void setInverseNameRelations(ArrayList inverseNameRelations){
		;
	}

	public ArrayList getTypeDesignations(){
		return typeDesignations;
	}

	/**
	 * 
	 * @param typeDesignations
	 */
	public void setTypeDesignations(ArrayList typeDesignations){
		;
	}

	public ArrayList getStatus(){
		return status;
	}

	/**
	 * 
	 * @param status
	 */
	public void setStatus(ArrayList status){
		;
	}

	public INomenclaturalReference getNomenclaturalReference(){
		return nomenclaturalReference;
	}

	/**
	 * 
	 * @param nomenclaturalReference
	 */
	public void setNomenclaturalReference(INomenclaturalReference nomenclaturalReference){
		;
	}

	public ArrayList getNameInSource(){
		return nameInSource;
	}

	/**
	 * 
	 * @param nameInSource
	 */
	public void setNameInSource(ArrayList nameInSource){
		;
	}

	public ArrayList getNewCombinations(){
		return newCombinations;
	}

	/**
	 * 
	 * @param newCombinations
	 */
	public void setNewCombinations(ArrayList newCombinations){
		;
	}

	public TaxonNameBase getBasionym(){
		return basionym;
	}

	/**
	 * 
	 * @param basionym
	 */
	public void setBasionym(TaxonNameBase basionym){
		;
	}

	public String getName(){
		return name;
	}

	/**
	 * 
	 * @param name
	 */
	public void setName(String name){
		;
	}

	public String getAppendedPhrase(){
		return appendedPhrase;
	}

	/**
	 * 
	 * @param appendedPhrase
	 */
	public void setAppendedPhrase(String appendedPhrase){
		;
	}

	public String getNomenclaturalMicroReference(){
		return nomenclaturalMicroReference;
	}

	/**
	 * 
	 * @param nomenclaturalMicroReference
	 */
	public void setNomenclaturalMicroReference(String nomenclaturalMicroReference){
		;
	}

	public boolean getHasProblem(){
		return hasProblem;
	}

	/**
	 * 
	 * @param hasProblem
	 */
	public void setHasProblem(boolean hasProblem){
		;
	}

	@Transient
	public StrictReferenceBase getCitation(){
		return null;
	}

	@Transient
	public String getCitationString(){
		return "";
	}

	@Transient
	public String[] getProblems(){
		return "";
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
	 * @param fullname
	 */
	public boolean parseName(String fullname){
		return false;
	}

}