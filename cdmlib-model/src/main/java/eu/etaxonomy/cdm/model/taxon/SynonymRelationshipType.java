/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;

/**
 * (e.g. homotypic, heterotypic, proparte ...)
 * <P>
 * This class corresponds in part to: <ul>
 * <li> TaxonRelationshipTerm according to the TDWG ontology
 * <li> RelationshipType according to the TCS
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:55
 * http://rs.tdwg.org/ontology/voc/TaxonConcept#TaxonRelationshipTerm
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SynonymRelationshipType")
@Entity
public class SynonymRelationshipType extends RelationshipTermBase<SynonymRelationshipType> {
	
	static Logger logger = Logger.getLogger(SynonymRelationshipType.class);

	private static final UUID uuidSynonymOf = UUID.fromString("1afa5429-095a-48da-8877-836fa4fe709e");
	private static final UUID uuidProParteSynonymOf = UUID.fromString("130b752d-2eff-4a62-a132-104ed8d13e5e");
	private static final UUID uuidPartialSynonymOf = UUID.fromString("8b0d1d34-cc00-47cb-999d-b67f98d1af6e");
	private static final UUID uuidHomotypicSynonymOf = UUID.fromString("294313a9-5617-4ed5-ae2d-c57599907cb2");
	private static final UUID uuidHeterotypicSynonymOf = UUID.fromString("4c1e2c59-ca55-41ac-9a82-676894976084");

	
	public SynonymRelationshipType() {
		super();
	}

	public SynonymRelationshipType(String term, String label, String labelAbbrev, boolean symmetric, boolean transitive) {
		super(term, label, labelAbbrev, symmetric, transitive);
	}


	public static final SynonymRelationshipType getByUuid(UUID uuid){
		return (SynonymRelationshipType) findByUuid(uuid);
	}
	
	public static final SynonymRelationshipType SYNONYM_OF(){
		return getByUuid(uuidSynonymOf);
	}

	public static final SynonymRelationshipType PRO_PARTE_SYNONYM_OF(){
		return getByUuid(uuidProParteSynonymOf);
	}

	public static final SynonymRelationshipType PARTIAL_SYNONYM_OF(){
		return getByUuid(uuidPartialSynonymOf);
	}

	/**
	 * Returns the synonym relationship type "is homotypic synonym of". This
	 * indicates that the {@link name.TaxonNameBase taxon name} used as a {@link Synonym synonym}
	 * and the taxon name used as the ("accepted/correct") {@link Taxon taxon} belong
	 * to the same {@link name.HomotypicalGroup homotypical group}.
	 */
	public static final SynonymRelationshipType HOMOTYPIC_SYNONYM_OF(){
		return getByUuid(uuidHomotypicSynonymOf);
	}

	/**
	 * Returns the synonym relationship type "is heterotypic synonym of". This
	 * indicates that the {@link name.TaxonNameBase taxon name} used as a {@link Synonym synonym}
	 * and the taxon name used as the ("accepted/correct") {@link Taxon taxon} do not
	 * belong to the same {@link name.HomotypicalGroup homotypical group}.
	 */
	public static final SynonymRelationshipType HETEROTYPIC_SYNONYM_OF(){
		return getByUuid(uuidHeterotypicSynonymOf);
	}

}