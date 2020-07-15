/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.model.common.ExtendedTimePeriod;

/**
 * This class represents a temporal fact .
 * A temporal fact handles facts which primarily define certain time periods
 * like seasons.
 *
 * @author a.mueller
 * @since 29-Apr-2020
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TemporalData", propOrder = {
    "period",
})
@XmlRootElement(name = "TemporalData")
@Entity
@Audited
@Indexed(index = "eu.etaxonomy.cdm.model.description.DescriptionElementBase")
public class TemporalData extends DescriptionElementBase {

    private static final long serialVersionUID = -1064249780729501786L;
    private static final Logger logger = Logger.getLogger(TemporalData.class);

    @XmlElement(name = "Period")
    private ExtendedTimePeriod period = ExtendedTimePeriod.NewExtendedInstance();

    public static TemporalData NewInstance(){
        TemporalData result = new TemporalData();
        return result;
    }

    /**
     * Creates a temporal fact with the given period.
     */
    public static TemporalData NewInstance(ExtendedTimePeriod period){
        TemporalData result = new TemporalData();
        result.setPeriod(period);
        return result;
    }

    public static TemporalData NewInstance(Feature feature){
        TemporalData result = new TemporalData();
        result.setFeature(feature);
        return result;
    }

    public static TemporalData NewInstance(Feature feature, ExtendedTimePeriod period){
        TemporalData result = new TemporalData();
        result.setFeature(feature);
        result.setPeriod(period);
        return result;
    }

// *************************** CONSTRUCTOR *************************************/

    /**
     * Class constructor: creates a new empty common name instance.
     * The corresponding {@link Feature feature} is set to {@link Feature#COMMON_NAME() COMMON_NAME}.
     */
    protected TemporalData(){}

// *************************** METHODS *****************************************/

    public ExtendedTimePeriod getPeriod() {
        return period;
    }
    public void setPeriod(ExtendedTimePeriod period) {
        this.period = period;
    }

//*********************************** CLONE *****************************************/

    /**
     * Clones <i>this</i> common name. This is a shortcut that enables to create
     * a new instance that differs only slightly from <i>this</i> common name by
     * modifying only some of the attributes.
     *
     * @see eu.etaxonomy.cdm.model.description.DescriptionElementBase#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public TemporalData clone() {

        try {
            TemporalData result = (TemporalData)super.clone();
            result.setPeriod(this.period.clone());
            return result;
            //no changes to ...
        } catch (CloneNotSupportedException e) {
            logger.warn("Object does not implement cloneable");
            e.printStackTrace();
            return null;
        }
    }

//*********************************** toString *****************************************/

    @Override
    public String toString(){
        if (period != null){
            return period.toString();
        }else{
            return super.toString();
        }
    }
}
