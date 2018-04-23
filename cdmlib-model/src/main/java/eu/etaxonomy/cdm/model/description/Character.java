/**
* Copyright (C) 2017 EDIT
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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.model.common.Language;

/**
 * A subclass of the Feature class that is meant for handling
 * Features/Characters for desriptions in the narrow sense of describing
 * an object.
 *
 * @author a.mueller
 \* @since 04.05.2017
 *
 * @deprecated This class is still experimental. It may be changed
 * or even deleted in future without notice.
 */

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name="Feature", factoryMethod="NewInstance", propOrder = {
        "structure",
        "property"
})
@XmlRootElement(name = "Feature")
@Entity
@Audited
@Deprecated
public class Character extends Feature {

    private static final long serialVersionUID = -5631282599057455256L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Feature.class);


    @XmlElement(name = "Structure")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @IndexedEmbedded
//    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private FeatureNode structure;

    @XmlElement(name = "Property")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @IndexedEmbedded
//    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private FeatureNode property;

/* ***************** CONSTRUCTOR AND FACTORY METHODS **********************************/


    public static Character NewInstance() {
        return new Character();
    }


    /**
     * Class constructor: creates a new character instance associated with
     * the given structure and property node
     *
     * @param structure The structure feature node for this character
     * @param property The property feature node for this character
     * @see #Feature()
     */
    public static Character NewInstance(FeatureNode structure, FeatureNode property){
        return new Character(structure, property, null, null, null);
    }


    /**
     * Class constructor: creates a new character instance associated with
     * the given structure and property node with a description
     * (in the {@link Language#DEFAULT() default language}), a label and a label
     * abbreviation.
     *
     * @param structure The structure feature node for this character
     * @param property The property feature node for this character
     * @param term
     *            the string (in the default language) describing the new
     *            feature to be created
     * @param label
     *            the string identifying the new feature to be created
     * @param labelAbbrev
     *            the string identifying (in abbreviated form) the new feature
     *            to be created
     * @see #Feature()
     */
    public static Character NewInstance(FeatureNode structure, FeatureNode property, String term, String label, String labelAbbrev){
        return new Character(structure, property, term, label, labelAbbrev);
    }


    //for hibernate use only
    @Deprecated
    protected Character() {
        super();
    }


    /**
     * Class constructor: creates a new character instance associated with
     * the given structure and property node with a description
     * (in the {@link Language#DEFAULT() default language}), a label and a label
     * abbreviation.
     *
     * @param structure The structure feature node for this character
     * @param property The property feature node for this character
     * @param term
     *            the string (in the default language) describing the new
     *            feature to be created
     * @param label
     *            the string identifying the new feature to be created
     * @param labelAbbrev
     *            the string identifying (in abbreviated form) the new feature
     *            to be created
     * @see #Feature()
     */
    protected Character(FeatureNode structure, FeatureNode property, String term, String label, String labelAbbrev) {
        super(term, label, labelAbbrev);
        this.structure = structure;
        this.property = property;
    }

 // ****************** GETTER / SETTER *********************************************/

    public FeatureNode getStructure() {
        return structure;
    }

    public FeatureNode getProperty() {
        return property;
    }
}
