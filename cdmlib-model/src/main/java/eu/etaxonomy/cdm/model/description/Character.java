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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermType;

/**
 * A subclass of the Feature class that is meant for handling
 * Features/Characters for descriptions in the narrow sense of describing
 * an object.
 *
 * @author a.mueller
 * @since 04.05.2017
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="Feature", factoryMethod="NewInstance", propOrder = {
        "structure",
        "structureModifier",
        "property",
        "propertyModifier"
})
@XmlRootElement(name = "Character")
@Entity
@Audited
public class Character extends Feature {

    private static final long serialVersionUID = -5631282599057455256L;
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @Deprecated
    @XmlElement(name = "Structure")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @IndexedEmbedded
//    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    /**
     * @deprecated the concept of structure modifiers has been abandoned (#10133)
     */
    private TermNode<DefinedTerm> structure;

    //#8120
    @XmlElement(name = "StructureModifier")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @IndexedEmbedded
    private DefinedTerm structureModifier;

    @XmlElement(name = "Property")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @IndexedEmbedded
//    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private TermNode<DefinedTerm> property;

    //#9507
    @XmlElement(name = "RatioToStructure")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @IndexedEmbedded
    private TermNode<DefinedTerm> ratioToStructure;

    //#8120
    /**
     * @deprecated experimental, may be removed in future
     */
    @Deprecated
    @XmlElement(name = "PropertyModifier")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @IndexedEmbedded
    private DefinedTerm propertyModifier;

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
    public static Character NewInstance(TermNode structure, TermNode property){
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
    public static Character NewInstance(TermNode structure, TermNode property, String term, String label, String labelAbbrev){
        return new Character(structure, property, term, label, labelAbbrev);
    }


    //for hibernate use only
    @Deprecated
    protected Character() {
        super();
        this.setTermType(TermType.Character);
        this.setSupportsTextData(false);
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
    protected Character(TermNode structure, TermNode property, String term, String label, String labelAbbrev) {
        super(term, label, labelAbbrev);
        this.setTermType(TermType.Character);
        this.structure = structure;
        this.property = property;
        this.setSupportsTextData(false);
    }

 // ****************** GETTER / SETTER *********************************************/

    public TermNode getStructure() {
        return structure;
    }
    public void setStructure(TermNode structure) {
        this.structure = structure;
    }

    public TermNode<DefinedTerm> getRatioToStructure() {
        return ratioToStructure;
    }
    public void setRatioToStructure(TermNode<DefinedTerm> ratioToStructure) {
        this.ratioToStructure = ratioToStructure;
    }

    public TermNode getProperty() {
        return property;
    }
    public void setProperty(TermNode property) {
        this.property = property;
    }

    /**
     * @deprecated the concept of structure modifiers has been abandoned (#10133)
     */
    @Deprecated
    public DefinedTerm getStructureModifier() {
        return structureModifier;
    }
    /**
     * @deprecated the concept of structure modifiers has been abandoned (#10133)
     */
    @Deprecated
    public void setStructureModifier(DefinedTerm structureModifier) {
        this.structureModifier = structureModifier;
    }

    /**
     * @return
     * @deprecated experimental, may be removed in future
     */
    @Deprecated
    public DefinedTerm getPropertyModifier() {
        return propertyModifier;
    }
    /**
     * @param propertyModifier
     * @deprecated experimental, may be removed in future
     */
    @Deprecated
    public void setPropertyModifier(DefinedTerm propertyModifier) {
        this.propertyModifier = propertyModifier;
    }

}
