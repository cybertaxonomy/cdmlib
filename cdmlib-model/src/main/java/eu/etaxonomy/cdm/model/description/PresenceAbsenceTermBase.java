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

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.VocabularyEnum;


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

	
//********************************** Constructor *******************************************************************/	

  	//for hibernate use only
  	@Deprecated
  	protected PresenceAbsenceTermBase() {
    	super(TermType.PresenceAbsenceTerm);
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
        super(TermType.PresenceAbsenceTerm, term, label, labelAbbrev);
    }
    
//******************************** METHODS ****************************/    

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#readCsvLine(java.util.List)
     */
    @Override
    public T readCsvLine(Class<T> termClass, List<String> csvLine, Map<UUID,DefinedTermBase> terms, boolean abbrevAsId) {
        T newInstance = super.readCsvLine(termClass, csvLine, terms, abbrevAsId);
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
    
    /**
     * Compares this OrderedTermBase with the specified OrderedTermBase for
     * order. Returns a -1, 0, or +1 if the orderId of this object is greater
     * than, equal to, or less than the specified object.
     * <p>
     * <b>Note:</b> The compare logic of this method is the <b>inverse logic</b>
     * of the the one implemented in
     * {@link java.lang.Comparable#compareTo(java.lang.Object)}
     *
     * @param orderedTerm
     *            the OrderedTermBase to be compared
     * @param skipVocabularyCheck
     *            whether to skip checking if both terms to compare are in the
     *            same vocabulary
     * @throws NullPointerException
     *             if the specified object is null
     */
    protected int performCompareTo(T presenceAbsenceTerm, boolean skipVocabularyCheck) {

    	PresenceAbsenceTermBase<?> presenceAbsenceTermLocal = CdmBase.deproxy(presenceAbsenceTerm, PresenceAbsenceTermBase.class);
        if(!skipVocabularyCheck){
            if (this.vocabulary == null || presenceAbsenceTermLocal.vocabulary == null){
                throw new IllegalStateException("An ordered term (" + this.toString() + " or " + presenceAbsenceTermLocal.toString() + ") of class " + this.getClass() + " or " + presenceAbsenceTermLocal.getClass() + " does not belong to a vocabulary and therefore can not be compared");
            }
            if (! this.getVocabulary().getUuid().equals(presenceAbsenceTermLocal.vocabulary.getUuid())){
               //throw new IllegalStateException("2 terms do not belong to the same vocabulary and therefore can not be compared" + this.getTitleCache() + " and " + orderedTermLocal.getTitleCache());
            	if (presenceAbsenceTermLocal.getVocabulary().getUuid().equals(VocabularyEnum.AbsenceTerm.getUuid()) || presenceAbsenceTermLocal.getVocabulary().getUuid().equals(VocabularyEnum.PresenceTerm.getUuid())){
            		logger.debug("2 presenceAbsence terms do not belong to the same vocabulary, the absent terms will be ordered behind the presence terms");
            		if (this.getVocabulary().getUuid().equals(VocabularyEnum.AbsenceTerm.getUuid())){
            			return 1;
            		}else{
            			return -1;
            		}
            	}else{
                	throw new IllegalStateException("2 terms do not belong to the same vocabulary and therefore can not be compared " + this.getTitleCache() + " and " + presenceAbsenceTerm.getTitleCache());
                }
            }
        }

        int orderThat;
        int orderThis;
        try {
            orderThat = presenceAbsenceTerm.orderIndex;//OLD: this.getVocabulary().getTerms().indexOf(orderedTerm);
            orderThis = orderIndex; //OLD: this.getVocabulary().getTerms().indexOf(this);
        } catch (RuntimeException e) {
            throw e;
        }
        if (orderThis > orderThat){
            return -1;
        }else if (orderThis < orderThat){
            return 1;
        }else {
            return 0;
        }
    }

}