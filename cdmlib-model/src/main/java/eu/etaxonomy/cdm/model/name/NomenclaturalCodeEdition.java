/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.DOI;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.term.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.term.IEnumTerm;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * The class for the nomenclature code edition of the 5 nomenclatural codes (ICNP, ICBN, ICNCP, ICZN and ICVCN)
 * ruling {@link TaxonName taxon names}.
 * <P>
 * See also https://www.wikidata.org/wiki/Q693148 and https://dev.e-taxonomy.eu/redmine/issues/9640
 * <P>
 * @see NomenclaturalCode
 *
 * @author a.mueller
 * @since 23.07.2019
 */
@XmlType(name = "NomenclaturalCodeEdition")
@XmlEnum
public enum NomenclaturalCodeEdition implements IEnumTerm<NomenclaturalCodeEdition> {

    //0
    /**
     * International Code of Nomenclature for algae, fungi, and plants.
     * Madrid 2017
     * Turland, N.J., Wiersema, J.H., Barrie, F.R., Gandhi, K.N., Gravendyck, J., Greuter, W.R., Hawksworth, D.L., Herendeen, P.S., Klopper, R.R., Knapp, S.D., Kusber, W.-H., Li, D.Z., May, T.W., Monro, A.M., Prado, J., Price, M.J., Smith, G.F. & Zamora Señoret, J.C 2025: International Code of Nomenclature for algae, fungi, and plants (Madrid Code): 1-288. – Chicago: The University of Chicago Press
     */
    @XmlEnumValue("Madrid")
    ICN_2024_MADRID(UUID.fromString("acac1bdb-3227-4cbf-b945-c252af439a0c"), "ICN", "Madrid", 2024, 2025, NomenclaturalCode.ICNAFP, null, "10.7208/chicago/9780226839479.001.0001" ),

    //1
    /**
     * International Code of Nomenclature for algae, fungi, and plants.
     * Shenzhen 2017
     * Turland, N. J., Wiersema, J. H., Barrie, F. R., Greuter, W., Hawksworth, D. L., Herendeen, P. S., Knapp, S., Kusber, W.-H., Li, D.-Z., Marhold, K., May, T. W., McNeill, J., Monro, A. M., Prado, J., Price, M. J. & Smith, G. F. (eds.) 2018: International Code of Nomenclature for algae, fungi, and plants (Shenzhen Code) adopted by the Nineteenth International Botanical Congress Shenzhen, China, July 2017. Regnum Vegetabile 159. Glashütten: Koeltz Botanical Books. DOI https://doi.org/10.12705/Code.2018
     */
    @XmlEnumValue("Shenzhen")
    ICN_2017_SHENZHEN(UUID.fromString("87e8ac37-97c4-43c7-a016-43f1a5c3503f"), "ICN", "Shenzhen", 2017, 2018, NomenclaturalCode.ICNAFP, "Q56701992", "10.12705/Code.2018" ),

    //2
    /**
     * International Code of Nomenclature for algae, fungi, and plants.
     * Melbourne 2011
     */
    @XmlEnumValue("Melbourne")
    ICN_2011_MELBOURNE(UUID.fromString("ea2ebf9e-e3eb-4aaf-8007-c6f9f8877451"), "ICN", "Melbourne", 2011, 2012, NomenclaturalCode.ICNAFP, "Q15895076", null),

    //3
    /**
     * International Code of Botanical Nomenclature.
     * Vienna 2005
     */
    @XmlEnumValue("Vienna")
    ICN_2005_VIENNA(UUID.fromString("d8f9f3d6-96af-4d83-a8d5-04ff62ba4d9c"), "ICBN", "Vienna", 2005, 2006, NomenclaturalCode.ICNAFP, "Q15895126", null),

    //4
    /**
     * International Code of Botanical Nomenclature.
     * Saint Louis 1999
     */
    @XmlEnumValue("Saint Louis")
    ICN_1999_ST_LOUIS(UUID.fromString("2746b72e-43be-4073-90d0-494a7afac271"), "ICBN", "Saint Louis", 1999, 2000, NomenclaturalCode.ICNAFP, "Q15895151", null),

    //5
    /**
     * International Code of Botanical Nomenclature.
     * Tokyo 1993
     */
    @XmlEnumValue("Tokyo")
    ICN_1993_TOKYO(UUID.fromString("5a846761-839f-4f77-b614-7181b9b29355"), "ICBN", "Tokyo", 1993, 1994, NomenclaturalCode.ICNAFP, "Q15895201", null),

