// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen.abcd206.in;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author pplitzner
 * @date Jan 23, 2015
 *
 */
public class Abcd206ImportReport {

    static private final Logger logger = Logger.getLogger(Abcd206ImportReport.class);


    private final List<Taxon> createdTaxa = new ArrayList<Taxon>();
    private final Map<Taxon, List<DerivedUnit>> taxonToAssociatedSpecimens =  new HashMap<Taxon, List<DerivedUnit>>();
    private final List<TaxonNameBase<?, ?>> createdNames = new ArrayList<TaxonNameBase<?,?>>();
    private final List<TaxonNode> createdTaxonNodes = new ArrayList<TaxonNode>();

    public void addTaxon(Taxon taxon){
        createdTaxa.add(taxon);
    }

    public void addName(TaxonNameBase<?, ?> taxonName){
        createdNames.add(taxonName);
    }

    public void addTaxonNode(TaxonNode taxonNode){
        createdTaxonNodes.add(taxonNode);
    }

    /**
     * @param taxon
     * @param derivedUnitBase
     */
    public void addIndividualAssociation(Taxon taxon, DerivedUnit derivedUnitBase) {
        List<DerivedUnit> associatedSpecimens = taxonToAssociatedSpecimens.get(taxon);
        if(associatedSpecimens==null){
            associatedSpecimens = new ArrayList<DerivedUnit>();
        }
        associatedSpecimens.add(derivedUnitBase);
        taxonToAssociatedSpecimens.put(taxon, associatedSpecimens);
    }

    public void printReport(URI reportUri) {
        PrintStream out;
        if(reportUri!=null){
            try {
                out = new PrintStream(new File(reportUri));
            } catch (FileNotFoundException e) {
                logger.warn("Report file could not be found.");
                out = System.out;
            }
        }
        else{
            out = System.out;
        }
        out.println("++++++++Import Report+++++++++");
        out.println("---Created Taxon Names---");
        for (TaxonNameBase<?, ?> taxonName : createdNames) {
            out.println(taxonName.getTitleCache());
        }
        out.println("\n");
        out.println("---Created Taxa---");
        for (Taxon taxon : createdTaxa) {
            out.println(taxon.getTitleCache());
        }
        out.println("\n");
        out.println("---Created Taxon Nodes---");
        for (TaxonNode taxonNode : createdTaxonNodes) {
            String nodeString = taxonNode.toString();
            if(taxonNode.getTaxon()!=null){
                nodeString += " ("+taxonNode.getTaxon().getTitleCache()+")";
            }
            if(taxonNode.getParent()!=null){
                nodeString += " with parent "+taxonNode.getParent();
                if(taxonNode.getParent().getTaxon()!=null){
                    nodeString += " ("+taxonNode.getParent().getTaxon().getTitleCache()+")";
                }
            }
            out.println(nodeString);
        }
        out.println("\n");
        out.println("---Taxa with associated specimens---");
        for(Entry<Taxon, List<DerivedUnit>> entry:taxonToAssociatedSpecimens.entrySet()){
            Taxon taxon = entry.getKey();
            List<DerivedUnit> specimens = entry.getValue();
            out.println(taxon.getTitleCache());
            for (DerivedUnit derivedUnit : specimens) {
                out.println("\t- "+derivedUnit.getTitleCache());
            }
        }
        out.close();
    }

}
