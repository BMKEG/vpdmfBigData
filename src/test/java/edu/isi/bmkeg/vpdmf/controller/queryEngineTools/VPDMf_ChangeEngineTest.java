package edu.isi.bmkeg.vpdmf.controller.queryEngineTools;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.SQLException;
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
import edu.isi.bmkeg.vpdmf.exceptions.AttributeAddressException;
import edu.isi.bmkeg.vpdmf.model.definitions.VPDMf;
import edu.isi.bmkeg.vpdmf.model.definitions.ViewDefinition;
import edu.isi.bmkeg.vpdmf.model.instances.AttributeInstance;
import edu.isi.bmkeg.vpdmf.model.instances.LightViewInstance;
import edu.isi.bmkeg.vpdmf.model.instances.ViewInstance;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/edu/isi/bmkeg/vpdmf/appCtx-VPDMfTest.xml" })
public class VPDMf_ChangeEngineTest {

	ApplicationContext ctx;

	VPDMf top;
	UMLmodel m;
	UMLModelSimpleParser p;

	String dbName;
	String login;
	String password;

	ChangeEngine ce;

	VPDMfKnowledgeBaseBuilder builder;
	File buildFile;

	ViewDefinition vd;
	ViewInstance vi;

	String sql;

	@Before
	public void setUp() throws Exception {

		ctx = AppContext.getApplicationContext();

		BmkegProperties prop = (BmkegProperties) ctx.getBean("bmkegProperties");

		login = prop.getDbUser();
		password = prop.getDbPassword();
		dbName = "basic_vpdmf_test";

		buildFile = ctx
				.getResource(
						"classpath:edu/isi/bmkeg/vpdmf/people/people-mysql-1.1.5-SNAPSHOT.zip")
				.getFile();

		builder = new VPDMfKnowledgeBaseBuilder(buildFile, login, password,
				dbName);
		builder.setLogin(login);
		builder.setPassword(password);

		try {
			builder.destroyDatabase(dbName);
		} catch (SQLException sqlE) {
			if (!sqlE.getMessage().contains("database doesn't exist")) {
				sqlE.printStackTrace();
				// Gully, avoids unnecessary isssues.
				// throw sqlE;
			}
		}

		builder.buildDatabaseFromArchive();

		ce = new ChangeEngineImpl(this.login, this.password, dbName);
		ce.connectToDB();

		top = ce.readTop();

		ce.closeDbConnection();

	}

	@After
	public void tearDown() throws Exception {

		builder.destroyDatabase(dbName);

	}

	@Test
	public final void testInsertAndDeleteQuery() throws Exception {

		try {

			ViewInstance vi = buildDummyView("TEST");

			ce.connectToDB(this.login, this.password, dbName);
			ce.turnOffAutoCommit();
			ce.executeInsertQuery(vi);
			ce.commitTransaction();
			ce.turnOnAutoCommit();

		} catch (Exception e) {

			e.printStackTrace();
			ce.rollbackTransaction();

			System.out.println("        *** transaction rolled back ***");
			throw e;

		} finally {

			ce.closeDbConnection();

		}

		// Need to run a query to check if the insert actually worked.
		ViewDefinition vd = top.getViews().get("Person");
		vi = new ViewInstance(vd);
		vi.readAttributeInstance("]Person|Person.initials", 0)
				.writeValueString("TEST");

		ce.connectToDB(this.login, this.password, dbName);

		List<LightViewInstance> viewList = ce.executeListQuery(vi);

		ce.closeDbConnection();

		LightViewInstance newVi = viewList.get(0);

		assertTrue("Inserted UID value should be vpdmfId=1, ", newVi
				.getUIDString().equals("vpdmfId=1"));

	}

	private ViewInstance buildDummyView(String temp) throws Exception,
			AttributeAddressException {
		ViewDefinition vd = top.getViews().get("Person");
		ViewInstance vi = new ViewInstance(vd);

		vi.readAttributeInstance("]Person|Person.initials", 0)
				.writeValueString(temp);
		vi.readAttributeInstance("]Person|Person.surname", 0).writeValueString(
				temp);
		vi.readAttributeInstance("]Person|Person.affiliation", 0)
				.writeValueString(temp);
		return vi;
	}

	@Test
	public final void testRollback() throws Exception {

		ViewInstance vi1 = buildDummyView("TEST1");

		ce.connectToDB(this.login, this.password, dbName);
		ce.turnOffAutoCommit();
		ce.executeInsertQuery(vi1);
		ce.rollbackTransaction();
		ce.turnOnAutoCommit();

		//
		// Need to run a query to check if the rollback actually worked.
		//
		ViewDefinition vd = top.getViews().get("Person");
		ViewInstance vi2 = new ViewInstance(vd);
		vi2.readAttributeInstance("]Person|Person.initials", 0)
				.writeValueString("TEMP");

		ce.connectToDB(this.login, this.password, dbName);

		int c = ce.executeCountQuery(vi2);

		ce.closeDbConnection();

		assertTrue("There should be no entry in the database after the rollback, list size = "
						+ c, c == 0);

	}

	@Test
	public final void testExecuteUpdateQuery() throws Exception {

		ViewInstance vi1 = buildDummyView("TEST1");

		ce.connectToDB(this.login, this.password, dbName);
		ce.turnOffAutoCommit();
		ce.executeInsertQuery(vi1);
		ce.turnOnAutoCommit();

		ce.storeViewInstanceForUpdate(vi1);

		//
		// Need to run a query to check if the rollback actually worked.
		//
		vi1.readAttributeInstance("]Person|Person.initials", 0)
				.writeValueString("NEW_VALUE");

		ce.executeUpdateQuery(vi1);

		ViewInstance vi2 = ce.executeUIDQuery("Person", 1L);
		
		assertTrue("label should be changed: " 
				+ vi2.getVpdmfLabel(), vi2.getVpdmfLabel().startsWith("NEW_VALUE"));
		
	}

	@Test
	@Ignore("Outdated")
	//
	// TODO: Deletion is not done very well at all, need to improve from a generic view-based perspective
	//
	public final void testDeleteView() throws Exception {

		try {

			ViewDefinition vd = top.getViews().get("Article");
			ViewInstance vi = new ViewInstance(vd);
			AttributeInstance ai = vi.readAttributeInstance(
					"]Resource|ViewTable.vpdmfId", 0);
			ai.writeValueString("32106");

			QueryEngineImpl vhf = new QueryEngineImpl(this.login, this.password, dbName);

			vhf.connectToDB();
			vhf.stat.execute("set autocommit=0;");

			//
			// Then delete the data
			//
			ce.connectToDB(this.login, this.password, dbName);
			ce.turnOffAutoCommit();
			ce.executeDeleteQuery("Article", 32106L);
			ce.commitTransaction();

		} catch (Exception e) {

			e.printStackTrace();
			ce.rollbackTransaction();

		} finally {

			ce.closeDbConnection();

		}

		ViewDefinition vd = top.getViews().get("Article");
		ViewInstance vi = new ViewInstance(vd);
		AttributeInstance ai = vi.readAttributeInstance(
				"]Resource|Resource.vpdmfId", 0);
		ai.writeValueString("32106");

		ce.connectToDB(this.login, this.password, dbName);

		List<LightViewInstance> viewList = ce.executeListQuery(vi);

		assertTrue("Should have removed view data with ViewTableId = 32106.",
				viewList.size() == 0);

	}

}