    //6
    /**
     * International Code of Botanical Nomenclature.
     * Berlin 1987
     */
    @XmlEnumValue("Berlin")
    ICN_1987_BERLIN(UUID.fromString("5702fc16-e194-4ad2-bdaf-4451af523db2"), "ICBN", "Berlin", 1987, 1988, NomenclaturalCode.ICNAFP, null, null),

    //7
    /**
     * International Code of Botanical Nomenclature.
     * Sydney 1981
     */
    @XmlEnumValue("Sydney")
    ICN_1981_SYDNEY(UUID.fromString("f956c556-6d79-4e0f-adeb-ed9d81f2fa24"), "ICBN", "Sydney", 1981, 1983, NomenclaturalCode.ICNAFP, null, null),

    //8
    /**
     * International Code of Botanical Nomenclature.
     * Leningrad 1975
     */
    @XmlEnumValue("Leningrad")
    ICN_1975_LENINGRAD(UUID.fromString("f52e50b4-4c75-42dd-8efe-39cd966da712"), "ICBN", "Leningrad", 1975, 1978, NomenclaturalCode.ICNAFP, null, null),

    //9
    /**
     * International Code of Botanical Nomenclature.
     * Seattle 1969
     */
    @XmlEnumValue("Seattle")
    ICN_1969_SEATTLE(UUID.fromString("2d19b5af-27cc-428e-affb-8a06563ff9cd"), "ICBN", "Seattle", 1969, 1972, NomenclaturalCode.ICNAFP, null, null),

    //10
    /**
     * International Code of Botanical Nomenclature.
     * Edinburgh 1959
     */
    @XmlEnumValue("Edinburgh")
    ICN_1964_EDINBURGH(UUID.fromString("7ca16490-10a4-4991-9d70-2aade1706c76"), "ICBN", "Edinburgh", 1964, 1966, NomenclaturalCode.ICNAFP, null, null),

    //11
    /**
     * International Code of Botanical Nomenclature.
     * Montreal 1959
     */
    @XmlEnumValue("Montreal")
    ICN_1959_EDINBURGH(UUID.fromString("bebcdcf2-b479-43d0-8635-f586334be204"), "ICBN", "Montreal", 1959, 1961, NomenclaturalCode.ICNAFP, null, null),

    //12
    /**
     * International Code of Botanical Nomenclature.
     * Paris 1954
     */
    @XmlEnumValue("Paris")
    ICN_1954_PARIS(UUID.fromString("c57adbf9-890e-4e38-a667-3fdf2328f3c8"), "ICBN", "Paris", 1954, 1956, NomenclaturalCode.ICNAFP, null, null),

    //13
    /**
     * International Code of Botanical Nomenclature.
     * Stockholm 1952
     */
    @XmlEnumValue("Stockholm")
    ICN_1950_STOCKHOLM(UUID.fromString("a0c07e5f-9b2b-4cd7-9f14-acd4729f175a"), "ICBN", "Stockholm", 1950, 1952, NomenclaturalCode.ICNAFP, null, null),

    //14
    /**
     * International Code of Botanical Nomenclature.
     * Stockholm 1952
     */
    @XmlEnumValue("Amsterdam")
    ICN_1935_AMSTERDAM(UUID.fromString("4676654f-41ef-4311-a708-ba9b4b301117"), "ICBN", "Amsterdam", 1935, 1950, NomenclaturalCode.ICNAFP, null, null),

    //15
    /**
     * International Code of Botanical Nomenclature.
     * Cambridge Rules 1935
     */
    @XmlEnumValue("Cambridge")
    //TODO abbrev correct?
    ICN_1930_CAMBRIDGE(UUID.fromString("e6f7e578-2ce5-434d-9904-aaf2a6e06c43"), "Rules", "Cambrdige", 1930, 1935, NomenclaturalCode.ICNAFP, null, null),

    //16
    /**
     * International Code of Botanical Nomenclature.
     * Cambridge Rules 1935
     */
    @XmlEnumValue("Brussels")
    //TODO abbrev correct?
    ICN_1910_BRUSSELS(UUID.fromString("954c4a71-86ec-489d-b7f1-1878e3b31ebb"), "Rules", "Brussels", 1910, 1912, NomenclaturalCode.ICNAFP, null, null),

