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
    protected static String pStart = "^";
    protected static String end = "$";
    protected static String anyEnd = ".*" + end;
    protected static String oWs = "\\s+"; //obligatory whitespaces
    protected static String fWs = "\\s*"; //facultative whitespcace

    public static String capitalWord = "\\p{javaUpperCase}\\p{javaLowerCase}*";
    protected static String capital2LetterWord = "\\p{javaUpperCase}\\p{javaLowerCase}+";
    protected static String nonCapitalWord = "\\p{javaLowerCase}+";
    protected static String word = "(" + capitalWord + "|" + nonCapitalWord + ")"; //word (capital or non-capital) with no '.' at the end
    protected static String uppercaseWord = "\\p{javaUpperCase}{2,}";
    protected static String apostrophWord = word + "('\\p{javaLowerCase}*)?";

    protected static String capitalDotWord = capitalWord + "\\.?"; //capitalWord with facultativ '.' at the end
    protected static String capital2charDotWord = "(" + capital2LetterWord + "\\.?|\\p{javaUpperCase}\\.)"; //capitalWord with facultativ '.' but minimum 2 characters (single capital word like 'L' is not allowed
    protected static String twoCapitalDotWord = "\\p{javaUpperCase}{2}\\.";   //e.g. NY.

    protected static String nonCapitalDotWord = nonCapitalWord + "\\.?"; //nonCapitalWord with facultativ '.' at the end
    protected static String dotWord = "(" + capitalWord + "|" + nonCapitalWord + ")\\.?"; //word (capital or non-capital) with facultativ '.' at the end
    protected static String obligateDotWord = "(" + capitalWord + "|" + nonCapitalWord + ")\\.+"; //word (capital or non-capital) with obligate '.' at the end

    //Words used in an epethiton for a TaxonName
    protected static String nonCapitalEpiWord = "[a-z\u00EF\u00EB\u00F6\\-]+";   //a-z + diaeresis for ieo
    protected static String capitalEpiWord = "[A-Z]"+ nonCapitalEpiWord;


   //years
    protected static String month = "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)";
    protected static String singleYear = "\\b" + "(?:17|18|19|20)" + "\\d{2}" + "\\b";                      // word boundary followed by either 17,18,19, or 20 (not captured) followed by 2 digits
    protected static String correctYearPhrase = singleYear + "("+ fWs + "-" + fWs + singleYear + ")?" ;
    								//+ "(" + month + ")?)" ;                 // optional month
    //!! also used by TimePeriodParser
    public static String incorrectYearPhrase = "(\"" + correctYearPhrase + "\"|" + correctYearPhrase + "|"
            + UTF8.ENGLISH_QUOT_START_LOW + correctYearPhrase + UTF8.ENGLISH_QUOT_END_HIGH + ")"
			+ fWs + "\\[" + singleYear + "\\]"  ;
    protected static String yearPhrase = "(" + correctYearPhrase + "|" + incorrectYearPhrase + ")";

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
    protected static String InfraGenusMarker = "(n|notho)?(subg(en)?\\.|sect\\.|subsect\\.|ser\\.|subser\\.|t\\.infgen\\.|\\[unranked\\])";
    protected static String aggrOrGroupMarker = "(aggr\\.|agg\\.|group)";
    protected static String infraSpeciesMarkerNoNotho = "(subsp\\.|convar\\.|var\\.|subvar\\.|f\\.|forma|subf\\.|f\\.\\ssp\\.|f\\.spec\\.|f\\.sp\\.|\\[unranked\\]|tax\\." + fWs + "infrasp\\.)";
    protected static String infraSpeciesMarker = "(n|notho)?" + infraSpeciesMarkerNoNotho;
    protected static String oldInfraSpeciesMarker = "(prol\\.|proles|race|taxon|sublusus)";


    //AuthorString
    protected static String qm = "[" + UTF8.RIGHT_SINGLE_QUOT + UTF8.ACUTE_ACCENT + "'`]";
    protected static String authorPart = "(" + "([OdDL]"+qm+"|"+ qm + "t\\s?|ten\\s||l[ae]\\s|zur\\s)?" + "(" + capital2charDotWord + "|DC.)I?" + "(" + qm + nonCapitalDotWord + ")?" + "|[vV][ao]n(\\sder)?|da|du|-e|de(n|l|\\sla)?)" ;
    protected static String author = "(" + authorPart + "(" + fWs + "|-)" + ")+" + "(f(il)?\\.|secundus|jun\\.|ter|bis)?" ;
    protected static String finalTeamSplitter = "(" + fWs + "(&)" + fWs + "|" + oWs + "et" + oWs + ")";
    protected static String notFinalTeamSplitter = "(?:" + fWs + "," + fWs + "|" + finalTeamSplitter + ")";
    protected static String authorTeam = fWs + "((?>" + author + notFinalTeamSplitter + ")*" + author + finalTeamSplitter + ")?(?:"  + author + "|al\\.)" +  fWs;
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

    protected static String nr2 = "\\d{1,2}";
    protected static String nr4 = "\\d{1,4}";
    protected static String nr5 = "\\d{1,5}";


    protected static String pPage = nr5 + "[a-zA-Z]?";
    protected static String pStrNo = "n\u00B0" + fWs + "(" + nr4 + ")";

    protected static String pBracketNr = "\\[" + nr4 + "\\]";
    protected static String pFolBracket = "\\[fol\\." + fWs + "\\d{1,2}(-\\d{1,2})?\\]";  //maybe merge with pTabFigPlate (see below)


    protected static String pRangeSep = "[-\u2013]";
    protected static String pRangeSepCo = "[-\u2013,]";

    protected static String pTabFigPlateStart = "([tT](abs?)?|[fF](igs?)?|[pP]l?s?)(\\.|\\s|$)";   //$ for only 'f'
    protected static String pAbcNr = "[a-zA-Z\u00DF]";
    protected static String pTabFigPlateNumber = "(" + nr4 + "|" + pAbcNr + "|" + nr4 + fWs + pAbcNr + ")" + "("+ pRangeSepCo + fWs + pAbcNr + ")?";
    protected static String pTabFigPlateNumbers = "(" + pTabFigPlateNumber + "(" + pRangeSepCo + fWs + pTabFigPlateNumber + ")?)";

    protected static String pTabFigPlate = pTabFigPlateStart + fWs + pTabFigPlateNumbers + "?";
    protected static String pTabFigPl = pTabFigPlate;

    //e.g.: p455; p.455; pp455-456; pp.455-456; pp.455,456; 455, 456; pages 456-457; pages 456,567
    protected static String pSinglePages = "(p\\.?)?" + fWs + pPage + "(," + pTabFigPl +"){0,2}";
    protected static String pMultiPages = "(pp\\.?|pages)?" + fWs + pPage + fWs + pRangeSepCo +fWs + pPage ;
    //static String pPages = pPage + "(," + fWs + "(" + pPage + "|" + pTabFig + ")" + ")?";
    protected static String pPages = "(" + pSinglePages +"|" + pMultiPages +")";
    protected static String pPagesTabFig = pPages +"([,\\.]" + fWs + pTabFigPl + "){1,2}";



    protected static String pCouv = "couv\\." + fWs + "\\d{1,3}";

    protected static String pTabSpecial = "tab\\." + fWs + "(ad" + fWs + "\\d{1,3}|alphab)";
    protected static String pPageSpecial = nr4 + fWs + "(in obs|, Expl\\. Tab)";
    protected static String pSpecialGardDict = capitalWord + oWs + "n\u00B0" + oWs + "\\d{1,2}";
    //TODO
    // protected static String pSpecialDetail = "(in err|in tab|sine pag|add\\. & emend|Emend|""\\d{3}"" \\[\\d{3}\\])";
 // protected static String pSpecialDetail = "(in err|in tab|sine pag|add\\. & emend|Emend|""\\d{3}"" \\[\\d{3}\\])";
    protected static String pSpecialDetail = "(in err|in tab|sine pag|add\\.)";


