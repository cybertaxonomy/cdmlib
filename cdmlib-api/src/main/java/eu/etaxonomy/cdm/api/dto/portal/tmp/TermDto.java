/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal.tmp;

import java.beans.Transient;
import java.util.Objects;
import java.util.UUID;

import eu.etaxonomy.cdm.api.dto.portal.IdentifiableDto;
import eu.etaxonomy.cdm.api.dto.portal.LabeledEntityDto;

/**
 * TODO should we inherit from  {@link IdentifiableDto}
 * @author muellera
 * @since 29.02.2024
 */
public class TermDto extends LabeledEntityDto {

    private String symbol1;
    private String symbol2;
    private String idInVocabulary;
    //TODO presenceAbsenceTerm specific
    private String defaultColor;
    //TODO currently used for PresenceAbsenceTerm comparison and ordering in map legend
    private int orderIndex = -1;
    //TODO currently used for PresenceAbsenceTerm symbol in CondensedDistribution if defined so
    private String abbrevLabel;

    public TermDto(UUID uuid, Integer id, String label) {
        super(uuid, id, label);
    }

    public String getSymbol1() {
        return symbol1;
    }
    public void setSymbol1(String symbol1) {
        this.symbol1 = symbol1;
    }

    public String getSymbol2() {
        return symbol2;
    }
    public void setSymbol2(String symbol2) {
        this.symbol2 = symbol2;
    }

    public String getIdInVocabulary() {
        return idInVocabulary;
    }
    public void setIdInVoc(String idInVocabulary) {
        this.idInVocabulary = idInVocabulary;
    }
    public void setIdInVocabulary(String idInVocabulary) {
        this.idInVocabulary = idInVocabulary;
    }

     public String getDefaultColor() {
        return defaultColor;
    }
    public void setDefaultColor(String defaultColor) {
        this.defaultColor = defaultColor;
    }

    @Transient
    //Note: is Integer to allow compareTo()
    public Integer getOrderIndex() {
        return orderIndex;
    }
    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex == null ? -1 : orderIndex;
    }

    public String getAbbrevLabel() {
        return abbrevLabel;
    }
    public void setAbbrevLabel(String abbrevLabel) {
        this.abbrevLabel = abbrevLabel;
    }

//******************* toString() ***********************/

    @Override
    public int hashCode() {
        return Objects.hash(this.getUuid());
    }

    //Required because TermDto is sometimes used as key in a map.
    //Maybe we should switch this by using the uuid instead or find
    //another solution
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TermDto other = (TermDto) obj;
        return Objects.equals(this.getUuid(), other.getUuid());
    }
}