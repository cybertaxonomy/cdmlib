package eu.etaxonomy.cdm.model.agent;



import javax.persistence.Entity;
import eu.etaxonomy.cdm.model.common.IdentifyableMediaEntity;

/**
 * The upmost (abstract) class for agents such as persons, teams 
 * or institutions used in nomenclatural or bibliographical references
 * for taxa and for information associated to them.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:57
 */
@Entity
public abstract class Agent extends IdentifyableMediaEntity{
	

}
