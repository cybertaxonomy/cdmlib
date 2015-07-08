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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * Gathers information about the ABCD import and presents them in a suitable way.
 * @author pplitzner
 * @date Jan 23, 2015
 *
 */
public class Abcd206ImportReport {

    static private final Logger logger = Logger.getLogger(Abcd206ImportReport.class);


    private final List<Taxon> createdTaxa = new ArrayList<Taxon>();
    private final Map<Taxon, List<UnitIdSpecimen>> taxonToAssociatedSpecimens =  new HashMap<Taxon, List<UnitIdSpecimen>>();
    private final Map<UnitIdSpecimen, List<UnitIdSpecimen>> derivateMap = new HashMap<UnitIdSpecimen, List<UnitIdSpecimen>>();
    private final List<UnitIdSpecimen> ignoredImports = new ArrayList<UnitIdSpecimen>();
    private final List<TaxonNameBase<?, ?>> createdNames = new ArrayList<TaxonNameBase<?,?>>();
    private final List<TaxonNode> createdTaxonNodes = new ArrayList<TaxonNode>();
    private final List<String> infoMessages = new ArrayList<String>();

    public void addTaxon(Taxon taxon){
        createdTaxa.add(taxon);
    }

    public void addName(TaxonNameBase<?, ?> taxonName){
        createdNames.add(taxonName);
    }

    public void addTaxonNode(TaxonNode taxonNode){
        createdTaxonNodes.add(taxonNode);
    }

    public void addDerivate(DerivedUnit parent, Abcd206ImportConfigurator config){
        addDerivate(parent, null, config);
    }

    public void addDerivate(DerivedUnit parent, DerivedUnit child, Abcd206ImportConfigurator config){
        UnitIdSpecimen parentUnitIdSpecimen = new UnitIdSpecimen(AbcdImportUtility.getUnitID(parent, config), parent);
        List<UnitIdSpecimen> children = derivateMap.get(parentUnitIdSpecimen);
        if(children==null){
            children = new ArrayList<UnitIdSpecimen>();
        }
        if(child!=null){
            children.add(new UnitIdSpecimen(AbcdImportUtility.getUnitID(child, config), child));
        }
        derivateMap.put(parentUnitIdSpecimen, children);
    }

    public void addIndividualAssociation(Taxon taxon, String derivedUnitId, DerivedUnit derivedUnitBase) {
        UnitIdSpecimen derivedUnitIdSpecimen = new UnitIdSpecimen(derivedUnitId, derivedUnitBase);
        List<UnitIdSpecimen> associatedSpecimens = taxonToAssociatedSpecimens.get(taxon);
        if(associatedSpecimens==null){
            associatedSpecimens = new ArrayList<UnitIdSpecimen>();
        }
        associatedSpecimens.add(derivedUnitIdSpecimen);
        taxonToAssociatedSpecimens.put(taxon, associatedSpecimens);
    }

    public void addIgnoredImport(String unitId, DerivedUnit derivedUnit){
        ignoredImports.add(new UnitIdSpecimen(unitId, derivedUnit));
    }

    public void addException(String message, Exception e) {
        infoMessages.add(message+"\n"+e.getMessage()+"\n"+e.toString());
    }

    public void addInfoMessage(String message) {
        infoMessages.add(message);
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
        out.println("---Created Taxon Names ("+createdNames.size()+")---");
        for (TaxonNameBase<?, ?> taxonName : createdNames) {
            out.println(taxonName.getTitleCache());
        }
        out.println("\n");
        out.println("---Created Taxa ("+createdTaxa.size()+")---");
        for (Taxon taxon : createdTaxa) {
            out.println(taxon.getTitleCache());
        }
        out.println("\n");
        out.println("---Created Taxon Nodes ("+createdTaxonNodes.size()+")---");
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
        for(Entry<Taxon, List<UnitIdSpecimen>> entry:taxonToAssociatedSpecimens.entrySet()){
            Taxon taxon = entry.getKey();
            List<UnitIdSpecimen> specimens = entry.getValue();
            out.println(taxon.getTitleCache() + " ("+specimens.size()+")");
            for (UnitIdSpecimen derivedUnit : specimens) {
                out.println("\t- "+formatSpecimen(derivedUnit));
                //check for derivatives
                List<UnitIdSpecimen> list = derivateMap.get(derivedUnit);
                for (UnitIdSpecimen derivate : list) {
                    out.println("\t\t- "+formatSpecimen(derivate));
                }
            }
        }
        out.println("\n");
        //all specimens
        Set<UnitIdSpecimen> allSpecimens = new HashSet<UnitIdSpecimen>();
        for (Entry<UnitIdSpecimen, List<UnitIdSpecimen>> entry : derivateMap.entrySet()) {
            allSpecimens.add(entry.getKey());
            allSpecimens.addAll(entry.getValue());
        }
        out.println("Specimens created: "+allSpecimens.size());
        out.println("\n");
        out.println("---Not imported---");
        for(UnitIdSpecimen specimen:ignoredImports){
            out.println(formatSpecimen(specimen));
        }
        out.println("\n");
        out.println("---Info messages---");
        for(String message:infoMessages){
            out.println(message);
            out.println("---");
        }
        if(out!=System.out){
            out.close();
        }
    }

    private String formatSpecimen(UnitIdSpecimen specimen){
        return "("+specimen.getUnitId()+") ["+specimen.getSpecimen().getRecordBasis()+"] "+specimen.getSpecimen().getTitleCache();
    }

    private class UnitIdSpecimen{
        private final String unitId;
        private final SpecimenOrObservationBase<?> specimen;


        public UnitIdSpecimen(String unitId, SpecimenOrObservationBase<?> specimen) {
            super();
            this.unitId = unitId;
            this.specimen = specimen;
        }
        public String getUnitId() {
            return unitId;
        }
        public SpecimenOrObservationBase<?> getSpecimen() {
            return specimen;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((specimen == null) ? 0 : specimen.hashCode());
            result = prime * result + ((unitId == null) ? 0 : unitId.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            UnitIdSpecimen other = (UnitIdSpecimen) obj;
            if (specimen == null) {
                if (other.specimen != null) {
                    return false;
                }
            } else if (!specimen.equals(other.specimen)) {
                return false;
            }
            if (unitId == null) {
                if (other.unitId != null) {
                    return false;
                }
            } else if (!unitId.equals(other.unitId)) {
                return false;
            }
            return true;
        }

    }

}
