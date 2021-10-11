/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.parser;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.UTF8;


/**
 * This class is a base class that separates regex parts of the parser from methods
 * @author a.mueller
 *
 */
public abstract class NonViralNameParserImplRegExBase  {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NonViralNameParserImplRegExBase.class);

	// good intro: http://java.sun.com/docs/books/tutorial/essential/regex/index.html

    //splitter
    protected static String epiSplitter = "(\\s+|\\(|\\))"; //( ' '+| '(' | ')' )
    protected static Pattern pattern = Pattern.compile(epiSplitter);

	public static final String hybridSign = UTF8.HYBRID.toString();  //  "\u00D7";

    //some useful non-terminals
	protected static String or = "|";
    protected static String pStart = "^";
    protected static String end = "$";
    protected static String anyEnd = ".*" + end;
    protected static String oWs = "\\s+"; //obligatory whitespaces
    protected static String fWs = "\\s*"; //facultative whitespcace
    protected static String dotSpaceOrBoth = "((?<=\\.)|\\s+)+";

    public static String capitalWord = "\\p{javaUpperCase}\\p{javaLowerCase}*";
    protected static String capital2LetterWord = "\\p{javaUpperCase}\\p{javaLowerCase}+";
    protected static String nonCapitalWord = "\\p{javaLowerCase}+";
    protected static String word = "(" + capitalWord + "|" + nonCapitalWord + ")"; //word (capital or non-capital) with no '.' at the end
    protected static String uppercaseWord = "\\p{javaUpperCase}{2,}";
    protected static String apostropheWord = word + "('\\p{javaLowerCase}*)?"; //word with optional apostrophe in between

    protected static String capitalDotWord = capitalWord + "\\.?"; //capitalWord with facultative '.' at the end
    protected static String capital2charDotWord = "(" + capital2LetterWord + "\\.?|\\p{javaUpperCase}\\.)"; //capitalWord with facultative '.' but minimum 2 characters (single capital word like 'L' is not allowed
    protected static String twoCapitalDotWord = "\\p{javaUpperCase}{2}\\.";   //e.g. NY.

    protected static String nonCapitalDotWord = nonCapitalWord + "\\.?"; //nonCapitalWord with facultative '.' at the end
    protected static String dotWord = "(" + capitalWord + "|" + nonCapitalWord + ")\\.?"; //word (capital or non-capital) with facultative '.' at the end
    protected static String obligateDotWord = "(" + capitalWord + "|" + nonCapitalWord + ")\\.+"; //word (capital or non-capital) with obligate '.' at the end

    //Words used in an epithet/name part for a TaxonName
    protected static String nonCapitalEpiWord = "[a-z\u00EF\u00EB\u00F6\u00FC\\-]+";   //a-z + diaeresis for ieou
    protected static String capitalEpiWord = "[A-Z]"+ nonCapitalEpiWord;

   //years
    protected static String month = "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)";
    protected static String singleYear = "\\b" + "(?:17|18|19|20)" + "\\d{2}" + "\\b";       // word boundary followed by either 17,18,19, or 20 (not captured) followed by 2 digits
    protected static String datePeriod = TimePeriodParser.strDateWithMonthesPeriod;
    protected static String SEP = TimePeriodParser.SEP;
    protected static String yearPeriod = singleYear + "("+ fWs + SEP + fWs + singleYear + ")?";
    protected static String correctYearPhrase = "(" + yearPeriod + "|" + datePeriod + ")" ;

    public static String verbStart = TimePeriodParser.verbatimStart;
    public static String verbEnd = TimePeriodParser.verbatimEnd;

    public static String verbatimYearPhrase = "(" + verbStart + correctYearPhrase + verbEnd + fWs + "\\[" + singleYear + "\\]" +"|"
            + correctYearPhrase + oWs+  "publ\\.?" + fWs + correctYearPhrase + ")" ;
    public static String undefinedYearPhrase = correctYearPhrase + fWs + "\\[" + correctYearPhrase + "\\]";
    protected static String yearPhrase = "(" + correctYearPhrase + "|" + verbatimYearPhrase + "|" + undefinedYearPhrase + ")";

    protected static String yearSeperator = "\\." + oWs;
    protected static String detailSeparator = ":" + oWs;
    protected static String referenceSeparator1 = "," + oWs ;
    protected static String inReferenceSeparator = oWs + "in" + oWs;
    protected static String referenceSeperator = "(" + referenceSeparator1 +"|" + inReferenceSeparator + ")" ;
    protected static String referenceAuthorSeparator = ","+ oWs;
    protected static String volumeSeparator = oWs ;
    protected static String referenceEnd = "\\.";


    //status
    protected static String status = "";

    //marker
    protected static String InfraGenusMarker = "(n|notho)?(subg(en)?\\.|sect\\.|subsect\\.|ser\\.|subser\\.|t\\.infgen\\.|\\[unranked\\]|\\[ranglos\\])";
    protected static String aggrOrGroupMarker = "(aggr\\.|agg\\.|group)";
    protected static String infraSpeciesMarkerNoNotho = "(subsp\\.|convar\\.|var\\.|subvar\\.|f\\.|forma|subf\\.|f\\.\\ssp\\.|f\\.spec\\.|f\\.sp\\.|\\[unranked\\]|\\[ranglos\\]|tax\\." + fWs + "infrasp\\.)";
    public static String infraSpeciesMarker = "(n|notho)?" + infraSpeciesMarkerNoNotho;
    public static String oldInfraSpeciesMarker = "((sub)?prol\\.|(sub)?proles|race|taxon|(sub)?lusus|(sub)?grex)";

    //AuthorString
    protected static String qm = "[" + UTF8.QUOT_SINGLE_RIGHT + UTF8.ACUTE_ACCENT + UTF8.QUOT_SINGLE_LEFT_HIGH + "'`]";
    protected static String authorPart = "(" + "([OdDL]"+qm+"|"+ qm + "t\\s?|ten\\s||l[ae]\\s|zur\\s)?" + "(" + capital2charDotWord + "|DC\\.)I?" + "(" + qm + nonCapitalDotWord + ")?" + "|[vV][ao]n(\\sder)?|da|du|-e|de(n|l|\\sla)?)" ;
    protected static String author = "((" + authorPart + "(" + fWs + "|-)" + ")+" + "(f(il)?\\.|secundus|jun\\.|ter|bis)?|Man in "+qm+"t Veld|Sant"+qm+"Anna)" ;
    protected static String finalTeamSplitter = "(" + fWs + "(&)" + fWs + "|" + oWs + "et" + oWs + ")";
    protected static String notFinalTeamSplitter = "(?:" + fWs + "," + fWs + "|" + finalTeamSplitter + ")";
    protected static String authorTeam = fWs + "(((?>" + author + notFinalTeamSplitter + ")*" + author + finalTeamSplitter + ")?(?:"  + author + "|al\\.)|hort\\.)" +  fWs;
    protected static String exString = "(ex\\.?)";
    protected static String authorAndExTeam = "(" + authorTeam + oWs + exString + oWs + ")?" + authorTeam;
    protected static String basStart = "\\(";
    protected static String basEnd = "\\)";
    protected static String botanicBasionymAuthor = basStart + "(" + authorAndExTeam + ")" + basEnd;  // '(' and ')' is for evaluation with RE.paren(x)
    protected static String fullBotanicAuthorString = fWs + "((" + botanicBasionymAuthor +")?" + fWs + authorAndExTeam + "|" + botanicBasionymAuthor +")"+ fWs;
    protected static String facultFullBotanicAuthorString = "(" +  fullBotanicAuthorString + ")?" ;

    //Zoo. Author
    //TODO does zoo author have ex-Author?
    protected static String zooAuthorYearSeperator = "(,|\\s)";
    protected static String zooAuthorAddidtion = fWs + zooAuthorYearSeperator + fWs + singleYear;
    protected static String zooAuthorTeam = authorTeam + zooAuthorAddidtion;
    protected static String zooBasionymAuthor = basStart + "(" + zooAuthorTeam + ")" + basEnd;
    protected static String fullZooAuthorString = fWs + "((" + zooBasionymAuthor +")?" + fWs + zooAuthorTeam + "|" + zooBasionymAuthor +")"+ fWs;
    protected static String facultFullZooAuthorString = "(" +  fullZooAuthorString + ")?" ;

    protected static String facultFullAuthorString2 = "(" + facultFullBotanicAuthorString + "|" + facultFullZooAuthorString + ")";

    protected static String basionymAuthor = "(" + botanicBasionymAuthor + "|" + zooBasionymAuthor+ ")";
    protected static String fullAuthorString = "(" + fullBotanicAuthorString + "|" + fullZooAuthorString+ ")";

    //details
    //TODO still not all parsed


    protected static String simpleRoman = "([IVXLC]+|[ivxlc]+)";

    protected static String nr2 = "\\d{1,2}";
    protected static String nr4 = "\\d{1,4}";
    protected static String nr5 = "\\d{1,5}";


    protected static String pPage = nr5 + "[a-zA-Z]?";
    protected static String pStrNo = "("+capitalWord + oWs + ")?n[o\u00B0]?\\.?" + fWs + "(" + nr4 + ")";

    protected static String pBracketNr = "\\[" + nr4 + "\\]";
    protected static String pFolBracket = "\\[fol\\." + fWs + "\\d{1,2}(-\\d{1,2})?\\]";  //maybe merge with pTabFigPlate (see below)


    protected static String pRangeSep = "[-\u2013]";
    protected static String pRangeSepCo = "[-\u2013,/]";

    protected static String pTabFigPlateStart = "([tT](abs?)?|[fF](igs?)?|[pP]l?s?)(\\.|\\s|$)";   //$ for only 'f'
    protected static String pAbcNr = "([a-zA-Z\u00DF]|bis)";
    protected static String pTabFigPlateNumber = "(" + nr4 + "|" + pAbcNr + "|" + nr4 + fWs + pAbcNr + "|" + simpleRoman + ")("+ pRangeSepCo + fWs + pAbcNr + ")?";
    protected static String pTabFigPlateNumbers = "(" + pTabFigPlateNumber + "(" + pRangeSepCo + fWs + pTabFigPlateNumber + ")?|s.n.)";

    protected static String pTabFigPlate = pTabFigPlateStart + fWs + pTabFigPlateNumbers + "?";
    protected static String pTabFigPl = pTabFigPlate;

    //e.g.: p455; p.455; pp455-456; pp.455-456; pp.455,456; 455, 456; pages 456-457; pages 456,567
    protected static String pSinglePages = "(p\\.?)?" + fWs + pPage + "(," + pTabFigPl +"){0,2}";
    protected static String pMultiPages = "(pp\\.?|pages)?" + fWs + pPage + fWs + pRangeSepCo +fWs + pPage ;
    //static String pPages = pPage + "(," + fWs + "(" + pPage + "|" + pTabFig + ")" + ")?";
    protected static String pPages = "(" + pSinglePages +"|" + pMultiPages +")";
    protected static String pPagesTabFig = pPages +"([,\\.]" + fWs + pTabFigPl + "){1,2}";

    protected static String pAnyWordWithNumber = word + "(\\.|\\s)" + fWs + "\\d{1,3}";

    protected static String pTabSpecial = "tab\\." + fWs + "(ad" + fWs + "\\d{1,3}|alphab)";
    protected static String pPageSpecial = nr4 + fWs + "(in obs|, Expl\\. Tab)";
    protected static String pSpecialGardDict = capitalWord + oWs + "n\u00B0" + oWs + "\\d{1,2}";
    //TODO
    // protected static String pSpecialDetail = "(in err|in tab|sine pag|add\\. & emend|Emend|""\\d{3}"" \\[\\d{3}\\])";
 // protected static String pSpecialDetail = "(in err|in tab|sine pag|add\\. & emend|Emend|""\\d{3}"" \\[\\d{3}\\])";
    protected static String pSpecialDetail = "(in err|in tab|sine pag|add\\.|s.p.?|errata)";


