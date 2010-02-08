/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.faunaEuropaea;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author a.babadshanjan
 * @created 13.09.2009
 * @version 1.0
 */
public class FaunaEuropaeaReferenceTaxon {
	
	private UUID taxonUuid;
//	private int taxonId;
	private Set<FaunaEuropaeaReference> references;	
	
	/**
	 * @param references
	 */
	public FaunaEuropaeaReferenceTaxon() {
		this.references = new HashSet<FaunaEuropaeaReference>();
	}
	/**
	 * @param taxonUuid
	 */
	public FaunaEuropaeaReferenceTaxon(UUID taxonUuid) {
		this();
		this.taxonUuid = taxonUuid;
	}
	/**
	 * @return the taxonUuid
	 */
	public UUID getTaxonUuid() {
		return taxonUuid;
	}
	/**
	 * @param taxonUuid the taxonUuid to set
	 */
	public void setTaxonUuid(UUID taxonUuid) {
		this.taxonUuid = taxonUuid;
	}
	/**
	 * @return the distributions
	 */
	public Set<FaunaEuropaeaReference> getReferences() {
		return references;
	}
	/**
	 * @param distributions the distributions to set
	 */
	public void setReferences(Set<FaunaEuropaeaReference> references) {
		this.references = references;
	}
	
	public void addReference(FaunaEuropaeaReference fauEuReference) {
		references.add(fauEuReference);
	}
}
