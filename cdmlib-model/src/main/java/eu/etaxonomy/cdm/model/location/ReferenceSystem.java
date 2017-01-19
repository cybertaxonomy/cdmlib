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

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermType;
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
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class ReferenceSystem extends DefinedTermBase<ReferenceSystem> {
	private static final long serialVersionUID = -9060720949197749047L;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ReferenceSystem.class);

	protected static Map<UUID, ReferenceSystem> termMap = null;

	private static final UUID uuidWGS84 = UUID.fromString("63f4dd55-00fa-49e7-96fd-2b7059a1c1ee");
	private static final UUID uuidGoogleEarth = UUID.fromString("1bb67042-2814-4b09-9e76-c8c1e68aa281");
	private static final UUID uuidGazetteer = UUID.fromString("e35f1d1c-9347-4190-bd47-a3b00632fcf3");
	private static final UUID uuidMap = UUID.fromString("6d72d148-458a-42eb-97b0-9824abcffc91");

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

//********************************** Constructor *******************************************************************/

  	//for hibernate use only
  	@Deprecated
  	protected ReferenceSystem() {
		super(TermType.ReferenceSystem);
	}

	/**
	 * Constructor
	 */
	private ReferenceSystem(String term, String label, String labelAbbrev) {
		super(TermType.ReferenceSystem, term, label, labelAbbrev);
	}

// ************************************* MTEHODS ***************************************************/

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#resetTerms()
	 */
	@Override
	public void resetTerms(){
		termMap = null;
	}



	protected static ReferenceSystem getTermByUuid(UUID uuid){
        if (termMap == null || termMap.isEmpty()){
            return getTermByClassAndUUID(ReferenceSystem.class, uuid);
        } else {
            return termMap.get(uuid);
        }
	}

	public static final ReferenceSystem WGS84(){
		return getTermByUuid(uuidWGS84);
	}

	public static final ReferenceSystem GOOGLE_EARTH(){
		return getTermByUuid(uuidGoogleEarth);
	}

	public static final ReferenceSystem GAZETTEER(){
		return getTermByUuid(uuidGazetteer);
	}

	public static final ReferenceSystem MAP(){
		return getTermByUuid(uuidMap);
	}

	@Override
	protected void setDefaultTerms(TermVocabulary<ReferenceSystem> termVocabulary){
		termMap = new HashMap<UUID, ReferenceSystem>();
		for (ReferenceSystem term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term);
		}
	}

}