    //17
    /**
     * International Code of Botanical Nomenclature.
     * Vienna Rules 1905
     */
    @XmlEnumValue("Vienna 1905")
    //TODO abbrev correct?
    ICN_1905_VIENNA(UUID.fromString("23781153-540f-4c54-a5ae-5d6b6f490332"), "Rules", "Vienna", 1905, 1906, NomenclaturalCode.ICNAFP, null, null),

    //18
    /**
     * International Code of Botanical Nomenclature.
     * Laws of botanical nomenclature 1867
     */
    @XmlEnumValue("Laws")
    //TODO abbrev correct?
    ICN_1867_LAWS(UUID.fromString("230314c3-0bb0-4488-8597-f4ef63d27781"), "Rules", "Laws of botanical nomenclature", 1867, 1867, NomenclaturalCode.ICNAFP, null, null),

//****************** FUNGI *******************/

    //f1
    /**
     * International Code of Botanical Nomenclature.
     * Chapter F, San Juan, 2018
     */
    @XmlEnumValue("San Juan CF 2018")
    ICN_2018_CHAP_F(UUID.fromString("c6404676-a925-418f-bbb8-7661dee125dc"), "ICNAFP-F", "Chapter F, San Juan", 2018, 2019, NomenclaturalCode.Fungi, "Q56701992","10.1186/s43008-019-0019-1"),

 //*********** ICZN ******************/
    //z1
    /**
     * International Code of Zoological Nomenclature. Fourth edition. The International Trust for Zoological Nomenclature, London, UK.
     * https://en.wikipedia.org/wiki/International_Code_of_Zoological_Nomenclature#Versions
     */
    @XmlEnumValue("ICZN_1999")
    ICZN_1999(UUID.fromString("98f61693-67c5-40e1-a802-4989cb5ac4eb"), "ICZN", null, 1999, 1999, NomenclaturalCode.ICZN, null, "10.5962/bhl.title.50608"),

    //z2
    /**
     * International Code of Zoological Nomenclature. Third edition. The International Trust for Zoological Nomenclature, London, UK.
     * https://en.wikipedia.org/wiki/International_Code_of_Zoological_Nomenclature#Versions
     */
    @XmlEnumValue("ICZN_1985")
    ICZN_1985(UUID.fromString("093110c8-f7e8-4f86-a794-e8e7b17c99d6"), "ICZN", null, 1985, 1985, NomenclaturalCode.ICZN, null, "10.5962/bhl.title.50611"),

    //z3
    /**
     * International Code of Zoological Nomenclature. Second edition. The International Trust for Zoological Nomenclature, London, UK.
     * https://en.wikipedia.org/wiki/International_Code_of_Zoological_Nomenclature#Versions
     */
    @XmlEnumValue("ICZN_1964")
    ICZN_1964(UUID.fromString("2b4a9e59-45dd-444b-bf93-741bc727c38c"), "ICZN", null, 1964, 1964, NomenclaturalCode.ICZN, null, "10.5962/bhl.title.50606"),

    //z4
    /**
     * International Code of Zoological Nomenclature: adopted by the XV International Congress of Zoology. The International Trust for Zoological Nomenclature, London, UK.
     * https://en.wikipedia.org/wiki/International_Code_of_Zoological_Nomenclature#Versions
     */
    @XmlEnumValue("ICZN_1961")
    ICZN_1961(UUID.fromString("b9de5507-1171-496c-8a8d-8af47e42724e"), "ICZN", null, 1961, 1961, NomenclaturalCode.ICZN, null, "10.5962/bhl.title.50303"),

    //TODO older versions, see wikipedia

   //bact1
    /**
     * Parker, C.T., Tindall, B.J. & Garrity, G.M. (2019) International Code of Nomenclature of Prokaryotes. 2008 Revision. Microbiology Society
     * https://en.wikipedia.org/wiki/International_Code_of_Nomenclature_of_Prokaryotes#Versions
     */
    @XmlEnumValue("ICNP_2008")
    ICNP_2008(UUID.fromString("1297a8e9-dfde-4db7-9f93-672453e8b3e6"), "ICNP", null, 2008, 2008, NomenclaturalCode.ICNP, null, "10.1099/ijsem.0.000778"),

