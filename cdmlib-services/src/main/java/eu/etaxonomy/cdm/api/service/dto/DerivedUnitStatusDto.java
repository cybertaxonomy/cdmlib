package eu.etaxonomy.cdm.api.service.dto;

import java.io.Serializable;
import eu.etaxonomy.cdm.model.occurrence.OccurrenceStatus;


public class DerivedUnitStatusDto implements Serializable{
	
	private static final long serialVersionUID = 6463365950608923394L;
	private SourceDTO statusSource;
	private String label;

	public DerivedUnitStatusDto(String label) {
		this.setLabel(label);
		
	}
	public static DerivedUnitStatusDto fromStatus(OccurrenceStatus status) {
		DerivedUnitStatusDto dto = new DerivedUnitStatusDto(status.getType().getLabel());
		dto.setStatusSource(SourceDTO.fromDescriptionElementSource(status.getSource()));		
		return dto;
	}

	public SourceDTO getStatusSource() {
		return statusSource;
	}

	public void setStatusSource(SourceDTO statusSource) {
		this.statusSource = statusSource;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

	

	
}
