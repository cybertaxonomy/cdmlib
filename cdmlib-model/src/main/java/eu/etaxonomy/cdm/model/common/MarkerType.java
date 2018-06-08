/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;

/**
 * Marker types similar to dynamically defined attributes. These  content types
 * like "IS_DOUBTFUL", "COMPLETE"  or specific local flags.
 * @author m.doering
 * @since 08-Nov-2007 13:06:33
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MarkerType")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class MarkerType extends DefinedTermBase<MarkerType> {
	private static final long serialVersionUID = -9117424749919907396L;
	public static final Logger logger = Logger.getLogger(MarkerType.class);

	private static final UUID uuidImported = UUID.fromString("96878790-4ceb-42a2-9738-a2242079b679");
	private static final UUID uuidToBeChecked = UUID.fromString("34204192-b41d-4857-a1d4-28992bef2a2a");
	private static final UUID uuidIsDoubtful = UUID.fromString("b51325c8-05fe-421a-832b-d86fc249ef6e");
	public static final UUID uuidComplete = UUID.fromString("b4b1b2ab-89a8-4ce6-8110-d60b8b1bc433");
	private static final UUID uuidPublish = UUID.fromString("0522c2b3-b21c-400c-80fc-a251c3501dbc");
	private static final UUID uuidInBibliography = UUID.fromString("2cdb492e-3b8b-4784-8c26-25159835231d");
	private static final UUID uuidEndemic = UUID.fromString("efe95ade-8a6c-4a0e-800e-437c8b50c45e");
	private static final UUID uuidModifiable = UUID.fromString("c21bc83f-c8ae-4126-adee-10dfe817e96a");
	private static final UUID uuidUse = UUID.fromString("2e6e42d9-e92a-41f4-899b-03c0ac64f039");
	private static final UUID uuidComputed = UUID.fromString("5cc15a73-2947-44e3-9319-85dd20736e55");
    private static final UUID uuidNomenclaturalRelevant = UUID.fromString("d520ffd4-4a59-453d-b2a1-cbaa50136439");

	protected static Map<UUID, MarkerType> termMap = null;

    @XmlAttribute(name = "isTechnical")
    @Field(analyze = Analyze.NO)
    private boolean isTechnical=false;

 // ***************************** FACTORY METHODD ************************/

	public static MarkerType NewInstance(String term, String label, String labelAbbrev){
		return new MarkerType(term, label, labelAbbrev);
	}


// ***************************** CONSTRUCTOR ******************************/

	//for hibernate use only
	@Deprecated
	protected MarkerType() {
		super(TermType.MarkerType);
	}

//***************************** CONSTRUCTOR **************************************/

	private MarkerType(String term, String label, String labelAbbrev) {
		super(TermType.MarkerType, term, label, labelAbbrev);
	}

// ******************** METHODS **************************************************/

	/**
	 * A flag indicating if markers of this type are user content or technical information
	 * to be used by applications only. E.g. a FeatureTree may have a marker that defines
	 * the role of this FeatureTree ("for ordering") whereas a {@link eu.etaxonomy.cdm.model.taxon.Taxon taxon}
	 * may have a user defined marker "completed" that indicates that this taxon does not
	 * need further investigation. The earlier will be flagged isTechnical=true whereas
	 * the later will be flagged as isTechnical=false
	 * @return the isTechnical
	 */
	public boolean isTechnical() {
		return isTechnical;
	}

	/**
	 * @param isTechnical the isTechnical to set
	 */
	public void setTechnical(boolean isTechnical) {
		this.isTechnical = isTechnical;
	}

//***************************** TERMS **************************************/


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#resetTerms()
	 */
	@Override
	public void resetTerms(){
		termMap = null;
	}


	protected static MarkerType getTermByUuid(UUID uuid){
        if (termMap == null || termMap.isEmpty()){
            return getTermByClassAndUUID(MarkerType.class, uuid);
        } else {
            return termMap.get(uuid);
        }
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

	public static final MarkerType IN_BIBLIOGRAPHY(){
		return getTermByUuid(uuidInBibliography);
	}

	public static final MarkerType ENDEMIC(){
		return getTermByUuid(uuidEndemic);
	}

	public static final MarkerType MODIFIABLE(){
		return getTermByUuid(uuidModifiable);
	}

	public static final MarkerType USE(){
		return getTermByUuid(uuidUse);
	}

    public static final MarkerType COMPUTED(){
        return getTermByUuid(uuidComputed);
    }

    // added in preparation for #7466
    public static final MarkerType NOMENCLATURAL_RELEVANT(){
        return getTermByUuid(uuidNomenclaturalRelevant);
    }

	@Override
	protected void setDefaultTerms(TermVocabulary<MarkerType> termVocabulary) {
		termMap = new HashMap<UUID, MarkerType>();
		for (MarkerType term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term);
		}
	}

}