    //bact2
    /**
     * Lapage, S.P., Sneath, P.H.A., Lessel, E.F., Skerman, V.B.D., Seeliger, H.P.R. & Clark, W.A. (1992). International Code of Nomenclature of Bacteria. Bacteriological Code. 1990 Revision. American Society for Microbiology, Washington, D.C., ISBN 1-55581-039-X
     * https://en.wikipedia.org/wiki/International_Code_of_Nomenclature_of_Prokaryotes#Versions
     */
    @XmlEnumValue("ICNP_1990")
    ICNP_1990(UUID.fromString("fd5d6dd3-de58-4244-8290-c8d89323f163"), "ICNB", null, 1990, 1990, NomenclaturalCode.ICNP, null, "10.5962/bhl.title.50303"),

    //bact3
    /**
     * Lapage, S.P., Sneath, P.H.A., Lessel, E.F., Skerman, V.B.D., Seeliger, H.P.R. & Clark, W.A. (1975). International Code of Nomenclature of Bacteria. 1975 Revision. American Society of Microbiology, Washington, D.C
     * https://en.wikipedia.org/wiki/International_Code_of_Nomenclature_of_Prokaryotes#Versions
     */
    @XmlEnumValue("ICNP_1975")
    ICNP_1975(UUID.fromString("2dafaf3c-abf6-4baf-82db-3332db25d654"), "ICNB", null, 1975, 1975, NomenclaturalCode.ICNP, null, null),

    //cult plants 1
    /**
     * ISHS Secretariat: ICNCP - International Code for the Nomenclature for Cultivated Plants (9th edition). International Society for Horticultural Science, 15. Juni 2016
     * https://www.ishs.org/news/icncp-international-code-nomenclature-cultivated-plants-9th-edition
     */
    @XmlEnumValue("ICNCP_2016")
    ICNCP_2016(UUID.fromString("e395e7be-405f-4214-a3a2-2b19eef6a055"), "ICNCP", null, 2016, 2016, NomenclaturalCode.ICNCP, null, null),

    //cult plants 2
    /**
     * International Code of Nomenclature for Cultivated Plants. 8th edition. [Scripta Horticulturae 10]
     * https://www.ishs.org/news/icncp-international-code-nomenclature-cultivated-plants-9th-edition
     */
    @XmlEnumValue("ICNCP_2009")
    ICNCP_2009(UUID.fromString("bc66202a-0ffb-4cf3-a1c9-520efd5e76e9"), "ICNCP", null, 2009, 2009, NomenclaturalCode.ICNCP, null, null),

    //cult plants 3
    /**
     * SHS (International Society for Horticultural Science; Hrsg.): International Code of Nomenclature for Cultivated Plants, 7th edition. In: Acta Horticulturae Band 647, 2004. ISBN 90-6605-527-8.
     * https://de.wikipedia.org/wiki/Internationaler_Code_der_Nomenklatur_der_Kulturpflanzen
     */
    @XmlEnumValue("ICNCP_2004")
    ICNCP_2004(UUID.fromString("a29abcee-1f34-4320-afad-eb7ee23cc473"), "ICNCP", null, 2004, 2004, NomenclaturalCode.ICNCP, null, null),

    //virus1
    /**
     * https://talk.ictvonline.org/ictv-reports/ictv_online_report/
     * 10th report
     */
    @XmlEnumValue("ICVCN_2018")
    //TODO abbrev correct?
    ICVCN_2018(UUID.fromString("6a0debf3-7932-4b0c-b1b1-46641c273f36"), "ICVCN", null, 2019, 2019, NomenclaturalCode.ICVCN, null, null),

    //virus2
    /**
     * https://talk.ictvonline.org/ictv-reports/ictv_9th_report/
     * 9th report
     */
    @XmlEnumValue("ICVCN_2011")
    //TODO abbrev correct?
    ICVCN_2011(UUID.fromString("b55c0209-59e2-4fa2-9d64-63addfb15d6b"), "ICVCN", null, 2011, 2011, NomenclaturalCode.ICVCN, null, null),

    ;

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    private static final String WIKIDATA_BASE_URL = "https://www.wikidata.org/wiki/";

	private String location;

	private Integer congressYear;

	private Integer publicationYear;

	private NomenclaturalCode code;

	private String wikiDataId;

	private DOI doi;

	private String abbrev;

	private Reference citation;

