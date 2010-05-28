package eu.etaxonomy.cdm.io.jaxb.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.model.reference.ReferenceBase;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "References", propOrder = {
	    "references"
})
@XmlRootElement(name = "References")
public class References extends CdmListWrapper<ReferenceBase> {
	
    @XmlElements({
    	@XmlElement(name = "ReferenceBase", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = ReferenceBase.class)
//    	@XmlElement(name = "Article", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Article.class),
//    	@XmlElement(name = "Book", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Book.class),
//    	@XmlElement(name = "BookSection", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = BookSection.class),
//    	@XmlElement(name = "CdDvd", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = CdDvd.class),
//    	@XmlElement(name = "Database", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Database.class),
//    	@XmlElement(name = "Generic", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Generic.class),
//    	@XmlElement(name = "InProceedings", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = InProceedings.class),
//    	@XmlElement(name = "Journal", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Journal.class),
//    	@XmlElement(name = "Map", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Map.class),
//    	@XmlElement(name = "Patent", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Patent.class),
//    	@XmlElement(name = "PersonalCommunication", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = PersonalCommunication.class),
//    	@XmlElement(name = "PrintSeries", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = PrintSeries.class),
//    	@XmlElement(name = "Proceedings", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Proceedings.class),
//    	@XmlElement(name = "Report", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Report.class),
//    	@XmlElement(name = "Thesis", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Thesis.class),
//    	@XmlElement(name = "WebPage", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = WebPage.class)
    })
	protected List<ReferenceBase> references = new ArrayList<ReferenceBase>();

	@Override
	public List<ReferenceBase> getElements() {
		return references;
	}

	@Override
	public void setElements(List<ReferenceBase> elements) {
		this.references = elements;
	}

}
