/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.operation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.service.DeleteResult;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.DefaultImportState;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author k.luther
 * @since 2015
 */
@Component
public class NonReferencedObjectsDeleter extends CdmImportBase<NonReferencedObjectsDeleterConfigurator, DefaultImportState<NonReferencedObjectsDeleterConfigurator>> {

    private static final long serialVersionUID = -3514276133181062270L;

    @Override
	protected void doInvoke(DefaultImportState<NonReferencedObjectsDeleterConfigurator> state) {

        doTaxonNames(state);
        doReferences(state);
        doTeams(state);
        doPersons(state);
	}

    private void doPersons(DefaultImportState<NonReferencedObjectsDeleterConfigurator> state) {
        if (state.getConfig().isDoAuthors()){
            doAgents(state, Person.class, "Person");
        }
    }

    private void doTeams(DefaultImportState<NonReferencedObjectsDeleterConfigurator> state) {
        if (state.getConfig().isDoAuthors()){
            doAgents(state, Team.class, "Team");
        }
    }

    private void doAgents(DefaultImportState<NonReferencedObjectsDeleterConfigurator> state, Class<? extends TeamOrPersonBase<?>> clazz, String label) {

        List<? extends TeamOrPersonBase<?>> authors =getAgentService().list(clazz, null, null, getOrderHint(), null);

        int deleted = 0;
        System.out.println("There are " + authors.size() + " " + label + "s.");
        for (TeamOrPersonBase<?> author: authors){
            long refObjectsCount = getCommonService().getReferencingObjectsCount(author);
            if (refObjectsCount == 0) {
                if (!state.getConfig().isDoOnlyReport()){
                    DeleteResult result = getAgentService().delete(author);
                    if (!result.isOk()){
                        System.out.println(label + " " + author.getTitleCache() + " with id " + author.getId() + " could not be deleted.");
                        result = null;
                    }else{
                        deleted++;
                        System.out.println("Deleted: " + author.getTitleCache() + "; id = " + author.getId());
                    }
                }else{
                    deleted++;
                    System.out.println(label + " to delete: " + author.getTitleCache() + "; id = " + author.getId());
                }
            }
        }
        System.out.println(deleted + " " + label + "s are deleted.");
    }


    private void doReferences(DefaultImportState<NonReferencedObjectsDeleterConfigurator> state) {
        String label = "reference";
        if (state.getConfig().isDoReferences()){
            List<String> propertyPath = new ArrayList<>();
            propertyPath.add("sources.citation");
            propertyPath.add("createdBy");
            propertyPath.add("identifiers.type");

            int pageSize = 500;
            int pageNumber = state.getConfig().getStartPage();

            TransactionStatus tx = this.startTransaction();
            Pager<Reference> page = getReferenceService().page(Reference.class, pageSize, pageNumber, getOrderHint(), propertyPath);
//            List<Reference> references = getReferenceService().list(Reference.class, null, null, getOrderHint(), propertyPath);

            int deleted = 0;
            System.out.println("There are " + page.getCount() + " " + label + "s");

            while (page.getPagesAvailable() > 0){

                System.out.println(pageNumber++);
                for (Reference ref : page.getRecords()) {
                    deleted = handleSingleReference(state, label, deleted, ref);
                }

                try {
                    this.commitTransaction(tx);
                } catch (Exception e) {
                    // TODO Exception handling for commit failure
                    e.printStackTrace();
                }
                tx = this.startTransaction();
                page = getReferenceService().page(Reference.class, pageSize, page.getNextIndex(), getOrderHint(), propertyPath);
            }
            this.commitTransaction(tx);
            System.out.println(deleted + " " + label + "s are deleted.");
        }
    }

