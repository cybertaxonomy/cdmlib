// $Id$
/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.model.description;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.cache.description.PolytomousKeyDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.generate.PolytomousKeyGenerator;

/**
 * This class represents a fixed single-access key (dichotomous or
 * polytomous) used to identify (assign a {@link Taxon taxon} to) a {@link SpecimenOrObservationBase
 * specimen or observation}. The key may be written manually or may be generated automatically
 * e.g. by the {@link PolytomousKeyGenerator}. The different paths to the taxa are expressed
 * by a decision graph consisting of {@link PolytomousKeyNode
 * PolytomousKeyNodes}. The root node of such graph is accessible by
 * {@link #getRoot()}. Refer to {@link PolytomousKeyNode} for detailed
 * documentation on the decision graph structure.
 *
 * @author h.fradin
 * @created 13.08.2009
 *
 * @author a.mueller
 * @version 2.0 (08.11.2010)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PolytomousKey", propOrder = {
        "coveredTaxa",
        "taxonomicScope",
        "geographicalScope",
        "scopeRestrictions",
        "root",
        "startNumber"})
@XmlRootElement(name = "PolytomousKey")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.description.PolytomousKey")
@Audited
public class PolytomousKey extends IdentifiableEntity<PolytomousKeyDefaultCacheStrategy> implements IIdentificationKey {
    private static final long serialVersionUID = -3368243754557343942L;
    private static final Logger logger = Logger.getLogger(PolytomousKey.class);

    @XmlElementWrapper(name = "CoveredTaxa")
    @XmlElement(name = "CoveredTaxon")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
    @NotNull
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    private Set<Taxon> coveredTaxa = new HashSet<Taxon>();

    @XmlElementWrapper(name = "TaxonomicScope")
    @XmlElement(name = "Taxon")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "PolytomousKey_Taxon", joinColumns = @JoinColumn(name = "polytomousKey_id"), inverseJoinColumns = @JoinColumn(name = "taxon_id"))
    @NotNull
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    private Set<Taxon> taxonomicScope = new HashSet<Taxon>();

    @XmlElementWrapper(name = "GeographicalScope")
    @XmlElement(name = "Area")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "PolytomousKey_NamedArea")
    @NotNull
    @Cascade({CascadeType.MERGE})
    private Set<NamedArea> geographicalScope = new HashSet<NamedArea>();

    @XmlElementWrapper(name = "ScopeRestrictions")
    @XmlElement(name = "Restriction")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "PolytomousKey_Scope")
    @NotNull
    @Cascade({CascadeType.MERGE})
    private Set<DefinedTerm> scopeRestrictions = new HashSet<DefinedTerm>();

    @XmlElement(name = "Root")
    @OneToOne(fetch = FetchType.LAZY)
    @Cascade({ CascadeType.SAVE_UPDATE, CascadeType.MERGE })
    private PolytomousKeyNode root;

    @XmlElement(name = "StartNumber")
    @Audited
    private int startNumber = 1;


// ***************** STATIC METHODS ********************************/

    /**
     * Creates a new empty identification multi-access key instance.
     */
    public static PolytomousKey NewInstance() {
        return new PolytomousKey();
    }

    /**
     * Creates a new empty identification polytomous key instance.
     */
    public static PolytomousKey NewTitledInstance(String title) {
        PolytomousKey result = new PolytomousKey();
        result.setTitleCache(title, true);
        return result;
    }

