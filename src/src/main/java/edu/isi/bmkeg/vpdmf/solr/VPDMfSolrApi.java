package edu.isi.bmkeg.vpdmf.solr;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import edu.isi.bmkeg.uml.model.UMLattribute;
import edu.isi.bmkeg.utils.xml.XmlBindingTools;
import edu.isi.bmkeg.vpdmf.exceptions.VPDMfException;
import edu.isi.bmkeg.vpdmf.model.definitions.PrimitiveDefinition;
import edu.isi.bmkeg.vpdmf.model.definitions.ViewDefinition;
import edu.isi.bmkeg.vpdmf.model.instances.AttributeInstance;
import edu.isi.bmkeg.vpdmf.model.instances.ViewInstance;
import edu.isi.bmkeg.vpdmf.solr.data.TimedCount;
import edu.isi.bmkeg.vpdmf.solr.data.UploadProcessTiming;

public class VPDMfSolrApi {

	private static Logger logger = Logger.getLogger(VPDMfSolrApi.class);
	
	private StreamingUpdateSolrServer server;
	
	private UploadProcessTiming eptData;
	private File eptDataFile;
	
	private int skipped;

	private int submitted;	
	private Date dateTimeLastSubmission;

	private long completed; 
	private Date dateTimeStoreLastPolled;

	// TODO: How to represent this in a vpdmf-enabled system?
	//private boolean filterOutNonJournalListRecords;

	private String solrUrl;

	private String login;
	
	private String password;

	// _________________________________________________________________________________
	// Insert / Update
	//
	private int batchStep;

	private ArrayList<SolrInputDocument> batch;

	
	// _________________________________________________________________________________
	// Query
	//
	private int start; 

	private int max;

	private long queryHitCount;
	
	private static int defaultMaxHits = 20000000;
	
	// _________________________________________________________________________________
	// Server
	//

	public VPDMfSolrApi(String solrUrl, String login, String password, int batchStep, File dataFile) throws Exception {

		this.skipped = 0;
		this.completed = 0;
		this.submitted = 0;

		this.solrUrl = solrUrl;
		this.login = login;
		this.password = password;
		
		this.batchStep = batchStep;
		this.max = VPDMfSolrApi.defaultMaxHits;

		eptData = new UploadProcessTiming();
		eptDataFile = dataFile;

		this.initializeServer();
		
	}
	
	private void initializeServer() throws Exception {

		server = new StreamingUpdateSolrServer(this.solrUrl, 10000, 1);

		// socket read timeout
		server.setSoTimeout(10000000);
		server.setConnectionTimeout(1000000);
		server.setDefaultMaxConnectionsPerHost(100);
		server.setMaxTotalConnections(100);
		server.setFollowRedirects(false);
		

		// defaults to 0. > 1 not recommended.
		server.setMaxRetries(1);

		// binary parser is used by default
		server.setParser(new XMLResponseParser());

		// allowCompression defaults to false.
		// Server side must support gzip or deflate for this to have any effect.
		server.setAllowCompression(true);

		batch = new ArrayList<SolrInputDocument>(batchStep);
		
		//set username and password
		URL u = new URL(this.solrUrl);
		String host = u.getHost();
		int port = u.getPort();
		Credentials creds = new UsernamePasswordCredentials(login,password);
		server.getHttpClient().getParams().setAuthenticationPreemptive(true);
		server.getHttpClient().getState().setCredentials(
				new AuthScope(host, port, AuthScope.ANY_REALM), 
				creds);

		this.completed = this.pollStore();

	}

	public void finalCommit() throws Exception {
		
		if(!isBatchEmpty()){
			commitDocsToStore();
		}
	
	} 

	
	public void close() throws Exception {
		
		if(!isBatchEmpty()){
			commitDocsToStore();
		}

		logger.info( submitted + " documents added to " + solrUrl );
		logger.info( skipped + " skipped" );
		
		if( this.eptDataFile != null) {
			XmlBindingTools.saveAsXml(this.eptData, this.eptDataFile);
		}
	
	} 

	private boolean isBatchEmpty(){
		
		if(batch.isEmpty()) 
			return true;
		
		return false;

	}

