package eu.etaxonomy.cdm.io.tcsrdf;

import org.apache.log4j.Logger;
import org.jdom.Element;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import eu.etaxonomy.cdm.io.common.mapping.CdmSingleAttributeMapperBase;
import eu.etaxonomy.cdm.io.common.mapping.IRdfMapper;
import eu.etaxonomy.cdm.io.tcsxml.CdmSingleAttributeXmlMapperBase;


/**
 * @author a.mueller
 */
public abstract class CdmSingleAttributeRDFMapperBase extends
		CdmSingleAttributeMapperBase implements IRdfMapper {


		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(CdmSingleAttributeXmlMapperBase.class);

		protected String sourceNameSpace;


		/**
		 * @param sourceElementString
		 * @param sourceNamespace
		 * @param cdmAttributeString
		 */
		protected CdmSingleAttributeRDFMapperBase(String sourceElementString, String subject, String cdmAttributeString){
			super(sourceElementString, cdmAttributeString);
			this.sourceNameSpace = subject;
		}


		/**
		 * Uses the Namespace of the parent Element
		 * @param sourceElementString
		 * @param cdmAttributeString
		 */
		protected CdmSingleAttributeRDFMapperBase(String sourceElementString, String cdmAttributeString){
			super(sourceElementString, cdmAttributeString);

		}

		public String getSourceElement(){
			return super.getSourceAttribute();
		}

		@Override
        public String getDestinationAttribute(){
			return super.getDestinationAttribute();
		}


		public String getSourceNamespace(){
			return sourceNameSpace;
		}

		/**
		 * Returns the namespace. If namespace is null, return parentElement namespace
		 * @param parentElement
		 * @return
		 */
		public String getSourceNameSpace(Statement resource){
			if (this.sourceNameSpace != null){
				return sourceNameSpace;
			}else if (resource != null){
				return resource.getPredicate().toString();
			}else{
				return null;
			}
		}

		@Override
        public boolean mapsSource(Resource content, Statement statement){
			if (! (content instanceof Element)){
				return false;
			}
			//Element element = (Element)content;
			if (content == null){
				return false;
			}else if (! content.getNameSpace().equals(getSourceElement())){
				return false;
			}
			String thisSourceNameSpace= getSourceNameSpace(statement);
			if (thisSourceNameSpace == null){
				if (content.getNameSpace() == null){
					return true;
				}else{
					return false;
				}
			}
			if (! thisSourceNameSpace.equals(content.getNameSpace())){
				return false;
			}
			return true;
		}

		@Override
        public String toString(){
			//TODO
			return this.getSourceElement();
		}
	}
