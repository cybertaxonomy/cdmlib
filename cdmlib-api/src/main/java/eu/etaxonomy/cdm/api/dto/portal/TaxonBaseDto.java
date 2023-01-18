/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

import java.util.List;

import eu.etaxonomy.cdm.format.common.TypedLabel;

/**
 * @author a.mueller
 * @date 07.01.2023
 */
public class TaxonBaseDto extends CdmBaseDto {

    //TODO should we distinguish data parts (e.g. on general page we do not need last updates from synonymy)
    //lastUpdated
    private String taxonLabel;
    private String nameLabel;

    //TODO should we keep formatting client side or should we do formatting on server side? Formatting means: filter, italics, order??
    private List<TypedLabel> typedTaxonLabel;
    private List<TypedLabel> typedNameLabel;

    public String getTaxonLabel() {
        return taxonLabel;
    }
    public void setTaxonLabel(String taxonLabel) {
        this.taxonLabel = taxonLabel;
    }

    public String getNameLabel() {
        return nameLabel;
    }
    public void setNameLabel(String nameLabel) {
        this.nameLabel = nameLabel;
    }

    public List<TypedLabel> getTypedTaxonLabel() {
        return typedTaxonLabel;
    }
    public void setTypedTaxonLabel(List<TypedLabel> typedTaxonLabel) {
        this.typedTaxonLabel = typedTaxonLabel;
    }

    public List<TypedLabel> getTypedNameLabel() {
        return typedNameLabel;
    }
    public void setTypedNameLabel(List<TypedLabel> typedNameLabel) {
        this.typedNameLabel = typedNameLabel;
    }

    //TaxonBase info
    //appendedPhras, useNameCache, doubtful, name, publish
    // => should all be part of the typedLabel

    //secsource  ?? how to handle? part of bibliography

    //TaxonName info
    //TODO do we need
    //rank, nameparts => all in typedLabel


    //relatedNames  //as RelatedDTO?

    //types ?? => Teil der homotypischen Gruppe, außer der Fall von Walter (für  name types?)

}
