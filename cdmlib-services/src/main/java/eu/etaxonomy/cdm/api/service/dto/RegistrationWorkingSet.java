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

import eu.etaxonomy.cdm.api.service.exception.RegistrationValidationException;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;

/**
 * @author a.kohlbecker
 * @since Mar 22, 2017
 *
 */
public class RegistrationWorkingSet {

    private List<RegistrationDTO> registrationDTOs = new ArrayList<>();

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

    public RegistrationWorkingSet(List<RegistrationDTO> registrationDTOs) throws RegistrationValidationException {
        validateAndAddDTOs(registrationDTOs, null);
    }

    /**
     * @param candidated
     * @throws RegistrationValidationException
     *
     */
    private void validateAndAdd(Set<Registration> candidates) throws RegistrationValidationException {
        List<RegistrationDTO> dtos = new ArrayList<>(registrationDTOs.size());
        candidates.forEach(reg -> dtos.add(new RegistrationDTO(reg)));
        validateAndAddDTOs(dtos, null);
    }

    /**
     * Validate and add all Registrations to the working set which are referring to the same publication
     * which is either the citation of the nomenclatural reference of the {@link TaxonName} or the
     * citation of the {@link TypeDesignations}. In case the citation is a section and this section is
     * having an in-reference the in-reference will be used instead.
     * Registration with a differing publication are not added to
     * the working set, instead a {@link RegistrationValidationException} is thrown which is a container for
     * all validation problems.
     *
     * @param candidates
     * @param problems
     *    Problems detected in prior validation and processing passed to this method to be completed. Can be <code>null</code>.
     * @throws RegistrationValidationException
     */
    private void validateAndAddDTOs(List<RegistrationDTO> candidates, List<String> problems) throws RegistrationValidationException {
        if(problems == null){
            problems = new ArrayList<>();
        }
        for(RegistrationDTO regDto : candidates){
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
                this.registrationDTOs.add(regDto);
                if(created == null || created.isAfter(regDto.getCreated())){
                    created = regDto.getCreated();
                }
        }

        if(!problems.isEmpty()){
            throw new RegistrationValidationException("", problems);
        }

    }

    /**
     * @param regDto
     * @return
     */
    protected Reference publicationUnit(RegistrationDTO regDto) {
        Reference ref = regDto.getCitation();
        while(ref.isOfType(ReferenceType.Section)&& ref.getInReference() != null){
            ref = ref.getInReference();
            if(!ref.isOfType(ReferenceType.Section)){
                break;
            }
        }
        return ref;
    }

    /**
     * @param reg
     * @throws RegistrationValidationException
     */
    public void add(Registration reg) throws RegistrationValidationException {
        Set<Registration> candidates = new HashSet<>();
        candidates.add(reg);
        validateAndAdd(candidates);
    }

    public void add(RegistrationDTO regDTO) throws RegistrationValidationException {
        validateAndAddDTOs(Arrays.asList(regDTO), null);
    }

    /**
     * @return the registrations
     */
    public List<Registration> getRegistrations() {
        List<Registration> regs = new ArrayList<>(registrationDTOs.size());
        registrationDTOs.forEach(dto -> regs.add(dto.registration()));
        return regs;
    }

    /**
     * Calculates the total count of messages in the registrations contained
     * in the working set.
     *
     * @return
     */
    public int messagesCount() {
        int messagesCount = 0;
        for(RegistrationDTO dto : getRegistrationDTOs()) {
            messagesCount = messagesCount + dto.getValidationProblems().size();
        }
        return messagesCount;
    }

    /**
     * Finds the lowest status in the registrations contained
     * in the working set.
     *
     * @return
     */
    public RegistrationStatus lowestStatus() {
        RegistrationStatus status = RegistrationStatus.REJECTED;
        for(RegistrationDTO dto : getRegistrationDTOs()) {
            if(dto.getStatus().compareTo(status) < 0){
                status = dto.getStatus();
            }
        }
        return status;
    }


    /**
     * @return the registrations
     */
    public List<RegistrationDTO> getRegistrationDTOs() {
        return registrationDTOs;
    }

    public Optional<RegistrationDTO> getRegistrationDTO(UUID registrationUuid) {
        return registrationDTOs.stream().filter(r -> r.getUuid().equals(registrationUuid) ).findFirst();
    }

    /**
     * @return the citationId
     */
    public UUID getCitationUuid() {
        return citationUuid;
    }

    /**
     * @return the citationString
     */
    public String getCitation() {
        return citationString;
    }

    public DateTime getRegistrationDate() {
        return registrationDTOs.get(0).getRegistrationDate();
    }

    public DateTime getCreationDate() {
        return registrationDTOs.get(0).getCreated();
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

}
