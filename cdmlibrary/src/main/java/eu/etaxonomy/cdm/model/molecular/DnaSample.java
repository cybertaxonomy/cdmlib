/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.molecular;


import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:43:16
 */
public class DnaSample extends IdentifiableEntity {
	static Logger logger = Logger.getLogger(DnaSample.class);

	@Description("")
	private String bankNumber;
	//Notes on extraction, purification or amplification process
	@Description("Notes on extraction, purification or amplification process")
	private String productionNotes;
	@Description("")
	private Calendar dateProduced;
	private ArrayList sequences;
	private TissueSample extractedFrom;
	private Collection storedAt;

	public ArrayList getSequences(){
		return sequences;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSequences(ArrayList newVal){
		sequences = newVal;
	}

	public Collection getStoredAt(){
		return storedAt;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setStoredAt(Collection newVal){
		storedAt = newVal;
	}

	public TissueSample getExtractedFrom(){
		return extractedFrom;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setExtractedFrom(TissueSample newVal){
		extractedFrom = newVal;
	}

	public String getBankNumber(){
		return bankNumber;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setBankNumber(String newVal){
		bankNumber = newVal;
	}

	public String getProductionNotes(){
		return productionNotes;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setProductionNotes(String newVal){
		productionNotes = newVal;
	}

	public Calendar getDateProduced(){
		return dateProduced;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setDateProduced(Calendar newVal){
		dateProduced = newVal;
	}

	public String generateTitle(){
		return "";
	}

}