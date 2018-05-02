/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import java.util.Set;

import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * A common interface for all identification keys.
 * @author a.mueller
 * @since 22.07.2009
 */
public interface IIdentificationKey {
	
    /**
     * The geographic scope of the key. The geographic scope is defined by the areas 
     * covered by this key.
     * @return
     */
    public Set<NamedArea> getGeographicalScope();
    public void addGeographicalScope(NamedArea geographicalScope);
    public void removeGeographicalScope(NamedArea geographicalScope);
    
    /**
     * Taxonomic Scope is the taxon which you want the key to be attached to e.g. the genus or whatever.
     * In some cases you might have e.g. a key with multiple roots (i.e. three sister genera with 
     * no parent made explicit), which would result in more than one taxon in the taxonomic scope set. 
     * @return set of taxa
     */
    public Set<Taxon> getTaxonomicScope();
    public void addTaxonomicScope(Taxon taxon);
    public void removeTaxonomicScope(Taxon taxon);
 
    
    /**
     * The scope of this key, this may be the sex, stage or any other character defined via a subclass
     * of {@link Scope}
     */
    public Set<DefinedTerm> getScopeRestrictions();
    public void addScopeRestriction(DefinedTerm scope);
    public void removeScopeRestriction(DefinedTerm scope);
   
    
    /**
     * Covered taxa are the tips of the key. If the key is a flora key, and the genus (scope of 
     * the key) has species which occur elsewhere, then there would be some child taxa of the
     * root which are not covered. Keys in monographs would usually cover all taxa belonging 
     * to the root taxon.
     * @return set of taxa
     */
    public Set<Taxon> getCoveredTaxa();
    public void addCoveredTaxon(Taxon taxon);
    public void removeCoveredTaxon(Taxon taxon);
}
