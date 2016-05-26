/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.cache.reference.old;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.reference.IReferenceCacheStrategy;

/**
 * @author a.mueller
 * @created 29.06.2008
 */
public class JournalDefaultCacheStrategy extends StrategyBase implements IReferenceCacheStrategy {
	private static final long serialVersionUID = 8495999505123620960L;


	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(JournalDefaultCacheStrategy.class);


	protected String beforeYear = ". ";
	protected String afterYear = "";
	protected String afterAuthor = ", ";


	final static UUID uuid = UUID.fromString("c84846cd-c862-462e-81b8-53cf4100ed32");

	@Override
	protected UUID getUuid() {
		return uuid;
	}


	/**
	 * Factory method
	 * @return
	 */
	public static JournalDefaultCacheStrategy NewInstance(){
		return new JournalDefaultCacheStrategy();
	}

	/**
	 * Constructor
	 */
	private JournalDefaultCacheStrategy(){
		super();
	}


	@Override
	public String getTitleCache(Reference journal) {
		return getTitleCache(journal, false);
	}


	@Override
	public String getFullAbbrevTitleString(Reference journal) {
		return getTitleCache(journal, true);
	}


	private String getTitleCache(Reference journal, boolean isAbbrev){
		if (journal == null){
			return null;
		}

		String title = CdmUtils.getPreferredNonEmptyString(journal.getTitle(), journal.getAbbrevTitle(), isAbbrev, true);

		String result = title;

//		//delete .
//		while (result.endsWith(".")){
//			result = result.substring(0, result.length()-1);
//		}

//		result = addYear(result, journal);


		TeamOrPersonBase<?> team = journal.getAuthorship();
		if (team != null){
			String author = CdmUtils.getPreferredNonEmptyString(team.getTitleCache(), team.getNomenclaturalTitle(), isAbbrev, true);
			if (StringUtils.isNotBlank(author)){
				result = author + afterAuthor + result;
			}
		}
		return result;
	}


	@Override
    public String getCitation(Reference referenceBase) {
		StringBuilder stringBuilder = new StringBuilder();

		TeamOrPersonBase<?> team = referenceBase.getAuthorship();
		if (team != null &&  ! (team.getTitleCache() == null) && ! team.getTitleCache().trim().equals("")){
			//String author = CdmUtils.Nz(team == null? "" : team.getTitleCache());
			stringBuilder.append(team.getTitleCache() + afterAuthor);
		}

		String year = CdmUtils.Nz(referenceBase.getYear());
		if (!"".equals(year)){
			stringBuilder.append(beforeYear + year);
		}

		return stringBuilder.toString();
	}


}
