/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * Reference systems for coordinates also according to OGC (Open Geographical
 * Consortium) The list should be extensible at runtime through configuration.
 * This needs to be investigated.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:49
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReferenceSystem")
@XmlRootElement(name = "ReferenceSystem")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class ReferenceSystem extends DefinedTermBase<ReferenceSystem> {
	private static final long serialVersionUID = 2704455299046749175L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ReferenceSystem.class);

	protected static Map<UUID, ReferenceSystem> termMap = null;		

	private static final UUID uuidWGS84 = UUID.fromString("63f4dd55-00fa-49e7-96fd-2b7059a1c1ee");
	
	/**
	 * Factory method
	 * @return
	 */
	public static ReferenceSystem NewInstance(){
		return new ReferenceSystem();
	}

	/**
	 * Factory method
	 * @return
	 */
	public static ReferenceSystem NewInstance(String term, String label, String labelAbbrev){
		return new ReferenceSystem(term, label, labelAbbrev);
	}
	
	/**
	 * Constructor
	 */
	public ReferenceSystem() {
	}
	
	/**
	 * Constructor
	 */
	private ReferenceSystem(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}


	protected static ReferenceSystem getTermByUuid(UUID uuid){
		if (termMap == null){
			DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
			vocabularyStore.initialize();
		}
		return (ReferenceSystem)termMap.get(uuid);
	}
	
	public static final ReferenceSystem WGS84(){
		return getTermByUuid(uuidWGS84);
	}

	@Override
	protected void setDefaultTerms(TermVocabulary<ReferenceSystem> termVocabulary){
		termMap = new HashMap<UUID, ReferenceSystem>();
		for (ReferenceSystem term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term); 
		}
	}

}