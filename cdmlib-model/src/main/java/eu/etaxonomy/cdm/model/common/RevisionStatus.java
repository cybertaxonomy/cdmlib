/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * Terms describing the revision / editorial status of an entity
 * (e.g. "new", "in process", "completed").
 *
 * See #11040
 *
 * @author muellera
 * @since 07.10.2026
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RevisionStatus")
@XmlRootElement(name = "RevisionStatus")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed
@Audited
public class RevisionStatus extends DefinedTermBase<RevisionStatus> {

    private static final long serialVersionUID = -4829173056148271832L;

    public static final UUID uuidNew = UUID.fromString("b5fb1ef4-5b00-4c90-9dd6-77be85238be0");
    public static final UUID uuidInProcess = UUID.fromString("7cae0b07-767f-4cd1-90cd-b11440e6ee66");
    public static final UUID uuidCompleted = UUID.fromString("04751795-1159-44b2-88ab-d9946fa867d0");

    protected static Map<UUID, RevisionStatus> termMap = null;

    private String defaultColor = "000000";

 // ***************************** FACTORY METHODS ************************/

    public static RevisionStatus NewInstance() {
        return new RevisionStatus();
    }

    public static RevisionStatus NewInstance(String term, String label, String labelAbbrev) {
        return new RevisionStatus(term, label, labelAbbrev);
    }

// ***************************** CONSTRUCTOR ******************************/

    //for hibernate use only
    @Deprecated
    protected RevisionStatus() {
        super(TermType.RevisionStatus);
    }

    protected RevisionStatus(String term, String label, String labelAbbrev) {
        super(TermType.RevisionStatus, term, label, labelAbbrev);
    }

// ***************************** TERMS **************************************/

    @Override
    public void resetTerms() {
        termMap = null;
    }

    protected static RevisionStatus getTermByUuid(UUID uuid) {
        if (termMap == null || termMap.isEmpty()) {
            return getTermByClassAndUUID(RevisionStatus.class, uuid);
        } else {
            return termMap.get(uuid);
        }
    }

    public static final RevisionStatus NEW() {
        return getTermByUuid(uuidNew);
    }

    public static final RevisionStatus IN_PROCESS() {
        return getTermByUuid(uuidInProcess);
    }

    public static final RevisionStatus COMPLETED() {
        return getTermByUuid(uuidCompleted);
    }

    @Override
    protected void setDefaultTerms(TermVocabulary<RevisionStatus> termVocabulary) {
        termMap = new HashMap<>();
        for (RevisionStatus term : termVocabulary.getTerms()) {
            termMap.put(term.getUuid(), term);
        }
    }

    @Override
    public RevisionStatus readCsvLine(Class<RevisionStatus> termClass, List<String> csvLine, TermType termType,
            Map<UUID, DefinedTermBase> terms, boolean abbrevAsId) {
        RevisionStatus newInstance = super.readCsvLine(termClass, csvLine, termType, terms, abbrevAsId);
        String color = csvLine.get(5);
        try {
            newInstance.setDefaultColor(color);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return newInstance;
    }

// ***************************** GETTER / SETTER ******************************/

    public String getDefaultColor() {
        return defaultColor;
    }

    /**
     * @param defaultColor the defaultColor to set (format {@code FFFFFF})
     * @throws ParseException if the color is not of the expected format
     */
    public void setDefaultColor(String defaultColor) throws ParseException {
        String regEx = "[0-9a-fA-F]{6}";
        if (StringUtils.isEmpty(defaultColor)) {
            this.defaultColor = null;
        } else if (defaultColor.matches(regEx)) {
            this.defaultColor = defaultColor;
        } else {
            throw new java.text.ParseException("Default color is not of correct format. Required is 'FFFFFF'", -1);
        }
    }
}