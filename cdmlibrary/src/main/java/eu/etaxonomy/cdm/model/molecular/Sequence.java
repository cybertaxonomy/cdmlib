/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.molecular;


import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Media;
import eu.etaxonomy.cdm.model.common.IReferencedEntity;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:38
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
	private ArrayList genBankAccession;
	private Locus locus;
	private ArrayList chromatograms;

	public Locus getLocus(){
		return locus;
	}

	/**
	 * 
	 * @param locus
	 */
	public void setLocus(Locus locus){
		;
	}

	public ReferenceBase getPublishedIn(){
		return publishedIn;
	}

	/**
	 * 
	 * @param publishedIn
	 */
	public void setPublishedIn(ReferenceBase publishedIn){
		;
	}

	public ArrayList getChromatograms(){
		return chromatograms;
	}

	/**
	 * 
	 * @param chromatograms
	 */
	public void setChromatograms(ArrayList chromatograms){
		;
	}

	public ArrayList getCitations(){
		return citations;
	}

	/**
	 * 
	 * @param citations
	 */
	public void setCitations(ArrayList citations){
		;
	}

	public ArrayList getGenBankAccession(){
		return genBankAccession;
	}

	/**
	 * 
	 * @param genBankAccession
	 */
	public void setGenBankAccession(ArrayList genBankAccession){
		;
	}

	public String getSequence(){
		return sequence;
	}

	/**
	 * 
	 * @param sequence
	 */
	public void setSequence(String sequence){
		;
	}

	public int getLength(){
		return length;
	}

	/**
	 * 
	 * @param length
	 */
	public void setLength(int length){
		;
	}

	public Calendar getDateSequenced(){
		return dateSequenced;
	}

	/**
	 * 
	 * @param dateSequenced
	 */
	public void setDateSequenced(Calendar dateSequenced){
		;
	}

	public boolean isBarcode(){
		return isBarcode;
	}

	/**
	 * 
	 * @param isBarcode
	 */
	public void setBarcode(boolean isBarcode){
		;
	}

	public String getCitationMicroReference(){
		return citationMicroReference;
	}

	/**
	 * 
	 * @param citationMicroReference
	 */
	public void setCitationMicroReference(String citationMicroReference){
		;
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