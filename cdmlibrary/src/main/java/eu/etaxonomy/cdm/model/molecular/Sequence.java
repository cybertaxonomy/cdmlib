/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.molecular;


import etaxonomy.cdm.model.reference.ReferenceBase;
import etaxonomy.cdm.model.reference.StrictReferenceBase;
import etaxonomy.cdm.model.common.IdentifiableEntity;
import etaxonomy.cdm.model.common.Media;
import etaxonomy.cdm.model.common.IReferencedEntity;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:15:16
 */
public class Sequence extends IdentifiableEntity implements IReferencedEntity {
	static Logger logger = Logger.getLogger(Sequence.class);

	//the sequence as a string of base pairs. 5'->3'
	@Description("the sequence as a string of base pairs. 5'->3'")
	private String sequence;
	//should be calculated in case sequence is set
	@Description("should be calculated in case sequence is set")
	private int length;
	//should be calculated in case sequence is set
	@Description("should be calculated in case sequence is set")
	private Calendar dateSequenced;
	//should be calculated in case sequence is set
	@Description("should be calculated in case sequence is set")
	private boolean isBarcode;
	//the sequence as a string of base pairs. 5'->3'
	@Description("the sequence as a string of base pairs. 5'->3'")
	private String citationMicroReference;
	private ReferenceBase publishedIn;
	private ArrayList citations;
	private ArrayList chromatograms;
	private ArrayList genBankAccession;
	private Locus locus;

	public Locus getLocus(){
		return locus;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setLocus(Locus newVal){
		locus = newVal;
	}

	public ReferenceBase getPublishedIn(){
		return publishedIn;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setPublishedIn(ReferenceBase newVal){
		publishedIn = newVal;
	}

	public ArrayList getChromatograms(){
		return chromatograms;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setChromatograms(ArrayList newVal){
		chromatograms = newVal;
	}

	public ArrayList getCitations(){
		return citations;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCitations(ArrayList newVal){
		citations = newVal;
	}

	public ArrayList getGenBankAccession(){
		return genBankAccession;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setGenBankAccession(ArrayList newVal){
		genBankAccession = newVal;
	}

	public String getSequence(){
		return sequence;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSequence(String newVal){
		sequence = newVal;
	}

	public int getLength(){
		return length;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setLength(int newVal){
		length = newVal;
	}

	public Calendar getDateSequenced(){
		return dateSequenced;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setDateSequenced(Calendar newVal){
		dateSequenced = newVal;
	}

	public boolean isBarcode(){
		return isBarcode;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setBarcode(boolean newVal){
		isBarcode = newVal;
	}

	public String getCitationMicroReference(){
		return citationMicroReference;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCitationMicroReference(String newVal){
		citationMicroReference = newVal;
	}

	@Transient
	public StrictReferenceBase getCitation(){
		return null;
	}

	@Transient
	public String getCitation(){
		return "";
	}

	public String generateTitle(){
		return "";
	}

}