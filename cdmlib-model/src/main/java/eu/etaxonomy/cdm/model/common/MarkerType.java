/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * Marker types similar to dynamically defined attributes. These  content types
 * like "IS_DOUBTFUL", "COMPLETE"  or specific local flags.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:33
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MarkerType")
@Entity
@Audited
public class MarkerType extends DefinedTermBase<MarkerType> {
	private static final long serialVersionUID = -9117424749919907396L;
	public static final Logger logger = Logger.getLogger(MarkerType.class);

	private static final UUID uuidImported = UUID.fromString("96878790-4ceb-42a2-9738-a2242079b679");
	private static final UUID uuidToBeChecked = UUID.fromString("34204192-b41d-4857-a1d4-28992bef2a2a");
	private static final UUID uuidIsDoubtful = UUID.fromString("b51325c8-05fe-421a-832b-d86fc249ef6e");
	private static final UUID uuidComplete = UUID.fromString("b4b1b2ab-89a8-4ce6-8110-d60b8b1bc433");
	private static final UUID uuidPublish = UUID.fromString("0522c2b3-b21c-400c-80fc-a251c3501dbc");
	private static MarkerType IMPORTED;
	private static MarkerType TO_BE_CHECKED;
	private static MarkerType IS_DOUBTFUL;
	private static MarkerType COMPLETE;
	private static MarkerType PUBLISH;

	protected static Map<UUID, MarkerType> termMap = null;		

	public static MarkerType NewInstance(String term, String label, String labelAbbrev){
		return new MarkerType(term, label, labelAbbrev);
	}
	
	protected static MarkerType getTermByUuid(UUID uuid){
		if (termMap == null){
			DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
			vocabularyStore.initialize();
		}
		return (MarkerType)termMap.get(uuid);
	}

	
	/**
	 * Constructor
	 * @param term
	 * @param label
	 */
	public MarkerType() {
		super();
	}
	
	/**
	 * Constructor
	 * @param term
	 * @param label
	 */
	protected MarkerType(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}


	public static final MarkerType IMPORTED(){
		return getTermByUuid(uuidImported);
	}

	public static final MarkerType TO_BE_CHECKED(){
		return getTermByUuid(uuidToBeChecked);
	}

	public static final MarkerType IS_DOUBTFUL(){
		return getTermByUuid(uuidIsDoubtful);
	}

	public static final MarkerType COMPLETE(){
		return getTermByUuid(uuidComplete);
	}

	public static final MarkerType PUBLISH(){
		return getTermByUuid(uuidPublish);
	}
	
	@Override
	protected void setDefaultTerms(TermVocabulary<MarkerType> termVocabulary) {
		termMap = new HashMap<UUID, MarkerType>();
		for (MarkerType term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), (MarkerType)term);
		}
	}

}