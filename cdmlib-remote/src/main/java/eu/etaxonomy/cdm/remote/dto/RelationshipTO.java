
package eu.etaxonomy.cdm.remote.dto;


/**
 * RelationshipTO represents various classes of relationships in the cdm model:
 * {@link TaxonRelationshipType}, {@link NameRelationshipType} & {@link SynonymRelationshipType}.
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 07.02.2008 17:06:31
 *
 */
public class RelationshipTO extends LocalisedTermTO {
	
	/**
	 * The uuid specifying the type of relationship
	 */
	private String typeUUID;  
}