//    Const romI = "[Ii]{0,3}"
//    	Const romX = "[Xx]{0,3}"
//    	Const romC = "[Cc]{0,3}"
//    	Const romM = "[Mm]{0,3}"
//    ' roman numbers
//    ' !! includes empty string: ""
//    romOne = "([Vv]?" & romI & or_ & "(IV|iv)" & or_ & "(IX|ix)" & ")"
//    romTen = "([Ll]?" & romX & or_ & "(XL|xl)" & or_ & "(XC|xc)" & ")"
//    romHun = "([Dd]?" & romC & or_ & "(CD|cd)" & or_ & "(CM|cm)" & ")"
//    romNr = "(?=[MDCLXVImdclxvi])(((" & romM & ")?" & romHun & ")?" & romTen & ")?" & romOne
    protected static String pRomNr = "ljfweffaflas"; //TODO rom number have to be tested first

//    "(,\\s*" + pTabFigPl + ")?" +
    protected static String pDetailAlternatives = "(" + pPages + "|" + pPageSpecial + "|" + pStrNo + "|" + pBracketNr +
    			"|" + pTabFigPl + "(,\\s*" + pTabFigPl + ")?" + "|" + pTabSpecial + "|" + pFolBracket + "|" + pCouv + "|" + pRomNr + "|" +
    			pSpecialGardDict + "|" + pSpecialDetail + "|" + pPagesTabFig + ")";

    protected static String detail = pDetailAlternatives;

    //reference
    protected static String volume = nr4 + "[a-z]?" + fWs + "(\\("+ nr4 + "[a-z]?"  + "([-\u2013]" + nr4 + ")?\\))?" + "(\\((Suppl|Beibl|App|Beil|Misc|Vorabdr)\\.\\))?";
    //this line caused problem https://dev.e-taxonomy.eu/trac/ticket/1556 in its original form: "([\u005E:\\.]" + fWs + ")";
    protected static String anySepChar = "([\u005E:a-zA-Z]" + fWs + ")"; //all characters except for the detail separator, a stricter version would be [,\\-\\&] and some other characters
