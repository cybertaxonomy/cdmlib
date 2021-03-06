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
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * This class defines the MatchStrategies that will be used by the ParseHandler.
 *
 * @author n.hoffmann
 * @since Jan 22, 2010
 */
public class MatchStrategyConfigurator {

    public enum MatchStrategy {
        NonViralName,
        TeamOrPerson,
        Reference
    }

    public static IMatchStrategyEqual getMatchStrategy(MatchStrategy strategy) throws MatchException {
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

	public static IMatchStrategyEqual NonViralNameMatchStrategy() throws MatchException{
		return getDefaultNonViralNameMatchStrategy();// PreferencesUtil.getMatchStrategy(NonViralName.class);
	}

	/**
	 * <p>TeamOrPersonMatchStrategy</p>
	 *
	 * @throws eu.etaxonomy.cdm.strategy.match.MatchException if any.
	 * @return a {@link eu.etaxonomy.cdm.strategy.match.IMatchStrategyEqual} object.
	 */
	public static IMatchStrategyEqual TeamOrPersonMatchStrategy() throws MatchException{
		return getDefaultTeamOrPersonMatchStrategy();// PreferencesUtil.getMatchStrategy(TeamOrPersonBase.class);
	}

	/**
	 * <p>ReferenceMatchStrategy</p>
	 *
	 * @throws eu.etaxonomy.cdm.strategy.match.MatchException if any.
	 * @return a {@link eu.etaxonomy.cdm.strategy.match.IMatchStrategyEqual} object.
	 */
	public static IMatchStrategyEqual ReferenceMatchStrategy() throws MatchException{
		return getDefaultReferenceMatchStrategy();// PreferencesUtil.getMatchStrategy(ReferenceBase.class);
	}

	/**
	 * <p>getDefaultNonViralNameMatchStrategy</p>
	 *
	 * @throws eu.etaxonomy.cdm.strategy.match.MatchException if any.
	 * @return a {@link eu.etaxonomy.cdm.strategy.match.IMatchStrategyEqual} object.
	 */
	public static IMatchStrategyEqual getDefaultNonViralNameMatchStrategy() throws MatchException{
		IMatchStrategyEqual strategy = DefaultMatchStrategy.NewInstance(TaxonName.class);

		strategy.setMatchMode("nomenclaturalSource", MatchMode.IGNORE);
		strategy.setMatchMode("combinationAuthorship", MatchMode.IGNORE);
		strategy.setMatchMode("exCombinationAuthorship", MatchMode.IGNORE);
		strategy.setMatchMode("basionymAuthorship", MatchMode.IGNORE);
		strategy.setMatchMode("exBasionymAuthorship", MatchMode.IGNORE);

		return strategy;
	}

	/**
	 * <p>getDefaultTeamOrPersonMatchStrategy</p>
	 *
	 * @return a {@link eu.etaxonomy.cdm.strategy.match.IMatchStrategyEqual} object.
	 * @throws eu.etaxonomy.cdm.strategy.match.MatchException if any.
	 */
	public static IMatchStrategyEqual getDefaultTeamOrPersonMatchStrategy() throws MatchException{
		IMatchStrategyEqual strategy = DefaultMatchStrategy.NewInstance(TeamOrPersonBase.class);

		return strategy;
	}

	/**
	 * <p>getDefaultReferenceMatchStrategy</p>
	 *
	 * @return a {@link eu.etaxonomy.cdm.strategy.match.IMatchStrategyEqual} object.
	 * @throws eu.etaxonomy.cdm.strategy.match.MatchException if any.
	 */
	public static IMatchStrategyEqual getDefaultReferenceMatchStrategy() throws MatchException{
		IMatchStrategyEqual strategy = DefaultMatchStrategy.NewInstance(Reference.class);

		strategy.setMatchMode("title", MatchMode.EQUAL);
		strategy.setMatchMode("inReference", MatchMode.IGNORE);

		return strategy;
	}
}
