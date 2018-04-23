/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.excel.taxa;

import org.codehaus.plexus.util.StringUtils;

import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author k.luther
 \* @since 19.02.2018
 *
 */
public class TaxonListImport extends TaxonExcelImportBase {

    private static final long serialVersionUID = 515631363871257717L;

    final String nameColumn = "Name";
    final String correctNameColumn = "Correct name if synonym";

    /**
     * {@inheritDoc}
     */
    @Override
    protected ExcelListRow createDataHolderRow() {

        return new ExcelListRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void analyzeSingleValue(
            KeyValue keyValue,
            TaxonExcelImportState state) {
        ExcelListRow excelRow = (ExcelListRow)state.getCurrentRow();
        String key = keyValue.key;
        String value = keyValue.value;
        Integer index = keyValue.index;

        if (key.equalsIgnoreCase(nameColumn)) {
            excelRow.setName(value);
        } else if(key.equalsIgnoreCase(correctNameColumn) ) {
            excelRow.setCorrectNameifSynonym(value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void firstPass(TaxonExcelImportState state) {

        //first import all names
        ExcelListRow taxonDataHolder = (ExcelListRow)state.getCurrentRow();
        TaxonName name = TaxonNameFactory.PARSED_BOTANICAL(taxonDataHolder.getName());
        name = getNameService().save(name);

        taxonDataHolder.setCdmUuid(name.getUuid());
        state.putName(taxonDataHolder.getName(), name);

        //import all taxa -> column correct name if synonym is empty

        if (StringUtils.isBlank(taxonDataHolder.getCorrectNameifSynonym() )){
            Taxon taxon = Taxon.NewInstance(state.getNameMap().get(taxonDataHolder.name), null);
            taxon = (Taxon) getTaxonService().save(taxon);
            state.putTaxon(name.getNameCache(), taxon);

        }
        return;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void secondPass(TaxonExcelImportState state) {
       //import all synonyms -> column correct name if synonym contains name
        ExcelListRow taxonDataHolder = (ExcelListRow)state.getCurrentRow();

        if (!StringUtils.isBlank(taxonDataHolder.getCorrectNameifSynonym() )){
            Taxon taxon = (Taxon)state.getTaxonBase(taxonDataHolder.correctNameifSynonym);
            Synonym syn = taxon.addHeterotypicSynonymName(state.getNameMap().get(taxonDataHolder.name));
            getTaxonService().saveOrUpdate(taxon);
//            state.putTaxon(nameColumn, syn);
        }else {
            //create parent child relationship
            TaxonBase taxonBase = state.getTaxonMap().get(taxonDataHolder.name);
            if (taxonBase instanceof Taxon){
                Taxon taxon = (Taxon) taxonBase;
                if (taxon.getName().isGenus()){
                    makeParent(state, null, taxon);
                }else{
                    TaxonBase parent =  state.getTaxonMap().get(taxon.getName().getGenusOrUninomial());
                    if (parent instanceof Taxon){
                        Taxon parentTaxon = (Taxon)parent;
                        makeParent(state, parentTaxon, taxon);
                    }
                }
            }
        }






    }

    private void makeParent(TaxonExcelImportState state, Taxon parentTaxon, Taxon childTaxon){
        Reference sec = state.getConfig().getSourceReference();

        Classification tree = state.getClassification();
        if (tree == null){
            if (state.getConfig().getClassificationUuid() != null){
                tree = getClassificationService().load(state.getConfig().getClassificationUuid());
                state.setClassification(tree);
            }
            if (tree == null){
                tree = makeTree(state, sec);
                getClassificationService().save(tree);
                state.setClassification(tree);
            }
        }
        boolean success;
        if (parentTaxon == null){
            success =  (null !=  tree.addChildTaxon(childTaxon, null, null));
        }else{
            success =  (null !=  tree.addParentChild(parentTaxon, childTaxon, null, null));
        }
        if (success == false){
            state.setUnsuccessfull();
        }

        return;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isIgnore(TaxonExcelImportState state) {
        // TODO Auto-generated method stub
        return false;
    }



}
