package eu.etaxonomy.cdm.io.jaxb;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.xml.bind.ValidationEventHandler;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import com.sun.xml.bind.IDResolver;

import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.ICollectionService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IFeatureTreeService;
import eu.etaxonomy.cdm.api.service.IMediaService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IUserService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.jaxb.UUIDAdapter;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

public class CdmIDResolver extends IDResolver {
	private static final Logger logger = Logger.getLogger(CdmIDResolver.class);

	IUserService userService;

	IAgentService agentService;

	ITermService termService;

	IVocabularyService vocabularyService;

	IDescriptionService descriptionService;

	IFeatureTreeService featureTreeService;

	IMediaService mediaService;

	INameService nameService;

	IOccurrenceService occurrenceService;

	ICollectionService collectionService;

	IReferenceService referenceService;

	ITaxonService taxonService;

	@Autowired
	public void setUserService(IUserService userService) {
		this.userService = userService;
	}

	@Autowired
	public void setAgentService(IAgentService agentService) {
		this.agentService = agentService;
	}

	@Autowired
	public void setTermService(ITermService termService) {
		this.termService = termService;
	}

	@Autowired
	public void setVocabularyService(IVocabularyService vocabularyService) {
		this.vocabularyService = vocabularyService;
	}

	@Autowired
	public void setDescriptionService(IDescriptionService descriptionService) {
		this.descriptionService = descriptionService;
	}

	@Autowired
	public void setFeatureTreeService(IFeatureTreeService featureTreeService) {
		this.featureTreeService = featureTreeService;
	}

	@Autowired
	public void setMediaService(IMediaService mediaService) {
		this.mediaService = mediaService;
	}

	@Autowired
	public void setNameService(INameService nameService) {
		this.nameService = nameService;
	}

	@Autowired
	public void setOccurrenceService(IOccurrenceService occurrenceService) {
		this.occurrenceService = occurrenceService;
	}

	@Autowired
	public void setCollectionService(ICollectionService collectionService) {
		this.collectionService = collectionService;
	}

	@Autowired
	public void setReferenceService(IReferenceService referenceService) {
		this.referenceService = referenceService;
	}

	@Autowired
	public void setTaxonService(ITaxonService taxonService) {
		this.taxonService = taxonService;
	}

	private HashMap<String,Object> idmap = null;

	@Override
	public void startDocument(ValidationEventHandler eventHandler) throws SAXException {
		if(idmap!=null) {
		    idmap.clear();
		}
	}

	@Override
	public void bind(String id, Object obj) throws SAXException {
		if(idmap==null) {
			idmap = new HashMap<String,Object>();
		}
		idmap.put(id,obj);
	}


	@Override
	public Callable<?> resolve(final String id, final Class targetType) throws SAXException {
		return new Callable() {
	        @Override
            public Object call() throws Exception {
	          logger.info("Resolving " + id + " for class " + targetType);

			  if(idmap==null || !idmap.containsKey(id)) {

				  String uuidPart = id.substring(UUIDAdapter.UUID_URN_PREFIX.length());
				  UUID uuid = UUID.fromString(uuidPart);
				  logger.info(uuid + " not in idmap, looking in database");
				  if(targetType.equals(User.class)) {
					  return resolveObject(uuid, targetType, userService);
				  } else if(AgentBase.class.isAssignableFrom(targetType) || INomenclaturalAuthor.class.isAssignableFrom(targetType)) {
					  return resolveObject(uuid, targetType, agentService);
				  } else if(DefinedTermBase.class.isAssignableFrom(targetType)) {
					  return resolveObject(uuid, targetType, termService);
				  } else if(TermVocabulary.class.isAssignableFrom(targetType)) {
					  return resolveObject(uuid, targetType, vocabularyService);
				  } else if(DescriptionBase.class.isAssignableFrom(targetType)) {
					  return resolveObject(uuid, targetType, descriptionService);
				  } else if(FeatureTree.class.isAssignableFrom(targetType)) {
					  return resolveObject(uuid, targetType, featureTreeService);
				  } else if(Media.class.isAssignableFrom(targetType)) {
					  return resolveObject(uuid, targetType, mediaService);
				  } else if(TaxonName.class.isAssignableFrom(targetType)) {
					  return resolveObject(uuid, targetType, nameService);
				  } else if(SpecimenOrObservationBase.class.isAssignableFrom(targetType)) {
					  return resolveObject(uuid, targetType, occurrenceService);
				  } else if(Collection.class.isAssignableFrom(targetType)) {
					  return resolveObject(uuid, targetType, collectionService);
				  } else if(Reference.class.isAssignableFrom(targetType)) {
					  return resolveObject(uuid, targetType, referenceService);
				  } else if(TaxonBase.class.isAssignableFrom(targetType)) {
					  return resolveObject(uuid, targetType, taxonService);
				  } else if(HomotypicalGroup.class.isAssignableFrom(targetType)) {
					  Object object = nameService.findHomotypicalGroup(uuid);
					  if(object == null) {
						  throw new SAXException(targetType.getSimpleName() + " with " + uuid + " not found");
					  }
					  return object;
				  } else {
					  /**
					   * Collections of IDREFS do not have a type at runtime
					   *  https://jaxb.dev.java.net/issues/show_bug.cgi?id=546
					   *
					   *   Maybe in the future we'll be able to add targetType to IDREF
					   *   but the spec has to be changed first so no fix is likely at the moment
					   *
					   */

					  AgentBase agent = agentService.find(uuid);
					  if(agent != null) {
						  return agent;
					  }
					  DefinedTermBase term = termService.find(uuid);
					  if(term != null) {
						  return term;
					  }
					  Media media = mediaService.find(uuid);
					  if(media != null) {
						  return media;
					  }
					  throw new SAXException(targetType.getSimpleName() + " with " + uuid + " not found");
				  }
			  } else {
				  return idmap.get(id);
			  }
			}
	    };
	}

	private Object resolveObject(UUID uuid, Class targetType, IService service) throws SAXException {
		Object object = service.find(uuid);
		  if(object == null) {
			  throw new SAXException(targetType.getSimpleName() + " with " + uuid + " not found");
		  }
		  return object;
	}
}