	@SuppressWarnings("unchecked")
    private NomenclaturalCodeEdition(UUID uuid, String abbrev, String location, Integer congressYear, Integer yearPublished, NomenclaturalCode code, String wikiDataId, String strDoi ){
		delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, makeTitleCache(location, code, congressYear), code.getKey() + congressYear, null);
		this.location = location;
		this.congressYear = congressYear;
		this.code = code;
		this.wikiDataId = wikiDataId;
		this.doi = (strDoi==null) ? null:DOI.fromString(strDoi);
	}

    private String makeTitleCache(String location, NomenclaturalCode code, Integer year) {
        return (StringUtils.isNotBlank(location)? location: code.getTitleCache()) + " " + year;
    }

    public String getTitleCache() {
        return getLabel();
    }

	@Override
	public String toString() {
		return this.getLabel();
	}

    public boolean isNonViral() {
        return this.code.isNonViral();
    }
    public boolean isZoological() {
        return this.code.isZoological();
    }
    public boolean isBotanical() {
        return this.code.isBotanical();
    }
    public boolean isCultivar() {
        return this.code.isCultivar();
    }
    public boolean isBacterial() {
        return this.code.isBacterial();
     }
     public boolean isViral() {
         return this.code.isViral();
     }
     public boolean isFungus() {
         return this.code.isFungus();
     }


