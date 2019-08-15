/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.description;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermNode;

/**
 * A class representing a state for a given feature. As instances of {@link State state}
 * are reusable they may represent multiple states in e.g. a given feature tree.
 * By handling them as a pair with feature it is expected to be explicit within a certain
 * feature tree branch and therefore can be used to define values like {@link TermNode#getInapplicableIf()}
 * or {@link TermNode#getOnlyApplicableIf()}.
 *
 * @author a.mueller
 * @since 08.08.2019
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="FeatureState", factoryMethod="NewInstance", propOrder = {
        "feature",
        "state"
})
@XmlRootElement(name = "FeatureState")
@Entity
@Audited
public class FeatureState extends VersionableEntity {

    private static final long serialVersionUID = -421832597710084356L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(FeatureState.class);

    @XmlElement(name = "Feature")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY, targetEntity=DefinedTermBase.class)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @NotNull
    private Feature feature;

    @XmlElement(name = "State")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY, targetEntity=DefinedTermBase.class)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @NotNull
    private State state;

//*************** CONSTRUCTOR AND FACTORY METHODS **********************************/

    public static FeatureState NewInstance() {
        return new FeatureState();
    }

    public static FeatureState NewInstance(Feature feature, State state){
        return new FeatureState(feature, state);
    }


    //for hibernate use only
    @Deprecated
    protected FeatureState() {}

    protected FeatureState(Feature feature, State state) {
        this.feature = feature;
        this.state = state;
    }


/* *************************************************************************************/

    public Feature getFeature() {
        return feature;
    }
    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public State getState() {
        return state;
    }
    public void setState(State state) {
        this.state = state;
    }


// ******************************* TO STRING *******************************************/
    @Override
    public String toString() {
        return "FeatureState [feature=" + feature + ", state=" + state + "]";
    }

//*********************************** CLONE ********************************************/


    @Override
    public Object clone() throws CloneNotSupportedException {
        FeatureState result = (FeatureState)super.clone();

        //no changes to: feature, state
        return result;
    }

}
