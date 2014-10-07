package edu.isi.bmkeg.vpdmf.bigdata;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.impl.BindingImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.rdfxml.RDFXMLWriter;

import com.bigdata.btree.IndexMetadata;
import com.bigdata.rdf.model.BigdataStatement;
import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSail.BigdataSailConnection;
import com.bigdata.rdf.sail.BigdataSailRepository;
import com.bigdata.rdf.sail.BigdataSailRepositoryConnection;
import com.bigdata.rdf.store.AbstractTripleStore;
import com.bigdata.rdf.store.BDS;

/**
 * Based on com.bigdata.samples.SampleCode.java.
 * 
 * @author mikep, gully@usc.edu
 */
public class BigDataBean {
    
    /**
     * Do you have log4j set up correctly?  Bigdata turns its logging level way
     * down by default (WARN).  You should not be seeing INFO or DEBUG log 
     * statements from bigdata - if you are, then this will severely impact
     * performance.
     */
    protected static final Logger log = Logger.getLogger(SampleCode.class);
    
    private BigdataSail sail;
    
    private Repository repo;

    private String namespace;
    
    private HashMap<String, Set<String>> namesIds = new HashMap();
    
    private HashMap<String, String> idsNames = new HashMap();

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
	public void setRepo(Repository repo) {
		this.repo = repo;
	}

	public Repository getRepo() {
		return repo;
	}
	
	public void setSail(BigdataSail sail) {
		this.sail = sail;
	}

	public BigdataSail getSail() {
		return sail;
	}
	
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getNamespace() {
		return namespace;
	}
	
	public void setNamesIds(HashMap<String, Set<String>> namesIds) {
		this.namesIds = namesIds;
	}

	public HashMap<String, Set<String>> getNamesIds() {
		return namesIds;
	}

	public void setIdsNames(HashMap<String, String> idsNames) {
		this.idsNames = idsNames;
	}

