package eu.etaxonomy.cdm.io.berlinModel;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownRankException;

public final class BerlinModelTransformer {
	private static final Logger logger = Logger.getLogger(BerlinModelTransformer.class);
 
	//REFERENCES
	public static int REF_ARTICLE = 1;
	public static int REF_PART_OF_OTHER_TITLE = 2;
	public static int REF_BOOK = 3;
	public static int REF_DATABASE = 4;
	public static int REF_INFORMAL = 5;
	public static int REF_NOT_APPLICABLE = 6;
	public static int REF_WEBSITE = 7;
	public static int REF_CD = 8;
	public static int REF_JOURNAL = 9;
	public static int REF_UNKNOWN = 10;
	
	//TaxonRelationShip
	public static int IS_INCLUDED_IN = 1;
	public static int IS_SYNONYM_OF = 2;
	public static int IS_MISAPPLIED_NAME_OF = 3;
	public static int IS_HOMOTYPIC_SYNONYM_OF = 6;
	public static int IS_HETEROTYPIC_SYNONYM_OF = 7;
	
	
	//TaxonRelationShip
	public static int T_STATUS_ACCEPTED = 1;
	public static int T_STATUS_SYNONYM = 2;
	public static int T_STATUS_PARTIAL_SYN = 3;
	public static int T_STATUS_PRO_PARTE_SYN = 4;
	public static int T_STATUS_UNRESOLVED = 5;
	public static int T_STATUS_ORPHANED = 6;
	
	
	//Facts
	public static int FACT_DESCRIPTION = 1;
	public static int FACT_GROWTH_FORM = 2;
	public static int FACT_HARDINESS = 3;
	public static int FACT_ECOLOGY = 4;
	public static int FACT_PHENOLOGY = 5;
	public static int FACT_KARYOLOGY = 6;
	public static int FACT_ILLUSTRATION = 7;
	public static int FACT_IDENTIFICATION = 8;
	public static int FACT_OBSERVATION = 9;
	public static int FACT_DISTIRBUTION_EM = 10;
	public static int FACT_DISTIRBUTION_WORLD = 11;
	
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
