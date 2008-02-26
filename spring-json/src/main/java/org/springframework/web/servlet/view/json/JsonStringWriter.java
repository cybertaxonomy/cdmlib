package org.springframework.web.servlet.view.json;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.springframework.validation.BindingResult;

public interface JsonStringWriter {
	public void convertAndWrite(Map obj, Writer writer, BindingResult br) throws IOException;
}
