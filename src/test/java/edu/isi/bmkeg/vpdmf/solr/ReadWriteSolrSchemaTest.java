package edu.isi.bmkeg.vpdmf.solr;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
import edu.isi.bmkeg.vpdmf.model.definitions.specs.VpdmfSpec;
import edu.isi.bmkeg.vpdmf.utils.VPDMfConverters;
import edu.isi.bmkeg.vpdmf.utils.VPDMfParser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/edu/isi/bmkeg/vpdmf/appCtx-VPDMfTest.xml" })
public class ReadWriteSolrSchemaTest {

	ApplicationContext ctx;

	VPDMfSolrUtils solr;
	
	File solrSchemaFile;
	File diglibUmlFile;
	File diglibSpecsFile;
	File diglibSpecsDir;
	File diglibSolrZip;

	VPDMf top;
	
	@Before
	public void setUp() throws Exception {		
		
		ctx = AppContext.getApplicationContext();

		solr = new VPDMfSolrUtils();
		
		diglibSpecsFile = ctx.getResource("classpath:edu/isi/bmkeg/vpdmf/digitalLibrary/digitalLibrary_vpdmf.xml")
				.getFile();
		diglibSpecsDir = ctx.getResource("classpath:edu/isi/bmkeg/vpdmf/digitalLibrary/specs")
				.getFile();
		List<File> views = VPDMfParser.getAllSpecFiles(diglibSpecsDir);

		diglibUmlFile = ctx.getResource(
				"classpath:edu/isi/bmkeg/vpdmf/digitalLibrary/digitalLibrary.xml").getFile();

		diglibSpecsDir = ctx.getResource("classpath:edu/isi/bmkeg/vpdmf/digitalLibrary/specs")
				.getFile();
		
		diglibSolrZip = new File( diglibUmlFile.getParentFile().getParent() + "/solrFiles_diglib.zip");
		
		VpdmfSpec vpdmfSpec = VPDMfConverters.readVpdmfSpecFromFile(diglibSpecsFile);
		
		UMLModelSimpleParser p = new UMLModelSimpleParser(UMLmodel.XMI_MAGICDRAW);
		p.parseUMLModelFile(diglibUmlFile);
		
		UMLmodel m = p.getUmlModels().get(0);
							
		VPDMfParser vpdmfP = new VPDMfParser();
		top = vpdmfP.buildAllViews(vpdmfSpec, m, views);			
		
	}

	@After
	public void tearDown() throws Exception {
	
		diglibSolrZip.delete();
	}
	
	@Test @Ignore("Outdated")
	public void testBuildSchema() throws Exception {

		String[] views = new String[] { 
				"ArticleCitation", "Corpus"
				};
		
		solr.buildSolrSpecZip(diglibSolrZip, views, top);
		
		long fileSize = diglibSolrZip.length();
		
//		assertEquals("Zip file expected to be 174.5 KB: ", 175598L, fileSize);
		
			
	}

	/*@Test
	public void testDumpCitationDataToSolrStore() throws Exception {

		ctx = AppContext.getApplicationContext();

		BmkegProperties prop = (BmkegProperties) ctx.getBean("bmkegProperties");
		
		String login = prop.getDbUser();
		String password =  prop.getDbPassword();
		String dbName = "diglib_vpdmf_test";
		
	    File buildFile = ctx.getResource("classpath:edu/isi/bmkeg/vpdmf/digitalLibrary_VPDMf.zip")
				.getFile();
	    
	    VPDMfKnowledgeBaseBuilder builder = new VPDMfKnowledgeBaseBuilder(buildFile, login, password, dbName);		
	    
	    try {
			builder.destroyDatabase(dbName);	    	
	    } catch (Exception e) {}
	    
		builder.buildDatabaseFromArchive();
		
		builder.destroy();
		
		int wait = 0;
		wait++;
			
	}*/
	
}
