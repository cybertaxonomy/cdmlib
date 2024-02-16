/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

import java.util.UUID;

import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.ref.TypedEntityReference;

/**
 * @author muellera
 * @since 16.02.2024
 */
public class SingleReadDTO extends TypedEntityReference<SingleRead>{

    private static final long serialVersionUID = -631770022738397847L;

    private PrimerDTO primer;

    private String sequence;

    private MediaDTO pherogram;

    private String direction;

    //MaterialOrMethodEvent
    private String materialOrMethod;

    public SingleReadDTO(Class<SingleRead> type, UUID uuid, String label) {
        super(type, uuid, label);
    }

    public PrimerDTO getPrimer() {
        return primer;
    }
    public void setPrimer(PrimerDTO primer) {
        this.primer = primer;
    }

    public String getSequence() {
        return sequence;
    }
    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public MediaDTO getPherogram() {
        return pherogram;
    }
    public void setPherogram(MediaDTO pherogram) {
        this.pherogram = pherogram;
    }

    public String getDirection() {
        return direction;
    }
    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getMaterialOrMethod() {
        return materialOrMethod;
    }
    public void setMaterialOrMethod(String materialOrMethod) {
        this.materialOrMethod = materialOrMethod;
    }
}