	public HashMap<String, String> getIdsNames() {
		return idsNames;
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	/**
	 * Sets up a BigData store for use within Spring. You can specify a single 
	 * file or a whole directory to load into the store (either set 'resource'
	 * to the path of the file or of the directory that you want to load). 
	 */
	public BigDataBean(String journalName, String resource, String baseURL) throws Exception {
		
		File res = new File(resource);		
		
		if(!res.exists()) {

			throw new Exception("Resource must be either a directory or an RDF-formatted file");
		
		} else {
		
			instantiateJournalFromRdfFile(journalName, resource, baseURL);
		
		}

	}
	    
    /**
     * Load a Properties object from a file.
     * 
     * @param resource
     * @return
     * @throws Exception
     */
    public Properties loadProperties(String resource) throws Exception {
        Properties p = new Properties();
        InputStream is = getClass().getResourceAsStream(resource);
        p.load(new InputStreamReader(new BufferedInputStream(is)));
        return p;
    }
    
    /**
     * Set up basic properties from the basic example shown in TestBaseball.java
     * @return
     */
    public Properties setUpBasicProperties(String journalFileName) {
    	
		Properties properties = new Properties();

		// Properties from the fullfeature.properties file. 
		properties.setProperty(
				AbstractTripleStore.Options.STATEMENT_IDENTIFIERS, 
				"true");
		
		// use one of our pre-configured option-sets or "modes"
        properties.setProperty(
                IndexMetadata.Options.WRITE_RETENTION_QUEUE_CAPACITY,
                "8000");

        // this option can be faster and make better use of disk if you have
        // enough ram and are doing large writes.
        properties.setProperty(
                IndexMetadata.Options.WRITE_RETENTION_QUEUE_CAPACITY,
                "8000");

        // when loading a large data file, it's sometimes better to do
        // database-at-once closure rather than incremental closure.  this
        // is how you do it.
        properties.setProperty(BigdataSail.Options.TRUTH_MAINTENANCE, "false");

        // we won't be doing any retraction, so no justifications either
        properties.setProperty(BigdataSail.Options.JUSTIFY, "false");

        // no free text search
        properties.setProperty(BigdataSail.Options.TEXT_INDEX, "false");

        // no statement identifiers
        properties.setProperty(BigdataSail.Options.STATEMENT_IDENTIFIERS,
                "false");

        // triples only.
        properties.setProperty(
                com.bigdata.rdf.store.AbstractTripleStore.Options.QUADS,
                "false");
				
        File journal = new File(journalFileName);
        System.out.println("journalFile="+journal.getAbsolutePath());
        
        // journal.deleteOnExit();
        properties.setProperty(BigdataSail.Options.FILE, journal
                .getAbsolutePath());
        
        return properties;
    	
    }
    
    public void instantiateJournalFromRdfFile(
    		String journalName, 
    		String resource, 
    		String baseURL) throws Exception {

        File journalFile = new File(journalName);
        boolean jnlExists = journalFile.exists();
        
        Properties properties = setUpBasicProperties(journalName);
		
        this.setSail( new BigdataSail(properties) );
        this.setRepo(new BigdataSailRepository(getSail()));
        this.getRepo().initialize();
        
        if(jnlExists) {
        	return;
        }
        
        long start = System.currentTimeMillis();        
        
		File res = new File(resource);		

        if( res.isDirectory() ) {
    		
			FileFilter filter = new FileFilter() {
			    public boolean accept(File file) {
			        return (file.getName().endsWith(".rdf") || file.getName().endsWith(".owl"));
			    }
			};
			File[] files = res.listFiles(filter);

			for(int i=0;i<files.length; i++) {
				
		        System.out.println("loading " + files[i].getName());
				loadDataFromDocument(repo, files[i].getPath(), baseURL);
				
			}
			
        } else {

            loadDataFromDocument(repo, resource, baseURL);
        	
        }
			
        long duration = System.currentTimeMillis() - start;

        long size = getSail().getDatabase().getStatementCount();

        System.out.println("loaded " + size + " stmts in " + duration + " millis.");
    
    }    
    
	public void loadDataFromDocument(Repository repo, String resource,
			String baseURL) throws Exception {

		RepositoryConnection cxn = repo.getConnection();
		cxn.setAutoCommit(false);
		try {
			InputStream is = new FileInputStream(resource);
			if (is == null)
				throw new Exception("Could not locate resource: " + resource);
			Reader reader = new InputStreamReader(new BufferedInputStream(is));
			cxn.add(reader, baseURL, RDFFormat.RDFXML);
			cxn.commit();
		} catch (Exception ex) {
			cxn.rollback();
			throw ex;
		} finally {
			// close the repository connection
			cxn.close();
		}

	}
    
    /**
     * Add a statement to a repository.
     * 
     * @param repo
     * @throws Exception
     */
    public void loadSomeData(Repository repo) throws Exception {
        RepositoryConnection cxn = repo.getConnection();
        cxn.setAutoCommit(false);
        try {
            Resource s = new URIImpl("http://www.bigdata.com/rdf#Mike");
            URI p = new URIImpl("http://www.bigdata.com/rdf#loves");
            Value o = new URIImpl("http://www.bigdata.com/rdf#RDF");
            Statement stmt = new StatementImpl(s, p, o);
            cxn.add(stmt);
            cxn.commit();
        } catch (Exception ex) {
            cxn.rollback();
            throw ex;
        } finally {
            // close the repository connection
            cxn.close();
        }
    }

    /**
     * Load a document into a repository.
     * 
     * @param repo
     * @param resource
     * @param baseURL
     * @throws Exception
     */
    public void loadSomeDataFromADocument(Repository repo, String resource, 
        String baseURL) throws Exception {
        
        RepositoryConnection cxn = repo.getConnection();
        cxn.setAutoCommit(false);
        try {
            InputStream is = getClass().getResourceAsStream(resource);
            if (is == null && new File(resource).exists())
                is = new FileInputStream(resource);
            if (is == null)
                throw new Exception("Could not locate resource: " + resource);
            Reader reader = new InputStreamReader(new BufferedInputStream(is));
            cxn.add(reader, baseURL, RDFFormat.RDFXML);
            cxn.commit();
        } catch (Exception ex) {
            cxn.rollback();
            throw ex;
        } finally {
            // close the repository connection
            cxn.close();
        }
        
    }
    
    /**
     * Read some statements from a repository.
     * 
     * @param repo
     * @param uri
     * @throws Exception
     */
    public void readSomeData(Repository repo, URI uri) throws Exception {
        
        /*
         * With MVCC, you read from a historical state to avoid blocking and
         * being blocked by writers.  BigdataSailRepository.getQueryConnection
         * gives you a view of the repository at the last commit point.
         */
        RepositoryConnection cxn;
        if (repo instanceof BigdataSailRepository) { 
            cxn = ((BigdataSailRepository) repo).getReadOnlyConnection();
        } else {
            cxn = repo.getConnection();
        }
        
        try {
            
            RepositoryResult<Statement> stmts = 
                cxn.getStatements(uri, null, null, true /* include inferred */);
            while (stmts.hasNext()) {
                Statement stmt = stmts.next();
                Resource s = stmt.getSubject();
                URI p = stmt.getPredicate();
                Value o = stmt.getObject();
                // do something with the statement
                log.info(stmt);
                
                // cast to BigdataStatement to get at additional information
                BigdataStatement bdStmt = (BigdataStatement) stmt;
                if (bdStmt.isExplicit()) {
                    // do one thing
                } else if (bdStmt.isInferred()) {
                    // do another thing
                } else { // bdStmt.isAxiom()
                    // do something else
                }
                log.info(bdStmt.getStatementType());
            }
            
        } finally {
            // close the repository connection
            cxn.close();
        }
        
    }
    
    /**
     * Execute a "select" query.
     * 
     * @param repo
     * @param query
     * @param ql
     * @throws Exception
     */
    public List<Map<String,String>> executeSelectQuery(Repository repo, String query, 
        QueryLanguage ql, String[] keys) throws Exception {
        
        /*
         * With MVCC, you read from a historical state to avoid blocking and
         * being blocked by writers.  BigdataSailRepository.getQueryConnection
         * gives you a view of the repository at the last commit point.
         */
        RepositoryConnection cxn;
        if (repo instanceof BigdataSailRepository) { 
            cxn = ((BigdataSailRepository) repo).getReadOnlyConnection();
        } else {
            cxn = repo.getConnection();
        }
          
        List<Map<String,String>> results = new ArrayList<Map<String,String>>();
        
        try {

            final TupleQuery tupleQuery = cxn.prepareTupleQuery(ql, query);
            tupleQuery.setIncludeInferred(true /* includeInferred */);
            TupleQueryResult result = tupleQuery.evaluate();
            // do something with the results
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                
                Map<String, String> map = new HashMap<String,String>();
                for(int i=0; i<keys.length; i++) {
                	map.put(keys[i], bindingSet.getValue(keys[i]).stringValue());
                }
                
                log.info(bindingSet);
                
                results.add(map);
                
            }
            
        } catch (Exception e) {

        	throw e;
            
        } finally {
            // close the repository connection
            cxn.close();
        }
        
        return results;
        
    }
    
