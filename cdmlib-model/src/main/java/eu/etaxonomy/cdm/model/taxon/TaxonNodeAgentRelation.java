/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.taxon;

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
import org.springframework.security.core.GrantedAuthority;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.TermType;

/**
 * This class relates a TaxonNode (Taxon within it's given publication context)
 * to an agent (person or team) and defining a type for this relationship.
 * This is to indicate that an agent plays a certain role for this taxon
 * (e.g. author of the according subtree, last scrutiny, ...).
 * It is not meant to define rights and roles which are only handled via the
 * {@link GrantedAuthority granted authorities}.
 * @author a.mueller
 \* @since 29.05.2015
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonNodeAgentRelation", propOrder = {
        "taxonNode",
        "agent",
        "type",
    })
@XmlRootElement(name = "TaxonNodeAgentRelation")
@Entity
@Audited
public class TaxonNodeAgentRelation extends AnnotatableEntity {
    private static final long serialVersionUID = -1476342569350403356L;
    private static final Logger logger = Logger.getLogger(TaxonNodeAgentRelation.class);

    @XmlElement(name = "TaxonNode")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
//    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})  //the
    @NotNull
    private TaxonNode taxonNode;

    @XmlElement(name = "Agent")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    @NotNull
    private TeamOrPersonBase<?> agent;

    @XmlElement(name = "Type")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.EAGER)
    private DefinedTerm type;

    protected static TaxonNodeAgentRelation NewInstance(TaxonNode taxonNode, TeamOrPersonBase<?> agent, DefinedTerm type){
        TaxonNodeAgentRelation result = new TaxonNodeAgentRelation();
        result.taxonNode = taxonNode;
        result.agent = agent;
        result.setType(type);
        taxonNode.addAgentRelation(result);
        return result;
    }

    private TaxonNodeAgentRelation(){}

//********************* GETTER / SETTER **********************/

    public TaxonNode getTaxonNode() {
        return taxonNode;
    }
    protected void setTaxonNode(TaxonNode taxonNode) {
        this.taxonNode = taxonNode;
    }

    public TeamOrPersonBase<?> getAgent() {
        return agent;
    }
    public void setAgent(TeamOrPersonBase<?> agent) {
        this.agent = agent;
    }

    public DefinedTerm getType() {
        return type;
    }
    public void setType(DefinedTerm type) {
        if (type != null && type.getTermType() != TermType.TaxonNodeAgentRelationType){
            throw new IllegalArgumentException("Only TaxonNode Agent Relation Type terms are allowed as TaxonNodeAgentRelation.type");
        }
        this.type = type;
    }

//************************ to String **********************************/


    @Override
    public String toString() {
        return "TaxonNodeAgentRelation [taxonNode=" + taxonNode +
                ", agent=" + agent + ", type=" + type + "]";
    }


//************************** clone *******************************************/

    /**
     * Clones <i>this</i> taxon node agent relation. This is a shortcut that enables to create
     * a new instance that differs only slightly from <i>this</i> relation.
     *
     * @see eu.etaxonomy.cdm.model.media.IdentifiableEntity#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone()  {
        try{
            TaxonNodeAgentRelation result = (TaxonNodeAgentRelation)super.clone();

            //no change to taxonNode, agent, type
            return result;
        }catch (CloneNotSupportedException e) {
            logger.warn("Object does not implement cloneable");
            e.printStackTrace();
            return null;
        }
    }

}
