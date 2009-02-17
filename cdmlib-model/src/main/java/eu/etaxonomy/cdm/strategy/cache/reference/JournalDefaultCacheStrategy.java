/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.cache.reference;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.strategy.StrategyBase;

/**
 * @author a.mueller
 * @created 29.06.2008
 * @version 1.0
 */
public class JournalDefaultCacheStrategy<T extends Journal> extends StrategyBase implements IReferenceBaseCacheStrategy<T> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(JournalDefaultCacheStrategy.class);

	
	protected String beforeYear = ". ";
	protected String afterYear = "";
	protected String afterAuthor = ", ";

	private String blank = " ";
	private String comma = ",";
	private String dot =".";
	
	final static UUID uuid = UUID.fromString("c84846cd-c862-462e-81b8-53cf4100ed32");
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.StrategyBase#getUuid()
	 */
	@Override
	protected UUID getUuid() {
		return uuid; 
	}
	
	
	/**
	 * Factory method
	 * @return
	 */
	public static JournalDefaultCacheStrategy NewInstance(){
		return new JournalDefaultCacheStrategy<Journal>();
	}
	
	/**
	 * Constructor
	 */
	private JournalDefaultCacheStrategy(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.common.IdentifiableEntity)
	 */
	public String getTitleCache(T journal) {
		String result = "";
		if (journal == null){
			return null;
		}
		String title = CdmUtils.Nz(journal.getTitle()).trim();
		if (!"".equals(title) ){
			result += title;
		}
		
//		//delete .
//		while (result.endsWith(".")){
//			result = result.substring(0, result.length()-1);
//		}
		
//		result = addYear(result, journal);
		TeamOrPersonBase<?> team = journal.getAuthorTeam();
		String author = (team == null ? "" : CdmUtils.Nz(team.getTitleCache()));
		if (! author.equals("")){
			result = author + afterAuthor + result;
		}
		return result;
	}
}
