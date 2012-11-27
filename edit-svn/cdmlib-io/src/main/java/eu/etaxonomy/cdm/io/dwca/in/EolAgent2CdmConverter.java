// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 * @date 22.11.2011
 *
 */
public class EolAgent2CdmConverter extends PartitionableConverterBase<DwcaImportState> implements IPartitionableConverter<CsvStreamItem, IReader<CdmBase>, String> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(EolAgent2CdmConverter.class);
	private static final String CORE_ID = "coreId";

	/**
	 * @param state
	 */
	public EolAgent2CdmConverter(DwcaImportState state) {
		super(state);
	}


	public IReader<MappedCdmBase> map(CsvStreamItem item ){
		List<MappedCdmBase> resultList = new ArrayList<MappedCdmBase>(); 
		
		Map<String, String> csv = item.map;
		Reference<?> sourceReference = state.getTransactionalSourceReference();
		String sourceReferecenDetail = null;
		
		//TODO is taxon needed here here?
//		String id = csv.get(CORE_ID);
//		Taxon taxon = getTaxonBase(id, item, Taxon.class, state);
		
		String id = item.get(TermUri.DC_IDENTIFIER);
		String firstName = item.get(TermUri.FOAF_FIRST_NAME);
		String familyName = item.get(TermUri.FOAF_FAMILY_NAME);
		String name = item.get(TermUri.FOAF_NAME);
		String organization = item.get(TermUri.EOL_ORGANIZATION);
		String accountName = item.get(TermUri.FOAF_ACCOUNT_NAME);
		
		Institution institution = null;
		User user = null;
		if (isNotBlank(organization)){
			institution = Institution.NewInstance();
			institution.setName(organization);
			MappedCdmBase<Institution>  mcb = new MappedCdmBase<Institution>(TermUri.EOL_ORGANIZATION, id, institution);
			resultList.add(mcb);
		}
		if (isNotBlank(accountName)){
			user = User.NewInstance(accountName, UUID.randomUUID().toString());   //TODO
			MappedCdmBase<User>  mcb = new MappedCdmBase<User>(TermUri.FOAF_ACCOUNT_NAME, id, user);
			resultList.add(mcb);
		}
		
		if (isPerson(item)){
			Person person = Person.NewInstance();
			person.setFirstname(firstName);
			person.setLastname(familyName);
			if (isNotBlank(name) && ! name.equalsIgnoreCase(person.getTitleCache())){
				//TODO FOAF_NAME allows multiple names per object
				person.setTitleCache(name, true);
			}
			if (institution != null){
				person.addInstitutionalMembership(institution, null, null, null);
			}
			if (isNotBlank(accountName)){
				
			}
			
			MappedCdmBase<Person>  mcb = new MappedCdmBase<Person>(item.term, id, person);
			resultList.add(mcb);
			
		}else{
			//still unclear, what does Agent all include? Teams, organizations, ...?
			String message = "Agent type unclear. Agent not handled.";
			fireWarningEvent(message, item, 8);
		}
		
//		resultList.add(mcb);

		
		//return
		return new ListReader<MappedCdmBase>(resultList);
		
	}
	
	
	private boolean isPerson(CsvStreamItem item) {
		String firstName = item.get(TermUri.FOAF_FIRST_NAME);
		String familyName = item.get(TermUri.FOAF_FAMILY_NAME);
		String accountName = item.get(TermUri.FOAF_ACCOUNT_NAME);
		if (isNotBlank(firstName) || isNotBlank(familyName) || isNotBlank(accountName) ){
			return true;
		}else{
			return false;
		}
			
	}


	@Override
	public String getSourceId(CsvStreamItem item) {
		String id = item.get(CORE_ID);
		return id;
	}

//**************************** PARTITIONABLE ************************************************

	@Override
	protected void makeForeignKeysForItem(CsvStreamItem item, Map<String, Set<String>> fkMap) {
		String value;
		String key;
		if ( hasValue(value = item.get(CORE_ID))){
			key = TermUri.DWC_TAXON.toString();
			Set<String> keySet = getKeySet(key, fkMap);
			keySet.add(value);
		}
	}
	
	
	@Override
	public final Set<String> requiredSourceNamespaces() {
		Set<String> result = new HashSet<String>();
 		result.add(TermUri.DWC_TAXON.toString());
 		return result;
	}	
	
//************************ STRING ************************************************/



	@Override
	public String toString(){
		return this.getClass().getName();
	}





}
