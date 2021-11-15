/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dto.FeatureDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;

/**
 * @author k.luther
 * @since May 15, 2020
 */
public class DescriptionBaseDto extends EntityDTO<DescriptionBase>{

    private static final long serialVersionUID = -1578895619195062502L;

    private UuidAndTitleCache<Taxon> taxonUuidAndTitleCache;
    private UuidAndTitleCache<SpecimenOrObservationBase> specimenDto;
    private int id;

    private UuidAndTitleCache<TaxonName> nameUuidAndTitleCache;

    private List<DescriptionElementDto> elements = new ArrayList<>();

    private EnumSet<DescriptionType> types = EnumSet.noneOf(DescriptionType.class);


    public DescriptionBaseDto(UUID uuid, String titleCache, UuidAndTitleCache<Taxon> taxonUuidAndTitleCache,  UuidAndTitleCache<SpecimenOrObservationBase> specimenDto, UuidAndTitleCache<TaxonName> nameUuidAndTitleCache, Integer id, List<DescriptionElementDto> elements, EnumSet<DescriptionType> types){
        super(uuid, titleCache);
        this.taxonUuidAndTitleCache = taxonUuidAndTitleCache;
        this.specimenDto = specimenDto;
        this.nameUuidAndTitleCache = nameUuidAndTitleCache;
        this.id = id;
        if (elements != null){
            this.elements = elements;
        }
        this.types = types;


    }

    public DescriptionBaseDto(UuidAndTitleCache<SpecimenOrObservationBase> specimen){
        super(SpecimenDescription.NewInstance());
        specimenDto = specimen;
//        if(specimen instanceof FieldUnit) {
//            specimenDto = FieldUnitDTO.fromEntity((FieldUnit)specimen);
//        } else {
//            specimenDto = DerivedUnitDTO.fromEntity((DerivedUnit)specimen);
//        }
    }

    public static String getDescriptionBaseDtoSelect(){
        String[] result = createSqlParts();

        return result[0]+result[1]+result[2] + result[3];
    }


    private static String[] createSqlParts() {
        String sqlSelectString = ""
                + "select a.uuid, "
                + "a.id, "
                + "a.titleCache, "
                //taxon
                + "t.uuid, "
                + "t.id, "
                + "t.titleCache, "
                //specimen
                + "s.uuid,  "
                + "s.id,  "
                + "s.titleCache, "
                //types
                + "a.types ";

        String sqlFromString =   " FROM DescriptionBase as a ";

        String sqlJoinString =  " LEFT JOIN a.taxon as t "
                + " LEFT JOIN a.describedSpecimenOrObservation as s ";

        String sqlWhereString = " WHERE a.uuid = :uuid";

        String[] result = new String[4];
        result[0] = sqlSelectString;
        result[1] = sqlFromString;
        result[2] = sqlJoinString;
        result[3] = sqlWhereString;
        return result;
    }

    public static String getDescriptionBaseDtoForTaxonSelect(){
        String[] result = createSqlPartsForTaxon();

        return result[0]+result[1]+result[2];
    }

    private static String[] createSqlPartsForTaxon() {



        String sqlSelectString = ""
                + "select a.uuid, "
                + "a.id, "
                + "a.titleCache, "
                //taxon
                + "t.uuid, "
                + "t.id, "
                + "t.titleCache, "

                //types
                + "a.types ";

        String sqlFromString =   " FROM DescriptionBase as a ";

        String sqlJoinString =  " LEFT JOIN a.taxon as t ";

        String sqlWhereString = " WHERE a.taxon.uuid = :uuid";

        String[] result = new String[4];
        result[0] = sqlSelectString;
        result[1] = sqlFromString;
        result[2] = sqlJoinString;
        result[3] = sqlWhereString;
        return result;
    }



//    public DescriptionBaseDto(SpecimenOrObservationBase specimen){
//        this(specimen, null, false, false);
//    }


    public UUID getDescriptionUuid() {
        return getCdmEntity().getUuid();
    }

    public String getTitleCache(){
        return getCdmEntity().getTitleCache();
    }

    public UuidAndTitleCache<Taxon> getTaxonDto() {
        return taxonUuidAndTitleCache;
    }

    public UuidAndTitleCache<SpecimenOrObservationBase> getSpecimenDto() {
        return specimenDto;
    }

    public UuidAndTitleCache<TaxonName> getNameDto() {
        return nameUuidAndTitleCache;
    }

    /**
     * @return
     */
    public List<DescriptionElementDto> getElements() {
        return elements;
    }

    public void addElement(DescriptionElementDto element){
        if (elements == null){
            elements = new ArrayList<>();
        }
        elements.add(element);
    }

