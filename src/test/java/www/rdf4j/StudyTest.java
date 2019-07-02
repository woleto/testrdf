package www.rdf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;
import org.eclipse.rdf4j.repository.*;
public class StudyTest {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws RDFParseException, UnsupportedRDFormatException, IOException {
		/*
		 * 创建本地数据库 
		File dataDir = new File("C:\\Users\\Administrator\\Desktop\\nativeDB"); 
		SailRepository db = new SailRepository(new NativeStore(dataDir)); 
		db.initialize();
		*/
		 
		/* 内存数据库 
		Repository db = new SailRepository(new MemoryStore());
		db.initialize();
		*/
		
		/* 内存可以存到本地的数据库 */
		File dataDir = new File("C:\\Users\\Administrator\\Desktop\\memoryDB\\"); 
		MemoryStore memoryStore = new MemoryStore(dataDir);
		memoryStore.setSyncDelay(1000L);
		Repository db = new SailRepository(memoryStore);
		db.initialize();
		
		
		try (RepositoryConnection conn = db.getConnection()) {
			ValueFactory f = db.getValueFactory();

			// create some resources and literals to make statements out of
			IRI alice = f.createIRI("http://supermap.ai.kg/geo/market/id#market004");
			RepositoryResult<Statement> statements = conn.getStatements(alice ,null,null, true);
			try {
			   while (statements.hasNext()) {
			      Statement st = statements.next();
			     System.out.println(st);
			   }
			}
			finally {
			   statements.close(); // make sure the result object is closed properly
			}
			

			String filename = "geo_example.ttl";

			// read the file 'example-data-artists.ttl' as an InputStream.
			InputStream input = new FileInputStream("C:\\Users\\Administrator\\Desktop\\知识图谱相关\\" + filename);

			Model model = Rio.parse(input, "", RDFFormat.TURTLE);
			// OutputStream fps=new
			// FileOutputStream("C:\\Users\\Administrator\\Desktop\\2.ttl",true);
			// Rio.write(model, fps, RDFFormat.NTRIPLES);
			// add the RDF data from the inputstream directly to our database
			// conn.add(input, "", RDFFormat.TURTLE );
			
			conn.add(model);

			// let's check that our data is actually in the database
			try (RepositoryResult<Statement> result = conn.getStatements(null, null, null);) {
				while (result.hasNext()) {
					Statement st = result.next();
					System.out.println("db contains: " + st);
				}
			}

			// We do a simple SPARQL SELECT-query that retrieves all resources
			// of
			// type `ex:Artist`, and their first names.
			String queryString = "PREFIX rd:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
			queryString += "PREFIX :<http://supermap.ai.kg/geo/> \n";
			queryString += "PREFIX CellId:<http://supermap.ai.kg/geo/cell/id#> \n";
			queryString += "PREFIX SchoolId:<http://supermap.ai.kg/geo/school/id#> \n";
			queryString += "PREFIX MarketId:<http://supermap.ai.kg/geo/market/id#> \n";
//			queryString += "SELECT ?s ?n?o \n";
//			queryString += "WHERE { ?s ?n ?o.\n";
//			queryString += "}";
			 queryString += "SELECT ?o \n";
			 queryString += "WHERE { \n";
			 queryString += " SchoolId:school001 :lifeCircle ?o. \n";
			 queryString += "}";
			TupleQuery query = conn.prepareTupleQuery(queryString);

			// A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
			try (TupleQueryResult result = query.evaluate()) {
				// we just iterate over all solutions in the result...
				while (result.hasNext()) {
					BindingSet solution = result.next();
					// ... and print out the value of the variable bindings for
					// ?s and ?n
//					System.out.println("?s = " + solution.getValue("s"));
//					System.out.println("?n = " + solution.getValue("n"));
					System.out.println("?o = " + solution.getValue("o"));

				}
			}

		} catch (Exception e) {
			System.out.println("---------------------" + e.getMessage());
		} finally {
			// before our program exits, make sure the database is properly shut
			// down.
			db.shutDown();
		}

	}

}