//    "(,\\s*" + pTabFigPl + ")?" +
    protected static String pDetailAlternatives = "(" + pPages + "|" + pPageSpecial + "|" + pStrNo + "|" + pBracketNr +
    			"|" + pTabFigPl + "(,\\s*" + pTabFigPl + ")?" + "|" + pTabSpecial + "|" + pFolBracket + "|" + pAnyWordWithNumber + "|" +
    			pSpecialGardDict + "|" + pSpecialDetail + "|" + pPagesTabFig + "|" + simpleRoman +  ")";

    protected static String detail = pDetailAlternatives;

    //reference
    protected static String bracketVolume = nr4 + "[A-Za-z]?" + "([-\u2013]" + nr4 + ")?\\))?" + "(\\(((\\d{1,2},\\s*)?(Suppl|Beibl|App|Beil|Misc|Vorabdr|Erg|Bih|(Sess\\.\\s*)?Extr|Reimpr|Bibl|Polypet|Litt|Phys|Orchid)\\.(\\s*\\d{1,4})?|Heft\\s*\\d{1,4}|Extra)";
    protected static String volume =     nr4 + "([-\u2013]"+nr4+")?" + "[A-Za-z]?" + fWs + "(\\("+ bracketVolume + "\\))?";
//    protected static String volume_old = nr4 + "[A-Za-z]?" + fWs + "(\\("+ nr4 + "[A-Za-z]?" + "([-\u2013]" + nr4 + ")?\\)|[-\u2013]"+nr4+")?" + "(\\(((\\d{1,2},\\s*)?(Suppl|Beibl|App|Beil|Misc|Vorabdr|Erg)\\.(\\s*\\d{1,4})?|Heft\\s*\\d{1,4})\\))?";
    //this line caused problem https://dev.e-taxonomy.eu/redmine/issues/1556 in its original form: "([\u005E:\\.]" + fWs + ")";
    protected static String anySepChar = "([\u005E:a-zA-Z]" + fWs + "|" +oWs + "&" + oWs + ")"; //all characters except for the detail separator, a stricter version would be [,\\-\\&] and some other characters
