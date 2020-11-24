/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlEnum;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.term.IEnumTerm;

/**
 * Represents a classification of nomenclatural status and/or name relationship types
 * which xxx a nomenclatural standing. Beside NONE 4 such classes exist.
 *
 * See #9272 for futher information.
 *
 * @author a.mueller
 * @since 06.11.2020
 */
@XmlEnum
public enum NomenclaturalStanding
        implements IEnumTerm<NomenclaturalStanding>, INomenclaturalStanding {

    /**
     * Indicates a name that is explicitly valid.
     */
    VALID(UUID.fromString("0ea6b9c7-f439-4b71-822b-799e7395fb1d"), "valid", "VA", 10),

    /**
     * Indicates an invalid ("not available" in zoology) "name"
     * where the rule is explicitly mentioned in the code.
     * A taxon name is "invalid" if it is not "valid"; this means that
     * the taxon name:<ul>
     * <li>has not been effectively published or
     * <li>has a form which does not comply with the rules of the
     *     {@link NomenclaturalCode nomenclature code} or
     * <li>is not accompanied by a description or diagnosis or by a reference to
     *     such a previously published description or diagnosis
     */
    INVALID(UUID.fromString("b1cc02a4-adb2-48f9-b8e6-85ed2f07b841"), "invalid", "IN", 8),

    /**
     * Indicates an invalid ("not available" in zoology) "name" where the reason is
     * not explicitly mentioned in the code.
     *
     * For nom status: rejected, definitely rejected, confused, ined., comb. ined., ambigous, orth. var.
     */
    OTHER_DESIGNATION(UUID.fromString("610633e9-ad90-40e1-a2a6-16313aa1449d"), "other designation", "OD", 6),

    /**
     * Indicates a valid but illegitimate name.
     */
    ILLEGITIMATE(UUID.fromString("67a51221-065f-4ad4-8e45-e4b265c06367"), "illegitimate", "IL", 4),

    /**
     * Indicates that no nomenclatural standing is connected to the given status or relationship.
     */
    NONE(UUID.fromString("c7b6d8e5-7f39-4664-839d-076069b0d1ad"), "none", "NO", 0),
    ;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(RankClass.class);

    int priority;

    @SuppressWarnings("unchecked")
    private NomenclaturalStanding(UUID uuid, String defaultString, String key, int priority){
        delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, null);
        this.priority = priority;
    }

//************************** METHODS *******************/

    @Override
    public boolean isDesignationOnly() {
        return this == OTHER_DESIGNATION;
    }

    @Override
    public boolean isIllegitimate() {
        return this == ILLEGITIMATE;
    }

    @Override
    public boolean isInvalidExplicit() {
        return this == INVALID;
    }

    @Override
    public boolean isValidExplicit() {
        return this == VALID;
    }

    @Override
    public boolean isNoStatus() {
        return this == NONE;
    }

    @Override
    public boolean isInvalid(){
        return this == INVALID || this == OTHER_DESIGNATION;
    }


    @Override
    public boolean isLegitimate() {
        return this == VALID || this == NONE;
    }


    @Override
    public boolean isValid() {
        return isLegitimate() || isIllegitimate() ;
    }

    /**
     * Returns <code>true</code> if this status does not indicate
     * that the respecting name is a designation only (invalid).
     * However, it may be illegitimate. <code>true</code> is also
     * returned if the status does not include an explicit nomenclatural standing.
     * So it is <code>true</code> for
     * {@link #VALID}, {@link #ILLEGITIMATE} and {@link #NONE}
     */
    public boolean isName() {
        return isLegitimate() || this == ILLEGITIMATE || this == NONE;
    }

// *************************** DELEGATE **************************************/

    private static EnumeratedTermVoc<NomenclaturalStanding> delegateVoc;
    private IEnumTerm<NomenclaturalStanding> delegateVocTerm;

    static {
        delegateVoc = EnumeratedTermVoc.getVoc(NomenclaturalStanding.class);
    }

    @Override
    public String getKey(){return delegateVocTerm.getKey();}

    @Override
    public String getMessage(){return delegateVocTerm.getMessage();}

    @Override
    public String getMessage(Language language){return delegateVocTerm.getMessage(language);}

    @Override
    public UUID getUuid() {return delegateVocTerm.getUuid();}

    @Override
    public NomenclaturalStanding getKindOf() {return delegateVocTerm.getKindOf();}

    @Override
    public Set<NomenclaturalStanding> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

    @Override
    public boolean isKindOf(NomenclaturalStanding ancestor) {return delegateVocTerm.isKindOf(ancestor); }

    @Override
    public Set<NomenclaturalStanding> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}


    public static NomenclaturalStanding getByKey(String key){return delegateVoc.getByKey(key);}
    public static NomenclaturalStanding getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}

    /**
     * Returns the nomenclatural standing with the highest priority.
     */
    public static NomenclaturalStanding highest(Set<NomenclaturalStanding> standings) {
        NomenclaturalStanding result = NONE;
        for (NomenclaturalStanding standing : standings){
            if (standing.priority > result.priority){
                result = standing;
            }
        }
        return result;
    }
}