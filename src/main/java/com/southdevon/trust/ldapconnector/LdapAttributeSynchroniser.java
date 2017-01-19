package com.southdevon.trust.ldapconnector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.Response;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchRequestImpl;
import org.apache.directory.api.ldap.model.message.SearchResultEntry;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.log4j.Logger;

import com.southdevon.trust.dto.UCSLdapUser;
import com.southdevon.trust.util.LdapUtil;
import com.southdevon.trust.util.PropertyUtil;

/**
 * This class is responsible for synchronising attributes from Identity vault edirectory and push it to UCS OpenLDAP store using Apache Directory API
 * @author nitinprabhu
 *
 */
public class LdapAttributeSynchroniser {
	
	
	private static final Logger logger = Logger.getLogger(LdapAttributeSynchroniser.class);
	private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	 
	public static void main(String[] args) 
	{

		Date date = new Date();
        
		logger.info("Started processing of LDAP Records at "+sdf.format(date));
		
		try 
		{
			//Retrieve edirectory connection for identity vault
			LdapConnection eDirectoryLdapConnection = LdapUtil.getLDAPConnectionForIdentityVault();
		
			//Retrieve records from identity vault
			Map<String, String> eDirectoryRecords=retrieveUserRecordsFromIdVault(eDirectoryLdapConnection);
		
			//get ldap connection for ucs
			LdapConnection ucsLdapConnection=LdapUtil.getLDAPConnectionForUCSLdap();
		
			syncESRAttributesToUCSLDAP(eDirectoryRecords, ucsLdapConnection);
		} 
		catch (LdapException ldapException) 
		{
			logger.error("Exception occured while acquiring connection LDAP."+ldapException);
		}
		
		logger.info("Completed processing of LDAP Records at "+sdf.format(date));
	}

	/**
	 * Synchronises employee number attribute from eDirectory Vault to OpenLDAP.
	 * If object to synchronise is availabe in eDirectory but not in OpenLDAP then common name for that object is stored in esrUsersAbsentFromAD.txt file
	 * @param eDirectoryRecords
	 * @param ucsLdapConnection
	 */
	private static void syncESRAttributesToUCSLDAP(Map<String, String> eDirectoryRecords,LdapConnection ucsLdapConnection) 
	{
		for (Map.Entry<String, String> entry : eDirectoryRecords.entrySet())
		{
			String idVaultESREmployeeNumber = entry.getValue();
		    		
		    String userName = entry.getKey();
		    
		    UCSLdapUser ucsLdapUser=retrieveUCSUserDetails(ucsLdapConnection,userName);
			
		    logger.debug("Processing LDAP Record from eDirectory for user with cn= "+userName+" and IDd Vault employee number "+idVaultESREmployeeNumber+" and UCS user "+ucsLdapUser);
		
		    if(ucsLdapUser.getUserDN()!=null)
		    {
		    	syncESREmployeeNumberToUCS(ucsLdapUser,ucsLdapConnection,idVaultESREmployeeNumber);
		    }
		    else
		    {
				String fileName="/home/syncldapattributes/esrUsersAbsentFromAD.txt";
				writeToFile("cn="+userName,fileName);
		    }
			
		  
		}
	}

	/**
	 * Retrieves cn and employee number attribute for all the users in the edirectory as specified in the basedn property of identityvaultldap.properties
	 * @param eDirectoryLdapConnection
	 * @return collection which consists of cn as the key and esr employee number as the value
	 */
	private static Map<String, String> retrieveUserRecordsFromIdVault(LdapConnection eDirectoryLdapConnection) 
	{
		Map<String, String> eDirectoryData=new HashMap<String, String>();
		 
		Properties identityVaultProperties=PropertyUtil.readPropertiesFile("identityvaultldap.properties");
		
		String baseDN=identityVaultProperties.getProperty("ldap.basedn");
		
		EntryCursor cursor;
		try 
		{
			cursor = eDirectoryLdapConnection.search( baseDN, "(objectclass=inetOrgPerson)", SearchScope.ONELEVEL );
			while (cursor.next())
			{
			   Entry entry = cursor.get();
					
			   if(entry.get("cn")!=null && entry.get("nhsivEmployeeNumber")!=null)
				{
					String userName= entry.get("cn").get().getString();
					String esrEmployeeNumber= entry.get("nhsivEmployeeNumber").get().getString();
						
					logger.debug("Username :"+userName +"ESR Employee Number :"+esrEmployeeNumber);
					eDirectoryData.put(userName, esrEmployeeNumber);
				}
				else
				{
					String fileName="/home/syncldapattributes/dnListForEmptyESREmployeeNum.txt";
					writeToFile(entry.getDn().getName(),fileName);
				}
					
			} 
			
			cursor.close();
		} 
		catch (Exception exception) 
		{
			logger.error("Exception occured while retrieving records from ID Vault.Exception message is  "+exception);
		}

		
		return eDirectoryData;
	}