//  protected static String anySepChar = "([,\\-\\&\\.\\+\\']" + fWs + ")";
    protected static String quotations = "\""+capitalDotWord + "(" + oWs + capitalDotWord +")*\"";

    protected static int authorSeparatorMaxPosition = 3;  //author may have a maximum of 2 words
    protected static String pTitleWordSeparator = "(\\."+ fWs+"|" + oWs + "|\\.?[-\u2013]"+oWs+"|\\.?" + oWs + "&(?!\\s*al\\.)" + oWs + ")";
    protected static String pSeriesPart = fWs + ",?" + fWs + "(([sS][e\u00E9]r|сер)("+oWs+"|\\."+fWs+")(\\d{1,2}|[A-Z](\\s*\\d{1,2})?)|n(ov)?\\.\\s*[sS](er)?\\.|Jerusalem Ser\\.|(Pt|Sect)\\.\\s*\\d{1,2}),?";  //Pt. (Part) and Sect. (Section) currently handled as series part, which is part of title, may be handled different later

    protected static String authorPrefix = "(Da(lla)?|Van|La|De)" + oWs; //should not include words allowed in first part of reference title
    protected static String firstTitleWord = "(?!"+authorPrefix+")" + word + "('\\p{javaLowerCase}*|[-\u2013]"+word+")?"; //word with optional apostrophe in between

    protected static String singleJournalTitles = "PhytoKeys|PLoS ONE";
    protected static String referenceTitleFirstPart = "(" + firstTitleWord + pTitleWordSeparator + "|" + twoCapitalDotWord + fWs + ")";
    protected static String referenceTitleBase = "("+ referenceTitleFirstPart + "*" + "("+ dotWord + "|" + uppercaseWord + "|" + quotations + ")"
                    + "|" +singleJournalTitles + ")";  //reference title may have words separated by whitespace or dot. The last word may not have a whitespace at the end. There must be at least one word
    protected static String referenceTitleBaseWithSeries = referenceTitleBase + "("+ pSeriesPart + ")?";
    protected static String referenceTitle = "("+referenceTitleBaseWithSeries +")";
    protected static String referenceTitleWithSepCharacters = "(((" + referenceTitle +"|\\(.+\\))"  + anySepChar + ")*" + referenceTitle + ")"; //,?
    //TODO test performance ??
    protected static String referenceTitleWithSepCharactersAndBrackets = referenceTitleWithSepCharacters + fWs + "(\\(" + referenceTitleWithSepCharacters + "\\)"+fWs+ ")?(" + referenceTitleWithSepCharacters +")?"  ;

    protected static String referenceTitleWithoutAuthor = "(" + referenceTitleFirstPart + "){"+ (authorSeparatorMaxPosition -1) +",}" + dotWord +
    			anySepChar + referenceTitleWithSepCharactersAndBrackets ;   //separators exist and first separator appears at position authorSeparatorMaxPosition or later
    protected static String referenceTitleWithPlaceBracket = referenceTitle + "(" + oWs + "\\(" + capitalWord + "(" + oWs + "(&\\s+)?" +  capitalWord + ")?" + "\\))?" ;

    protected static String editionSeparator = "(" + oWs + "|," + fWs + ")ed\\.?" + oWs;  //
    public static String pEdition = nr2;

    protected static String pVolPart = volumeSeparator + volume;
    protected static String pEditionPart = "(" + editionSeparator +  pEdition +"([A-Z]|\\s*bis)?|,\\s*(jubilee|nouv\\.) ed\\.)";
    protected static String pEditionVolPart = pEditionPart + fWs + "," + volumeSeparator + volume;
    protected static String pEditionVolAlternative = "(" + pEditionPart + "|" + pVolPart + "|" + pEditionVolPart + ")?";