    /**
	 * Count ids in namespace.
	 * @return
	 * @throws Exception
	 */
	public int getTopIdInNamespace(String namespace) throws Exception {

		int max = 0;		
		
		this.resetNamespaceHash(namespace);

        Pattern patt = Pattern.compile(namespace + ".*_(\\d+)");

		Iterator<String> idsIt = this.getIdsNames().keySet().iterator();
		while(idsIt.hasNext()) {
			String id = idsIt.next();

            Matcher mat = patt.matcher(id);
            
        	if(mat.find()) {
        		Integer number = new Integer(mat.group(1)); 
        		int i = number.intValue();
        		if(i>max)
        			max = i;        		
        	}

		}
		
		return max;
        	
	}
	
	public String lookupUniqueIdFromName(String ns, String n) throws Exception {
		Set<String> ids = this.lookupIdsFromName(ns, n);
		
		if(ids == null ) {
			throw new Exception( n + " not found in '" + ns + "'");
		} else if(ids.size() > 1) {
			throw new Exception("More than one node in '" + ns + "' has name '" + n + "'");
		}
		
		String id = ids.iterator().next();
		
		return id;
		
	}
	
	public Set<String> lookupIdsFromName(String ns, String n) throws Exception {
		
		if( this.namespace == null || !this.namespace.equals(ns) ) {
			this.resetNamespaceHash(ns);
		}

		Set<String> ids = this.namesIds.get(n);
	
		return ids;
		
	}

	public String lookupNameFromId(String ns, String id) throws Exception {
		
		if( this.namespace == null || !this.namespace.equals(ns) ) {
			this.resetNamespaceHash(ns);
		}

		String name = this.idsNames.get(id);
	
		return name;
		
	}
	
