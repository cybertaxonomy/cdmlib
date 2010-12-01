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

import java.util.Set;

import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * 
 * @author a.mueller
 * @created 22.07.2009
 * @version 1.0
 */
public interface IIdentificationKey {
    public Set<NamedArea> getGeographicalScope();
    public void addGeographicalScope(NamedArea geographicalScope);
    public void removeGeographicalScope(NamedArea geographicalScope);
    public Set<Taxon> getTaxonomicScope();
    public void addTaxonomicScope(Taxon taxon);
    public void removeTaxonomicScope(Taxon taxon);
    public void addScopeRestriction(Scope scope);
    public void removeScopeRestriction(Scope scope);
    public Set<Scope> getScopeRestrictions();
    public Set<Taxon> getCoveredTaxa();
    public void addCoveredTaxon(Taxon taxon);
    public void removeCoveredTaxon(Taxon taxon);
}
