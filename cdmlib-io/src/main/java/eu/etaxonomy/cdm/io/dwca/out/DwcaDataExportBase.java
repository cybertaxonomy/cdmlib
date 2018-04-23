/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.dwca.out;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IOriginalSource;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @since 18.04.2011
 *
 */
public abstract class DwcaDataExportBase extends DwcaExportBase{

    private static final long serialVersionUID = -5467295551060073610L;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DwcaDataExportBase.class);

    abstract protected void handleTaxonNode(DwcaTaxExportState state, TaxonNode node)throws IOException, FileNotFoundException, UnsupportedEncodingException;




    /**
     * Creates the locationId, locality, countryCode triple
     * @param state
     * @param record
     * @param area
     */
    protected void handleArea(DwcaTaxExportState state, IDwcaAreaRecord record, NamedArea area, TaxonBase<?> taxon, boolean required) {
        if (area != null){
            record.setLocationId(area);
            record.setLocality(area.getLabel());
            if (area.isInstanceOf(Country.class)){
                Country country = CdmBase.deproxy(area, Country.class);
                record.setCountryCode(country.getIso3166_A2());
            }
        }else{
            if (required){
                String message = "Description requires area but area does not exist for taxon " + getTaxonLogString(taxon);
                state.getResult().addWarning(message);
            }
        }
    }


    protected String getTaxonLogString(TaxonBase<?> taxon) {
        return taxon.getTitleCache() + "(" + taxon.getId() + ")";
    }


    protected String getSources(ISourceable<?> sourceable, DwcaTaxExportConfigurator config) {
        String result = "";
        for (IOriginalSource<?> source: sourceable.getSources()){
            if (StringUtils.isBlank(source.getIdInSource())){//idInSource indicates that this source is only data provenance, may be changed in future
                if (source.getCitation() != null){
                    String ref = source.getCitation().getTitleCache();
                    result = CdmUtils.concat(config.getSetSeparator(), result, ref);
                }
            }
        }
        return result;
    }

    protected String getSources3(ISourceable<?> sourceable, DwcaTaxExportConfigurator config) {
        String result = "";
        for (IOriginalSource<?> source: sourceable.getSources()){
                if (source.getCitation() != null){
                    String ref = source.getCitation().getTitleCache();
                    result = CdmUtils.concat(config.getSetSeparator(), result, ref);
                }
        }
        return result;
    }

    protected String getSources2(Set<DescriptionElementSource> set, DwcaTaxExportConfigurator config) {
        String result = "";
        for(DescriptionElementSource source: set){
            if (StringUtils.isBlank(source.getIdInSource())){//idInSource indicates that this source is only data provenance, may be changed in future
                if (source.getCitation() != null){
                    String ref = source.getCitation().getTitleCache();
                    result = CdmUtils.concat(config.getSetSeparator(), result, ref);
                }
            }
        }
        return result;
    }

    @Override
    public abstract boolean isIgnore(DwcaTaxExportState state);

}