//    protected static String pVolRefTitle = referenceTitle + "(" + pVolPart + ")?";
    protected static String pVolRefTitle = referenceTitleWithPlaceBracket + "(" + pVolPart + ")?";
    protected static String softEditionVolRefTitle = referenceTitleWithSepCharactersAndBrackets + pEditionVolAlternative;
    protected static String softVolNoAuthorRefTitle = referenceTitleWithoutAuthor + "(" + volumeSeparator +  volume + ")?";

    protected static String pBookReference = softEditionVolRefTitle;
    protected static String pBookSectionReference = authorTeam + referenceAuthorSeparator + softEditionVolRefTitle;
    protected static String pArticleReference = pVolRefTitle;
    protected static String pSoftArticleReference = softVolNoAuthorRefTitle;

    protected static String pReferenceSineDetail = "(" + pArticleReference + "|" + pBookSectionReference + "|" + pBookReference + ")";

    protected static String pReference = pReferenceSineDetail + detailSeparator + detail +
					yearSeperator + yearPhrase + "(" + referenceEnd + ")?";

    //static String strictBook = referenc

    protected static Pattern referencePattern = Pattern.compile(pReference);
    protected static Pattern referenceSineDetailPattern = Pattern.compile(pReferenceSineDetail);

    protected static String originalSpellingName = "("+capitalEpiWord +")?(" + fWs + "\\(" + capitalEpiWord + "\\))?(" +
                        fWs + nonCapitalEpiWord+")?(" + oWs + infraSpeciesMarker + oWs + nonCapitalEpiWord + ")?";
    protected static String originalSpellingStart = "\\[as \"";
    protected static String originalSpellingEnd = "\"\\]";

    protected static String pOriginalSpelling = oWs + originalSpellingStart + originalSpellingName + originalSpellingEnd;
    protected static Pattern originalSpellingPattern = Pattern.compile(pOriginalSpelling);

    protected static String pNomStatusNom =
            "nom\\." + fWs + "(ambig\\.|dub\\.|confus\\.|superfl\\.|nud\\.|illeg\\.|inval\\.|cons\\.(\\s*(prop|des)\\.)?|altern(ativ)?\\.|subnud\\.|nov\\.|legit\\.|sanct\\.|val\\.|"+
    			"rej\\.("+ fWs + "prop\\.)?|provis\\.|utique"+fWs+"rej\\.("+fWs+"prop\\.)?)";
    protected static String pNomStatusOrth = "orth\\." + fWs + "(var\\.|rej\\.|cons\\.("+fWs+"prop\\.)?)";
    protected static String pNomStatusComb = "comb\\." + fWs + "(inval\\.|illeg\\.|nov\\.)";
    protected static String pNomStatusOpus = "op\\." + fWs + "utique" + fWs + "oppr\\.";
    protected static String pNomStatusIned = "ined\\.";


    protected static String pNomStatus = "(" + pNomStatusNom + "|" + pNomStatusOrth + "|" +pNomStatusComb + "|" + pNomStatusOpus + "|" + pNomStatusIned + ")";
    protected static String pNomStatusPhrase1 = "," + fWs + pNomStatus;
    protected static String pNomStatusPhrase2 = "\\[" + fWs + pNomStatus + "\\]";

    protected static String pNomStatusPhrase = "(?:" + pNomStatusPhrase1 + "|" + pNomStatusPhrase2 + ")";

