package eu.etaxonomy.cdm.remoting.httpinvoker;

import org.apache.log4j.Logger;

public class DemoServiceImpl implements DemoService {

	Dto dto;
	public static final Logger logger = Logger.getLogger(DemoServiceImpl.class);
	
	@Override
	public Dto getData() {
		logger.info("Server send back to client: " + dto.getText()+ " " + dto.getNumber());
		return this.dto;
	}

	@Override
	public void setData(Dto dto) {
		logger.info("Server received from client: " + dto.getText()+ " " + dto.getNumber());
		this.dto=dto;
	}

}