//	public static NomenclaturalCodeEdition fromString(String string){
//		xx;
//	    for(NomenclaturalCodeEdition code : NomenclaturalCodeEdition.values()){
//			if(code.name().equalsIgnoreCase(string)) {
//				return code;
//			}
//		}
//		return null;
//	}

    public static List<NomenclaturalCodeEdition> forCode(NomenclaturalCode code){
        List<NomenclaturalCodeEdition> result = new ArrayList<>();

        Set<NomenclaturalCode> allCodes = code.getGeneralizationOf(true);
        allCodes.add(code);

        for(NomenclaturalCodeEdition edition : NomenclaturalCodeEdition.values()){
            for (NomenclaturalCode nomCode: allCodes){
                if(edition.getCode().equals(nomCode) && !result.contains(edition)) {
                    result.add(edition);
                }
            }
        }
        return result;
    }

    public String getAbbrev() {
          return abbrev;
    }

    public String getLocation() {
        return location;
    }
    public Integer getCongressYear() {
        return congressYear;
    }
    public NomenclaturalCode getCode() {
        return code;
    }
    public String getWikiDataId() {
        return wikiDataId;
    }
    public URI getWikiDataUri() {
        return StringUtils.isEmpty(wikiDataId)? null: URI.create(WIKIDATA_BASE_URL + wikiDataId);
    }
    public DOI getDoi() {
        return this.doi;
    }

    private Reference getCitation() {
        if (this == NomenclaturalCodeEdition.ICN_2024_MADRID) {
            citation = ReferenceFactory.newBook();
            citation.setTitle("International Code of Nomenclature for algae, fungi, and plants (Madrid Code). Regnum Vegetabile 162");
            Team team = Team.NewInstance();
            citation.setAuthorship(team);
            team.addTeamMember(Person.NewInstance(null, "Turland", "N.J.", null));
            team.addTeamMember(Person.NewInstance(null, "Wiersema", "J.H.", null));
            team.addTeamMember(Person.NewInstance(null, "Barrie", "F.R.", null));
            team.addTeamMember(Person.NewInstance(null, "Gandhi", "K.N.", null));
            team.addTeamMember(Person.NewInstance(null, "Gravendyck", "J.", null));
            team.addTeamMember(Person.NewInstance(null, "Greuter", "W.", null));
            team.addTeamMember(Person.NewInstance(null, "Hawksworth", "D.L.", null));
            team.addTeamMember(Person.NewInstance(null, "Herendeen", "P.S.", null));
            team.addTeamMember(Person.NewInstance(null, "Klopper", "R.R.", null));
            team.addTeamMember(Person.NewInstance(null, "Knapp", "S.", null));
            team.addTeamMember(Person.NewInstance(null, "Kusber", "W.-H.", null));
            team.addTeamMember(Person.NewInstance(null, "Li", "D.-Z.", null));
            team.addTeamMember(Person.NewInstance(null, "May", "T.W.", null));
            team.addTeamMember(Person.NewInstance(null, "Monro", "A.M.", null));
            team.addTeamMember(Person.NewInstance(null, "Prado", "J.", null));
            team.addTeamMember(Person.NewInstance(null, "Price", "M.J.", null));
            team.addTeamMember(Person.NewInstance(null, "Smith", "G.F.", null));
            team.addTeamMember(Person.NewInstance(null, "Zamora Se" + UTF8.SMALL_N_TILDE + "oret", "J.C.", null));
            citation.setAuthorIsEditor(false);  //here they are all authors //#10733

            citation.setDatePublished(TimePeriodParser.parseStringVerbatim("2025"));
            citation.setPublisher("The University of Chicago Press", "Chicago");
            citation.setDoi(ICN_2024_MADRID.getDoi());
            citation.setIsbn("978-0-226-83946-2");
            citation.setSeriesPart(null);  //TODO ??
            //TODO is this according to the official how to cite? See also Shenzhen Code; https://www.iapt-taxon.org/nomen/pages/intro/citation.html
            citation.setPages("i-xlvii, 1-288");
            citation.setUuid(UUID.fromString("6c2ba9b0-0368-4222-a4c0-5930d7c60825"));
        } else if (this.citation == null) {
            if (this == NomenclaturalCodeEdition.ICN_2017_SHENZHEN) {
                citation = ReferenceFactory.newBook();
                citation.setTitle("International Code of Nomenclature for algae, fungi, and plants (Shenzhen Code), adopted by the Nineteenth International Botanical Congress, Shenzhen, China, July 2017.");
                Team team = Team.NewInstance();
                citation.setAuthorship(team);
                team.addTeamMember(Person.NewInstance(null, "Turland", "N.J.", null));
                team.addTeamMember(Person.NewInstance(null, "Wiersema", "J.H.", null));
                team.addTeamMember(Person.NewInstance(null, "Barrie", "F.R.", null));
                team.addTeamMember(Person.NewInstance(null, "Greuter", "W.", null));
                team.addTeamMember(Person.NewInstance(null, "Hawksworth", "D.L.", null));
                team.addTeamMember(Person.NewInstance(null, "Herendeen", "P.S.", null));
                team.addTeamMember(Person.NewInstance(null, "Knapp", "S.", null));
                team.addTeamMember(Person.NewInstance(null, "Kusber", "W.-H.", null));
                team.addTeamMember(Person.NewInstance(null, "Li", "D.-Z.", null));
                team.addTeamMember(Person.NewInstance(null, "Marhold", "K.", null));
                team.addTeamMember(Person.NewInstance(null, "May", "T.W.", null));
                team.addTeamMember(Person.NewInstance(null, "McNeill", "J.", null));
                team.addTeamMember(Person.NewInstance(null, "Monro", "A.M.", null));
                team.addTeamMember(Person.NewInstance(null, "Prado", "J.", null));
                team.addTeamMember(Person.NewInstance(null, "Price", "M.J.", null));
                team.addTeamMember(Person.NewInstance(null, "Smith", "G.F.", null));
                citation.setAuthorIsEditor(true);

                citation.setDatePublished(TimePeriodParser.parseStringVerbatim("2018"));
                citation.setPublisher("Koeltz Botanical Books", "Glash"+UTF8.U_UMLAUT+"tten");
                citation.setDoi(ICN_2017_SHENZHEN.getDoi());
                citation.setSeriesPart("Regnum Vegetabile 159");
                //removed as it is not part of the official How to cite https://www.iapt-taxon.org/nomen/pages/intro/citation.html
                //citation.setPages("xxxviii + 254 pp.");
                citation.setUuid(UUID.fromString("34426499-8ffe-48aa-bc61-34e2abdea676"));
            } else if (this == NomenclaturalCodeEdition.ICN_2011_MELBOURNE) {
                citation = ReferenceFactory.newBook();
                citation.setTitle("International Code of Nomenclature for algae, fungi, and plants (Melbourne Code), adopted by the Eighteenth International Botanical Congress, Melbourne, Australia, July 2011");
                Team team = Team.NewInstance();
                citation.setAuthorship(team);
                team.addTeamMember(Person.NewInstance(null, "McNeill", "J.", null));
                team.addTeamMember(Person.NewInstance(null, "Barrie", "F.R.", null));
                team.addTeamMember(Person.NewInstance(null, "Buck", "W.R.", null));
                team.addTeamMember(Person.NewInstance(null, "Demoulin", "V.", null));
                team.addTeamMember(Person.NewInstance(null, "Greuter", "W.", null));
                team.addTeamMember(Person.NewInstance(null, "Hawksworth", "D.L.", null));
                team.addTeamMember(Person.NewInstance(null, "Herendeen", "P.S.", null));
                team.addTeamMember(Person.NewInstance(null, "Knapp", "S.", null));
                team.addTeamMember(Person.NewInstance(null, "Marhold", "K.", null));
                team.addTeamMember(Person.NewInstance(null, "Prado", "J.", null));
                team.addTeamMember(Person.NewInstance(null, "Prud’homme van Reine", "W.F.", null));
                team.addTeamMember(Person.NewInstance(null, "Smith", "G.F.", null));
                team.addTeamMember(Person.NewInstance(null, "Wiersema", "J.H.", null));
                team.addTeamMember(Person.NewInstance(null, "Turland", "N.J.", null));
                citation.setAuthorIsEditor(true);

                citation.setDatePublished(TimePeriodParser.parseStringVerbatim("2012"));
                citation.setPublisher("Koeltz Scientific Books", "K"+UTF8.O_UMLAUT+"nigstein");
                citation.setDoi(ICN_2011_MELBOURNE.getDoi());
                citation.setSeriesPart("Regnum Vegetabile 154");
                //removed as it is not part of the official How to cite https://www.iapt-taxon.org/nomen/pages/intro/citation.html
                //citation.setPages("xxx + 208 pp.");
                citation.setUuid(UUID.fromString("14f0c87e-b536-499c-95db-2ceb9cb1f871"));
                citation.setIsbn("978-3-87429-425-6");
            } else if (this == NomenclaturalCodeEdition.ICN_2005_VIENNA) {
                citation = ReferenceFactory.newBook();
                citation.setTitle("International Code of Botanical Nomenclature (Vienna Code), adopted by the Seventeenth International Botanical Congress, Vienna, Austria, July 2005");
                Team team = Team.NewInstance();
                citation.setAuthorship(team);
                team.addTeamMember(Person.NewInstance(null, "McNeill", "J.", null));
                team.addTeamMember(Person.NewInstance(null, "Barrie", "F.R.", null));
                team.addTeamMember(Person.NewInstance(null, "Burdet", "H.M.", null));
                team.addTeamMember(Person.NewInstance(null, "Demoulin", "V.", null));
                team.addTeamMember(Person.NewInstance(null, "Hawksworth", "D.L.", null));
                team.addTeamMember(Person.NewInstance(null, "Marhold", "K.", null));
                team.addTeamMember(Person.NewInstance(null, "Nicolson", "D.H.", null));
                team.addTeamMember(Person.NewInstance(null, "Prado", "J.", null));
                team.addTeamMember(Person.NewInstance(null, "Silva", "P.C.", null));
                team.addTeamMember(Person.NewInstance(null, "Skog", "J.E.", null));
                team.addTeamMember(Person.NewInstance(null, "Wiersema", "J.H.", null));
                team.addTeamMember(Person.NewInstance(null, "Turland", "N.J.", null));
                citation.setAuthorIsEditor(true);

                citation.setDatePublished(TimePeriodParser.parseStringVerbatim("2006"));
                citation.setPublisher("A.R.G. Gantner Verlag KG", null);
                citation.setDoi(getDoi());
                citation.setSeriesPart("Regnum Vegetabile 146");
                //removed as it is not part of the official How to cite https://www.iapt-taxon.org/nomen/pages/intro/citation.html
                //citation.setPages("xviii + 568 pp.");
                citation.setUuid(UUID.fromString("5851fd49-c30d-48c2-8d6f-4f6ee8e4b832"));
                citation.setIsbn("3-906166-48-1");
            } else if (this == NomenclaturalCodeEdition.ICN_1999_ST_LOUIS) {
                citation = ReferenceFactory.newBook();
                citation.setTitle("International Code of Botanical Nomenclature (Saint Louis Code), adopted by the Sixteenth International Botanical Congress, St Louis, Missouri, July-August 1999");
                Team team = Team.NewInstance();
                citation.setAuthorship(team);
                team.addTeamMember(Person.NewInstance(null, "Greuter", "W.", null));
                team.addTeamMember(Person.NewInstance(null, "McNeill", "J.", null));
                team.addTeamMember(Person.NewInstance(null, "Barrie", "F.R.", null));
                team.addTeamMember(Person.NewInstance(null, "Burdet", "H.M.", null));
                team.addTeamMember(Person.NewInstance(null, "Demoulin", "V.", null));
                team.addTeamMember(Person.NewInstance(null, "Filguerias", "T.S.", null));
                team.addTeamMember(Person.NewInstance(null, "Nicolson", "D.H.", null));
                team.addTeamMember(Person.NewInstance(null, "Silva", "P.C.", null));
                team.addTeamMember(Person.NewInstance(null, "Skog", "J.E.", null));
                team.addTeamMember(Person.NewInstance(null, "Trehane", "P.", null));
                team.addTeamMember(Person.NewInstance(null, "Turland", "N.J.", null));
                team.addTeamMember(Person.NewInstance(null, "Hawksworth", "D.L.", null));
                citation.setAuthorIsEditor(true);

                citation.setDatePublished(TimePeriodParser.parseStringVerbatim("2000"));
                citation.setPublisher("Koeltz Scientific Books", "K"+UTF8.O_UMLAUT+"nigstein");
                citation.setDoi(getDoi());
                citation.setSeriesPart("Regnum Vegetabile 138");
                //removed as it is not part of the official How to cite https://www.iapt-taxon.org/nomen/pages/intro/citation.html
//                citation.setPages("xviii + 474 pp.");
                citation.setUuid(UUID.fromString("f5e152aa-ce36-4e67-91e6-91dda2a7240d")); //any
                citation.setIsbn("3-904144-22-7");
            } else if (this == NomenclaturalCodeEdition.ICN_2018_CHAP_F) {
                citation = ReferenceFactory.newArticle();
                citation.setTitle("Chapter F of the International Code of Nomenclature for algae, fungi, and plants as approved by the 11th International Mycological Congress, San Juan, Puerto Rico, July 2018");
                Team team = Team.NewInstance();
                citation.setAuthorship(team);
                team.addTeamMember(Person.NewInstance(null, "May", "T.W.", null));
                team.addTeamMember(Person.NewInstance(null, "Redhead", "S.A.", null));
                team.addTeamMember(Person.NewInstance(null, "Bensch", "K.", null));
                team.addTeamMember(Person.NewInstance(null, "Hawksworth", "D.L.", null));
                team.addTeamMember(Person.NewInstance(null, "Lendemer", "J.", null));
                team.addTeamMember(Person.NewInstance(null, "Lombard", "L.", null));
                team.addTeamMember(Person.NewInstance(null, "Turland", "N.J.", null));
                citation.setAuthorIsEditor(true);

                citation.setDatePublished(TimePeriodParser.parseStringVerbatim("2019"));
                Reference journal = ReferenceFactory.newJournal();
                citation.setInJournal(journal);
                journal.setTitle("IMA Fungus");
                citation.setVolume("10, 21");
                citation.setDoi(getDoi());
                citation.setUuid(UUID.fromString("5da7685b-18bf-4a2a-9b6a-b19f1c8747f7"));
                citation.setIsbn("3-904144-22-7");
            }else {
                //TODO other nom codes not yet implemented
            }
        }
        return citation;
    }

    public IdentifiableSource getSource() {
        if (getCitation() != null) {
            return IdentifiableSource.NewPrimarySourceInstance(getCitation(), null);
        } else {
            return null;
        }
    }

// *************************** DELEGATE **************************************/

	private static EnumeratedTermVoc<NomenclaturalCodeEdition> delegateVoc;
	private IEnumTerm<NomenclaturalCodeEdition> delegateVocTerm;

	static {
		delegateVoc = EnumeratedTermVoc.getVoc(NomenclaturalCodeEdition.class);
	}

	@Override
	public String getKey(){return delegateVocTerm.getKey();}

	@Override
    public String getLabel(){return delegateVocTerm.getLabel();}

	@Override
    public String getLabel(Language language){return delegateVocTerm.getLabel(language);}


	@Override
    public UUID getUuid() {return delegateVocTerm.getUuid();}

	@Override
    public NomenclaturalCodeEdition getKindOf() {return delegateVocTerm.getKindOf();}

	@Override
    public Set<NomenclaturalCodeEdition> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

	@Override
	public boolean isKindOf(NomenclaturalCodeEdition ancestor) {return delegateVocTerm.isKindOf(ancestor);	}

	@Override
    public Set<NomenclaturalCodeEdition> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}


	public static NomenclaturalCodeEdition getByKey(String key){return delegateVoc.getByKey(key);}
    public static NomenclaturalCodeEdition getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}
}