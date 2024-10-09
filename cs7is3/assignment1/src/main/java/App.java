package liul4;

import java.io.IOException;

import java.util.ArrayList;

import java.nio.file.Paths;
import java.nio.file.Files;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.FileReader;


import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;



import org.apache.lucene.index.Term;
import org.apache.lucene.index.DirectoryReader;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;


 
public class App
{
	// Directory where the search index will be saved
	private static String INDEX_DIRECTORY = "../assignment1/index";
	private static String RESULT_DIRECTORY = "../assignment1/result";
	private static int MAX_RESULTS = 5;

	public static void main(String[] args) throws IOException
	{
		
		indexDocument();
		buildQuery();
	}

	public static void indexDocument() throws IOException{
		// Analyzer that is used to process TextField
		Analyzer analyzer = new StandardAnalyzer();
		
		// ArrayList of documents in the corpus
		ArrayList<Document> documents = new ArrayList<Document>();

		// Open the directory that contains the search index
		Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY_DOC));

		// Set up an index writer to add process and save documents to the index
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		IndexWriter iwriter = new IndexWriter(directory, config);
		
		
        addDocument(iwriter, "../assignment1/cran/cran.test.all.1400");
		//preprocessing(iwriter, "../assignment1/cran/cran.qry");

		// Commit everything and close
		iwriter.close();
		directory.close();
	}

	public static void addDocument(IndexWriter iwriter, String filePath) throws IOException {
		System.out.print("Indexing Document\n");
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		String line;
		StringBuilder content = new StringBuilder();
		Document doc = null;
		boolean writeContent = false;
		while ((line = reader.readLine()) != null) {
			
			if (line.startsWith(".I")) {
				if (writeContent == true){
					System.out.print(content.toString() + "\n");
			        doc.add(new TextField("content", content.toString(), Field.Store.YES));
				}
				writeContent = false;
				System.out.printf("Indexing \"%s\"\n", line);
				// Index current document and create a new one
				if (doc != null) {
					// Add the document to the index
					iwriter.addDocument(doc);
				}
				// Start a new document
				doc = new Document();
				content = new StringBuilder();
				// Extract the document id
				System.out.printf("id is \"%s\"\n", line.substring(3).trim());
				doc.add(new StringField("id", line.substring(3).trim(), Field.Store.YES));
			} else if (line.startsWith(".T")) {
				// Read the title
				String title = reader.readLine();
				System.out.printf("title is \"%s\"\n", title);
				doc.add(new TextField("title", title, Field.Store.YES));
			} else if (line.startsWith(".A")) {
				// Read the author
				String author = reader.readLine();
				System.out.printf("author is \"%s\"\n", author);
				doc.add(new TextField("author", author, Field.Store.YES));
			} else if (line.startsWith(".W")) {
				// Read the content of the document
				System.out.print("content is \n");
				writeContent = true;
				
		    } else if (writeContent == true && !line.startsWith(".")){
		    	content.append(line).append(" ");
		    }
			//else if (writeContent == true && line.startsWith(".")){
		    //	writeContent = false;
		    //}
		}
		// Add the last document to the index
		if (doc != null) {
			System.out.print(content.toString() + "\n");
			doc.add(new TextField("content", content.toString(), Field.Store.YES));
			iwriter.addDocument(doc);
		}
		reader.close();
	
	}

	public static void query() throws IOException{
		// Open the folder that contains our search index
		Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY_DOC));
		
		// create objects to read and search across the index
		DirectoryReader ireader = DirectoryReader.open(directory);
		IndexSearcher isearcher = new IndexSearcher(ireader);

		// builder class for creating our query
		//BooleanQuery.Builder query = new BooleanQuery.Builder();
		Directory queryDirectory = FSDirectory.open(Paths.get(INDEX_DIRECTORY_QRY));
        DirectoryReader queryReader = DirectoryReader.open(queryDirectory);
        IndexSearcher querySearcher = new IndexSearcher(queryReader);

		// Set up analyzer and query parser
        Analyzer analyzer = new StandardAnalyzer();
        QueryParser parser = new QueryParser("query", analyzer);
		
        // Open the results file for writing
        BufferedWriter resultWriter = new BufferedWriter(new FileWriter(RESULT_DIRECTORY));
		

		// Get the set of results from the searcher
		//ScoreDoc[] hits = isearcher.search(query.build(), MAX_RESULTS).scoreDocs;
		
		// Print the results
		System.out.println("Documents: " + hits.length);
		for (int i = 0; i < hits.length; i++)
		{
			Document hitDoc = isearcher.doc(hits[i].doc);
			System.out.println(i + ") " + hitDoc.get("filename") + " " + hits[i].score);
		}

		// close everything we used
		ireader.close();
		directory.close();
	}

	public static void queryIndex() throws IOException{
		Analyzer analyzer = new StandardAnalyzer();
		// Open the folder that contains our search index
		Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
		DirectoryReader ireader = DirectoryReader.open(directory);
		// search the index
		IndexSearcher isearcher = new IndexSearcher(ireader);
		// query parser to parse the content
        QueryParser parser = new QueryParser("content", analyzer);

		// Set up an index writer to add process and save documents to the index
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);	

		//addQuery(iwriter, "../assignment1/cran/cran.qry");
		//addQuery(iwriter, "../assignment1/cran/cran.test.qry");

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
		String line;
		String queryText = "";
		System.out.print("readinging query\n");

        line = reader.readLine();
		System.out.printf("Query ID : \"%s\"\n", line);

		while ((line = reader.readLine()) != null) {
			if (line.startsWith(".W")) {
				System.out.printf("process query");
				//StringBuilder queryString = new StringBuilder();
		    } else if (!line.startsWith(".")){
		    	//queryString.append(line).append(" ");
				queryText += " " + line;
		    } else if (line.startsWith(".I")){
				
				// TODO
				// process query
				// queryText = queryString.trim();
				queryText = queryText.trim();
				Query query = parser.parse(queryText);

				// Get the set of results
				ScoreDoc[] hits = isearcher.search(query, MAX_RESULTS).scoreDocs;

				// Print the results
				System.out.println("Documents: " + hits.length);
				for (int i = 0; i < hits.length; i++)
				{
					Document hitDoc = isearcher.doc(hits[i].doc);
					System.out.println(i + ") " + hitDoc.get("filename") + " " + hits[i].score);
				}

				System.out.println();	

				queryText = ""

				System.out.printf("Query ID : \"%s\"\n", line);

			}
		}
		// TODO: Check whether last query is processed?
		queryText = queryText.trim();
		Query query = parser.parse(queryText);
		// Get the set of results
		ScoreDoc[] hits = isearcher.search(query, MAX_RESULTS).scoreDocs;
		// Print the results
		System.out.println("Documents: " + hits.length);
		for (int i = 0; i < hits.length; i++)
		{
			Document hitDoc = isearcher.doc(hits[i].doc);
			System.out.println(i + ") " + hitDoc.get("filename") + " " + hits[i].score);
		}

		reader.close();
	
	
		// Some words that we want to find and the field in which we expect
		// to find them
		

		// Commit everything and close
		iwriter.close();
		directory.close();
	}

	public static void addQuery(IndexWriter iwriter, String filePath) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		String line;
		StringBuilder content = new StringBuilder();
		Document doc = null;
		boolean writeContent = false;
		System.out.print("Indexing query\n");
		while ((line = reader.readLine()) != null) {
			  
			if (line.startsWith(".I")) {
				if (writeContent == true){
					System.out.print(content.toString() + "\n");
			        doc.add(new TextField("content", content.toString(), Field.Store.YES));
				}
				writeContent = false;
				System.out.printf("Indexing \"%s\"\n", line);
				// Index current query and create a new one
				if (doc != null) {
					// Add the query to the index
					iwriter.addDocument(doc);
				}
				// Start a new query
				doc = new Document();
				content = new StringBuilder();
				// Extract the document id
				System.out.printf("id is \"%s\"\n", line.substring(3).trim());
				doc.add(new StringField("id", line.substring(3).trim(), Field.Store.YES));
			} else if (line.startsWith(".W")) {
				// Read the content of the document
				System.out.print("content is \n");
				writeContent = true;
				
		    } else if (writeContent == true && !line.startsWith(".")){
		    	content.append(line).append(" ");
		    }
		}
		// Add the last document to the index
		if (doc != null) {
			System.out.print(content.toString() + "\n");
			doc.add(new TextField("content", content.toString(), Field.Store.YES));
			iwriter.addDocument(doc);
		}
		reader.close();
	
	}
}

    // not correct, only index document
	/*
	public static void indexQuery() throws IOException{
		Analyzer analyzer = new StandardAnalyzer();
		ArrayList<Document> documents = new ArrayList<Document>();
		// Open the folder that contains our search index
		Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY_QRY));

		// Set up an index writer to add process and save documents to the index
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		IndexWriter iwriter = new IndexWriter(directory, config);
		

		//addQuery(iwriter, "../assignment1/cran/cran.qry");
		addQuery(iwriter, "../assignment1/cran/cran.test.qry");
		// Some words that we want to find and the field in which we expect
		// to find them
		

		// Commit everything and close
		iwriter.close();
		directory.close();
	}
*/