    private int handleSingleReference(DefaultImportState<NonReferencedObjectsDeleterConfigurator> state, String label,
            int deleted, Reference ref) {

        long refObjects = getCommonService().getReferencingObjectsCount(ref);
        if (refObjects == 0) {
            if (isIgnore(state, ref)){
                System.out.println("Ignore: " + ref.getId() + "\t" + ref.getType() + "\t" +ref.getTitleCache() + "\t" + ref.getCreated()+ "\t" +
                        (ref.getCreatedBy() == null? "" : ref.getCreatedBy().getUsername()) + "\t" +
                        ref.getUpdated() + "\t" +  getSources(ref));
            }else{
                if (!state.getConfig().isDoOnlyReport()){
                    DeleteResult result = getReferenceService().delete(ref);
                    if (!result.isOk()){
                        System.out.println(label + " " + ref.getTitle() + " with id " + ref.getId() + " could not be deleted.");
                        result = null;
                    }else{
                        deleted++;
                        System.out.println("Deleted: " + ref.getTitleCache() + "; id = " + ref.getId());
                    }
                }else{
                    deleted++;
                    System.out.println(label + " to delete: " + ref.getTitleCache() + "; id = " + ref.getId());
                }
            }
        }
        return deleted;
    }

    private boolean isIgnore(DefaultImportState<NonReferencedObjectsDeleterConfigurator> state, Reference ref) {
        if (state.getConfig().isKeepReferencesWithTitle() && isNotBlank(ref.getTitle())
                || state.getConfig().isKeepRisSources() && hasRISSource(ref)
                || hasIdentifierTypeToKeep(state, ref)){
            return true;
        }else{
            return false;
        }
    }

    private boolean hasIdentifierTypeToKeep(DefaultImportState<NonReferencedObjectsDeleterConfigurator> state,
            Reference ref) {
        return state.getConfig().getIdentifierTypesToKeep().stream()
                .anyMatch(idType -> ref.getIdentifiers().stream()
                        .anyMatch(identifier -> identifier.getType().getUuid().equals(idType)));
    }

    private String getSources(Reference ref) {
        String result = "";
        for (IdentifiableSource source : ref.getSources()){
            result += source.getType() + ": " + (source.getCitation() == null? "" : source.getCitation().getTitleCache()) + "\t";
        }
        return result;
    }

    private boolean hasRISSource(Reference ref) {
        for (IdentifiableSource source : ref.getSources()){
            Reference citation = source.getCitation();
            if (citation != null && citation.getTitleCache().startsWith("RIS Reference")){
                return true;
            }
        }
        return false;
    }

    private List<OrderHint> getOrderHint() {
        List<OrderHint> orderHint = new ArrayList<>();
        orderHint.add(OrderHint.ORDER_BY_ID);
        return orderHint;
    }


    private void doTaxonNames(DefaultImportState<NonReferencedObjectsDeleterConfigurator> state) {
        String label = "taxon names";
        if (state.getConfig().isDoTaxonNames()){
            List<String> propertyPath = new ArrayList<>();
//            propertyPath.add("sources.citation");
//            propertyPath.add("createdBy");

            List<TaxonName> taxonNames =getNameService().list(TaxonName.class, null, null, getOrderHint(), propertyPath);

            int deleted = 0;
            System.out.println("There are " + taxonNames.size() + " " + label + "s");
            for (TaxonName name: taxonNames){
                long refObjects = getCommonService().getReferencingObjectsCount(name);
                if (refObjects == 0) {
//                    if (isIgnore(state, name)){
//                        System.out.println("Ignore: " + ref.getId() + "\t" + ref.getType() + "\t" +ref.getTitleCache() + "\t" + ref.getCreated()+ "\t" +
//                                (ref.getCreatedBy() == null? "" : ref.getCreatedBy().getUsername()) + "\t" +
//                                ref.getUpdated() + "\t" +  getSources(ref));
//                    }else{
                        if (!state.getConfig().isDoOnlyReport()){
                            DeleteResult result = getNameService().delete(name);
                            if (!result.isOk()){
                                System.out.println(label + " " + name.getTitleCache() + " with id " + name.getId() + " could not be deleted.");
                                result = null;
                            }else{
                                deleted++;
                                //System.out.println("Deleted: " + ref.getTitleCache() + "; id = " + ref.getId());
                            }
                        }else{
                            deleted++;
                            System.out.println(label + " to delete: " + name.getTitleCache() + "; id = " + name.getId());
                        }
//                    }
                }
            }
            System.out.println(deleted + " " + label + "s are deleted.");
        }
    }

    @Override
	protected boolean doCheck(DefaultImportState<NonReferencedObjectsDeleterConfigurator> state) {
		return true;
	}

	@Override
	protected boolean isIgnore(DefaultImportState<NonReferencedObjectsDeleterConfigurator> state) {
		return false;
	}
}
