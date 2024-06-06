/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.taxon;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * This class makes allows to compare two {@link TaxonBase taxa} by
 * comparing the publication dates of the corresponding
 * {@link eu.etaxonomy.cdm.model.name.TaxonName taxon names}.
 *
 * For ordering taxa within a homotypic group it is prefered
 * to use the {@link HomotypicGroupTaxonComparator} instead
 * which gives more exact result.
 *
 * This comparator here is mostly for comparing taxa outside
 * a homotypic group e.g. comparing two taxa that belong to
 * 2 different homotypic groups. This is e.g. relevant when
 * ordering homotypic groups by there "first" taxon name.
 *
 * @author a.mueller
 * @since 11.06.2008
 */
public class TaxonComparator implements Comparator<TaxonBase>, Serializable {

    private static final long serialVersionUID = -1433623743189043446L;
	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	final private boolean includeRanks;

    public TaxonComparator() {
        this.includeRanks = false;
    }

    public TaxonComparator(boolean includeRanks) {
        this.includeRanks = includeRanks;
    }

    /**
     * Returns an integer generated by comparing first the nomenclatural status and then the
     * {@link eu.etaxonomy.cdm.model.name.INomenclaturalReference#getYear() publication years}
     * of both {@link eu.etaxonomy.cdm.model.name.TaxonName taxon names}
     * used in the given {@link TaxonBase taxa}.
     * If 1 name has status of type nom. inval. or nom. nudum the name is put to the end of a
     * list (returns +1 for a status in taxon1 and -1 for a status in taxon2). If both do have
     * no status or the same status, the publication date is taken for comparison.
     * Nom. nudum is handled as more "severe" status then nom.inval.
     *
     * Returns a negative value if the publication year corresponding to the
     * first given taxon precedes the publication year corresponding to the
     * second given taxon. Returns a positive value if the contrary is true and
     * 0 if both publication years and the date, when they are created, are identical.
     * In case one of the publication
     * years is "null" and the other is not, the "empty" publication year will
     * be considered to be always preceded by the "not null" publication year.
     * If both publication years are "null" the creation date is used for the comparison
     *
     * @see		java.lang.String#compareTo(String)
     * @see		java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(
            @SuppressWarnings("rawtypes") TaxonBase taxonBase1,
            @SuppressWarnings("rawtypes") TaxonBase taxonBase2) {
        int result;

        if (taxonBase1 == null && taxonBase2 == null){
            return 0;
        }else if (taxonBase1 == null){
            return -1;
        }else if (taxonBase2 == null){
            return 1;
        }
        if (taxonBase1.equals(taxonBase2)){
        	return 0;
        }

        TaxonName name1 = taxonBase1.getName();
        TaxonName name2 = taxonBase2.getName();

        //set to end if a taxon has nomenclatural status "nom. inval." or "nom. nud."
        int statusCompareWeight = compareStatus(name1, name2);

        if (statusCompareWeight != 0){
        	return Integer.signum(statusCompareWeight);
        }

        //TODO discuss if we should also include nom. illeg. here on taxon level comparison
        result = compare(name1, name2, false);

        if (result == 0){
            DateTime date11 = taxonBase1.getCreated();
            DateTime date12 = taxonBase2.getCreated();
            if (date11 == null && date12 == null) {
                result = 0;
            }else if (date11 == null) {
                return 1;
            }else if (date12 == null) {
                return -1;
            }else{
            	result = date11.compareTo(date12);
            }
        }
        if (result == 0){
        	//the Comparator contract
        	return taxonBase1.getUuid().compareTo(taxonBase1.getUuid());
        }else{
        	return result;
        }
    }

    protected int compareStatus(TaxonName taxonName, TaxonName taxonName2) {
        int statusCompareWeight = 0;
        statusCompareWeight += computeStatusCompareWeight(taxonName);
        statusCompareWeight -= computeStatusCompareWeight(taxonName2);
        return statusCompareWeight;
    }

	private int computeStatusCompareWeight(TaxonName taxonName) {
        //NOTE: this represented an ordered list for different types of invalid designation status.
        //      but not clear were this order came from, it is partly mentioned
	    //in #5794, it is unclear if it fits to the longer list mentioned in #9272
	    //With #9272 and finally #9566 we decided to handle all invalid status with same weight
		if (taxonName != null && taxonName.isInvalid()){
			return 1;
		}else{
		    return 0;
		}
	}

    protected int compareNomIlleg(TaxonName taxonName1, TaxonName taxonName2) {
        int isNomIlleg1 = isNomIlleg(taxonName1);
        int isNomIlleg2 = isNomIlleg(taxonName2);
        return isNomIlleg1 - isNomIlleg2;
    }

    private int isNomIlleg(TaxonName taxonName) {
        if (taxonName == null || taxonName.getStatus() == null){
            return 0;
        }
        Set<NomenclaturalStatus> status = taxonName.getStatus();
        for (NomenclaturalStatus nomStatus : status){
            if (nomStatus.getType() != null){
                if (nomStatus.getType().equals(NomenclaturalStatusType.ILLEGITIMATE())){
                    return 1;
                }
            }
        }
        return 0;
    }

    private Integer getIntegerDate(TaxonName name){
        Integer result;

       if (name == null){
            result = null;
        }else{
            if (name.isZoological()){
                result = name.getPublicationYear();
            }else{
                Reference ref = name.getNomenclaturalReference();
                if (ref == null){
                    result = null;
                }else{
                    if (ref.getDatePublished() == null){
                    	Reference inRef = ref.getInReference();
                    	if (inRef == null){
                            result = null;
                        }else{
                            if (inRef.getDatePublished() == null){
                            	result = null;
                            }else{
                            	result = ref.getInReference().getDatePublished().getStartYear();
                            }
                        }
                    }else{
                        result = ref.getDatePublished().getStartYear();
                    }
                }
            }
        }

        return result;
    }

    /**
     * @param includeNomIlleg
     *    if <code>true</code> and if both names have no date or same date, the only
     *    name having nom. illeg. state is handled as if the name was published later than the name
     *    without status nom. illeg.
     * @return the compare value
     */
    protected int compare(TaxonName name1, TaxonName name2, boolean includeNomIlleg) {
        int result;

        //dates
        Integer intDate1 = getIntegerDate(name1);
        Integer intDate2 = getIntegerDate(name2);

        if (intDate1 == null && intDate2 == null){
            result = 0;
        }else if (intDate1 == null){
            return 1;
        }else if (intDate2 == null){
            return -1;
        }else{
            result = intDate1.compareTo(intDate2);
        }

        //nom. illeg.
        if (result == 0 && includeNomIlleg){
            result = compareNomIlleg(name1, name2);
            if (result != 0){
                return result;
            }
        }

        if (result == 0 && includeRanks){
            Rank rank1 = name1 == null? null : name1.getRank();
            Rank rank2 = name2 == null? null : name2.getRank();

            if (rank1 == null && rank2 == null){
                result = 0;
            }else if (rank1 == null){
                return 1;
            }else if (rank2 == null){
                return -1;
            }else{
                //for some strange reason compareTo for ranks returns 1 if rank2 is lower. So we add minus (-)
                result = - rank1.compareTo(rank2);
            }
        }

        if (result == 0 && name1 != null && name2 != null){
            result = name1.compareToName(name2);
            if (result != 0){
                return result;
            }
        }
        return result;
    }
}