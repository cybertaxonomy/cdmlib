/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.name;


import etaxonomy.cdm.model.reference.INomenclaturalReference;
import etaxonomy.cdm.model.reference.StrictReferenceBase;
import etaxonomy.cdm.model.common.IReferencedEntity;
import etaxonomy.cdm.model.common.IdentifiableEntity;
import org.apache.log4j.Logger;

/**
 * The upmost (abstract) class for scientific taxon names regardless of the any
 * particular nomenclatural code. The scientific name including author strings and
 * maybe year is stored in IdentifiableEntity.titleCache
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:15:22
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
	private Rank rank;
	private ArrayList newCombinations;
	private TaxonNameBase basionym;
	/**
	 * if set, the Reference.isNomenclaturallyRelevant flag should be set to true!
	 */
	private INomenclaturalReference nomenclaturalReference;
	private ArrayList typeDesignations;
	private ArrayList nameInSource;
	private ArrayList nameRelations;
	private ArrayList inverseNameRelations;
	private ArrayList status;

	public Rank getRank(){
		return rank;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setRank(Rank newVal){
		rank = newVal;
	}

	public ArrayList getNameRelations(){
		return nameRelations;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setNameRelations(ArrayList newVal){
		nameRelations = newVal;
	}

	public ArrayList getInverseNameRelations(){
		return inverseNameRelations;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setInverseNameRelations(ArrayList newVal){
		inverseNameRelations = newVal;
	}

	public ArrayList getTypeDesignations(){
		return typeDesignations;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setTypeDesignations(ArrayList newVal){
		typeDesignations = newVal;
	}

	public ArrayList getStatus(){
		return status;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setStatus(ArrayList newVal){
		status = newVal;
	}

	public INomenclaturalReference getNomenclaturalReference(){
		return nomenclaturalReference;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setNomenclaturalReference(INomenclaturalReference newVal){
		nomenclaturalReference = newVal;
	}

	public ArrayList getNameInSource(){
		return nameInSource;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setNameInSource(ArrayList newVal){
		nameInSource = newVal;
	}

	public ArrayList getNewCombinations(){
		return newCombinations;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setNewCombinations(ArrayList newVal){
		newCombinations = newVal;
	}

	public TaxonNameBase getBasionym(){
		return basionym;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setBasionym(TaxonNameBase newVal){
		basionym = newVal;
	}

	public String getName(){
		return name;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setName(String newVal){
		name = newVal;
	}

	public String getAppendedPhrase(){
		return appendedPhrase;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setAppendedPhrase(String newVal){
		appendedPhrase = newVal;
	}

	public String getNomenclaturalMicroReference(){
		return nomenclaturalMicroReference;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setNomenclaturalMicroReference(String newVal){
		nomenclaturalMicroReference = newVal;
	}

	public boolean getHasProblem(){
		return hasProblem;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setHasProblem(boolean newVal){
		hasProblem = newVal;
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