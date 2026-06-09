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
import javax.persistence.Index;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.IIntextReferenceTarget;
import eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchable;
import eu.etaxonomy.cdm.strategy.merge.IMergable;

/**
 * The upmost (abstract) class for agents such as persons, teams or institutions.
 * An agent is a conscious entity which can make decisions, act and create
 * according to its own knowledge and goals and which may be approached.
 * Agents can be authors for nomenclatural or bibliographical references as well
 * as creators of pictures or field collectors or administrators of collections.
 *
 * @author m.doering
 * @since 08-Nov-2007 13:06:57
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AgentBase", propOrder = {
})
@Entity
@Audited
@Table(name = "AgentBase", indexes = {@Index(name = "agentTitleCacheIndex", columnList = "titleCache")})
public abstract class AgentBase<S extends IIdentifiableEntityCacheStrategy<? extends AgentBase<S>>>
        extends IdentifiableMediaEntity<S>
        implements IMergable, IMatchable, IIntextReferenceTarget{

	private static final long serialVersionUID = 7732768617469448829L;
	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();


    @Override
    public AgentBase<S> clone() throws CloneNotSupportedException {

        AgentBase<S> result = (AgentBase<S>)super.clone();

        return result;
    }

}