// ************************** CONSTRUCTOR ************************/

    /**
     * Class constructor: creates a new empty multi-access key instance.
     */
    protected PolytomousKey() {
        super();
        root = PolytomousKeyNode.NewInstance();
        root.setNodeNumber(getStartNumber());
        root.setKey(this);
        this.cacheStrategy = PolytomousKeyDefaultCacheStrategy.NewInstance();
    }

    // ************************ GETTER/ SETTER

    /**
     * Returns the topmost {@link PolytomousKeyNode polytomous key node} (root
     * node) of <i>this</i> polytomous key. The root node does not have any
     * parent. Since polytomous key nodes recursively point to their child nodes
     * the complete polytomous key is defined by its root node.
     */
    public PolytomousKeyNode getRoot() {
        return root;
    }

    /**
     * This method should be used by Hibernate only. If we want to make this
     * method public we have to think about biderionality and also what should
     * happen with the old root node.
     *
     * @see #getRoot()
     */
    public void setRoot(PolytomousKeyNode root) {
        this.root = root;
        if (root != null){
            root.setKey(this);
        }
    }

    /**
     * Returns the set of possible {@link Taxon taxa} corresponding to
     * <i>this</i> identification key.
     */
    @Override
    public Set<Taxon> getCoveredTaxa() {
        if (coveredTaxa == null) {
            this.coveredTaxa = new HashSet<Taxon>();
        }
        return coveredTaxa;
    }

    /**
     * @see #getCoveredTaxa()
     */
    protected void setCoveredTaxa(Set<Taxon> coveredTaxa) {
        this.coveredTaxa = coveredTaxa;
    }

    /**
     * Adds a {@link Taxon taxa} to the set of {@link #getCoveredTaxa() covered
     * taxa} corresponding to <i>this</i> identification key.
     *
     * @param taxon
     *            the taxon to be added to <i>this</i> identification key
     * @see #getCoveredTaxa()
     */
    @Override
    public void addCoveredTaxon(Taxon taxon) {
        this.coveredTaxa.add(taxon);
    }

    /**
     * Removes one element from the set of {@link #getCoveredTaxa() covered
     * taxa} corresponding to <i>this</i> identification key.
     *
     * @param taxon
     *            the taxon which should be removed
     * @see #getCoveredTaxa()
     * @see #addCoveredTaxon(Taxon)
     */
    @Override
    public void removeCoveredTaxon(Taxon taxon) {
        this.coveredTaxa.remove(taxon);
    }

    /**
     * Returns the set of {@link NamedArea named areas} indicating the
     * geospatial data where <i>this</i> identification key is valid.
     */
    @Override
    public Set<NamedArea> getGeographicalScope() {
        if (geographicalScope == null) {
            this.geographicalScope = new HashSet<NamedArea>();
        }
        return geographicalScope;
    }

    /**
     * Adds a {@link NamedArea geoScope} to the set of {@link #getGeoScopes()
     * geogspatial scopes} corresponding to <i>this</i> identification key.
     *
     * @param geoScope
     *            the named area to be added to <i>this</i> identification key
     * @see #getGeoScopes()
     */
    @Override
    public void addGeographicalScope(NamedArea geoScope) {
        this.geographicalScope.add(geoScope);
    }

    /**
     * Removes one element from the set of {@link #getGeoScopes() geogspatial
     * scopes} corresponding to <i>this</i> identification key.
     *
     * @param geoScope
     *            the named area which should be removed
     * @see #getGeoScopes()
     * @see #addGeoScope(NamedArea)
     */
    @Override
    public void removeGeographicalScope(NamedArea geoScope) {
        this.geographicalScope.remove(geoScope);
    }

    /**
     * Returns the set of {@link Taxon taxa} that define the taxonomic scope of
     * <i>this</i> identification key
     */
    @Override
    public Set<Taxon> getTaxonomicScope() {
        if (taxonomicScope == null) {
            this.taxonomicScope = new HashSet<Taxon>();
        }
        return taxonomicScope;
    }

    /**
     * Adds a {@link Taxon taxa} to the set of {@link #getTaxonomicScope()
     * taxonomic scopes} corresponding to <i>this</i> identification key.
     *
     * @param taxon
     *            the taxon to be added to <i>this</i> identification key
     * @see #getTaxonomicScope()
     */
    @Override
    public void addTaxonomicScope(Taxon taxon) {
        this.taxonomicScope.add(taxon);
    }

    /**
     * Removes one element from the set of {@link #getTaxonomicScope() taxonomic
     * scopes} corresponding to <i>this</i> identification key.
     *
     * @param taxon
     *            the taxon which should be removed
     * @see #getTaxonomicScope()
     * @see #addTaxonomicScope(Taxon)
     */
    @Override
    public void removeTaxonomicScope(Taxon taxon) {
        this.taxonomicScope.remove(taxon);
    }

    /**
     * Returns the set of {@link Scope scope restrictions} corresponding to
     * <i>this</i> identification key
     */
    @Override
    public Set<DefinedTerm> getScopeRestrictions() {
        if (scopeRestrictions == null) {
            this.scopeRestrictions = new HashSet<DefinedTerm>();
        }
        return scopeRestrictions;
    }

    /**
     * Adds a {@link Scope scope restriction} to the set of
     * {@link #getScopeRestrictions() scope restrictions} corresponding to
     * <i>this</i> identification key.
     *
     * @param scopeRestriction
     *            the scope restriction to be added to <i>this</i>
     *            identification key
     * @see #getScopeRestrictions()
     */
    @Override
    public void addScopeRestriction(DefinedTerm scopeRestriction) {
        this.scopeRestrictions.add(scopeRestriction);
    }

    /**
     * Removes one element from the set of {@link #getScopeRestrictions() scope
     * restrictions} corresponding to <i>this</i> identification key.
     *
     * @param scopeRestriction
     *            the scope restriction which should be removed
     * @see #getScopeRestrictions()
     * @see #addScopeRestriction(Scope)
     */
    @Override
    public void removeScopeRestriction(DefinedTerm scopeRestriction) {
        this.scopeRestrictions.remove(scopeRestriction);
    }


    /**
     * The first number for the automated numbering of {@link PolytomousKeyNode key nodes}.
     * Default value is 1.
     * @return
     */
    public int getStartNumber() {
        return startNumber;
    }

    /**
     * @see #getStartNumber()
     * @param startNumber
     */
    public void setStartNumber(int startNumber) {
        this.startNumber = startNumber;
    }

    // ******************** toString *****************************************/

    private class IntegerObject {
        int number = 0;

        int inc() {
            return number++;
        };

        @Override
        public String toString() {
            return String.valueOf(number);
        }
    }

    public String print(PrintStream stream) {
        String title = this.getTitleCache() + "\n";
        String strPrint = title;

        if (stream != null) {
            stream.print(title);
        }

        PolytomousKeyNode root = this.getRoot();
        strPrint += printNode(root, null, "  ", stream);
        return strPrint;
    }

    /**
     * TODO this is a preliminary implementation
     *
     * @param node
     * @param identation
     * @param no
     * @param myNumber
     * @param stream
     * @return
     */
    private String printNode(PolytomousKeyNode node, PolytomousKeyNode parent2,
            String identation, PrintStream stream) {
        String separator = ", ";

        String result = identation + node.getNodeNumber() + ". ";
        if (node != null) {
            // key choice
            String question = null;
            String feature = null;
            if (node.getQuestion() != null) {
                question = node.getQuestion().getLabelText(Language.DEFAULT());
            }
            if (node.getFeature() != null) {
                feature = node.getFeature().getLabel(Language.DEFAULT());
            }
            result += CdmUtils.concat(" - ", question, feature) + "\n";
            ;

            // Leads
            char nextCounter = 'a';
            for (PolytomousKeyNode child : node.getChildren()) {
                String leadNumber = String.valueOf(nextCounter++);
                if (child.getStatement() != null) {
                    String statement = child.getStatement().getLabelText(
                            Language.DEFAULT());
                    result += identation + "  " + leadNumber + ") "
                            + (statement == null ? "" : (statement));
                    result += " ... ";
                    // child node
                    if (!child.isLeaf()) {
                        result += child.getNodeNumber() + separator;
                    }
                    // taxon
                    if (child.getTaxon() != null) {
                        String strTaxon = "";
                        if (child.getTaxon().getName() != null) {
                            strTaxon = child.getTaxon().getName()
                                    .getTitleCache();
                        } else {
                            strTaxon = child.getTaxon().getTitleCache();
                        }
                        result += strTaxon + separator;
                    }
                    // subkey
                    if (child.getSubkey() != null) {
                        String subkey = child.getSubkey().getTitleCache();
                        result += subkey + separator;
                    }
                    // other node
                    if (child.getOtherNode() != null) {
                        PolytomousKeyNode otherNode = child.getOtherNode();
                        String otherNodeString = null;
                        if (child.getKey().equals(otherNode.getKey())) {
                            otherNodeString = String.valueOf(otherNode
                                    .getNodeNumber());
                        } else {
                            otherNodeString = otherNode.getKey() + " "
                                    + otherNode.getNodeNumber();
                        }
                        result += otherNodeString + separator;
                    }

                    result = StringUtils.chomp(result, separator);
                    result += "\n";
                }
            }

            if (stream != null) {
                stream.print(result);
            }
            for (PolytomousKeyNode child : node.getChildren()) {
                if (!child.isLeaf()) {
                    result += printNode(child, node, identation + "", stream);
                }
            }
        }
        return result;
    }

    //
    // public List<PolytomousKeyNode> getChildren() {
    // return getRoot().getChildren();
    // }

    // *********************** CLONE ************************************/

    /**
     * Clones <i>this</i> PolytomousKey. This is a shortcut that enables to
     * create a new instance that differs only slightly from <i>this</i>
     * PolytomousKey by modifying only some of the attributes.
     *
     * @see eu.etaxonomy.cdm.model.common.IdentifiableEntity#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {
        PolytomousKey result;

        try {
            result = (PolytomousKey) super.clone();

            result.coveredTaxa = new HashSet<Taxon>();
            for (Taxon taxon : this.coveredTaxa) {
                result.addCoveredTaxon(taxon);
            }

            result.geographicalScope = new HashSet<NamedArea>();
            for (NamedArea area : this.geographicalScope) {
                result.addGeographicalScope(area);
            }

            result.root = (PolytomousKeyNode) this.root.clone();

            result.scopeRestrictions = new HashSet<DefinedTerm>();
            for (DefinedTerm scope : this.scopeRestrictions) {
                result.addScopeRestriction(scope);
            }

            result.taxonomicScope = new HashSet<Taxon>();
            for (Taxon taxon : this.taxonomicScope) {
                result.addTaxonomicScope(taxon);
            }

            return result;

        } catch (CloneNotSupportedException e) {
            logger.warn("Object does not implement cloneable");
            e.printStackTrace();
            return null;
        }

    }

}
