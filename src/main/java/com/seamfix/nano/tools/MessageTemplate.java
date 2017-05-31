package com.seamfix.nano.tools;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;

/**
 * The Class MessageTemplate.
 *
 * @author segz
 */

@Singleton
public class MessageTemplate {
	
	private String templateLocation = "com/nano/etls/msghandler/templates";
	
	private Configuration configuration ;
	
	@PostConstruct
	public void init(){
		Version incompatibleImprovements = Configuration.VERSION_2_3_23; 
		configuration = new Configuration(incompatibleImprovements);
		configuration.setObjectWrapper(new DefaultObjectWrapper(incompatibleImprovements));
		
		try {
			configuration.setDirectoryForTemplateLoading(new File(templateLocation));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.getCause();
		}
	}
	
	/**
	 * Transform template with message body to stringWriter.
	 *
	 * @param templateModel - template structure
	 * @param templateFilename - template file name
	 * @return {@link StringWriter}
	 * @throws IOException - I/O exception
	 * @throws TemplateException - free-marker template exception
	 */
	public StringWriter transform(Map<String, Object> templateModel, String templateFilename) throws IOException, TemplateException{
		
		Template template = configuration.getTemplate(templateFilename);
		StringWriter stringWriter = new StringWriter();
		
		template.process(templateModel, stringWriter);
		
		return stringWriter;
	}
	
	/**
	 * Transform.
	 *
	 * @param templateModel - template structure
	 * @return {@link StringWriter}
	 * @throws IOException - I/O exception
	 * @throws TemplateException - free-marker template exception
	 */
	public StringWriter transform(Map<String, Object> templateModel) throws IOException, TemplateException{
		
		Template template = configuration.getTemplate("main_template.ftl");
		StringWriter stringWriter = new StringWriter();
		
		template.process(templateModel, stringWriter);
		
		return stringWriter;
	}

}