	public String queryUniqueIdFromName(String n) throws Exception {
		Set<String> ids = this.queryIdsFromName(n);

		if(ids == null ) {
			throw new Exception( n + " not found");
		} else if(ids.size() > 1) {
			throw new Exception("More than one node has name '" + n + "'");
		}
		
		String id = ids.iterator().next();
		
		return id;
		
	}
	
	public Set<String> queryIdsFromName(String name) throws Exception {
		
		String query = 
			"SELECT C\n"+
			"FROM \n"+ 
			"	{C} rdfs:label {L} \n" +
			"WHERE \n" +
			"	L = \"" + name + "\"@en\n" +
			"USING NAMESPACE \n" + 
			"	ro = <http://www.obofoundry.org/ro/ro.owl#>,\n"+
			"	kefed = <http://bmkeg.isi.edu/kefed/>";
		
		String[] keys = new String[] {"C"};

		Iterator<Map<String,String>> it = this.executeSelectQuery(this.getRepo(), query, QueryLanguage.SERQL, keys).iterator();
		Set<String> results = new HashSet(); 
		while(it.hasNext()) {
			Map<String,String> r = it.next();
			results.add(r.get("C"));
		}
		
		return results;
		
	}


	private void resetNamespaceHash(String namespace) throws Exception {

		this.namespace = namespace;
		
		this.idsNames = new HashMap<String,String>();
	    this.namesIds = new HashMap<String,Set<String>>();
	
		Repository repo = this.getRepo();

		RepositoryConnection cxn = repo.getConnection();
		
		String query = 
			"CONSTRUCT {C} rdfs:label {L} \n"+
			"FROM \n"+ 
			"	{C} rdfs:label {L} \n" +
			"WHERE \n" +
			"	C LIKE \"" + namespace + "*\"\n" +
			"USING NAMESPACE \n" + 
			"	ro = <http://www.obofoundry.org/ro/ro.owl#>,\n"+
			"	kefed = <http://bmkeg.isi.edu/kefed/>";
		
		long start = System.currentTimeMillis();
		
        GraphQuery graphQuery = cxn.prepareGraphQuery(QueryLanguage.SERQL, query);
		
        GraphQueryResult result = graphQuery.evaluate();

        long duration = System.currentTimeMillis() - start;

        int count = 0;
        
        try {
            while (result.hasNext()) {
                Statement st = result.next();
        		count++;
        		
                String id = st.getSubject().stringValue();
                String name = st.getObject().stringValue();
                
                this.idsNames.put(id, name);
                
                if( this.namesIds.containsKey(name) ) {
                
                	Set ids = this.namesIds.get(name);
                	ids.add(id);
                
                } else {
                	
                	HashSet<String> ids = new HashSet();
                	ids.add(id);
                	this.namesIds.put(name, ids);
                                   	
                }
            }

            System.out.println( count + " statements in " + duration + " ms" ) ;
                		
            
        } catch (Exception e) {
        	
        	throw e;
        	
        } finally {

        	result.close();
        
        }     	
                	
	}
	
	public List<Map<String,String>> listClassesInOWL() throws Exception {

		String sparql = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n"
			+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"
				+ "SELECT ?id ?name ?p ?pName\n" 
				+ "WHERE {\n"
				+ "	?id rdfs:label ?name . \n" 
				+ "	?id rdfs:subClassOf ?p . \n" 
				+ "	?p rdfs:label ?pName . \n" 
				+ "}\n";

		String[] keys = new String[] { "id", "name", "p", "pName" };

		long start = System.currentTimeMillis();

		List<Map<String,String>> results = this.executeSelectQuery(repo,
				sparql, QueryLanguage.SPARQL, keys);

		long duration = System.currentTimeMillis() - start;

		return results;

	}
	
