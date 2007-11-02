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
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:05
 */
@Entity
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
	 * @param sequences
	 */
	public void setSequences(ArrayList sequences){
		;
	}

	public Collection getStoredAt(){
		return storedAt;
	}

	/**
	 * 
	 * @param storedAt
	 */
	public void setStoredAt(Collection storedAt){
		;
	}

	public TissueSample getExtractedFrom(){
		return extractedFrom;
	}

	/**
	 * 
	 * @param extractedFrom
	 */
	public void setExtractedFrom(TissueSample extractedFrom){
		;
	}

	public String getBankNumber(){
		return bankNumber;
	}

	/**
	 * 
	 * @param bankNumber
	 */
	public void setBankNumber(String bankNumber){
		;
	}

	public String getProductionNotes(){
		return productionNotes;
	}

	/**
	 * 
	 * @param productionNotes
	 */
	public void setProductionNotes(String productionNotes){
		;
	}

	public Calendar getDateProduced(){
		return dateProduced;
	}

	/**
	 * 
	 * @param dateProduced
	 */
	public void setDateProduced(Calendar dateProduced){
		;
	}

	public String generateTitle(){
		return "";
	}

}