/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.Language;

/**
 * @author a.mueller
 * @since 2023-03-23
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IdentifierType", propOrder = {
        "urlPattern",
    })
@XmlRootElement(name = "IdentifierType")
@Entity
@Audited
public class IdentifierType
        extends DefinedTermBase<IdentifierType> {

    private static final long serialVersionUID = -6965540410672076893L;

	//Identifier Type
	public static final UUID uuidSampleDesignation = UUID.fromString("fadeba12-1be3-4bc7-9ff5-361b088d86fc");
	public static final UUID uuidLsid = UUID.fromString("26729412-9df6-4cc3-9e5d-501531ca21f0");
	public static final UUID uuidAlternativeFieldNumber = UUID.fromString("054fd3d1-1961-42f8-b024-b91184ac9e0c");
    public static final UUID uuidTropicosNameIdentifier = UUID.fromString("6205e531-75b0-4f2a-9a9c-b1247fb080ab");
    public static final UUID uuidIpniNameIdentifier = UUID.fromString("009a602f-0ff6-4231-93db-f458e8229aca");
    public static final UUID uuidWfoNameIdentifier = UUID.fromString("048e0cf9-f59c-42dd-bfeb-3a5cba0191c7");
    //currently only used in Caryophyllales_spp
    public static final UUID uuidPlantListIdentifier = UUID.fromString("06e4c3bd-7bf6-447a-b96e-2844b279f276");
    public static final UUID uuidIndexFungorumIdentifier = UUID.fromString("f405be9f-359a-49ba-b09b-4a7920386190");

    //#10260
    //a pattern representing an URL and which includes the placeholde "{@ID}"
    //which will be replaced by the actual identifier
    private String urlPattern;

	/**
     * Creates a new empty {@link IdentifierType} instance.
     *
     * @see #NewInstance(String, String, String)
     */
    public static IdentifierType NewInstance() {
        return new IdentifierType();
    }

	public static IdentifierType NewInstance(Set<Representation> representations){
	    IdentifierType term = new IdentifierType();
	    for (Representation representation : representations) {
            term.addRepresentation(representation);
        }
	    return term;
	}

	public static IdentifierType NewInstance(String description, String label, String labelAbbrev, Language lang){
		return new IdentifierType(description, label, labelAbbrev, lang);
	}
	public static IdentifierType NewInstance(String description, String label, String labelAbbrev){
		return new IdentifierType(description, label, labelAbbrev, null);
	}

//******************* CONSTRUCTOR ***********************************/

	IdentifierType(){
	    super(TermType.IdentifierType);
	}

	public IdentifierType(String description, String label, String labelAbbrev, Language lang) {
		super(TermType.IdentifierType, description, label, labelAbbrev, lang);
	}
	public IdentifierType(String description, String label, String labelAbbrev) {
		super(TermType.IdentifierType, description, label, labelAbbrev);
	}

//*************************** TERM MAP *********************/


    protected static Map<UUID, IdentifierType> termMap = null;

    public static IdentifierType getTermByUuid(UUID uuid){
        if (termMap == null || termMap.isEmpty()){
            return getTermByClassAndUUID(IdentifierType.class, uuid);
        } else{
            return termMap.get(uuid);
        }
    }

    public static IdentifierType IDENTIFIER_NAME_TROPICOS(){
        return getTermByUuid(uuidTropicosNameIdentifier);
    }
    public static IdentifierType IDENTIFIER_NAME_IPNI(){
        return getTermByUuid(uuidIpniNameIdentifier);
    }
    public static IdentifierType IDENTIFIER_NAME_WFO(){
        return getTermByUuid(uuidWfoNameIdentifier);
    }
    public static IdentifierType IDENTIFIER_NAME_IF(){
        return getTermByUuid(uuidIndexFungorumIdentifier);
    }

// ******************** GETTER /SETTER *********


    /**
     * A pattern which represents an URL and which includes the placeholder "{@ID}"
     * which will be replaced by the actual identifier
     * @see https://dev.e-taxonomy.eu/redmine/issues/10260
     */
    public String getUrlPattern() {
        return urlPattern;
    }
    /**
     * @see #getUrlPattern()
     */
    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

	@Override
	public void resetTerms() {
		termMap = null;
	}

	@Override
	protected void setDefaultTerms(TermVocabulary<IdentifierType> termVocabulary) {
		if (termMap == null){
			termMap = new HashMap<>();
		}
		for (IdentifierType term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term);
		}
	}

    @Override
    public IdentifierType readCsvLine(Class<IdentifierType> termClass, List<String> csvLine, TermType termType, Map<UUID,DefinedTermBase> terms, boolean abbrevAsId) {
        try {
            IdentifierType newInstance = super.readCsvLine(termClass, csvLine, termType, terms, abbrevAsId);
            String urlPattern = CdmUtils.Ne(csvLine.get(5));
            newInstance.setUrlPattern(urlPattern);
            return newInstance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	protected int partOfCsvLineIndex(){
		return 6;
	}
}