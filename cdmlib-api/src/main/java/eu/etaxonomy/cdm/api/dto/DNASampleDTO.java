/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.molecular.AmplificationResult;
import eu.etaxonomy.cdm.model.molecular.DnaQuality;

/**
 * @author k.luther
 * @since 02.08.2018
 */
public class DNASampleDTO extends DerivedUnitDTO {

    private static final long serialVersionUID = -1050277075084265067L;

    private Set<SequenceDTO> sequences = new HashSet<>();

    private Set<AmplificationResult> amplificationResults = new HashSet<>();

    private DnaQuality dnaQuality;

    //TODO
    public DNASampleDTO(Class type, UUID uuid, String label) {
        super(type, uuid, label);
    }

    public Set<SequenceDTO> getSequences() {
        return sequences;
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

    @Override
    public boolean isHasDna() {
        return true;
    }
}