	public boolean checkIfServerIsOn() {
		
		SolrQuery q = new SolrQuery("test:test");
		q.setStart(start);
		q.setRows(new Integer(max));
		QueryResponse rsp = null;
		
		try {
			rsp = server.query( q );			
		} catch (SolrServerException e) {
	
			if( e.getCause() instanceof ConnectException)  {
				return false;
			} else {
				return true;				 
			}
				
		}
		
		return false;

	}

	// _________________________________________________________________________________
	// Insert views into the store
	//
	public SolrInputDocument convertViewInstanceToSolrDocument(ViewInstance vi)
			throws Exception {

		SolrInputDocument solrDoc = new SolrInputDocument();

		ViewDefinition vd = vi.getDefinition();
		Iterator<String> pvNameIt = vd.getSubGraph().getNodes().keySet()
				.iterator();
		while (pvNameIt.hasNext()) {
			String pvName = pvNameIt.next();
			PrimitiveDefinition pd = (PrimitiveDefinition) vd.getSubGraph()
					.getNodes().get(pvName);
			int pvCount = vi.countPrimitives(pd);

			Iterator<UMLattribute> adIt = pd.readAttributes().iterator();
			while (adIt.hasNext()) {
				UMLattribute ad = adIt.next();

				String addr = "]" + pd.getName() + "|"
						+ ad.getParentClass().getBaseName() + "."
						+ ad.getBaseName();

				if (pvCount > 0) {
					List<String> fieldValue = new ArrayList<String>();
					for (int i = 0; i < pvCount; i++) {
						AttributeInstance ai = vi
								.readAttributeInstance(addr, i);
						fieldValue.add(ai.readValueString());
					}
					solrDoc.addField(addr, fieldValue);
				} else {
					AttributeInstance ai = vi.readAttributeInstance(addr, 0);
					solrDoc.addField(addr, ai.readValueString());
				}

			}

		}

		return solrDoc;

	}
	
	public void skipDoc(String id) {
		
		this.skipped++;	

		logger.info("Skipped: " + id );
		
	}
	
	public void addDocToStore(SolrInputDocument doc) throws SolrServerException, IOException {

		if (batch.size() < batchStep ) {

			batch.add(doc);

		} else	{
		
			this.commitDocsToStore();
			batch.add(doc);
		
		}
	
	}

	public void rollback() throws SolrServerException, IOException {
		
		UpdateResponse u = server.rollback();
	
	}

	
	public void commitDocsToStore() throws SolrServerException, IOException {
	
		submitted += batch.size();

		logger.info("Added this run: " + submitted + ", total: " + completed);
		
		server.add( batch.iterator() );
		server.commit(true, true);
		batch.clear();
		
		this.dateTimeLastSubmission = new Date();
		
		TimedCount tc = new TimedCount();
		tc.setCount(submitted);
		tc.setTime(this.dateTimeLastSubmission);
		
		this.eptData.getSubmitted().add(tc);
	
	}
	
	public int deleteDocsfromStore(String query) throws SolrServerException, IOException {
		
		logger.info( "Deleting... " + query );

		UpdateResponse r = server.deleteByQuery(query);
		server.commit(true, true);
		int s = r.getStatus();
		
		return s;
	
	}

	public int deleteAllDocsfromStore() throws SolrServerException, IOException {
		
		logger.info( "Deleting... " );

		UpdateResponse r = server.deleteByQuery("*:*");
		server.commit(true, true);
		int s = r.getStatus();
		
		return s;
	
	}
	
	
	// _________________________________________________________________________________
	// Query view from the store
	//
	public String convertViewInstanceToSolrQuery(ViewInstance vi) throws Exception {

		String solrQuery = "";
		
		Iterator<String> addrIt = vi.readAttributeAddresses().iterator();
		while( addrIt.hasNext() ) {
			String addr = addrIt.next();
			
			if( addr.contains("ViewTable.viewType")) 
				continue;
			
			AttributeInstance ai = vi.readAttributeInstance(addr, 0);
			
			if( ai.getValue() != null && ai.readValueString().length() > 0 ) {
				
				String q = ai.getAddress() + ":" + ai.readValueString(); 
				
				int pCount = vi.countPrimitives(addr);
				if( pCount > 1 ) { 
					for(int i=1; i<pCount; i++) {
						ai = vi.readAttributeInstance(addr, i);
						q += " OR " + ai.getAddress() + ":" + ai.readValueString();
					}
					
					q = "(" + q + ")";
				
				}
				
				if( solrQuery.length() > 0 ) {
					solrQuery += " AND ";
				}
				solrQuery += q;
					
			}
			
		}
		
		solrQuery = solrQuery.replaceAll("\\ ", "\\\\ ");
		solrQuery = solrQuery.replaceAll("\\]", "\\\\]");
		
		return solrQuery;
		
	}		
	
