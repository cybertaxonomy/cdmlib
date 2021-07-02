/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.taxon;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VersionableEntity;

/**
 * see https://dev.e-taxonomy.eu/redmine/issues/9692
 *
 * @author a.mueller
 * @since 02.07.2021
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonomicOperation", propOrder = {
    "type",
    "timePeriod"
})
@XmlRootElement(name = "TaxonomicOperation")
@Entity
@Audited
public class TaxonomicOperation extends VersionableEntity {

    private static final long serialVersionUID = 6044997707560990508L;

    @XmlAttribute(name ="TaxonomicOperationType")
    @NotNull
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumUserType",
        parameters = {@org.hibernate.annotations.Parameter(name="enumClass", value="eu.etaxonomy.cdm.model.taxon.TaxonomicOperationType")}
    )
    @Audited
    @Column(length=20)
    private TaxonomicOperationType type;

    private TimePeriod timePeriod = TimePeriod.NewInstance();

// *************************** FACTORY METHODS **************************/

    public static TaxonomicOperation NewInstance(TaxonomicOperationType type){
        return new TaxonomicOperation(type, null);
    }

// ************************ CONSTRUCTOR *******************************/

    @Deprecated  //for jaxb/hibernate use only
    private TaxonomicOperation(){}

    private TaxonomicOperation(TaxonomicOperationType type, TimePeriod timePeriod) {
        this.type = type;
        this.setTimePeriod(timePeriod);
    }

// ********************* GETTER / SETTER ********************************/

    public TaxonomicOperationType getType() {
        return type;
    }
    public void setType(TaxonomicOperationType type) {
        this.type = type;
    }

    public TimePeriod getTimePeriod() {
        return timePeriod;
    }
    public void setTimePeriod(TimePeriod timePeriod) {
        this.timePeriod = timePeriod;
    }
}