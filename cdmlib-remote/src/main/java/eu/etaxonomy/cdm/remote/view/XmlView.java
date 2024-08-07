package eu.etaxonomy.cdm.remote.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamResult;

import org.springframework.oxm.Marshaller;
import org.springframework.web.servlet.view.AbstractView;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * View class which serializes objects as xml
 *
 * @author ben
 * @see javax.xml.transform.Source
 * @see com.ibm.lsid.MetadataResponse
 * @deprecated use {@link org.springframework.web.servlet.view.xml.MarshallingView} instead
 */
@Deprecated
public class XmlView extends AbstractView {

    private Marshaller marshaller;

    private boolean locationHeader = false;

    private String locationPrefix = "";

    public void setLocationHeader(boolean locationHeader) {
        this.locationHeader = locationHeader;
    }

    public void setLocationPrefix(String locationPrefix) {
        this.locationPrefix = locationPrefix;
    }

    public XmlView() {

    }

    public void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }


    @Override
    protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        for(Object object : model.values()) {
            if(object instanceof IdentifiableEntity) {
                IdentifiableEntity<?> identifiableEntity = (IdentifiableEntity<?>)object;
                if(locationHeader) {
                    response.addHeader("Location", locationPrefix + identifiableEntity.getUuid().toString());
                }
                marshaller.marshal(identifiableEntity, new StreamResult(response.getOutputStream()));
            } else if(object instanceof Throwable) {
                eu.etaxonomy.cdm.io.jaxb.Error error = new eu.etaxonomy.cdm.io.jaxb.Error((Throwable)object);
                marshaller.marshal(error, new StreamResult(response.getOutputStream()));
            }
        }
    }
}