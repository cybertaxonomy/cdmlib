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
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.DOI;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.term.IEnumTerm;

/**
 * The class for the nomenclature code edition of the 5 nomenclatural codes (ICNB, ICBN, ICNCP, ICZN and ICVCN)
 * ruling {@link TaxonName taxon names}.
 * <P>
 * See also https://www.wikidata.org/wiki/Q693148
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
     * Shenzhen 2017
     * Turland, N. J., Wiersema, J. H., Barrie, F. R., Greuter, W., Hawksworth, D. L., Herendeen, P. S., Knapp, S., Kusber, W.-H., Li, D.-Z., Marhold, K., May, T. W., McNeill, J., Monro, A. M., Prado, J., Price, M. J. & Smith, G. F. (eds.) 2018: International Code of Nomenclature for algae, fungi, and plants (Shenzhen Code) adopted by the Nineteenth International Botanical Congress Shenzhen, China, July 2017. Regnum Vegetabile 159. Glashütten: Koeltz Botanical Books. DOI https://doi.org/10.12705/Code.2018
     */
    @XmlEnumValue("Shenzhen")
    ICN_2017_SHENZHEN(UUID.fromString("87e8ac37-97c4-43c7-a016-43f1a5c3503f"), "Shenzhen", 2017, NomenclaturalCode.ICNAFP, "Q56701992","10.12705/Code.2018" ),

    //1
    /**
     * International Code of Nomenclature for algae, fungi, and plants.
     * Melbourne 2011
     */
    @XmlEnumValue("Melbourne")
    ICN_2011_MELBOURNE(UUID.fromString("ea2ebf9e-e3eb-4aaf-8007-c6f9f8877451"), "Melbourne", 2011, NomenclaturalCode.ICNAFP, "Q15895076", null),

    //2
    /**
     * International Code of Botanical Nomenclature.
     * Vienna 2005
     */
    @XmlEnumValue("Vienna")
    ICN_2005_VIENNA(UUID.fromString("d8f9f3d6-96af-4d83-a8d5-04ff62ba4d9c"), "Vienna", 2005, NomenclaturalCode.ICNAFP, "Q15895126", null),

    //3
    /**
     * International Code of Botanical Nomenclature.
     * Saint Louis 1999
     */
    @XmlEnumValue("Saint Louis")
    ICN_1999_ST_LOUIS(UUID.fromString("2746b72e-43be-4073-90d0-494a7afac271"), "Saint Louis", 1999, NomenclaturalCode.ICNAFP, "Q15895151", null),

    //4
    /**
     * International Code of Botanical Nomenclature.
     * Tokyo 1993
     */
    @XmlEnumValue("Tokyo")
    ICN_1993_TOKYO(UUID.fromString("5a846761-839f-4f77-b614-7181b9b29355"), "Tokyo", 1993, NomenclaturalCode.ICNAFP, "Q15895201", null),

    //5
    /**
     * International Code of Botanical Nomenclature.
     * Berlin 1987
     */
    @XmlEnumValue("Berlin")
    ICN_1987_BERLIN(UUID.fromString("5702fc16-e194-4ad2-bdaf-4451af523db2"), "Berlin", 1987, NomenclaturalCode.ICNAFP, null, null),

    //6
    /**
     * International Code of Botanical Nomenclature.
     * Sydney 1981
     */
    @XmlEnumValue("Sydney")
    ICN_1981_Sydney(UUID.fromString("f956c556-6d79-4e0f-adeb-ed9d81f2fa24"), "Sydney", 1981, NomenclaturalCode.ICNAFP, null, null),

    //7
    /**
     * International Code of Botanical Nomenclature.
     * Leningrad 1975
     */
    @XmlEnumValue("Leningrad")
    ICN_1975_TOKYO(UUID.fromString("f52e50b4-4c75-42dd-8efe-39cd966da712"), "Leningrad", 1975, NomenclaturalCode.ICNAFP, null, null),

    //8
    /**
     * International Code of Botanical Nomenclature.
     * Seattle 1969
     */
    @XmlEnumValue("Seattle")
    ICN_1969_Seattle(UUID.fromString("2d19b5af-27cc-428e-affb-8a06563ff9cd"), "Seattle", 1969, NomenclaturalCode.ICNAFP, null, null),

    //9
    /**
     * International Code of Botanical Nomenclature.
     * Stockholm 1952
     */
    @XmlEnumValue("Stockholm")
    ICN_1952_Stockholm(UUID.fromString("a0c07e5f-9b2b-4cd7-9f14-acd4729f175a"), "Stockholm", 1952, NomenclaturalCode.ICNAFP, null, null),

    //10
    /**
     * International Code of Botanical Nomenclature.
     * Cambridge Rules 1935
     */
    @XmlEnumValue("Cambridge")
    ICN_1935_CAMBRIDGE(UUID.fromString("e6f7e578-2ce5-434d-9904-aaf2a6e06c43"), "Cambrdige", 1935, NomenclaturalCode.ICNAFP, null, null),

    //11
    /**
     * International Code of Botanical Nomenclature.
     * Vienna Rules 1905
     */
    @XmlEnumValue("Vienna 1905")
    ICN_1905_VIENNA(UUID.fromString("23781153-540f-4c54-a5ae-5d6b6f490332"), "Vienna", 1905, NomenclaturalCode.ICNAFP, null, null),

    //12
    /**
     * International Code of Botanical Nomenclature.
     * Laws of botanical nomenclature 1867
     */
    @XmlEnumValue("Laws")
    ICN_1867_LAWS(UUID.fromString("230314c3-0bb0-4488-8597-f4ef63d27781"), "Laws of botanical nomenclature", 1867, NomenclaturalCode.ICNAFP, null, null),


    //*** ICZN **/
    //z1
    /**
     * International Code of Zoological Nomenclature. Fourth edition. The International Trust for Zoological Nomenclature, London, UK.
     * https://en.wikipedia.org/wiki/International_Code_of_Zoological_Nomenclature#Versions
     */
    @XmlEnumValue("ICZN_1999")
    ICZN_1999(UUID.fromString("98f61693-67c5-40e1-a802-4989cb5ac4eb"), null, 1999, NomenclaturalCode.ICZN, null, "10.5962/bhl.title.50608"),

    //z2
    /**
     * International Code of Zoological Nomenclature. Third edition. The International Trust for Zoological Nomenclature, London, UK.
     * https://en.wikipedia.org/wiki/International_Code_of_Zoological_Nomenclature#Versions
     */
    @XmlEnumValue("ICZN_1985")
    ICZN_1985(UUID.fromString("093110c8-f7e8-4f86-a794-e8e7b17c99d6"), null, 1985, NomenclaturalCode.ICZN, null, "10.5962/bhl.title.50611"),

    //z3
    /**
     * International Code of Zoological Nomenclature. Second edition. The International Trust for Zoological Nomenclature, London, UK.
     * https://en.wikipedia.org/wiki/International_Code_of_Zoological_Nomenclature#Versions
     */
    @XmlEnumValue("ICZN_1964")
    ICZN_1964(UUID.fromString("2b4a9e59-45dd-444b-bf93-741bc727c38c"), null, 1964, NomenclaturalCode.ICZN, null, "10.5962/bhl.title.50606"),

    //z4
    /**
     * International Code of Zoological Nomenclature: adopted by the XV International Congress of Zoology. The International Trust for Zoological Nomenclature, London, UK.
     * https://en.wikipedia.org/wiki/International_Code_of_Zoological_Nomenclature#Versions
     */
    @XmlEnumValue("ICZN_1961")
    ICZN_1961(UUID.fromString("b9de5507-1171-496c-8a8d-8af47e42724e"), null, 1961, NomenclaturalCode.ICZN, null, "10.5962/bhl.title.50303"),

    //TODO older versions, see wikipedia

   //bact1
    /**
     * Parker, C.T., Tindall, B.J. & Garrity, G.M. (2019) International Code of Nomenclature of Prokaryotes. 2008 Revision. Microbiology Society
     * https://en.wikipedia.org/wiki/International_Code_of_Nomenclature_of_Prokaryotes#Versions
     */
    @XmlEnumValue("ICNB_2008")
    ICNB_2008(UUID.fromString("1297a8e9-dfde-4db7-9f93-672453e8b3e6"), null, 2008, NomenclaturalCode.ICNB, null, "10.1099/ijsem.0.000778"),

    //bact2
    /**
     * Lapage, S.P., Sneath, P.H.A., Lessel, E.F., Skerman, V.B.D., Seeliger, H.P.R. & Clark, W.A. (1992). International Code of Nomenclature of Bacteria. Bacteriological Code. 1990 Revision. American Society for Microbiology, Washington, D.C., ISBN 1-55581-039-X
     * https://en.wikipedia.org/wiki/International_Code_of_Nomenclature_of_Prokaryotes#Versions
     */
    @XmlEnumValue("ICNB_1990")
    ICNB_1990(UUID.fromString("fd5d6dd3-de58-4244-8290-c8d89323f163"), null, 1990, NomenclaturalCode.ICNB, null, "10.5962/bhl.title.50303"),

    //bact3
    /**
     * Lapage, S.P., Sneath, P.H.A., Lessel, E.F., Skerman, V.B.D., Seeliger, H.P.R. & Clark, W.A. (1975). International Code of Nomenclature of Bacteria. 1975 Revision. American Society of Microbiology, Washington, D.C
     * https://en.wikipedia.org/wiki/International_Code_of_Nomenclature_of_Prokaryotes#Versions
     */
    @XmlEnumValue("ICNB_1975")
    ICNB_1975(UUID.fromString("2dafaf3c-abf6-4baf-82db-3332db25d654"), null, 1975, NomenclaturalCode.ICNB, null, null),

    //cult plants 1
    /**
     * ISHS Secretariat: ICNCP - International Code for the Nomenclature for Cultivated Plants (9th edition). International Society for Horticultural Science, 15. Juni 2016
     * https://www.ishs.org/news/icncp-international-code-nomenclature-cultivated-plants-9th-edition
     */
    @XmlEnumValue("ICNCP_2016")
    ICNB_2016(UUID.fromString("e395e7be-405f-4214-a3a2-2b19eef6a055"), null, 2016, NomenclaturalCode.ICNCP, null, null),

    //cult plants 2
    /**
     * International Code of Nomenclature for Cultivated Plants. 8th edition. [Scripta Horticulturae 10]
     * https://www.ishs.org/news/icncp-international-code-nomenclature-cultivated-plants-9th-edition
     */
    @XmlEnumValue("ICNCP_2009")
    ICNB_2009(UUID.fromString("bc66202a-0ffb-4cf3-a1c9-520efd5e76e9"), null, 2009, NomenclaturalCode.ICNCP, null, null),

    //cult plants 3
    /**
     * SHS (International Society for Horticultural Science; Hrsg.): International Code of Nomenclature for Cultivated Plants, 7th edition. In: Acta Horticulturae Band 647, 2004. ISBN 90-6605-527-8.
     * https://de.wikipedia.org/wiki/Internationaler_Code_der_Nomenklatur_der_Kulturpflanzen
     */
    @XmlEnumValue("ICNCP_2004")
    ICNB_2004(UUID.fromString("a29abcee-1f34-4320-afad-eb7ee23cc473"), null, 2004, NomenclaturalCode.ICNCP, null, null),

    //virus1
    /**
     * https://talk.ictvonline.org/ictv-reports/ictv_online_report/
     * 10th report
     */
    @XmlEnumValue("ICVCN_2018")
    ICVCN_2018(UUID.fromString("6a0debf3-7932-4b0c-b1b1-46641c273f36"), null, 2019, NomenclaturalCode.ICVCN, null, null),

    //virus2
    /**
     * https://talk.ictvonline.org/ictv-reports/ictv_9th_report/
     * 9th report
     */
    @XmlEnumValue("ICVCN_2011")
    ICVCN_2011(UUID.fromString("b55c0209-59e2-4fa2-9d64-63addfb15d6b"), null, 2011, NomenclaturalCode.ICVCN, null, null),

    ;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(NomenclaturalCodeEdition.class);

    private static final String WIKIDATA_BASE_URL = "https://www.wikidata.org/wiki/";

	private String location;

	private Integer year;

	private NomenclaturalCode code;

	private String wikiDataId;

	private DOI doi;

	private NomenclaturalCodeEdition(UUID uuid, String location, Integer year, NomenclaturalCode code, String wikiDataId, String strDoi ){
		delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, makeTitleCache(location, code, year), code.getKey() + year, null);
		this.location = location;
		this.year=year;
		this.code=code;
		this.wikiDataId = wikiDataId;
		this.doi = (strDoi==null) ? null:DOI.fromString(strDoi);
	}

    /**
     * @param location
     * @param code
     * @param year
     * @return
     */
    private String makeTitleCache(String location, NomenclaturalCode code, Integer year) {
        return (StringUtils.isNotBlank(location)? location: code.getTitleCache()) + " " + year;
    }

    public String getTitleCache() {
        return getLabel();
    }

	@Override
	public String toString() {
		return this.name();
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
                  if(edition.getCode().equals(nomCode) && !result.contains(nomCode)) {
                      result.add(edition);
                  }
              }
          }
          return result;
     }


    public String getLocation() {
        return location;
    }
    public Integer getYear() {
        return year;
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
