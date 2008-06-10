/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;



import javax.persistence.Entity;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;

import eu.etaxonomy.cdm.model.media.IdentifyableMediaEntity;

/**
 * The upmost (abstract) class for agents such as persons, teams or institutions.
 * An agent is a conscious entity which can take decisions, act and create
 * according to its own knowledge and goals and which may be approached.
 * Agents can be authors for nomenclatural or bibliographical references as well
 * as creators of pictures or field collectors or administrators of collections.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:57
 */
@Entity
@Table(appliesTo="Agent", indexes = { @Index(name = "agentTitleCacheIndex", columnNames = { "titleCache" }) })
public abstract class Agent extends IdentifyableMediaEntity{
	

	
	
}
