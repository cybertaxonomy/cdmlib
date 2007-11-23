/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageSet;
import eu.etaxonomy.cdm.model.occurrence.ObservationalUnit;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * {type is "host" or "hybrid_parent"}
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:28
 */
@Entity
public class IndividualsAssociation extends FeatureBase {
	static Logger logger = Logger.getLogger(IndividualsAssociation.class);
	private MultilanguageSet description;
	private ObservationalUnit observationalUnit2;

	@ManyToOne
	public ObservationalUnit getObservationalUnit2(){
		return this.observationalUnit2;
	}
	public void setObservationalUnit2(ObservationalUnit observationalUnit2){
		this.observationalUnit2 = observationalUnit2;
	}

	
	public MultilanguageSet getDescription(){
		return this.description;
	}
	private void setDescription(MultilanguageSet description){
		this.description = description;
	}
	public void addDescription(LanguageString description){
		this.description.add(description);
	}
	public void addDescription(String text, Language lang){
		this.description.add(text, lang);
	}
	public void removeDescription(Language lang){
		this.description.remove(lang);
	}

}