//  protected static String anySepChar = "([,\\-\\&\\.\\+\\']" + fWs + ")";

    protected static int authorSeparatorMaxPosition = 4;  //Author may have a maximum of 4 words
    protected static String pTitleWordSeparator = "(\\."+ fWs+"|" + oWs + "|\\.?[-\u2013])";
    protected static String pSeriesPart = ",?" + fWs + "[sS]er(\\.)?" + oWs + "\\d{1,2},?";

    protected static String referenceTitleFirstPart = "(" + apostrophWord + pTitleWordSeparator + "|" + twoCapitalDotWord + fWs + ")";
    protected static String referenceTitle = referenceTitleFirstPart + "*" + "("+ dotWord + "|" + uppercaseWord + "|" + pSeriesPart + ")";  //reference title may have words seperated by whitespace or dot. The last word may not have a whitespace at the end. There must be at least one word
    protected static String referenceTitleWithSepCharacters = "(((" + referenceTitle +"|\\(.+\\))"  + anySepChar + ")*" + referenceTitle + ")"; //,?
    //TODO test performance ??
    protected static String referenceTitleWithSepCharactersAndBrackets = referenceTitleWithSepCharacters + fWs + "(\\(" + referenceTitleWithSepCharacters + "\\)"+fWs+ ")?(" + referenceTitleWithSepCharacters +")?"  ;

    protected static String referenceTitleWithoutAuthor = "(" + referenceTitleFirstPart + ")" + "{"+ (authorSeparatorMaxPosition -1) +",}" + dotWord +
    			anySepChar + referenceTitleWithSepCharactersAndBrackets ;   //separators exist and first separator appears at position authorSeparatorMaxPosition or later
    protected static String referenceTitleWithPlaceBracket = referenceTitle + "(" + oWs + "\\(" + capitalWord + "(" + oWs + capitalWord + ")?" + "\\))?" ;

    protected static String editionSeparator = "(" + oWs + "|," + fWs + ")ed\\.?" + oWs;  //
    protected static String pEdition = nr2;

    protected static String pVolPart = volumeSeparator +  volume;
    protected static String pEditionPart = editionSeparator +  pEdition;
    protected static String pEditionVolPart = editionSeparator +  pEdition + fWs + "," + volumeSeparator +  volume;
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

    protected static String pNomStatusNom =
            "nom\\." + fWs + "(ambig\\.|dub\\.|confus\\.|superfl\\.|nud\\.|illeg\\.|inval\\.|cons\\.(\\s*(prop|des)\\.)?|altern(ativ)?\\.|subnud\\.|nov\\.|legit\\.|sanct\\.|valid|"+
    			"rej\\.("+ fWs + "prop\\.)?|provis\\.|utique"+fWs+"rej\\.("+fWs+"prop\\.)?|orth\\."+fWs+"cons\\.("+fWs+"prop\\.)?)";
    protected static String pNomStatusOrthVar = "orth\\." + fWs + "(var\\.|rej\\.)";
    protected static String pNomStatusComb = "comb\\." + fWs + "(inval\\.|illeg\\.|nov\\.)";
    protected static String pNomStatusOpus = "opus\\." + fWs + "utique" + fWs + "oppr\\.";
    protected static String pNomStatusIned = "ined\\.";


    protected static String pNomStatus = "(" + pNomStatusNom + "|" + pNomStatusOrthVar + "|" +pNomStatusComb + "|" + pNomStatusOpus + "|" + pNomStatusIned + ")";
    protected static String pNomStatusPhrase1 = "," + fWs + pNomStatus;
    protected static String pNomStatusPhrase2 = "\\[" + fWs + pNomStatus + "\\]";

    protected static String pNomStatusPhrase = "(?:" + pNomStatusPhrase1 + "|" + pNomStatusPhrase2 + ")";

