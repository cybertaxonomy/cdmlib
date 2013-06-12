/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;


/**
 * This (abstract) class represents terms describing the {@link AbsenceTerm absence}
 * (like "extinct") or the {@link PresenceTerm presence} (like "cultivated") of a {@link Taxon taxon}
 * in a {@link NamedArea named area}. Splitting the terms in two subclasses allows to
 * assign them automatically to absent or present status. These terms are only
 * used for {@link Distribution distributions}.

 *
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:44
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PresenceAbsenceTermBase")
@XmlSeeAlso({
    AbsenceTerm.class,
    PresenceTerm.class
})
@Entity
@Audited
public abstract class PresenceAbsenceTermBase<T extends PresenceAbsenceTermBase<?>> extends OrderedTermBase<T> {
    private static final long serialVersionUID = 1596291470042068880L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(PresenceAbsenceTermBase.class);

    private String defaultColor = "000000";


    /**
     * Class constructor: creates a new empty presence or absence term.
     *
     * @see #PresenceAbsenceTermBase(String, String, String)
     */
    protected PresenceAbsenceTermBase() {
    }

    /**
     * Class constructor: creates a new presence or absence term with a description
     * (in the {@link Language#DEFAULT() default language}), a label and a label abbreviation.
     *
     * @param	term  		 the string (in the default language) describing the
     * 						 new presence or absence term to be created
     * @param	label  		 the string identifying the new presence or absence term to be created
     * @param	labelAbbrev  the string identifying (in abbreviated form) the
     * 						 new presence or absence term to be created
     * @see 				 #PresenceAbsenceTermBase()
     */
    protected PresenceAbsenceTermBase(String term, String label, String labelAbbrev) {
        super(term, label, labelAbbrev);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#readCsvLine(java.util.List)
     */
    @Override
    public T readCsvLine(Class<T> termClass, List<String> csvLine, Map<UUID,DefinedTermBase> terms) {
        T newInstance = super.readCsvLine(termClass, csvLine, terms);
        String abbreviatedLabel = (String)csvLine.get(4);
//		String uuid = (String)csvLine.get(0);
//		map.put(abbreviatedLabel, UUID.fromString(uuid));
        String color = (String)csvLine.get(5);
        newInstance.setDefaultColor(color);
        newInstance.getRepresentation(Language.DEFAULT()).setAbbreviatedLabel(abbreviatedLabel);
        return newInstance;
    }

    /**
     * @return the defaultColor
     */
    public String getDefaultColor() {
        return defaultColor;
    }

    /**
     * @param defaultColor the defaultColor to set
     */
    //TODO check RGB length 6 and between 000000 and FFFFFF
    public void setDefaultColor(String defaultColor) {
        this.defaultColor = defaultColor;
    }

}