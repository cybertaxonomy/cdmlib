/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;



import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import javax.persistence.*;

/**
 * http://rs.tdwg.org/ontology/voc/TaxonRank#TaxonRankTerm
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:46
 */
@Entity
public class Rank extends OrderedTermBase {
	static Logger logger = Logger.getLogger(Rank.class);
	
	private static final UUID uuidEmpire = UUID.fromString("ac470211-1586-4b24-95ca-1038050b618d");
	private static final UUID uuidDomain = UUID.fromString("ffca6ec8-8b88-417b-a6a0-f7c992aac19b");
	private static final UUID uuidSuperKingdom = UUID.fromString("64223610-7625-4cfd-83ad-b797bf7f0edd");
	private static final UUID uuidKingdom = UUID.fromString("fbe7109d-66b3-498c-a697-c6c49c686162");
	private static final UUID uuidSubKingdom = UUID.fromString("a71bd9d8-f3ab-4083-afb5-d89315d71655");
	private static final UUID uuidInfraKingdom = UUID.fromString("1e37930c-86cf-44f6-90fd-7822928df260");
	private static final UUID uuidSuperPhylum = UUID.fromString("0d0cecb1-e254-4607-b210-6801e7ecbb04");
	private static final UUID uuidPhylum = UUID.fromString("773430d2-76b4-438c-b817-97a543a33287");

	private static final UUID uuidDivision = UUID.fromString("7e56f5cc-123a-4fd1-8cbb-6fd80358b581");

	private static final UUID uuidClass = UUID.fromString("f23d14c4-1d34-4ee6-8b4e-eee2eb9a3daf");

	private static final UUID uuidOrder = UUID.fromString("b0785a65-c1c1-4eb4-88c7-dbd3df5aaad1");

	private static final UUID uuidFamily = UUID.fromString("af5f2481-3192-403f-ae65-7c957a0f02b6");
	private static final UUID uuidSubFamily = UUID.fromString("862526ee-7592-4760-a23a-4ff3641541c5");

	private static final UUID uuidTribe = UUID.fromString("4aa6890b-0363-4899-8d7c-ee0cb78e6166");

	private static final UUID uuidGenus = UUID.fromString("1b11c34c-48a8-4efa-98d5-84f7f66ef43a");
	private static final UUID uuidSubGenus = UUID.fromString("78786e16-2a70-48af-a608-494023b91904");
	private static final UUID uuidInfraGenus = UUID.fromString("a9972969-82cd-4d54-b693-a096422f13fa");
	private static final UUID uuidSection = UUID.fromString("3edff68f-8527-49b5-bf91-7e4398bb975c");
	
	private static final UUID uuidSpeciesAggregate = UUID.fromString("1ecae058-4217-4f75-9c27-6d8ba099ac7a");
	private static final UUID uuidInfraGenericTaxon = UUID.fromString("41bcc6ac-37d3-4fd4-bb80-3cc5b04298b9");
	private static final UUID uuidSpecies = UUID.fromString("b301f787-f319-4ccc-a10f-b4ed3b99a86d");
	
	private static final UUID uuidSubSpecies = UUID.fromString("462a7819-8b00-4190-8313-88b5be81fad5");
	private static final UUID uuidInfraSpecies = UUID.fromString("f28ebc9e-bd50-4194-9af1-42f5cb971a2c");
	private static final UUID uuidVariety = UUID.fromString("d5feb6a5-af5c-45ef-9878-bb4f36aaf490");
	private static final UUID uuidBioVariety = UUID.fromString("a3a364cb-1a92-43fc-a717-3c44980a0991");
	
	public Rank() {
		// TODO Auto-generated constructor stub
			super();
	}

	public Rank(String term, String label) {
		super(term, label);
		// TODO Auto-generated constructor stub
	}

	public static final Rank EMPIRE(){
		return (Rank)findByUuid(uuidEmpire);
	}

	public static final Rank DOMAIN(){
		return (Rank)findByUuid(uuidDomain);
	}

	public static final Rank SUPER_KINGDOM(){
		return (Rank)findByUuid(uuidSuperKingdom);
	}

	public static final Rank KINGDOM(){
		return (Rank)findByUuid(uuidKingdom);
	}

	public static final Rank SUBKINGDOM(){
		return (Rank)findByUuid(uuidSubKingdom);
	}

	public static final Rank INFRAKINGDOM(){
		return (Rank)findByUuid(uuidInfraKingdom);
	}