	public List<Map<String,String>> listRelationsInOWL() throws Exception {

		String sparql = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n"
			+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"
				+ "SELECT ?id ?name ?p ?pName\n" 
				+ "WHERE {\n"
				+ "	?id rdfs:label ?name . \n" 
				+ "	?id owl:equivalentClass ?equiv . \n" 
				+ "	?equiv owl:equivalentClass ?equiv . \n" 
				+ "	_:equiv owl:Class _:class . \n" 
				+ "	_:equiv owl:Class _:class . \n" 
				+ "	?p rdfs:label ?pName . \n" 
				+ "}\n";

		String[] keys = new String[] { "id", "name", "p", "pName" };

		long start = System.currentTimeMillis();

		List<Map<String,String>> results = this.executeSelectQuery(repo,
				sparql, QueryLanguage.SPARQL, keys);

		long duration = System.currentTimeMillis() - start;

		return results;

	}
	
	
    /**
     * Execute a "construct" query.
     * 
     * @param repo
     * @param query
     * @param ql
     * @throws Exception
     */
    public void executeConstructQuery(Repository repo, String query, 
        QueryLanguage ql) throws Exception {
            	
        /*
         * With MVCC, you read from a historical state to avoid blocking and
         * being blocked by writers.  BigdataSailRepository.getQueryConnection
         * gives you a view of the repository at the last commit point.
         */
        RepositoryConnection cxn;
        if (repo instanceof BigdataSailRepository) { 
            cxn = ((BigdataSailRepository) repo).getReadOnlyConnection();
        } else {
            cxn = repo.getConnection();
        }
        
        try {

            // silly construct queries, can't guarantee distinct results
            final Set<Statement> results = new LinkedHashSet<Statement>();
            final GraphQuery graphQuery = cxn.prepareGraphQuery(ql, query);
            graphQuery.setIncludeInferred(true /* includeInferred */);
            graphQuery.evaluate(new StatementCollector(results));
            // do something with the results
            for (Statement stmt : results) {
            	
            	
                log.info(stmt);
            }

        } finally {
            // close the repository connection
            cxn.close();
        }
        
    }
    
    
    
    

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // EVERYTHING HERE ARE FROM PREVIOUS EXAMPLES


    /**
     * Demonstrate execution of a free-text query.
     * 
     * @param repo
     * @throws Exception
     */
    public void executeFreeTextQuery(Repository repo) throws Exception {
        
        RepositoryConnection cxn = repo.getConnection();
        cxn.setAutoCommit(false);
        try {
            cxn.add(new URIImpl("http://www.bigdata.com/A"), RDFS.LABEL,
                    new LiteralImpl("Yellow Rose"));
            cxn.add(new URIImpl("http://www.bigdata.com/B"), RDFS.LABEL,
                    new LiteralImpl("Red Rose"));
            cxn.add(new URIImpl("http://www.bigdata.com/C"), RDFS.LABEL,
                    new LiteralImpl("Old Yellow House"));
            cxn.add(new URIImpl("http://www.bigdata.com/D"), RDFS.LABEL,
                    new LiteralImpl("Loud Yell"));
            cxn.commit();
        } catch (Exception ex) {
            cxn.rollback();
            throw ex;
        } finally {
            // close the repository connection
            cxn.close();
        }
        
        String query = "select ?x where { ?x <"+BDS.SEARCH+"> \"Yell\" . }";
        //executeSelectQuery(repo, query, QueryLanguage.SPARQL);
        // will match A, C, and D
        
    }

    /**
     * Demonstrate execution of statement level provenance.
     * 
     * @param repo
     * @throws Exception
     */
    public void executeProvenanceQuery(Repository repo) throws Exception {
        
        RepositoryConnection cxn = repo.getConnection();
        cxn.setAutoCommit(false);
        try {
            cxn.remove((Resource)null, (URI)null, (Value)null);
            cxn.commit();
            
            cxn.add(getReader(getClass(), "provenance.rdf"), 
                "", RDFFormat.RDFXML);
            cxn.commit();
            
            RepositoryResult<Statement> results = 
                cxn.getStatements(null, null, null, false);
            while(results.hasNext()) {
                log.info(results.next());
            }
            
        } catch (Exception ex) {
            cxn.rollback();
            throw ex;
        } finally {
            // close the repository connection
            cxn.close();
        }

        /*
         * With MVCC, you read from a historical state to avoid blocking and
         * being blocked by writers.  BigdataSailRepository.getQueryConnection
         * gives you a view of the repository at the last commit point.
         */
        if (repo instanceof BigdataSailRepository) { 
            cxn = ((BigdataSailRepository) repo).getReadOnlyConnection();
        } else {
            cxn = repo.getConnection();
        }

        try {

            RepositoryResult<Statement> results = 
                cxn.getStatements(null, null, null, false);
            while(results.hasNext()) {
                log.info(results.next());
            }
            
        } finally {
            // close the repository connection
            cxn.close();
        }
        
        String NS = "http://www.bigdata.com/rdf#";
        String MIKE = NS + "Mike";
        String LOVES = NS + "loves";
        String RDF = NS + "RDF";
        String query = 
            "construct { ?sid ?p ?o } " +
            "where { " +
            "  ?sid ?p ?o ." +
            "  graph ?sid { <"+MIKE+"> <"+LOVES+"> <"+RDF+"> } " +
            "}";
        executeConstructQuery(repo, query, QueryLanguage.SPARQL);
        // should see the provenance information for { Mike loves RDF }
        
    }

