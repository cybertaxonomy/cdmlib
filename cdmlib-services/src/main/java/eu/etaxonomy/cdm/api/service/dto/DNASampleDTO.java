/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.HashSet;
import java.util.Set;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.molecular.AmplificationResult;
import eu.etaxonomy.cdm.model.molecular.DnaQuality;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;

/**
 * @author k.luther
 * @since 02.08.2018
 *
 */
public class DNASampleDTO extends PreservedSpecimenDTO{

    private Set<SequenceDTO> sequences = new HashSet<SequenceDTO>();

    private Set<AmplificationResult> amplificationResults = new HashSet<AmplificationResult>();

    private DnaQuality dnaQuality;




    public DNASampleDTO(DerivedUnit derivedUnit) {
        super(derivedUnit);
        DnaSample dnaSample = HibernateProxyHelper.deproxy(derivedUnit, DnaSample.class);
        Set<SequenceDTO> seqDtos = new HashSet<>();
        for (Sequence seq: dnaSample.getSequences()){
            seqDtos.add(new SequenceDTO(seq));
        }
        this.setSequences(seqDtos);
        this.amplificationResults = dnaSample.getAmplificationResults();
        this.dnaQuality = dnaSample.getDnaQuality();
    }


    public void setSequences(Set<SequenceDTO> sequences) {
        this.sequences = sequences;
    }


    public Set<AmplificationResult> getAmplificationResults() {
        return amplificationResults;
    }


    public void setAmplificationResults(Set<AmplificationResult> amplificationResults) {
        this.amplificationResults = amplificationResults;
    }


    public DnaQuality getDnaQuality() {
        return dnaQuality;
    }


    public void setDnaQuality(DnaQuality dnaQuality) {
        this.dnaQuality = dnaQuality;
    }


    public Set<SequenceDTO> getSequences() {
        return sequences;
    }


}
