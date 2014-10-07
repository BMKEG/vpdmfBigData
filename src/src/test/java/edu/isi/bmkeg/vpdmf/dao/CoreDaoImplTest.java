package edu.isi.bmkeg.vpdmf.dao;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.uml.sources.UMLModelSimpleParser;
import edu.isi.bmkeg.utils.springContext.AppContext;
import edu.isi.bmkeg.utils.springContext.BmkegProperties;
import edu.isi.bmkeg.vpdmf.controller.VPDMfKnowledgeBaseBuilder;
import edu.isi.bmkeg.vpdmf.model.definitions.VPDMf;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/edu/isi/bmkeg/vpdmf/appCtx-VPDMfTest.xml" })
public class CoreDaoImplTest {

	Logger log = Logger.getLogger("edu.isi.bmkeg.vpdmf.dao.CoreDaoImplTest");

	ApplicationContext ctx;

	VPDMf top;
	ClassLoader cl;
	UMLmodel m;
	UMLModelSimpleParser p;

	String dbName;
	String login;
	String password;
	String workingDirectory;
	
	CoreDao coreDao;

	VPDMfKnowledgeBaseBuilder builder;

	File buildFile;
	
	String sql;
	
	// DO WE NEED TO REBUILD THE DATABASE FROM SCRATCH AFTER EVERY TEST?
	static boolean REBUILD_DB = true;

	@Before
	public void setUp() throws Exception {

		ctx = AppContext.getApplicationContext();

		BmkegProperties prop = (BmkegProperties) ctx.getBean("bmkegProperties");
		
		login = prop.getDbUser();
		password =  prop.getDbPassword();
		dbName = "basic_vpdmf_test";
		workingDirectory = prop.getWorkingDirectory();
		
		buildFile = ctx
				.getResource(
						"classpath:edu/isi/bmkeg/vpdmf/people/people-mysql-1.1.5-SNAPSHOT.zip")
				.getFile();
	    
	    builder = new VPDMfKnowledgeBaseBuilder(buildFile, login, password, dbName);
		
	    if( REBUILD_DB || !builder.checkIfKbExists(dbName) ) {

	    	try {
				builder.destroyDatabase(dbName);
			} catch (SQLException sqlE) {
				// Gully: Make sure that this runs, avoid silly issues.
				if( !sqlE.getMessage().contains("database doesn't exist") ) {
					sqlE.printStackTrace();
					throw sqlE;
				}
			}
			builder.buildDatabaseFromArchive();
			
		}
	    
	    File jarLocation = new File(buildFile.getParent() + "/people-jpa-1.1.5-SNAPSHOT.jar" );
		
	    coreDao = new CoreDaoImpl();
		coreDao.init(login, password, dbName, workingDirectory);
	    
		URL url = jarLocation.toURI().toURL();
		URL[] urls = new URL[]{url};
		cl = new URLClassLoader(urls);
	    
	}

	@After
	public void tearDown() throws Exception {

		if( REBUILD_DB ) {
			builder.destroyDatabase(dbName);
		}
		
	}

	@Test 
	public final void testSortedListQuery() throws Exception {}

/*		ViewTable_qo tsObject = (ViewTable_qo) Class.forName("edu.isi.bmkeg.triage.model.qo.TriageScore_qo", true, cl).newInstance();
		tsObject.getClass().getDeclaredMethod("setInScore", String.class).invoke(tsObject, "<vpdmf-sort-0>");
	    
		List<LightViewInstance> viewList = coreDao.list(tsObject, "TriageScore");
			    	    	    
	    assertTrue("Viewlist needs to have 4 views: ", viewList.size() == 4);
	}*/
	
}