	/**
	 * Replaces attribute employee number in OpenLDAP only if the attribute value is absent or is different compared to employee number attribute in Identity vault
	 * @param ucsLdapUser
	 * @param ucsLdapConnection
	 * @param esrEmployeeNumber
	 */
	private static void syncESREmployeeNumberToUCS(UCSLdapUser ucsLdapUser, LdapConnection ucsLdapConnection,String esrEmployeeNumber) 
	{
		//Only modify if UCS employee number attribute is different from ID vault attribute value.
		
		
		Modification esrEmpNo = new DefaultModification( ModificationOperation.REPLACE_ATTRIBUTE, "employeeNumber",esrEmployeeNumber);

		try 
		{
			if(!StringUtils.equals(esrEmployeeNumber, ucsLdapUser.getEsrEmployeeNumber()))
			{
				ucsLdapConnection.modify( ucsLdapUser.getUserDN(), esrEmpNo );
				logger.debug("Successfully Added ESR Employee number to user "+ucsLdapUser.getUserDN());
			}
			else
			{
				logger.debug("Skipping updating of employee number in UCS as it is in sync with Id Vault eDirectory "+ucsLdapUser.getUserDN());
			}
			
			
			
		} 
		catch (Exception exception) 
		{
			logger.error("Updation of Employee number in UCS failed for user DN "+ucsLdapUser.getUserDN()+".Exception is "+exception);
		}
	}

	/**
	 * This method retrieves  UCS user details for all the users found in eDirectory.If the user is not found then cn is persisted for that user in exceptionUCSUserDNRetrieval.txt
	 * @param ucsLdapConnection
	 * @param userName
	 * @return user data from UCS
	 */
	private static UCSLdapUser retrieveUCSUserDetails(LdapConnection ucsLdapConnection,String userName) 
	{
		UCSLdapUser ucsLdapUser=new UCSLdapUser();
	
		  try 
			{
				// Create the SearchRequest object
				SearchRequest req = new SearchRequestImpl();
				req.setScope( SearchScope.SUBTREE );
				req.addAttributes("dn","employeeNumber");
				req.setTimeLimit(0);
				req.setFilter( "(uid="+userName+")" );
				
				Properties ucsProperties=PropertyUtil.readPropertiesFile("ucsldap.properties");
				
				req.setBase( new Dn(ucsProperties.getProperty("ldap.basedn")));
				
				// Process the request
				SearchCursor searchCursor = ucsLdapConnection.search( req );
			
				while ( searchCursor.next() )
				{
					Response response = searchCursor.get();

					// process the SearchResultEntry
					if ( response instanceof SearchResultEntry )
					{
						Entry resultEntry = ( ( SearchResultEntry ) response ).getEntry();
						      
						String userDN=resultEntry.getDn().getName();
						ucsLdapUser.setUserDN(userDN);
						
						Attribute empNoAttribute=resultEntry.get("employeeNumber");
						
						if(empNoAttribute!=null)
						{
							String esrEmployeeNumber=empNoAttribute.get().getString();
							ucsLdapUser.setEsrEmployeeNumber(esrEmployeeNumber);
						}
					}
					
				}
		
		  } 
		  catch (Exception exception)
		  {
			  logger.error("Exception occured while retrieving user details from UCS.Exception message is  "+exception);
			  String fileName="/home/syncldapattributes/exceptionUCSUserDNRetrieval.txt";
			  writeToFile("cn="+userName,fileName);
		  } 
		 
		return ucsLdapUser;
	}
	
	/**
	 * This method writes data to the file passed as an argument to this method
	 * @param data
	 * @param fileName
	 */
	public static void writeToFile(String data,String fileName)
	{
		File rejectedDNList = new File(fileName);
		FileWriter fileWriter;
		try 
		{
			fileWriter = new FileWriter(rejectedDNList, true);
			fileWriter.write(data+"\n");
			fileWriter.close();
		} 
		catch (IOException exception)
		{
			logger.error("IOException occured while writing data  "+data+" to file "+fileName+" Exception message is  "+exception);
		} 
		
	}

}
