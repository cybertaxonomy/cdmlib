// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.match;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * This class defines the MatchStrategies that will be used by the ParseHandler.
 *
 * @author n.hoffmann
 * @created Jan 22, 2010
 * @version 1.0
 */
public class MatchStrategyConfigurator {


    public enum MatchStrategy {
        NonViralName,
        TeamOrPerson,
        Reference
    }

    public static IMatchStrategy getMatchStrategy(MatchStrategy strategy) throws MatchException {
        switch(strategy) {
        case NonViralName :
            return NonViralNameMatchStrategy();
        case TeamOrPerson:
            return TeamOrPersonMatchStrategy();
        case Reference :
            return ReferenceMatchStrategy();
        default :
            return null;
        }
    }
	/**
	 * <p>NonViralNameMatchStrategy</p>
	 *
	 * @throws eu.etaxonomy.cdm.strategy.match.MatchException if any.
	 * @return a {@link eu.etaxonomy.cdm.strategy.match.IMatchStrategy} object.
	 */
	public static IMatchStrategy NonViralNameMatchStrategy() throws MatchException{
		return getDefaultNonViralNameMatchStrategy();// PreferencesUtil.getMatchStrategy(NonViralName.class);
	}

	/**
	 * <p>TeamOrPersonMatchStrategy</p>
	 *
	 * @throws eu.etaxonomy.cdm.strategy.match.MatchException if any.
	 * @return a {@link eu.etaxonomy.cdm.strategy.match.IMatchStrategy} object.
	 */
	public static IMatchStrategy TeamOrPersonMatchStrategy() throws MatchException{
		return getDefaultTeamOrPersonMatchStrategy();// PreferencesUtil.getMatchStrategy(TeamOrPersonBase.class);
	}

	/**
	 * <p>ReferenceMatchStrategy</p>
	 *
	 * @throws eu.etaxonomy.cdm.strategy.match.MatchException if any.
	 * @return a {@link eu.etaxonomy.cdm.strategy.match.IMatchStrategy} object.
	 */
	public static IMatchStrategy ReferenceMatchStrategy() throws MatchException{
		return getDefaultReferenceMatchStrategy();// PreferencesUtil.getMatchStrategy(ReferenceBase.class);
	}

	/**
	 * <p>getDefaultNonViralNameMatchStrategy</p>
	 *
	 * @throws eu.etaxonomy.cdm.strategy.match.MatchException if any.
	 * @return a {@link eu.etaxonomy.cdm.strategy.match.IMatchStrategy} object.
	 */
	public static IMatchStrategy getDefaultNonViralNameMatchStrategy() throws MatchException{
		IMatchStrategy strategy = DefaultMatchStrategy.NewInstance(NonViralName.class);

		strategy.setMatchMode("nomenclaturalReference", MatchMode.IGNORE);
		strategy.setMatchMode("combinationAuthorship", MatchMode.IGNORE);
		strategy.setMatchMode("exCombinationAuthorship", MatchMode.IGNORE);
		strategy.setMatchMode("basionymAuthorship", MatchMode.IGNORE);
		strategy.setMatchMode("exBasionymAuthorship", MatchMode.IGNORE);

		return strategy;
	}

	/**
	 * <p>getDefaultTeamOrPersonMatchStrategy</p>
	 *
	 * @return a {@link eu.etaxonomy.cdm.strategy.match.IMatchStrategy} object.
	 * @throws eu.etaxonomy.cdm.strategy.match.MatchException if any.
	 */
	public static IMatchStrategy getDefaultTeamOrPersonMatchStrategy() throws MatchException{
		IMatchStrategy strategy = DefaultMatchStrategy.NewInstance(TeamOrPersonBase.class);

		return strategy;
	}


	/**
	 * <p>getDefaultReferenceMatchStrategy</p>
	 *
	 * @return a {@link eu.etaxonomy.cdm.strategy.match.IMatchStrategy} object.
	 * @throws eu.etaxonomy.cdm.strategy.match.MatchException if any.
	 */
	public static IMatchStrategy getDefaultReferenceMatchStrategy() throws MatchException{
		IMatchStrategy strategy = DefaultMatchStrategy.NewInstance(Reference.class);

		strategy.setMatchMode("title", MatchMode.EQUAL);
		strategy.setMatchMode("inReference", MatchMode.IGNORE);

		return strategy;
	}


}
