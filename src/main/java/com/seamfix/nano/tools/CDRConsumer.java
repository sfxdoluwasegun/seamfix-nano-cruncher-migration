/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seamfix.nano.tools;

import org.jboss.ejb3.annotation.ResourceAdapter;
import org.jboss.logging.Logger;

import com.nano.gcruncher.CDRCruncher;

import java.io.File;
import java.io.IOException;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 *
 * @author martin
 */

@ResourceAdapter(value = "cdractivemq")
@MessageDriven(mappedName = "queue/cdrpath", activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"), 
		@ActivationConfigProperty(propertyName="destination", propertyValue="queue/cdrpath"), 
		@ActivationConfigProperty(propertyName = "maxSessions", propertyValue = "20"), 
		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")})
public class CDRConsumer implements MessageListener {

	private Logger log = Logger.getLogger(getClass());
	
	@Inject
	private CDRCruncher cruncher ;

	@Resource
	private MessageDrivenContext messageDrivenContext ;

	@Override
	public void onMessage(Message message) {
		// TODO Auto-generated method stub

		MapMessage mapMessage = (MapMessage) message ;
		try {
			File file = new File(mapMessage.getString("filepath"));
			if (!file.exists())
				messageDrivenContext.setRollbackOnly();
			
			cruncher.handleFile(file);
		} catch (JMSException | IOException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
	}

}
