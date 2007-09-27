/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import eu.etaxonomy.cdm.model.publication.PublicationBase;
import eu.etaxonomy.cdm.model.common.FactBase;
import eu.etaxonomy.cdm.model.common.Media;
import eu.etaxonomy.cdm.model.common.Fact;
import org.apache.log4j.Logger;
import java.util.*;
import javax.persistence.*;

/**
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:36:14
 */
@Entity
public class TaxonFact extends FactBase implements Fact {
	static Logger logger = Logger.getLogger(TaxonFact.class);

	private TaxonFactType type;
	private PublicationBase citation;
	private ArrayList medias;

	public PublicationBase getCitation(){
		return citation;
	}

	public ArrayList getMedias(){
		return medias;
	}

	public TaxonFactType getType(){
		return type;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCitation(PublicationBase newVal){
		citation = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setMedias(ArrayList newVal){
		medias = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setType(TaxonFactType newVal){
		type = newVal;
	}

}