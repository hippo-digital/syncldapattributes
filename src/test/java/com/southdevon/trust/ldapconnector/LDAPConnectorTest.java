package com.southdevon.trust.ldapconnector;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchResultEntry;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.southdevon.trust.util.LdapUtil;


@RunWith(PowerMockRunner.class)
@PrepareForTest(LdapUtil.class)
public class LDAPConnectorTest {
	
	
	@Test
	public void test_ldapConnector_to_sync_from_edirectory_to_ldap()
	{
		try 
		
		{
			PowerMockito.mockStatic(LdapUtil.class);
			
			LdapConnection ldapConnection = creatMockLdapConnectionForIdVault();
			
			createMockDataForIdentityVault(ldapConnection);
			
			LdapConnection ucsLdapConnection = createMockLdapConnectionForUCS();
			
			createMockDataForUCS(ucsLdapConnection);
			
			String args [] =new String[]{};
			
			LdapAttributeSynchroniser.main(args);
			
		} 
		catch (Exception e) 
		{
			Assert.fail("Exception occured while synchronising ldap attributes");
		}
		
	}

	private LdapConnection createMockLdapConnectionForUCS() throws LdapException {
		LdapConnection ucsLdapConnection=Mockito.mock(LdapConnection.class);
		
		Mockito.when(LdapUtil.getLDAPConnectionForUCSLdap()).thenReturn(ucsLdapConnection);
		return ucsLdapConnection;
	}

	private LdapConnection creatMockLdapConnectionForIdVault() throws LdapException {
		LdapConnection ldapConnection=Mockito.mock(LdapConnection.class);

		
		
		Mockito.when(LdapUtil.getLDAPConnectionForIdentityVault()).thenReturn(ldapConnection);
		return ldapConnection;
	}

	private void createMockDataForUCS(LdapConnection ucsLdapConnection)
			throws LdapException, CursorException, LdapInvalidDnException {
		SearchCursor searchCursor=Mockito.mock(SearchCursor.class);
		
		Mockito.when(ucsLdapConnection.search( Mockito.any(SearchRequest.class))).thenReturn(searchCursor);
		
		Mockito.when(searchCursor.next()).thenReturn(true).thenReturn(false);
		
		SearchResultEntry searchResultEntry=Mockito.mock(SearchResultEntry.class);
		
		Mockito.when(searchCursor.get()).thenReturn(searchResultEntry);
		
		Entry ucsEntry=Mockito.mock(Entry.class);
		
		Mockito.when(searchResultEntry.getEntry()).thenReturn(ucsEntry);
		
		Mockito.when(searchResultEntry.get("employeeNumber")).thenReturn(null);
		
		Mockito.when(ucsEntry.getDn()).thenReturn(new Dn("uid=admin,cn=users,dc=sdhc,dc=xsdhis,dc=nhs,dc=uk"));
	}

	private void createMockDataForIdentityVault(LdapConnection ldapConnection) throws LdapException, CursorException {
		EntryCursor cursor=Mockito.mock(EntryCursor.class);
		Entry entry=Mockito.mock(Entry.class);
		
		Mockito.when(ldapConnection.search( "ou=ESR,ou=active,ou=users,o=trustiv", "(objectclass=inetOrgPerson)", SearchScope.ONELEVEL )).thenReturn(cursor);
		
		Mockito.when(cursor.next()).thenReturn(true).thenReturn(false);
		Mockito.when(cursor.get()).thenReturn(entry);
		
		Attribute cnAttribute=Mockito.mock(Attribute.class);
		Attribute empNoAttribute=Mockito.mock(Attribute.class);
		
		Mockito.when(entry.get("cn")).thenReturn(cnAttribute);
		Mockito.when(entry.get("nhsivEmployeeNumber")).thenReturn(empNoAttribute);
		
		Value cn=Mockito.mock(Value.class);
		
		Mockito.when(cnAttribute.get()).thenReturn(cn);
		Mockito.when(cn.getString()).thenReturn("testcn");
		
		Value empNo=Mockito.mock(Value.class);

		Mockito.when(empNoAttribute.get()).thenReturn(empNo);
		Mockito.when(empNo.getString()).thenReturn("122333");
	}
}
