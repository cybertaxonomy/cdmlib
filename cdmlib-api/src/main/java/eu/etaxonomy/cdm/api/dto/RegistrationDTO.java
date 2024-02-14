/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.ref.EntityReference;
import eu.etaxonomy.cdm.ref.TypedEntityReference;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * Reading DTO for {@link Registration}s. See also RegistrationWrapperDTO from service layer
 * which is very similar.
 *
 * @author muellera
 * @since 14.02.2024
 */
public class RegistrationDTO extends TypedEntityReference<Registration> {

    private static final long serialVersionUID = 8913979459243562903L;

    private RegistrationType registrationType;

    private String submitterUserName;

    private String citationDetail;

    private String summary;

    private List<TaggedText> summaryTaggedText = new ArrayList<>();

    private RankedNameReference nameRef;

    private String nomenclaturalCitationString;

    private String bibliographicCitationString;

    private String bibliographicInRefCitationString;

    private List<String> validationProblems = new ArrayList<>();


    public static class RankedNameReference extends EntityReference {

        private static final long serialVersionUID = -9107203008234036562L;

        private boolean isSupraSpecific;

        public RankedNameReference(UUID uuid, String label, boolean isSupraSpecific) {
            super(uuid, label);
            this.isSupraSpecific = isSupraSpecific;
        }
        public boolean isSupraGeneric() {
            return isSupraSpecific;
        }
    }

    // **************** CONSTRUCTOR **************************/

    public RegistrationDTO(Class<Registration> type, UUID uuid) {
        super(type, uuid);
    }

    // ************* GETTER / SETTER **************************/

    public RegistrationType getRegistrationType() {
        return registrationType;
    }
    public void setRegistrationType(RegistrationType registrationType) {
        this.registrationType = registrationType;
    }

    public String getSubmitterUserName() {
        return submitterUserName;
    }
    public void setSubmitterUserName(String submitterUserName) {
        this.submitterUserName = submitterUserName;
    }

    public String getCitationDetail() {
        return citationDetail;
    }
    public void setCitationDetail(String citationDetail) {
        this.citationDetail = citationDetail;
    }

    public RankedNameReference getNameRef() {
        return nameRef;
    }
    public void setNameRef(RankedNameReference nameRef) {
        this.nameRef = nameRef;
    }

    public String getSummary() {
        return summary;
    }
    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<? extends TaggedText> getSummaryTaggedText() {
        return summaryTaggedText;
    }
    public void addSummaryTaggedText(List<TaggedText> summaryToAdd) {
        summaryTaggedText.addAll(summaryToAdd);
    }

    /**
     * The bibliographic citation is either reference which is directly
     * associated with the registration.
     * <p>
     * <b>Note:</b>Compare with {@link #getBibliographicInRefCitationString()}
     *
     * @return the bibliographicCitationString
     */
    public String getBibliographicCitationString() {
        return bibliographicCitationString;
    }
    public void setBibliographicCitationString(String bibliographicCitationString) {
        this.bibliographicCitationString = bibliographicCitationString;
    }

    /**
     * The bibliographic in-reference citation is either taken from the reference which is directly
     * associated with the registration. In case this reference is a {@link eu.etaxonomy.cdm.model.reference.ReferenceType#Section} or
     * {@link eu.etaxonomy.cdm.model.reference.ReferenceType#BookSection} the inReference will be taken instead.
     * <p>
     * <b>Note:</b>Compare with {@link #getBibliographicCitationString()}
     *
     * @return the bibliographicInRefCitationString
     */
    public String getBibliographicInRefCitationString() {
       return bibliographicInRefCitationString;
    }
    public void setBibliographicInRefCitationString(String bibliographicInRefCitationString) {
        this.bibliographicInRefCitationString = bibliographicInRefCitationString;
    }

    /**
     * The nomenclatural citation is always the nomenclaturalCitation of the reference which is directly
     * associated with the registration.
     * <p>
     * <b>Note:</b>Compare with {@link #getBibliographicCitationString()}
     *
     * @return the nomenclaturalCitationString
     */
    public String getNomenclaturalCitationString() {
        return nomenclaturalCitationString;
    }
    public void setNomenclaturalCitationString(String nomenclaturalCitationString) {
        this.nomenclaturalCitationString = nomenclaturalCitationString;
    }

    public List<String> getValidationProblems() {
        return validationProblems;
    }
    public void addValidationProblem(String problem) {
        validationProblems.add(problem);
    }
}