/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;

import eu.etaxonomy.cdm.api.service.exception.TypeDesignationSetException;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;

/**
 * @author a.kohlbecker
 * @since Mar 22, 2017
 */
public class RegistrationWorkingSet {

    private List<RegistrationWrapperDTO> registrationWrapperDTOs = new ArrayList<>();

    private UUID citationUuid = null;

    private DateTime created = null;

    private String citationString = null;

    /**
     * Creates an empty working set
     */
    public RegistrationWorkingSet(Reference citation) {
        citationUuid = citation.getUuid();
        this.citationString= citation.getTitleCache();

    }

    public RegistrationWorkingSet(List<RegistrationWrapperDTO> registrationWrapperDTOs) throws TypeDesignationSetException {
        validateAndAddDTOs(registrationWrapperDTOs, null);
    }

    private void validateAndAdd(Set<Registration> candidates) throws TypeDesignationSetException {
        List<RegistrationWrapperDTO> dtos = new ArrayList<>(registrationWrapperDTOs.size());
        candidates.forEach(reg -> dtos.add(new RegistrationWrapperDTO(reg)));
        validateAndAddDTOs(dtos, null);
    }

    /**
     * Validate and add all Registrations to the working set which are referring to the same publication
     * which is either the citation of the nomenclatural reference of the {@link TaxonName} or the
     * citation of the {@link TypeDesignations}. In case the citation is a section and this section is
     * having an in-reference the in-reference will be used instead.
     * Registration with a differing publication are not added to
     * the working set, instead a {@link TypeDesignationSetException} is thrown which is a container for
     * all validation problems.
     *
     * @param candidates
     * @param problems
     *    Problems detected in prior validation and processing passed to this method to be completed. Can be <code>null</code>.
     * @throws TypeDesignationSetException
     */
    private void validateAndAddDTOs(List<RegistrationWrapperDTO> candidates, List<String> problems) throws TypeDesignationSetException {
        if(problems == null){
            problems = new ArrayList<>();
        }
        for(RegistrationWrapperDTO regDto : candidates){
                Reference citation = publicationUnit(regDto);
                if(citationUuid == null){
                    citationUuid = citation.getUuid();
                    citationString = citation.getTitleCache();
                } else {
                    if(!citation.getUuid().equals(citationUuid)){
                        problems.add("Removing Registration " + regDto.getSummary() + " from set since this refers to a different citationString.");
                        continue;
                    }
                }
                this.registrationWrapperDTOs.add(regDto);
                if(created == null || created.isAfter(regDto.getCreated())){
                    created = regDto.getCreated();
                }
        }

        if(!problems.isEmpty()){
            throw new TypeDesignationSetException("", problems);
        }

    }

    protected Reference publicationUnit(RegistrationWrapperDTO regDto) {
        Reference ref = regDto.getCitation();
        while(ref.isOfType(ReferenceType.Section)&& ref.getInReference() != null){
            ref = ref.getInReference();
            if(!ref.isOfType(ReferenceType.Section)){
                break;
            }
        }
        return ref;
    }

    public void add(Registration reg) throws TypeDesignationSetException {
        Set<Registration> candidates = new HashSet<>();
        candidates.add(reg);
        validateAndAdd(candidates);
    }

    public void add(RegistrationWrapperDTO regDTO) throws TypeDesignationSetException {
        validateAndAddDTOs(Arrays.asList(regDTO), null);
    }

    /**
     * @return the registrations
     */
    public List<Registration> getRegistrations() {
        List<Registration> regs = new ArrayList<>(registrationWrapperDTOs.size());
        registrationWrapperDTOs.forEach(dto -> regs.add(dto.registration()));
        return regs;
    }

    /**
     * Calculates the total count of validation problems in the registrations contained
     * in the working set.
     *
     * @return
     */
    public int validationProblemsCount() {
        int validationProblemsCount = 0;
        for(RegistrationWrapperDTO dto : getRegistrationWrapperDTOs()) {
            validationProblemsCount = validationProblemsCount + dto.getValidationProblems().size();
        }
        return validationProblemsCount;
    }

    /**
     * Finds the lowest status in the registrations contained
     * in the working set.
     *
     * @return
     */
    public RegistrationStatus lowestStatus() {
        RegistrationStatus status = RegistrationStatus.REJECTED;
        for(RegistrationWrapperDTO dto : getRegistrationWrapperDTOs()) {
            if(dto.getStatus().compareTo(status) < 0){
                status = dto.getStatus();
            }
        }
        return status;
    }


    /**
     * @return the registrations
     */
    public List<RegistrationWrapperDTO> getRegistrationWrapperDTOs() {
        return registrationWrapperDTOs;
    }

    public Optional<RegistrationWrapperDTO> getRegistrationWrapperDTO(UUID registrationUuid) {
        return registrationWrapperDTOs.stream().filter(r -> r.getUuid().equals(registrationUuid) ).findFirst();
    }

    public UUID getCitationUuid() {
        return citationUuid;
    }

    public String getCitation() {
        return citationString;
    }

    public DateTime getRegistrationDate() {
        return registrationWrapperDTOs.isEmpty()? null: registrationWrapperDTOs.get(0).getRegistrationDate();
    }

    public DateTime getCreationDate() {
        return registrationWrapperDTOs.isEmpty()? null: registrationWrapperDTOs.get(0).getCreated();
    }

    /**
     * The creation time stamp of a registration set always is
     * the creation DateTime of the oldest Registration contained
     * in the set.
     *
     * @return
     */
    public DateTime getCreated(){
        return created;
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        registrationWrapperDTOs.forEach(dto -> str.append(dto.getIdentifier() + " : " + dto.getSummary()).append("\n"));
        return str.toString();
    }

}
