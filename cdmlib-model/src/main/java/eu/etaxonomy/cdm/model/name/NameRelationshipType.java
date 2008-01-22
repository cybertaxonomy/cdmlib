/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import org.apache.log4j.Logger;

import java.util.*;
import javax.persistence.*;

/**
 * http://rs.tdwg.org/ontology/voc/TaxonName.rdf#NomenclaturalNoteTypeTerm
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:38
 */
@Entity
public class NameRelationshipType extends RelationshipTermBase {
	static Logger logger = Logger.getLogger(NameRelationshipType.class);

	private static final UUID uuidOrthographicVariant = UUID.fromString("eeaea868-c4c1-497f-b9fe-52c9fc4aca53");
	private static final UUID uuidLaterHomonym = UUID.fromString("80f06f65-58e0-4209-b811-cb40ad7220a6");
	private static final UUID uuidTreatedAsLaterHomonym = UUID.fromString("2990a884-3302-4c8b-90b2-dfd31aaa2778");
	private static final UUID uuidAlternativeName = UUID.fromString("049c6358-1094-4765-9fae-c9972a0e7780");
	private static final UUID uuidBasionym = UUID.fromString("25792738-98de-4762-bac1-8c156faded4a");
	private static final UUID uuidReplacedSynonym = UUID.fromString("71c67c38-d162-445b-b0c2-7aba56106696");
	private static final UUID uuidConservedAgainst = UUID.fromString("e6439f95-bcac-4ebb-a8b5-69fa5ce79e6a");
	private static final UUID uuidValidatedByName = UUID.fromString("a176c9ad-b4c2-4c57-addd-90373f8270eb");
	private static final UUID uuidLaterValidatedByName = UUID.fromString("a25ee4c1-863a-4dab-9499-290bf9b89639");
	private static final UUID uuidBlockingNameFor = UUID.fromString("1dab357f-2e12-4511-97a4-e5153589e6a6");
	
	public NameRelationshipType() {
		super();
	}
	public NameRelationshipType(String term, String label, boolean symmetric, boolean transitive) {
		super(term, label, symmetric, transitive);
		// TODO Auto-generated constructor stub
	}


	@Transient
	public boolean isInvalidType(){
		//TODO: implement isX method. Maybe as persistent class attribute?
		//True, if enum is of type:
		//Invalid,
		//Nudum,
		//Provisional,
		//CombinationInvalid,
		//ValidatedByName,
		//LaterValidatedByName

		return false;
	}

	@Transient
	public boolean isLegitimateType(){
		//TODO: implement isX method. Maybe as persistent class attribute?
		//True, if enum is of type:
		//Legitimate,
		//Basionym,
		//ReplacedSynonym,
		//Novum,
		//AlternativeName,
		//Alternativ,
		//ConservedAgainst,
		//Conserved,
		//OrthographyConserved,
		//RejectedProp,
		//UtiqueRejectedProp

		return false;
	}

	@Transient
	public boolean isIllegitimateType(){
		//TODO: implement isX method. Maybe as persistent class attribute?
		//True, if enum is of type:
		//Illegitimate,
		//Superfluous,
		//LaterHomonym,
		//TreatedAsLaterHomonym,
		//RejectedInFavour,
		//Rejected,
		//UtiqueRejected,
		//ConservedProp,
		//OrthographyConservedProp

		return false;
	}

	
	public static final NameRelationshipType ORTHOGRAPHIC_VARIANT(){
		  return (NameRelationshipType)findByUuid(uuidOrthographicVariant);
		}
		public static final NameRelationshipType LATER_HOMONYM(){
		  return (NameRelationshipType)findByUuid(uuidLaterHomonym);
		}
		public static final NameRelationshipType TREATED_AS_LATER_HOMONYM(){
		  return (NameRelationshipType)findByUuid(uuidTreatedAsLaterHomonym);
		}
		public static final NameRelationshipType ALTERNATIVE_NAME(){
		  return (NameRelationshipType)findByUuid(uuidAlternativeName);
		}
		public static final NameRelationshipType BASIONYM(){
		  return (NameRelationshipType)findByUuid(uuidBasionym);
		}
		public static final NameRelationshipType REPLACED_SYNONYM(){
		  return (NameRelationshipType)findByUuid(uuidReplacedSynonym);
		}
		public static final NameRelationshipType CONSERVED_AGAINST(){
		  return (NameRelationshipType)findByUuid(uuidConservedAgainst);
		}
		public static final NameRelationshipType VALIDATED_BY_NAME(){
		  return (NameRelationshipType)findByUuid(uuidValidatedByName);
		}
		public static final NameRelationshipType LATER_VALIDATED_BY_NAME(){
		  return (NameRelationshipType)findByUuid(uuidLaterValidatedByName);
		}
		public static final NameRelationshipType BLOCKING_NAME_FOR(){
		  return (NameRelationshipType)findByUuid(uuidBlockingNameFor);
		}

}