	public long pollStore() throws Exception {

		SolrQuery q = new SolrQuery("*:*");
		q.setStart(0);
		q.setRows(new Integer(1));		
		QueryResponse rsp = server.query( q );

		this.dateTimeStoreLastPolled = new Date();
		long count = rsp.getResults().getNumFound();
		this.completed = count;
		
		TimedCount tc = new TimedCount();
		tc.setCount(count);
		tc.setTime(this.dateTimeStoreLastPolled);
		this.eptData.getPolled().add(tc);
		
		q = null;
		rsp = null;
		
		return count;
			
	}	
	
	public List<ViewInstance> readDocsFromStore(String query, ViewDefinition vd) throws Exception {

		List<ViewInstance> l = new ArrayList<ViewInstance>();
		
		SolrQuery q = new SolrQuery(query);
		q.setStart(start);
		q.setRows(new Integer(max));
		QueryResponse rsp = null;
		rsp = server.query( q );
		
		SolrDocumentList docs = rsp.getResults();
		this.queryHitCount = rsp.getResults().getNumFound();
		
		for( int i=0; i<(int)this.queryHitCount; i++) {
			SolrDocument sd = docs.get(i);
		
			ViewInstance vi = new ViewInstance(vd);
			Iterator<String> it = vi.readAttributeAddresses().iterator();
			while( it.hasNext() ) {
				String addr = it.next();
				
				AttributeInstance ai = vi.readAttributeInstance(addr, 0);
				UMLattribute ad = ai.getDefinition();
				String type = ad.getType().getBaseName();
				Object o = sd.getFieldValue(addr);

				if( o == null ) {
					continue;	
				}
				
				if( o == null ) {
				
					continue;
					
				} else if( o instanceof List) {
					
					List<Object> ll = (List<Object>) o;
					for( int j=0; j<ll.size(); j++) {
						Object oo = ll.get(j);
						checkType(type, addr, oo);

						if( j > 0 && vi.countPrimitives(addr) < (j+1) ) {
							int brk = addr.indexOf("]");
							int ln = addr.indexOf("|");
							String pName = addr.substring(brk + 1, ln);
							vi.addNewPrimitiveInstance(pName, j);
						}
						
						ai = vi.readAttributeInstance(addr, j);
						ai.setValue(oo);
						
					}
					
				} else {
					
					checkType(type, addr, o);
					ai = vi.readAttributeInstance(addr, 0);
					ai.setValue(o);					

				}
			
			}
			
			vi.updateIndexes();
			
			l.add(vi);
			
		}
				
		return l;
	
	}	
	
	private void checkType(String type, String addr, Object o) throws VPDMfException {
		
		if( (type.equals("int") && !(o instanceof Integer) ) || 
				(type.equals("long") && !(o instanceof Long) ) || 
				(type.equals("serial") && !(o instanceof Long) ) || 
				(type.equals("float") && !(o instanceof Float) ) || 
				(type.equals("double") && !(o instanceof Double) ) || 
				(type.equals("boolean") && !(o instanceof Boolean) ) || 
				(type.equals("short") && !(o instanceof Short) ) || 
				(type.equals("char") && !(o instanceof String) ) || 
				(type.equals("String ") && !(o instanceof String) ) || 
				(type.equals("url") && !(o instanceof String) ) || 
				(type.equals("longString") && !(o instanceof String) ) ) {

			throw new VPDMfException("Mismatch in SOLR data " + addr + ", type: " + type);

		}
	
	}
	
	
}
