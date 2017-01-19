package com.southdevon.trust.util;

import java.util.Properties;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.DefaultLdapConnectionFactory;
import org.apache.directory.ldap.client.api.DefaultPoolableLdapConnectionFactory;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapConnectionPool;
import org.apache.log4j.Logger;

public class LdapUtil 
{
	private static final Logger logger = Logger.getLogger(LdapUtil.class);

	public static LdapConnection getLDAPConnectionForIdentityVault() throws LdapException
	{
		LdapConnectionConfig config = setupLdapConfiguration("identityvaultldap.properties");
		
		LdapConnection ldapConnection = getLdapConnectionFromPool(config);
		
		return ldapConnection;
	}
	
	public static LdapConnection getLDAPConnectionForUCSLdap() throws LdapException
	{
		LdapConnectionConfig config = setupLdapConfiguration("ucsldap.properties");
		
		LdapConnection ldapConnection = getLdapConnectionFromPool(config);
		
		return ldapConnection;
	}
	
	private static  LdapConnectionConfig setupLdapConfiguration(String connectionPropetiesFileName)
	{
		Properties identityVaultProperties=PropertyUtil.readPropertiesFile(connectionPropetiesFileName);
		
		LdapConnectionConfig config = new LdapConnectionConfig();
		config.setLdapHost(identityVaultProperties.getProperty("ldap.host"));
		config.setLdapPort(Integer.valueOf(identityVaultProperties.getProperty("ldap.port")));
		config.setName(identityVaultProperties.getProperty("ldap.admin.userdn"));
		config.setCredentials(identityVaultProperties.getProperty("ldap.admin.password"));
		
		return config;
	}
	
	private static LdapConnection getLdapConnectionFromPool(LdapConnectionConfig config) throws LdapException
	{
		DefaultLdapConnectionFactory factory = new DefaultLdapConnectionFactory(config);

		// optional, values below are defaults
		GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
		
		LdapConnectionPool pool = new LdapConnectionPool(new DefaultPoolableLdapConnectionFactory(factory),poolConfig);
		
		LdapConnection ldapConnection = null;
		try 
		{
			ldapConnection = pool.getConnection();
		} 
		catch (LdapException ldapException) 
		{
			logger.info("Exception occured while acquiring connection with LDAP.Error is "+ldapException.getMessage());
			throw ldapException;
		}
		
		return ldapConnection;
	}

	
}