    public EnumSet<DescriptionType> getTypes() {
        return types;
    }

    public void setTypes(EnumSet<DescriptionType> types) {
        this.types = types;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static DescriptionBaseDto fromDescription(DescriptionBase desc) {
        UuidAndTitleCache<Taxon> taxonUuidAndTitleCache = null;
        UuidAndTitleCache<SpecimenOrObservationBase> specimenDto = null;
        UuidAndTitleCache<TaxonName> nameUuidAndTitleCache = null;
        if (desc instanceof TaxonDescription){
            Taxon taxon = HibernateProxyHelper.deproxy(((TaxonDescription)desc).getTaxon(), Taxon.class);
            if (taxon != null){
                taxonUuidAndTitleCache = new UuidAndTitleCache<Taxon>(taxon.getUuid(), taxon.getId(), taxon.getTitleCache());
            }
        }
        if (desc instanceof SpecimenDescription){
            SpecimenDescription specimenDesc = HibernateProxyHelper.deproxy(desc, SpecimenDescription.class);
            SpecimenOrObservationBase specimen = specimenDesc.getDescribedSpecimenOrObservation();
            specimenDto = new UuidAndTitleCache<>(specimen.getUuid(), specimen.getId(), specimen.getTitleCache());

//            if (specimen != null){
//                if (specimen instanceof FieldUnit){
//                    specimenDto = FieldUnitDTO.fromEntity((FieldUnit)specimen);
//                }else{
//                    specimenDto = DerivedUnitDTO.fromEntity((DerivedUnit)specimen);
//                }
//            }
        }
        if (desc instanceof TaxonNameDescription){
            TaxonNameDescription nameDesc = HibernateProxyHelper.deproxy(desc, TaxonNameDescription.class);
            TaxonName name = nameDesc.getTaxonName();
            if (name != null){
                nameUuidAndTitleCache = new UuidAndTitleCache<TaxonName>(name.getUuid(), name.getId(), name.getTitleCache());
            }
        }

        List<DescriptionElementDto> elements = new ArrayList<>();
        for (Object element: desc.getElements()){
            if (element instanceof CategoricalData){
                Feature feature = ((CategoricalData) element).getFeature();
                FeatureDto featureDto = FeatureDto.fromFeature(feature);
                CategoricalDataDto dto = CategoricalDataDto.fromCategoricalData((CategoricalData)element);
                elements.add(dto);
            }
            if (element instanceof QuantitativeData){
                Feature feature = ((QuantitativeData) element).getFeature();
                FeatureDto featureDto = FeatureDto.fromFeature(feature);
                QuantitativeDataDto dto = QuantitativeDataDto.fromQuantitativeData((QuantitativeData)element);
                elements.add(dto);
            }
        }


        DescriptionBaseDto dto = new DescriptionBaseDto(desc.getUuid(), desc.getTitleCache(), taxonUuidAndTitleCache, specimenDto, nameUuidAndTitleCache, desc.getId(), elements, desc.getTypes());
        return dto;
    }

    /**
     * @param result
     * @return
     */
    public static List<DescriptionBaseDto> descriptionBaseDtoListFrom(List<Object[]> result) {
        List<DescriptionBaseDto> dtoResult = new ArrayList<>();
        DescriptionBaseDto dto;
        for (Object[] o: result){
            UuidAndTitleCache<Taxon> taxonUuidAndTitleCache = null;
            UuidAndTitleCache<SpecimenOrObservationBase> specimenUuidAndTitleCache = null;
            UuidAndTitleCache<TaxonName> nameUuidAndTitleCache = null;
            EnumSet<DescriptionType> type = null;
            if (o[3] != null){
                taxonUuidAndTitleCache = new UuidAndTitleCache<>((UUID)o[3], (Integer)o[4], (String)o[5]);
            }
            if (o[6] != null && o[6] instanceof UUID){
                specimenUuidAndTitleCache = new UuidAndTitleCache<>((UUID)o[6], (Integer)o[7], (String)o[8]);
            }else if (o[6] instanceof EnumSet<?>){
                type = (EnumSet<DescriptionType>)o[6];
            }else if (o.length >7 && o[9] instanceof EnumSet<?>){
                type = (EnumSet<DescriptionType>)o[9];
            }
//            if (o[9] != null){
//                nameUuidAndTitleCache = new UuidAndTitleCache<>((UUID)o[9], (Integer)o[10], (String)o[11]);
//            }
            dto = new DescriptionBaseDto((UUID)o[0], (String)o[2], taxonUuidAndTitleCache, specimenUuidAndTitleCache, nameUuidAndTitleCache, (Integer)o[1], null, type);
            dtoResult.add(dto);
        }

        return dtoResult;

    }










}
