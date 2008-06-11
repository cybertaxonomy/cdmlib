/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import org.apache.log4j.Logger;
import javax.persistence.*;
import java.util.UUID;


/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:17
 * http://rs.tdwg.org/ontology/voc/TaxonConcept#TaxonRelationshipTerm
 */
@Entity
public class TaxonRelationshipType extends RelationshipTermBase<TaxonRelationshipType> {
	static Logger logger = Logger.getLogger(TaxonRelationshipType.class);

	private static final UUID uuidTaxonomicallyIncludedIn = UUID.fromString("d13fecdf-eb44-4dd7-9244-26679c05df1c");
	private static final UUID uuidMisappliedNameFor = UUID.fromString("1ed87175-59dd-437e-959e-0d71583d8417");
	private static final UUID uuidInvalidDesignationFor = UUID.fromString("605b1d01-f2b1-4544-b2e0-6f08def3d6ed");
	private static final UUID uuidContradiction = UUID.fromString("a8f03491-2ad6-4fae-a04c-2a4c117a2e9b");
	private static final UUID uuidCongruentTo = UUID.fromString("60974c98-64ab-4574-bb5c-c110f6db634d");
	private static final UUID uuidIncludes = UUID.fromString("0501c385-cab1-4fbe-b945-fc747419bb13");
	private static final UUID uuidOverlaps = UUID.fromString("2046a0fd-4fd6-45a1-b707-2b91547f3ec7");
	private static final UUID uuidExcludes = UUID.fromString("4535a63c-4a3f-4d69-9350-7bf02e2c23be");
	private static final UUID uuidDoesNotExclude = UUID.fromString("0e5099bb-87c0-400e-abdc-bcfed5b5eece");
	private static final UUID uuidDoesNotOverlap = UUID.fromString("ecd2382b-3d94-4169-9dd2-2c4ea1d24605");
	private static final UUID uuidNotIncludedIn = UUID.fromString("89dffa4e-e004-4d42-b0d1-ae1827529e43");
	private static final UUID uuidNotCongruentTo = UUID.fromString("6c16c33b-cfc5-4a00-92bd-a9f9e448f389");
	
	public TaxonRelationshipType() {
		super();
	}
	public TaxonRelationshipType(String term, String label, String labelAbbrev, boolean symmetric, boolean transitive) {
		super(term, label, labelAbbrev, symmetric, transitive);
	}

	public static final TaxonRelationshipType getByUuid(UUID uuid){
		return (TaxonRelationshipType) findByUuid(uuid);
	}


	public static final TaxonRelationshipType TAXONOMICALLY_INCLUDED_IN(){
		return getByUuid(uuidTaxonomicallyIncludedIn);
	}
	public static final TaxonRelationshipType MISAPPLIEDNAMEFOR(){
		return (TaxonRelationshipType)findByUuid(uuidMisappliedNameFor);
	}
	public static final TaxonRelationshipType INVALIDDESIGNATIONFOR(){
		return (TaxonRelationshipType)findByUuid(uuidInvalidDesignationFor);
	}
	public static final TaxonRelationshipType CONTRADICTION(){
		return (TaxonRelationshipType)findByUuid(uuidContradiction);
	}
	public static final TaxonRelationshipType CONGRUENTTO(){
		return (TaxonRelationshipType)findByUuid(uuidCongruentTo);
	}
	public static final TaxonRelationshipType INCLUDES(){
		return (TaxonRelationshipType)findByUuid(uuidIncludes);
	}
	public static final TaxonRelationshipType OVERLAPS(){
		return (TaxonRelationshipType)findByUuid(uuidOverlaps);
	}
	public static final TaxonRelationshipType EXCLUDES(){
		return (TaxonRelationshipType)findByUuid(uuidExcludes);
	}
	public static final TaxonRelationshipType DOESNOTEXCLUDE(){
		return (TaxonRelationshipType)findByUuid(uuidDoesNotExclude);
	}
	public static final TaxonRelationshipType DOESNOTOVERLAP(){
		return (TaxonRelationshipType)findByUuid(uuidDoesNotOverlap);
	}
	public static final TaxonRelationshipType NOTINCLUDEDIN(){
		return (TaxonRelationshipType)findByUuid(uuidNotIncludedIn);
	}
	public static final TaxonRelationshipType NOTCONGRUENTTO(){
		return (TaxonRelationshipType)findByUuid(uuidNotCongruentTo);
	}


}