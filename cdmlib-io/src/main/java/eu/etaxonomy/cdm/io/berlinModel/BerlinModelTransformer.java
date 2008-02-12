package eu.etaxonomy.cdm.io.berlinModel;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownRankException;

public final class BerlinModelTransformer {
	private static final Logger logger = Logger.getLogger(BerlinModelTransformer.class);

	
	/** Creates an cdm-Rank by the berlinModel rankId
	 * @param doubt doubtfulFalg
	 * @return "true" if doubt = "a"
	 */
	public static Rank rankId2Rank (int rankId) throws UnknownRankException{
		switch (rankId){
			case 1: return Rank.KINGDOM();
			case 3: return Rank.SUBKINGDOM();
			case 5: return Rank.PHYLUM();
			case 7: return Rank.SUBPHYLUM();
			case 8: return Rank.DIVISION();
			case 9: return Rank.SUBDIVISION();
			case 10: return Rank.CLASS();
			case 13: return Rank.SUBCLASS();
			case 16: return Rank.SUPERORDER();
			case 18: return Rank.ORDER();
			case 19: return Rank.SUBORDER();
			case 20: return Rank.FAMILY();
			case 25: return Rank.SUBFAMILY();
			case 30: return Rank.TRIBE();
			case 35: return Rank.SUBTRIBE();
			case 40: return Rank.GENUS();
			case 42: return Rank.SUBGENUS();
			case 45: return Rank.SECTION();
			case 47: return Rank.SUBSECTION();
			case 50: return Rank.SERIES();
			case 52: return Rank.SUBSERIES();
			case 58: return Rank.SPECIESAGGREGATE();
			//FIXME
			//case 59: return Rank.SPECIESAGGREGATE();
			case 60: return Rank.SPECIES();
			case 65: return Rank.SUBSPECIES();
			case 68: return Rank.CONVAR();
			case 70: return Rank.VARIETY();
			case 73: return Rank.SUBVARIETY();
			case 80: return Rank.FORM();
			case 82: return Rank.SUBFORM();
			case 84: return Rank.SPECIALFORM();
			case 98: return Rank.INFRAGENERICTAXON();
			case 99: return Rank.INFRASPECIFICTAXON();
			default: {
				throw new UnknownRankException("Unknown Rank id" + Integer.valueOf(rankId).toString());
			}
		}		
	}
		
	
}