    /**
     * Demonstrate execution of historical query using a read-only transaction.
     * 
     * @param repo
     * @throws Exception
     */
    public void executeHistoricalQuery(Repository repo) throws Exception {

        if (!(repo instanceof BigdataSailRepository)) {
            return;
        }
        
        URI MIKE = new URIImpl(BDS.NAMESPACE+"Mike");
        URI BRYAN = new URIImpl(BDS.NAMESPACE+"Bryan");
        URI PERSON = new URIImpl(BDS.NAMESPACE+"Person");
        
        RepositoryConnection cxn = repo.getConnection();
        cxn.setAutoCommit(false);
        try {
            cxn.remove((Resource)null, (URI)null, (Value)null);
            cxn.commit();
            
            cxn.add(MIKE, RDF.TYPE, PERSON);
            cxn.commit();

            long time = System.currentTimeMillis();
            
            Thread.sleep(1000);
            
            cxn.add(BRYAN, RDF.TYPE, PERSON);
            cxn.commit();
            
            RepositoryConnection history = 
                ((BigdataSailRepository) repo).getReadOnlyConnection(time);
            
            String query = 
                "select ?s " +
                "where { " +
                "  ?s <"+RDF.TYPE+"> <"+PERSON+"> " +
                "}";
            
            try {

                final TupleQuery tupleQuery = 
                    history.prepareTupleQuery(QueryLanguage.SPARQL, query);
                tupleQuery.setIncludeInferred(false /* includeInferred */);
                TupleQueryResult result = tupleQuery.evaluate();

                Collection<BindingSet> answer = new LinkedList<BindingSet>();
                answer.add(createBindingSet(
                        new BindingImpl("s", MIKE)));
                
                compare(result, answer);
                
            } finally {
                // close the repository connection
                history.close();
            }

            // should see only Mike, not Bryan
            
        } catch (Exception ex) {
            cxn.rollback();
            throw ex;
        } finally {
            // close the repository connection
            cxn.close();
        }

    }

