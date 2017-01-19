package com.southdevon.trust.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.southdevon.trust.ldapconnector.LdapAttributeSynchroniser;

public class PropertyUtil {
	
	private static final Logger logger = Logger.getLogger(LdapAttributeSynchroniser.class);

	public static Properties readPropertiesFile(String resourceName) 
	{
		Properties props = new Properties();
		
		try 
		{
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			
			try(InputStream resourceStream = loader.getResourceAsStream(resourceName))
			{
				logger.info("Loading properties file "+resourceName);
			    props.load(resourceStream);
			}
		} 
		catch (IOException ioException) 
		{
			logger.error("Exception occured while loading properties file "+resourceName+"Exception message is "+ioException);
		}
			

		return props;
	}

}