// Soraya
//pro syn.
//provisional synonym
//fossil name

    //cultivars and hybrids
    protected static String cultivarWord = "[a-zA-Z0-9-,\u2019!/\\.\\\\]+";
    protected static String cultivarPhrase = cultivarWord + "("+ oWs + cultivarWord + ")*";   //TODO still unclear if groups and grex really may also contain special characters
    protected static String cultivarStatus = qm + cultivarPhrase + qm; //for stricter rules see Art. 21.xxx (but most of them are time dependend)
    public static String group = "(Gp|Groupe?|Grupp(en?|o))";
    public static String grex = "g(re)?x";
    protected static String cultivarGroupStatus = (cultivarPhrase + oWs + group + or + group + oWs + cultivarPhrase);
    protected static String cultivarGrexStatus = (cultivarPhrase + oWs + grex );

    protected static String cultivarOld =  oWs + "'..+'"; //Careful with apostroph in author names
    protected static String cultivarOldMarker = oWs + "(cv\\.|')";
    protected static String notho = "notho";
    protected static String hybridPart = "([xX]" + oWs + "|"+hybridSign+"|"+notho+")";
    protected static String noNothoHybridPart = "([xX]" + oWs + "|"+hybridSign+")";
    protected static String hybridFull = "(" +oWs +"|"+ pStart +")" + noNothoHybridPart;  //for some reason infraspecific notho ranks do not parse if notho is allowed as uninomial prefix.
    protected static String hybridFormularSeparator = oWs + "[" + hybridSign + "xX]" + oWs;


    //  Name String
    protected static String genusOrSupraGenus = "("+hybridFull+")?" + capitalEpiWord;
    protected static String infraGenus = capitalEpiWord + oWs + InfraGenusMarker + oWs + capitalEpiWord;
    protected static String aggrOrGroup = capitalEpiWord + oWs + nonCapitalEpiWord + oWs + aggrOrGroupMarker;
    protected static String spNov = "sp\\.(\\s*nov\\.)?(\\s*\\d{1,2})?";
    protected static String specificEpi = "(" + nonCapitalEpiWord + "|" + spNov + ")";
    protected static String species = genusOrSupraGenus + oWs + "("+hybridPart+")?" + specificEpi;
    protected static String speciesWithInfraGen = genusOrSupraGenus + oWs + "\\(" + capitalEpiWord + "\\)" + oWs + specificEpi;

    protected static String infraSpecies = species + oWs + infraSpeciesMarker + oWs + "("+hybridPart+")?" + nonCapitalEpiWord;
    protected static String zooInfraSpecies = species + oWs + "(" + infraSpeciesMarker + oWs +")?" + "("+hybridPart+")?" + nonCapitalEpiWord;
    protected static String oldInfraSpecies = capitalEpiWord + oWs +  nonCapitalEpiWord + oWs + oldInfraSpeciesMarker + oWs + nonCapitalEpiWord;
    protected static String autonym = capitalEpiWord + oWs + "(" + nonCapitalEpiWord +")" + oWs + fullBotanicAuthorString +  oWs + infraSpeciesMarker + oWs + "\\1";  //2-nd word and last word are the same
    protected static String genusAutonym = "("+capitalEpiWord+")" + oWs + fullBotanicAuthorString + oWs + InfraGenusMarker + oWs + "\\1";  //1st word and last word are the same
    //autonym patterns used within anyBotanicalFullName pattern as we need another group number there
    protected static String autonym2 =     "("+capitalEpiWord+")" + oWs
            + "(" + hybridSign + "?(" + nonCapitalEpiWord +")" + oWs + fullBotanicAuthorString + oWs + infraSpeciesMarker + oWs + "\\4|"  //infraspecific autonym
            +       fullBotanicAuthorString + oWs + InfraGenusMarker + oWs + "\\2"  //infrageneric autonym
            + ")";  //2-nd word and last word are the same

    protected static String anyPureBotanicName = "(" + genusOrSupraGenus + "|" + infraGenus + "|" + aggrOrGroup + "|" + species + "|" +
                    speciesWithInfraGen + "|" + infraSpecies + "|" + oldInfraSpecies + "|" + autonym + "|" + genusAutonym + ")+";
    protected static String anyBotanicName = anyPureBotanicName; //no difference yet, cultivars are currently implemented differently. was: (anyPureBotanicName + or + cultivar + or + cultivarGroup);
    protected static String cultivarGr = "(\\s+(?<cultivar>" + cultivarStatus + "))?";
    protected static String cultivarGroupGr = "(\\s+((?<cultivarGroup>" + cultivarGroupStatus + ")|\\((?<cultivarBrGroup>" + cultivarGroupStatus + ")\\)))?";