// Soraya
//opus utique oppr.
//pro syn.
//provisional synonym
//fossil name


    //cultivars and hybrids
    protected static String cultivar = oWs + "'..+'"; //Achtung mit Hochkomma in AuthorNamen
    protected static String cultivarMarker = oWs + "(cv\\.|')";
    protected static String notho = "notho";
    protected static String hybridPart = "([xX]" + oWs + "|"+hybridSign+"|"+notho+")";
    protected static String noNothoHybridPart = "([xX]" + oWs + "|"+hybridSign+")";
    protected static String hybridFull = "(" +oWs +"|"+ pStart +")" + noNothoHybridPart;  //for some reason infraspecific notho ranks do not parse if notho is allowed as uninomial prefix.
    protected static String hybridFormularSeparator = oWs + "[" + hybridSign + "xX]" + oWs;


    //  Name String
    protected static String genusOrSupraGenus = "("+hybridFull+")?" + capitalEpiWord;
    protected static String infraGenus = capitalEpiWord + oWs + InfraGenusMarker + oWs + capitalEpiWord;
    protected static String aggrOrGroup = capitalEpiWord + oWs + nonCapitalEpiWord + oWs + aggrOrGroupMarker;
    protected static String species = genusOrSupraGenus + oWs + "("+hybridPart+")?" + nonCapitalEpiWord;
    protected static String speciesWithInfraGen = genusOrSupraGenus + oWs + "\\(" + capitalEpiWord + "\\)" + oWs + nonCapitalEpiWord;

    protected static String infraSpecies = species + oWs + infraSpeciesMarker + oWs + "("+hybridPart+")?" + nonCapitalEpiWord;
    protected static String zooInfraSpecies = species + oWs + "(" + infraSpeciesMarker + oWs +")?" + "("+hybridPart+")?" + nonCapitalEpiWord;
    protected static String oldInfraSpecies = capitalEpiWord + oWs +  nonCapitalEpiWord + oWs + oldInfraSpeciesMarker + oWs + nonCapitalEpiWord;
    protected static String autonym = capitalEpiWord + oWs + "(" + nonCapitalEpiWord +")" + oWs + fullBotanicAuthorString +  oWs + infraSpeciesMarker + oWs + "\\1";  //2-nd word and last word are the same
    //autonym pattern used within anyBotanicalFullName pattern
    protected static String autonym2 = capitalEpiWord + oWs + "(" + nonCapitalEpiWord +")" + oWs + fullBotanicAuthorString +  oWs + infraSpeciesMarker + oWs + "\\2";  //2-nd word and last word are the same


    protected static String anyBotanicName = "(" + genusOrSupraGenus + "|" + infraGenus + "|" + aggrOrGroup + "|" + species + "|" +
                    speciesWithInfraGen + "|" + infraSpecies + "|" + oldInfraSpecies + "|" + autonym   + ")+";
    protected static String anyZooName = "(" + genusOrSupraGenus + "|" + infraGenus + "|" + aggrOrGroup + "|" + species + "|" +
                    speciesWithInfraGen + "|" +zooInfraSpecies + "|" +  oldInfraSpecies + ")+";
    protected static String anyBotanicFullName = "(" + autonym2 + "|" + anyBotanicName + oWs + fullBotanicAuthorString + ")"  ;
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
    protected static Pattern cultivarPattern = Pattern.compile(cultivar);
    protected static Pattern cultivarMarkerPattern = Pattern.compile(cultivarMarker);

    protected static Pattern genusOrSupraGenusPattern = Pattern.compile(pStart + genusOrSupraGenus + facultFullAuthorString2 + end);
    protected static Pattern infraGenusPattern = Pattern.compile(pStart + infraGenus + facultFullAuthorString2 + end);
    protected static Pattern aggrOrGroupPattern = Pattern.compile(pStart + aggrOrGroup + fWs + end); //aggr. or group has no author string
    protected static Pattern speciesPattern = Pattern.compile(pStart + species + facultFullAuthorString2 + end);
    protected static Pattern speciesWithInfraGenPattern = Pattern.compile(pStart + speciesWithInfraGen + facultFullAuthorString2 + end);
    protected static Pattern infraSpeciesPattern = Pattern.compile(pStart + infraSpecies + facultFullAuthorString2 + end);
    protected static Pattern zooInfraSpeciesPattern = Pattern.compile(pStart + zooInfraSpecies + facultFullAuthorString2 + end);
    protected static Pattern oldInfraSpeciesPattern = Pattern.compile(pStart + oldInfraSpecies + facultFullAuthorString2 + end);
    protected static Pattern autonymPattern = Pattern.compile(pStart + autonym + fWs + end);
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
    protected static Pattern anyZooFullNamePattern = Pattern.compile(anyZooFullName);


}
