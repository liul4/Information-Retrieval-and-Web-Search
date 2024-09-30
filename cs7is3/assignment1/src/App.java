package liul4;

import java.io.IOException;

import java.util.ArrayList;

import javax.management.Query;

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
 
public class App
{
	// Directory where the search index will be saved
	private static String INDEX_DIRECTORY = "../assignment1/index";
	private static int MAX_RESULTS = 50;

	public static void main(String[] args) throws IOException
	{
		
		createIandex();
	}

	public static void createIndex() throws IOException{
		// Analyzer that is used to process TextField
		Analyzer analyzer = new StandardAnalyzer();
		
		// ArrayList of documents in the corpus
		ArrayList<Document> documents = new ArrayList<Document>();

		// Open the directory that contains the search index
		Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

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

	public static void queryIndex() throws IOException{
		// Open the folder that contains our search index
		Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
		
		// create objects to read and search across the index
		DirectoryReader ireader = DirectoryReader.open(directory);
		IndexSearcher isearcher = new IndexSearcher(ireader);

		// builder class for creating our query
		BooleanQuery.Builder query = new BooleanQuery.Builder();

		addQuery(iwriter, "../assignment1/cran/cran.qry");
		// Some words that we want to find and the field in which we expect
		// to find them
		Query term1 = new TermQuery(new Term("content", "raven"));
		Query term2 = new TermQuery(new Term("content", "lenore"));
		Query term3 = new TermQuery(new Term("content", "criticism"));

		// construct our query using basic boolean operations.
		query.add(new BooleanClause(term1, BooleanClause.Occur.SHOULD	));   // AND
		query.add(new BooleanClause(term2, BooleanClause.Occur.MUST));     // OR
		query.add(new BooleanClause(term3, BooleanClause.Occur.MUST_NOT)); // NOT

		// Get the set of results from the searcher
		ScoreDoc[] hits = isearcher.search(query.build(), MAX_RESULTS).scoreDocs;
		
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

	public static void addQuery() throws IOException{}

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
		}
		// Add the last document to the index
		if (doc != null) {
			System.out.print(content.toString() + "\n");
			doc.add(new TextField("content", content.toString(), Field.Store.YES));
			iwriter.addDocument(doc);
		}
		reader.close();
	
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