//    protected static String cultivarGroupGrx = "(\\s+\\((?<cultivarBrGroup>" + cultivarGroupStatus + ")\\))?";
    protected static String cultivarGrexGr =  "(\\s+(?<cultivarGrex>"       + cultivarGrexStatus  +    "))?";

    //to be used in combination if cultivar name is not a pure name but contains additional information (e.g. author)
    protected static String anyCultivarNameUnordered = anyPureBotanicName + oWs + "(" + cultivarStatus + or + cultivarGroupStatus + or + "\\(" + cultivarGroupStatus + "\\)" + or + cultivarGrexStatus + ")";
    protected static String anyCultivarName = "("+ anyPureBotanicName + ")(?!\\s*$)(" + cultivarGrexGr + cultivarGroupGr + cultivarGr + ")";

    protected static String anyZooName = "(" + genusOrSupraGenus + "|" + infraGenus + "|" + aggrOrGroup + "|" + species + "|" +
                    speciesWithInfraGen + "|" +zooInfraSpecies + "|" +  oldInfraSpecies + ")+";
    protected static String anyBotanicFullName = "(" + autonym2 + "|" + anyBotanicName + oWs + fullBotanicAuthorString + ")"  ;
    protected static String anyCultivarFullName = anyCultivarName + oWs + "(?<cultivarAuthor>" + authorTeam +")";
    protected static String anyZooFullName = anyZooName + oWs + fullZooAuthorString ;
    protected static String anyFullName = "(" + anyBotanicFullName + "|" + anyZooFullName + ")";
    protected static String abbrevHybridGenus = "([A-Z](\\.\\s*|\\s+))";
    protected static String abbrevHybridSecondPartWithSpecies = abbrevHybridGenus + "?" + nonCapitalEpiWord + "(" + oWs + infraSpeciesMarkerNoNotho + oWs + nonCapitalEpiWord + ")?";  //#5983 first step but still to strict
    protected static String abbrevHybridSecondPartOnlyInfraSpecies = infraSpeciesMarkerNoNotho + oWs + nonCapitalEpiWord;
    protected static String abbrevHybridSecondPart = "(" + abbrevHybridSecondPartWithSpecies + "|" + abbrevHybridSecondPartOnlyInfraSpecies + ")";

    protected static String hybridSecondPart = "(" + anyFullName  + "|" +  anyBotanicName + "|" + anyZooName + "|" + abbrevHybridSecondPart + ")";
    protected static String hybridFullName = "(" + anyFullName  + "|" +  anyBotanicName + "|" + anyZooName + ")" + hybridFormularSeparator + hybridSecondPart ;

    //Pattern
    protected static Pattern oWsPattern = Pattern.compile(oWs);
    protected static Pattern finalTeamSplitterPattern = Pattern.compile(finalTeamSplitter);
    protected static Pattern anyCultivarNamePattern = Pattern.compile(anyCultivarName);
    protected static Pattern anyCultivarNameUnorderedPattern = Pattern.compile(anyCultivarNameUnordered);

    protected static Pattern genusOrSupraGenusPattern = Pattern.compile(pStart + genusOrSupraGenus + facultFullAuthorString2 + end);
    protected static Pattern infraGenusPattern = Pattern.compile(pStart + infraGenus + facultFullAuthorString2 + end);
    protected static Pattern aggrOrGroupPattern = Pattern.compile(pStart + aggrOrGroup + fWs + end); //aggr. or group has no author string
    protected static Pattern speciesPattern = Pattern.compile(pStart + species + facultFullAuthorString2 + end);
    protected static Pattern speciesWithInfraGenPattern = Pattern.compile(pStart + speciesWithInfraGen + facultFullAuthorString2 + end);
    protected static Pattern infraSpeciesPattern = Pattern.compile(pStart + infraSpecies + facultFullAuthorString2 + end);
    protected static Pattern zooInfraSpeciesPattern = Pattern.compile(pStart + zooInfraSpecies + facultFullAuthorString2 + end);
    protected static Pattern oldInfraSpeciesPattern = Pattern.compile(pStart + oldInfraSpecies + facultFullAuthorString2 + end);
    protected static Pattern autonymPattern = Pattern.compile(pStart + autonym + fWs + end);
    protected static Pattern genusAutonymPattern = Pattern.compile(pStart + genusAutonym + fWs + end);
    protected static Pattern hybridFormulaPattern = Pattern.compile(pStart + hybridFullName + fWs + end);

    protected static Pattern botanicBasionymPattern = Pattern.compile(botanicBasionymAuthor);
    protected static Pattern zooBasionymPattern = Pattern.compile(zooBasionymAuthor);
    protected static Pattern basionymPattern = Pattern.compile(basionymAuthor);

    protected static Pattern zooAuthorPattern = Pattern.compile(zooAuthorTeam);
    protected static Pattern zooAuthorAddidtionPattern = Pattern.compile(zooAuthorAddidtion);

    protected static Pattern exAuthorPattern = Pattern.compile(oWs + exString);

    protected static Pattern fullBotanicAuthorStringPattern = Pattern.compile(fullBotanicAuthorString);
    protected static Pattern fullZooAuthorStringPattern = Pattern.compile(fullZooAuthorString);
    protected static Pattern fullAuthorStringPattern = Pattern.compile(fullAuthorString);

    protected static Pattern anyBotanicFullNamePattern = Pattern.compile(anyBotanicFullName);
    protected static Pattern anyCultivarFullNamePattern = Pattern.compile(anyCultivarFullName);
    protected static Pattern anyZooFullNamePattern = Pattern.compile(anyZooFullName);

    protected static Pattern spNovPattern = Pattern.compile(spNov);
}