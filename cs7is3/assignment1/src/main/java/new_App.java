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

import java.io.BufferedWriter;
import java.io.FileWriter;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.DirectoryReader;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;

import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import java.io.StringReader;
import org.apache.lucene.analysis.CharArraySet;

 
public class App
{
	// Directory where the search index will be saved
	private static String INDEX_DIRECTORY = "../assignment1/index";
	private static String RESULT_DIRECTORY = "../assignment1/result/";
	private static int MAX_RESULTS = 50;

	public static void main(String[] args) throws IOException
	{
		indexDocument();
		try {
                        queryIndex(); 
                } catch (ParseException e) {
                        e.printStackTrace();
                }
	}

	//public static void rewriteEval() throws IOException{}

	public static void indexDocument() throws IOException{
		// Analyzer that is used to process TextField
		Analyzer analyzer = new StandardAnalyzer();
		
		// ArrayList of documents in the corpus
		ArrayList<Document> documents = new ArrayList<Document>();

		// Open the directory that contains the search index
		Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

		// Set up an index writer to add process and save documents to the index
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		// BM25 Scoring
		config.setSimilarity(new BM25Similarity());
		// Vector Space Model (VSM) Scoring
		//config.setSimilarity(new ClassicSimilarity());
		IndexWriter iwriter = new IndexWriter(directory, config);

		System.out.print("Indexing Document\n");
		BufferedReader reader = new BufferedReader(new FileReader("../assignment1/cran/cran.all.1400"));
		String line;
		StringBuilder content = new StringBuilder();
		Document doc = null;
		boolean writeContent = false;
		while ((line = reader.readLine()) != null) {
			
			if (line.startsWith(".I")) {
				if (writeContent == true){
					//System.out.print("Before\n");
                                        //System.out.print(content.toString() + "\n");
                                        //System.out.print("After\n");
                                        String processed_content = content.toString();
					processed_content = processText(processed_content);
                                        //System.out.print(processed_content + "\n");
                                        doc.add(new TextField("content", processed_content, Field.Store.YES));
				}
				writeContent = false;
				//System.out.printf("Indexing \"%s\"\n", line);
				// Index current document and create a new one
				if (doc != null) {
					// Add the document to the index
					iwriter.addDocument(doc);
				}
				// Start a new document
				doc = new Document();
				content = new StringBuilder();
				// Extract the document id
				//System.out.printf("id is \"%s\"\n", line.substring(3).trim());
				doc.add(new StringField("id", line.substring(3).trim(), Field.Store.YES));
			} else if (line.startsWith(".T")) {
				// Read the title
				String title = reader.readLine();
				//System.out.printf("title is \"%s\"\n", title);
				doc.add(new TextField("title", title, Field.Store.YES));
			} else if (line.startsWith(".A")) {
				// Read the author
				String author = reader.readLine();
				//System.out.printf("author is \"%s\"\n", author);
				doc.add(new TextField("author", author, Field.Store.YES));
			} else if (line.startsWith(".W")) {
				// Read the content of the document
				//System.out.print("content is \n");
				writeContent = true;
				
		    } else if (writeContent == true && !line.startsWith(".")){
		    	content.append(line.trim()).append(" ");
		    }
		}
		// Add the last document to the index
		if (doc != null) {
			//System.out.print("Before\n");
                        //System.out.print(content.toString() + "\n");
                        //System.out.print("After\n");
                        String processed_content = content.toString();
			processed_content = processText(processed_content);
                        //System.out.print(processed_content + "\n");
                        doc.add(new TextField("content", processed_content, Field.Store.YES));
		}
		reader.close();
		iwriter.close();
		directory.close();
	}

	public static void queryIndex() throws IOException, ParseException{
		Analyzer analyzer = new StandardAnalyzer();
		// Open the folder that contains our search index
		Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
		DirectoryReader ireader = DirectoryReader.open(directory);
		// search the index
		IndexSearcher isearcher = new IndexSearcher(ireader);
		// query parser to parse the content
                QueryParser parser = new QueryParser("content", analyzer);

		// BM25 Scoring
		isearcher.setSimilarity(new BM25Similarity());
		// Vector Space Model (VSM) Scoring
		// isearcher.setSimilarity(new ClassicSimilarity());

                // write to result file
		BufferedWriter writer = new BufferedWriter(new FileWriter(RESULT_DIRECTORY + "result.txt"));
		String result = "";

                // read query
                BufferedReader reader = new BufferedReader(new FileReader("../assignment1/cran/cran.qry"));
		String line;
		String queryText = "";
		int queryID = 1;
		System.out.print("readinging query\n");

                line = reader.readLine();
		System.out.printf("Query: \"%s\" ID: %d \n", line, queryID);

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
                                queryText = processText(queryText);
				Query query = parser.parse(queryText);

				// Get the set of results
				ScoreDoc[] hits = isearcher.search(query, MAX_RESULTS).scoreDocs;

				// Print the results
				System.out.println("Documents: " + hits.length);
				for (int i = 0; i < hits.length; i++)
				{
					Document hitDoc = isearcher.doc(hits[i].doc);
					System.out.println(i + ") " + hitDoc.get("id") + " " + hits[i].score);

                                        result = String.valueOf(queryID) + " " + "Q0" + " " + hitDoc.get("id") + " " + String.valueOf(i+1) + " " + String.valueOf(hits[i].score) + " " + "STANDARD\n";
					writer.write(result);
				}

				System.out.println();	

				queryText = "";

				queryID++;
				System.out.printf("Query: \"%s\" ID: %d \n", line, queryID);

			}
		}
		
		// TODO: Check whether last query is processed?
		//queryText = queryText.trim();
		queryText = processText(queryText);
		Query query = parser.parse(queryText);
		// Get the set of results
		ScoreDoc[] hits = isearcher.search(query, MAX_RESULTS).scoreDocs;
		// Print the results
		System.out.println("Documents: " + hits.length);
		for (int i = 0; i < hits.length; i++)
		{
			Document hitDoc = isearcher.doc(hits[i].doc);
			System.out.println(i + ") " + hitDoc.get("id") + " " + hits[i].score);
                        result = String.valueOf(queryID) + " " + "Q0" + " " + hitDoc.get("id") + " " + String.valueOf(i+1) + " " + String.valueOf(hits[i].score) + " " + "STANDARD\n";
			writer.write(result);
		}

                writer.close();
		reader.close();
		directory.close();
	}

	public static String processText(String processed_content) throws IOException {
        Analyzer tokenAnalyzer = new StandardAnalyzer();
        TokenStream tokenStream = tokenAnalyzer.tokenStream("content", new StringReader(processed_content));
        // Convert to lowercase
        // stemming (Porter Stemmer)
        tokenStream = new PorterStemFilter(tokenStream);
        StringBuilder processedText = new StringBuilder();
        
        try {
            CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
        
            // Process tokens and append them to the result
            while (tokenStream.incrementToken()) {
                processedText.append(term.toString()).append(" ");
            }
            tokenStream.end();
        } finally {
            tokenStream.close();
        }
        return processedText.toString().trim();
    }

}