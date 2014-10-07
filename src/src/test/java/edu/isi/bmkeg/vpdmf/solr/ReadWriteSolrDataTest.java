package edu.isi.bmkeg.vpdmf.solr;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.isi.bmkeg.utils.springContext.AppContext;
import edu.isi.bmkeg.utils.springContext.BmkegProperties;
import edu.isi.bmkeg.vpdmf.controller.VPDMfKnowledgeBaseBuilder;
import edu.isi.bmkeg.vpdmf.controller.queryEngineTools.QueryEngineImpl;
import edu.isi.bmkeg.vpdmf.model.definitions.VPDMf;
import edu.isi.bmkeg.vpdmf.model.definitions.ViewDefinition;
import edu.isi.bmkeg.vpdmf.model.instances.AttributeInstance;
import edu.isi.bmkeg.vpdmf.model.instances.ViewInstance;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/edu/isi/bmkeg/vpdmf/appCtx-VPDMfTest.xml" })
public class ReadWriteSolrDataTest {

	ApplicationContext ctx;

	VPDMfSolrApi solr;
	
	File solrSchemaFile;
	File buildFile;

	String storeUrl, login, password, dbName;
	File archiveDir, uploadDataFile;
	
	VPDMf top;
	VPDMfKnowledgeBaseBuilder builder;
	
	@Before
	public void setUp() throws Exception {		
		
		ctx = AppContext.getApplicationContext();

		BmkegProperties prop = (BmkegProperties) ctx.getBean("bmkegProperties");
		
		storeUrl = "http://localhost:8080/apache-solr-3.6.0/ArticleCitation/";
		archiveDir = new File(prop.getHomeDirectory()
				+ "/corpora/medline/inputArchives/");

		uploadDataFile = new File(prop.getHomeDirectory()
				+ "/corpora/medline/uploadData.xml");
		
		login = prop.getDbUser();
		password =  prop.getDbPassword();

		try {
			solr = new VPDMfSolrApi(storeUrl, login, password, 45, uploadDataFile);
		} catch (Exception e) {
			// Gully: Do nothing, this is OK.
		}
		
		// need to only use lowercase for database names within linux systems
		dbName = "diglib_vpdmf_test";
		
	    buildFile = ctx.getResource("classpath:edu/isi/bmkeg/vpdmf/digitalLibrary_VPDMf.zip")
				.getFile();
	    
	    builder = new VPDMfKnowledgeBaseBuilder(buildFile, login, password, dbName);		

		try {
			builder.destroyDatabase(dbName);
		} catch (SQLException sqlE) {
			// Gully: Make sure that this runs, avoid silly issues.
			if( !sqlE.getMessage().contains("database doesn't exist") ) {
				sqlE.printStackTrace();
//				throw sqlE;
			}
		}
		//builder.buildDatabaseFromArchive();		
		
	}

	@After
	public void tearDown() throws Exception {
		
		builder.destroyDatabase(dbName);	

	}

	@Test @Ignore("disabled because SOLR server is not running")
	public void testDumpCitationDataToSolrStore() throws Exception {

		if(solr == null || !solr.checkIfServerIsOn() ) {
			System.err.println("Server is not on, can't run tests.");
			assert(true);
			return; 
		}
	    
		QueryEngineImpl qe = new QueryEngineImpl(this.login, this.password, dbName);
		qe.connectToDB(this.login, this.password, this.dbName);
	 
	    top = qe.readTop();
				
		ViewDefinition vd = top.getViews().get("ArticleCitation");

		ViewInstance vi = new ViewInstance(vd);
		 
	    List<ViewInstance> viewList = qe.executeFullQuery(vi);
	    
	    qe.closeDbConnection();
	
	    Iterator<ViewInstance> it = viewList.iterator();
	    while(it.hasNext()) {
	    	vi = it.next();
	    	SolrInputDocument doc = solr.convertViewInstanceToSolrDocument(vi);
	    	solr.addDocToStore(doc);
	    }
	    	    
	    // pass the test if it runs without errors*/
			
	}
	
	@Test @Ignore("disabled because SOLR server is not running")
	public void testReadCitationsFromSolrStore() throws Exception {

		if(solr == null || !solr.checkIfServerIsOn() ) {
			System.err.println("Server is not on, can't run tests.");
			assert(true);
			return; 
		}
		
		QueryEngineImpl qe = new QueryEngineImpl(this.login, this.password, dbName);
		qe.connectToDB(this.login, this.password, this.dbName);
	 
	    top = qe.readTop();
		ViewDefinition vd = top.getViews().get("ArticleCitation");
		ViewInstance vi = new ViewInstance(vd);
		AttributeInstance ai = vi.readAttributeInstance("]Scientist|Person.fullName", 0);
		ai.writeValueString("S Hagg");

		String q = solr.convertViewInstanceToSolrQuery(vi);
		List<ViewInstance> l = solr.readDocsFromStore(q, vd);

		assertEquals("Data should be 'vpdmfId=32106'", "vpdmfId=32106", l.get(0).getName());
		
	}
	
	@Test @Ignore("disabled because SOLR server is not running")
	public void testDeleteCitationsFromSolrStore() throws Exception {

		if(solr == null || !solr.checkIfServerIsOn() ) {
			System.err.println("Server is not on, can't run tests.");
			assert(true);
			return; 
		}
		
		QueryEngineImpl qe = new QueryEngineImpl(this.login, this.password, dbName);
		qe.connectToDB(this.login, this.password, this.dbName);
	 
	    top = qe.readTop();
		ViewDefinition vd = top.getViews().get("ArticleCitation");
		ViewInstance vi = new ViewInstance(vd);
		AttributeInstance ai = vi.readAttributeInstance("]Scientist|Person.fullName", 0);
		ai.writeValueString("S Hagg");

		String q = solr.convertViewInstanceToSolrQuery(vi);
		int s = solr.deleteDocsfromStore(q);
		
		List<ViewInstance> l = solr.readDocsFromStore(q, vd);
		
		assertEquals("Should not be able to find any papers with 'S Hagg' as an author", 0, l.size());
		
	}
	
	@Test @Ignore("disabled because SOLR server is not running")
	public void testDeleteAllCitationsFromSolrStore() throws Exception {

		if(solr == null || !solr.checkIfServerIsOn() ) {
			System.err.println("Server is not on, can't run tests.");
			assert(true);
			return; 
		}

		int s = solr.deleteAllDocsfromStore();
		
		assertEquals("Return status should be '0'", 0, s);
		
	}

}