    /**
     * Run a simple LUBM load and query benchmark.
     * 
     * @param lubmResource the ZIP file containing the LUBM data files
     * @param filter helps filter out non-data files in the ZIP file
     * @throws Exception
     */
    public void doLUBMTest(final String lubmResource) 
        throws Exception {
        
        /*
         We are going to use the "fast load" mode for this LUBM test.  In fast 
         mode, we lose certain features, like the full text index and statement 
         identifiers.  The database also does not do inference automatically, so
         we have to tell the inference engine explicitly when to compute 
         closure.  Also, there is no recording of justification chains for 
         inferences, so this mode is extremely bad for retraction.  If we were 
         to retract a statement, we would have to tell the inference engine to 
         remove all inferences and completely re-compute the closure for the 
         entire database!
         */
        final Properties properties = loadProperties("fastload.properties");
        
        if (properties.getProperty(com.bigdata.journal.Options.FILE) == null) {
            /*
             * Create a backing temporary file iff none was specified in the
             * properties file.
             */
            final File journal = File.createTempFile("bigdata", ".jnl");
            journal.deleteOnExit();
            properties.setProperty(BigdataSail.Options.FILE, journal
                    .getAbsolutePath());
        }
        
        // instantiate a sail
        BigdataSail sail = new BigdataSail(properties);
        Repository repo = new BigdataSailRepository(sail);
        repo.initialize();

        RepositoryConnection cxn = repo.getConnection();
        cxn.setAutoCommit(false);
        try {
            // fast range count!
            long stmtsBefore = sail.getDatabase().getStatementCount();
//            // full index scan!
//            long stmtsBefore = cxn.size();
            log.info("statements before: " + stmtsBefore);
            long start = System.currentTimeMillis();
            
            // first add the LUBM ontology
            cxn.add(getReader(getClass(), "univ-bench.owl"), LUBM.NS,
                    RDFFormat.RDFXML);
            
            // then process the LUBM sample data files one at a time
            InputStream is = getClass().getResourceAsStream(lubmResource);
            ZipInputStream zis = 
                new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze = null;
            while ((ze = zis.getNextEntry()) != null) {
                if (ze.isDirectory()) {
                    continue;
                }
                String name = ze.getName();
                log.info(name);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] bytes = new byte[4096];
                int count;
                while ((count = zis.read(bytes, 0, 4096)) != -1) {
                    baos.write(bytes, 0, count);
                }
                baos.close();
                Reader reader = new InputStreamReader(
                    new ByteArrayInputStream(baos.toByteArray())
                    );
                cxn.add(reader, LUBM.NS, RDFFormat.RDFXML);
            }
            zis.close();
            
            // autocommit is false, we need to commit our SAIL "transaction"
            cxn.commit();
            
            // when we are in "fast load" mode there is no automatic inference
            // as statements are loaded.  we therefore must invoke the inference
            // engine ourselves when we are done loading data.
            BigdataSailConnection sailCxn = (BigdataSailConnection)
                ((BigdataSailRepositoryConnection) cxn).getSailConnection();
            sailCxn.computeClosure();
            sailCxn.getTripleStore().commit();

            // gather statistics
            long elapsed = System.currentTimeMillis() - start;
            // fast range count!
            long stmtsAfter = ((BigdataSailRepository)repo).getDatabase().getStatementCount();
//            // full index scan!
//            long stmtsAfter = cxn.size();
            long stmtsAdded = stmtsAfter - stmtsBefore;
            int throughput =
                    (int) ((double) stmtsAdded / (double) elapsed * 1000d);
            log.info("statements after: " + stmtsAfter);
            log.info("loaded: " + stmtsAdded + " in " + elapsed
                    + " millis: " + throughput + " stmts/sec");
            
            // ok, now let's do one of the LUBM queries
            /*
            [query4]
            PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>
            SELECT ?x ?y1 ?y2 ?y3
            WHERE{
                ?x a ub:Professor;
                    ub:worksFor <http://www.Department0.University0.edu>;
                    ub:name ?y1;
                    ub:emailAddress ?y2;
                    ub:telephone ?y3.
            }
            */
            // build LUBM query 4 using the handy SparqlBuilder utility
            // note: SparqlBuilder is for "construct" queries only
            // but you could modify it easily to do "select" instead
            SparqlBuilder sb = new SparqlBuilder();
            sb.addTriplePattern("?x", RDF.TYPE, LUBM.PROFESSOR);
            sb.addTriplePattern("?x", LUBM.WORKS_FOR, 
                    new URIImpl("http://www.Department0.University0.edu"));
            sb.addTriplePattern("?x", LUBM.NAME, "?y1");
            sb.addTriplePattern("?x", LUBM.EMAIL_ADDRESS, "?y2");
            sb.addTriplePattern("?x", LUBM.TELEPHONE, "?y3");

            log.info("evaluating LUBM query 4...");
            start = System.currentTimeMillis();
            
            final GraphQuery graphQuery = 
                cxn.prepareGraphQuery(QueryLanguage.SPARQL, sb.toString());
            final StringWriter sw = new StringWriter();
            graphQuery.evaluate(new RDFXMLWriter(sw));
            
            elapsed = System.currentTimeMillis() - start;
            log.info("done. evaluted in " + elapsed + " millis");
            
            // if you want to see the output, here it is:
            log.info(sw.toString());
                        
        } catch (Exception ex) {
            cxn.rollback();
            throw ex;
        } finally {
            // close the repository connection
            cxn.close();
        }
                    
    }
    
    public void doU10() throws Exception {
        doLUBMTest("U10.zip");
    }

    public void doU1() throws Exception {
        doLUBMTest("U1.zip");
    }

    public Reader getReader(Class c, String resource) {
        InputStream is = c.getResourceAsStream(resource);
        return new InputStreamReader(new BufferedInputStream(is));
    }
    
    /**
     * Are you running with the -server JVM option?  You should be.  Also, try
     * increasing heap size with -Xmx512m or even -Xmx1024m if you have enough
     * RAM.
     * 
     * @param args
     */
    public static void main(final String[] args) {
        // use one of our pre-configured option-sets or "modes"
        final String propertiesFile = "fullfeature.properties";
        // final String propertiesFile = "rdfonly.properties";
        // final String propertiesFile = "fastload.properties";
        // final String propertiesFile = "quads.properties";
        try {
            SampleCode sampleCode = new SampleCode();

            log.info("Reading properties from file: " + propertiesFile);

            final Properties properties = sampleCode.loadProperties(propertiesFile);

            if (properties.getProperty(com.bigdata.journal.Options.FILE) == null) {
                /*
                 * Create a backing file iff none was specified in the
                 * properties file.
                 */
                final File journal = File.createTempFile("bigdata", ".jnl");
                log.info(journal.getAbsolutePath());
                // journal.deleteOnExit();
                properties.setProperty(BigdataSail.Options.FILE, journal
                        .getAbsolutePath());
            }
            
            // instantiate a sail
            BigdataSail sail = new BigdataSail(properties);
            Repository repo = new BigdataSailRepository(sail);
            repo.initialize();
            
            // demonstrate some basic functionality
            URI MIKE = new URIImpl("http://www.bigdata.com/rdf#Mike");
            sampleCode.loadSomeData(repo);
            System.out.println("Loaded sample data.");
            sampleCode.readSomeData(repo, MIKE);
            sampleCode.executeSelectQuery(repo, "select ?p ?o where { <"+MIKE.toString()+"> ?p ?o . }", QueryLanguage.SPARQL);
            System.out.println("Did SELECT query.");
            sampleCode.executeConstructQuery(repo, "construct { <"+MIKE.toString()+"> ?p ?o . } where { <"+MIKE.toString()+"> ?p ?o . }", QueryLanguage.SPARQL);
            System.out.println("Did CONSTRUCT query.");
            sampleCode.executeFreeTextQuery(repo);
            System.out.println("Did free text query.");
            sampleCode.executeProvenanceQuery(repo);
            System.out.println("Did provenance query.");
            sampleCode.executeHistoricalQuery(repo);
            System.out.println("Did historical query.");
            
            System.out.println("done.");
            
            repo.shutDown();
            
            // run one of the LUBM tests
            //sampleCode.doU10(); // I see loaded: 1752215 in 116563 millis: 15032 stmts/sec, what do you see?
            //sampleCode.doU1();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    protected BindingSet createBindingSet(final Binding... bindings) {
        final QueryBindingSet bindingSet = new QueryBindingSet();
        if (bindings != null) {
            for (Binding b : bindings) {
                bindingSet.addBinding(b);
            }
        }
        return bindingSet;
    }
    
    protected void compare(final TupleQueryResult result,
            final Collection<BindingSet> answer)
            throws QueryEvaluationException {

        try {
            
            final Collection<BindingSet> extraResults = new LinkedList<BindingSet>();
            Collection<BindingSet> missingResults = new LinkedList<BindingSet>();
    
            int resultCount = 0;
            int nmatched = 0;
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                resultCount++;
                boolean match = false;
                if(log.isInfoEnabled())
                    log.info(bindingSet);
                Iterator<BindingSet> it = answer.iterator();
                while (it.hasNext()) {
                    if (it.next().equals(bindingSet)) {
                        it.remove();
                        match = true;
                        nmatched++;
                        break;
                    }
                }
                if (match == false) {
                    extraResults.add(bindingSet);
                }
            }
            missingResults = answer;
    
            for (BindingSet bs : extraResults) {
                if (log.isInfoEnabled()) {
                    log.info("extra result: " + bs);
                }
            }
            
            for (BindingSet bs : missingResults) {
                if (log.isInfoEnabled()) {
                    log.info("missing result: " + bs);
                }
            }
            
            if (!extraResults.isEmpty() || !missingResults.isEmpty()) {
                throw new RuntimeException("matchedResults=" + nmatched + ", extraResults="
                        + extraResults.size() + ", missingResults="
                        + missingResults.size());
            }

        } finally {
            
            result.close();
            
        }
        
    }

}
