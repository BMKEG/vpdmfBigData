package edu.isi.bmkeg.vpdmf.solr;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.common.SolrInputDocument;

import com.google.common.io.Files;

import edu.isi.bmkeg.uml.interfaces.SolrUmlInterface;
import edu.isi.bmkeg.uml.model.UMLattribute;
import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.utils.Converters;
import edu.isi.bmkeg.utils.TextUtils;
import edu.isi.bmkeg.utils.superGraph.SuperGraphTraversal;
import edu.isi.bmkeg.utils.xml.XmlBindingTools;
import edu.isi.bmkeg.vpdmf.controller.queryEngineTools.ChangeEngineImpl;
import edu.isi.bmkeg.vpdmf.model.definitions.IndexElement;
import edu.isi.bmkeg.vpdmf.model.definitions.PrimitiveDefinition;
import edu.isi.bmkeg.vpdmf.model.definitions.PrimitiveDefinitionGraph;
import edu.isi.bmkeg.vpdmf.model.definitions.VPDMf;
import edu.isi.bmkeg.vpdmf.model.definitions.ViewDefinition;
import edu.isi.bmkeg.vpdmf.model.instances.AttributeInstance;
import edu.isi.bmkeg.vpdmf.model.instances.ViewInstance;
import edu.isi.bmkeg.vpdmf.solr.specs.CoreListSpec;
import edu.isi.bmkeg.vpdmf.solr.specs.CoreSpec;
import edu.isi.bmkeg.vpdmf.solr.specs.FieldSpec;
import edu.isi.bmkeg.vpdmf.solr.specs.SchemaSpec;
import edu.isi.bmkeg.vpdmf.solr.specs.SolrSpec;

public class VPDMfSolrUtils {

	private static Logger logger = Logger.getLogger(VPDMfSolrUtils.class);

	private Map<String, File> cleanUp = new HashMap<String, File>();
	
	// _________________________________________________________________________________
	// Schema
	//
	private File tempConfigDirectory;

	private SchemaSpec schema;


	// _________________________________________________________________________________

	public SchemaSpec getSchema() {
		return schema;
	}

	public void setSchema(SchemaSpec schema) {
		this.schema = schema;
	}

	// _________________________________________________________________________________

	public void buildSolrSpecZip(File solrSchemaZip, String[] views, VPDMf top)
			throws Exception {
		
		File confZip = new File(ClassLoader.getSystemResource(
				"edu/isi/bmkeg/vpdmf/solr/conf.zip").getFile());
		tempConfigDirectory = Files.createTempDir();
		tempConfigDirectory.deleteOnExit();
		
		Converters.unzipIt(confZip, tempConfigDirectory);
		
		File schemaFile = new File(tempConfigDirectory.getPath()
				+ "/conf/schema-template.xml");
		String spec = TextUtils.readFileToString(schemaFile);
		spec = spec.replaceAll("[\\t\\n]", "");
		StringReader reader = new StringReader(spec);

		this.setSchema(XmlBindingTools.parseXML(reader, SchemaSpec.class));

		File confDir = new File(tempConfigDirectory.getPath() + "/conf");

		Map<String, File> fileMap = new HashMap<String, File>();

		SolrSpec solr = this.buildSolrCoresForVPDMf(top, views);

		File solrFile = new File(tempConfigDirectory.getPath() + "/solr.xml");
		XmlBindingTools.saveAsXml(solr, solrFile);

		this.cleanUp.put(solrFile.getPath(), solrFile);
		fileMap.put("solr.xml", solrFile);

		for (int i = 0; i < views.length; i++) {
			String key = views[i];
			ViewDefinition vd = top.getViews().get(key);

			SchemaSpec schema = this.buildSolrSchemaForView(vd);

			File solrSchema = new File(tempConfigDirectory.getPath() + "/"
					+ vd.getName().toLowerCase() + "_schema.xml");

			XmlBindingTools.saveAsXml(schema, solrSchema);

			fileMap.put(vd.getName().toLowerCase() + "/conf/schema.xml",
					solrSchema);
			this.cleanUp.put(solrSchema.getPath(), solrSchema);

			this.addToFileMap(fileMap, confDir, vd.getName().toLowerCase());

		}

		Converters.zipIt(fileMap, solrSchemaZip);
		Converters.recursivelyDeleteFiles(tempConfigDirectory);

	}