	public static final Rank SUPERPHYLUM(){
		return (Rank)findByUuid(uuidSuperPhylum);
	}

	public static final Rank PHYLUM(){
		return (Rank)findByUuid(uuidPhylum);
	}

	public static final Rank SUBPHYLUM(){
		return null;
	}

	public static final Rank INFRAPHYLUM(){
		return null;
	}

	public static final Rank SUPERDIVISION(){
		return null;
	}

	public static final Rank DIVISION(){
		return (Rank)findByUuid(uuidDivision);
	}

	public static final Rank SUBDIVISION(){
		return null;
	}

	public static final Rank INFRADIVISION(){
		return null;
	}

	public static final Rank SUPERCLASS(){
		return null;
	}

	public static final Rank CLASS(){
		return (Rank)findByUuid(uuidClass);
	}

	public static final Rank SUBCLASS(){
		return null;
	}

	public static final Rank INFRACLASS(){
		return null;
	}

	public static final Rank SUPERORDER(){
		return null;
	}

	public static final Rank ORDER(){
		return (Rank)findByUuid(uuidOrder);
	}

	public static final Rank SUBORDER(){
		return null;
	}

	public static final Rank INFRAORDER(){
		return null;
	}

	public static final Rank SUPERFAMILY(){
		return null;
	}

	public static final Rank FAMILY(){
		return (Rank)findByUuid(uuidFamily);
	}

	public static final Rank SUBFAMILY(){
		return (Rank)findByUuid(uuidSubFamily);
	}

	public static final Rank INFRAFAMILY(){
		return null;
	}

	public static final Rank SUPERTRIBE(){
		return null;
	}

	public static final Rank TRIBE(){
		return (Rank)findByUuid(uuidTribe);
	}

	public static final Rank SUBTRIBE(){
		return null;
	}

	public static final Rank INFRATRIBE(){
		return null;
	}

	public static final Rank SUPRAGENERIC_TAXON(){
		return null;
	}

	public static final Rank GENUS(){
		return (Rank)findByUuid(uuidGenus);
	}

	public static final Rank SUBGENUS(){
		return (Rank)findByUuid(uuidSubGenus);
	}

	public static final Rank INFRAGENUS(){
		return (Rank)findByUuid(uuidInfraGenus);
	}

	public static final Rank SECTION(){
		return (Rank)findByUuid(uuidSection);
	}

	public static final Rank SUBSECTION(){
		return null;
	}

	public static final Rank SERIES(){
		return null;
	}

	public static final Rank SUBSERIES(){
		return null;
	}

	public static final Rank SPECIES_AGGREGATE(){
		return (Rank)findByUuid(uuidSpeciesAggregate);
	}

	public static final Rank INFRAGENERIC_TAXON(){
		return (Rank)findByUuid(uuidInfraGenericTaxon);
	}

	public static final Rank SPECIES(){
		return (Rank)findByUuid(uuidSpecies);
	}

	public static final Rank SUBSPECIFIC_AGGREGATE(){
		return null;
	}

	public static final Rank SUBSPECIES(){
		return (Rank)findByUuid(uuidSubSpecies);
	}
	
	public static final Rank INFRASPECIES(){
		return (Rank)findByUuid(uuidInfraSpecies);
	}

	public static final Rank VARIETY(){
		return (Rank)findByUuid(uuidVariety);
	}

	public static final Rank BIO_VARIETY(){
		return (Rank)findByUuid(uuidBioVariety);
	}

	public static final Rank PATHO_VARIETY(){
		return null;
	}

	public static final Rank SUBVARIETY(){
		return null;
	}

	public static final Rank SUBSUBVARIETY(){
		return null;
	}

	public static final Rank CONVAR(){
		return null;
	}

	public static final Rank FORM(){
		return null;
	}

	public static final Rank SPECIAL_FORM(){
		return null;
	}

	public static final Rank SUBFORM(){
		return null;
	}

	public static final Rank SUBSUBFORM(){
		return null;
	}

	public static final Rank INFRASPECIFIC_TAXON(){
		return null;
	}

	public static final Rank CANDIDATE(){
		return null;
	}

	public static final Rank DENOMINATION_CLASS(){
		return null;
	}

	public static final Rank GREX(){
		return null;
	}

	public static final Rank GRAFT_CHIMAERA(){
		return null;
	}

	public static final Rank CULTIVAR_GROUP(){
		return null;
	}

	public static final Rank CULTIVAR(){
		return null;
	}

}