	private void addToFileMap(Map<String, File> fileMap, File dirToAdd,
			String stem) {

		File[] ff = dirToAdd.listFiles();
		for (int j = 0; j < ff.length; j++) {
			File f = ff[j];
			if (f.getName().equals("schema.xml")) {

				continue;

			} else if (f.isDirectory()) {

				this.addToFileMap(fileMap, f, stem);

			} else {

				String path = f.getPath().substring(
						tempConfigDirectory.getPath().length());
				fileMap.put(stem + path, f);
				this.cleanUp.put(f.getPath(), f);
			}

		}

	}

	private SolrSpec buildSolrCoresForVPDMf(VPDMf top, String[] views)
			throws Exception {

		SolrSpec solr = new SolrSpec();
		CoreListSpec cores = new CoreListSpec();
		solr.setCoreList(cores);

		for (int i = 0; i < views.length; i++) {
			String key = views[i];
			ViewDefinition vd = top.getViews().get(key);

			if( vd == null ) {
				throw new Exception("View " + key + 
						" not present in model. Please check " + top.getUmlModel().getName() + 
						"_vpdmf.xml file");
			}
			
			if (vd.getType() != ViewDefinition.DATA
					&& vd.getType() != ViewDefinition.COLLECTION)
				continue;

			CoreSpec core = new CoreSpec();
			core.setName(vd.getName());
			core.setInstanceDir(vd.getName().toLowerCase());

			cores.getCoreList().add(core);

		}
		return solr;

	}

	private SchemaSpec buildSolrSchemaForView(ViewDefinition vd)
			throws Exception {

		this.schema.setName(vd.getName().toLowerCase());
		UMLattribute pk = vd.getPrimaryPrimitive().getPrimaryClass()
				.getPkArray().get(0);
		
		SolrUmlInterface sui = new SolrUmlInterface();
		UMLmodel m = vd.getTop().getUmlModel();
		sui.setUmlModel(m);
		sui.convertAttributes();

		Set<String> ieAddresses = new HashSet<String>();
		Iterator<IndexElement> ieIt = vd.getIndexElements().values().iterator();
		while (ieIt.hasNext()) {
			IndexElement ie = ieIt.next();
			ieAddresses.add(ie.getAttributeAddress());

			if( ie.isUniqueKey() ) {
				this.schema.setUniqueKey( "]" + ie.getP() + "|"
					+ ie.getC()	+ "." + ie.getA());
			}

		}

		PrimitiveDefinitionGraph pdg = (PrimitiveDefinitionGraph) vd
				.getSubGraph();
		SuperGraphTraversal traversal = new SuperGraphTraversal(pdg);
		traversal.traverseDependency();
		traversal.buildEdgeTraversal();

		Iterator<String> pvKeyIt = vd.getSubGraph().getNodes().keySet()
				.iterator();
		while (pvKeyIt.hasNext()) {
			String pvKey = pvKeyIt.next();
			PrimitiveDefinition pv = (PrimitiveDefinition) vd.getSubGraph()
					.getNodes().get(pvKey);

			Iterator<String> attrAddrIt = pv.readAttributeAddresses()
					.iterator();
			while (attrAddrIt.hasNext()) {
				String attrAddr = attrAddrIt.next();

				UMLattribute ad = vd.readAttributeDefinition(attrAddr);

				FieldSpec f = new FieldSpec();

				f.setName(attrAddr);
				f.setType(ad.getType().getImplName());
				f.setMultiValued(pv.isMultiple());
				f.setStored(true);
				f.setIndexed(true);

				schema.getFields().add(f);

			}
		
		}
		
		schema.cleanUpElements();

		return schema;

	}

	// _________________________________________________________________________________




}
