package queryprocessor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import indexer.Indexer;
import indexer.Indexer.indexData;
import indexer.SortedVector_InvertedIndex;
import indexer.Stemmer;

public class QueryProcessor_Servlet extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String requestUrl = request.getRequestURI();
		String query = requestUrl.substring("/DynamicSearch/api/search/".length());
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
		response.setHeader("Access-Control-Allow-Headers",
				"Origin, X-Requested-With, Content-Type, Accept, X-Auth-Token, X-Csrf-Token, WWW-Authenticate, Authorization");
		response.setHeader("Access-Control-Allow-Credentials", "false");
		response.setHeader("Access-Control-Max-Age", "3600");
		String json = "[ \n";

		// get stop words
		java.util.List<String> stopWords;
		String path = (String) (System.getProperty("user.dir") + "/inventory/english_stopwords.txt");
		stopWords = (java.util.List<String>) Files.readAllLines(Paths.get(path));

		// read indexedDocs.txt file
		ArrayList<String> indexedDocs = new ArrayList<String>();

		BufferedReader in = new BufferedReader(
				new FileReader((String) (System.getProperty("user.dir") + "/inventory/indexedDocs.txt")));
		String url;
		while ((url = in.readLine()) != null)
			indexedDocs.add(url);
		in.close();

		// read invertedIndex.txt
		SortedVector_InvertedIndex mat1 = new SortedVector_InvertedIndex(Files
				.readString(Paths.get((String) (System.getProperty("user.dir") + "/inventory/InvertedIndex.txt"))));

		// ///// Dummy query to be replaced with interface response/////
		// //String query = "how to sell ads against content?";
		// /////////////////////
		//
		// stemming and stop words removal
		Stemmer s = new Stemmer();
		ArrayList<String> allWords = Stream.of((query.toLowerCase()).replaceAll("[^a-zA-Z0-9]", " ").split(" "))
				.collect(Collectors.toCollection(ArrayList<String>::new));
		allWords.removeAll(stopWords);
		allWords.removeAll(Arrays.asList("", null));
		for (int i = 0; i < allWords.size(); ++i) {
			for (int j = 0; j < allWords.get(i).length(); ++j)
				s.add(allWords.get(i).charAt(j));
			s.stem();
			allWords.set(i, s.toString());
		}

		/// Print all query tokens
		// for (int i = 0; i < allWords.size(); i++) {
		// System.out.println(allWords.get(i));
		// }

		// index of row containing search term
		int vectorIndex;

		// vector of index data for all docs containing search term
		Vector<indexData> indexDataVector = new Vector<indexData>();

		// index of doc containing search term
		int docIndex;

		// vector of unique doc indices
		Vector<Integer> uniqueDocIndex = new Vector<Integer>();

		// vector of unique urls
		Vector<String> uniqueURL = new Vector<String>();

		// Print query word, index of corresponding document(s) and their url(s)
		for (int j = 0; j < allWords.size(); j++) {

			System.out.println(j);

			String queryTerm = allWords.get(j);

			System.out.println(queryTerm);

			// index of row containing search term
			vectorIndex = mat1.getKeyIndex(queryTerm);

			if (vectorIndex != -1) {
				// vector of index data for all docs containing search term
				indexDataVector = mat1.get(vectorIndex).getValue();

				// vector of unique doc indices
				uniqueDocIndex = new Vector<Integer>();

				for (int i = 0; i < indexDataVector.size(); i++) {
					docIndex = indexDataVector.get(i).docIndex;
					if (!uniqueDocIndex.contains(docIndex)) {
						uniqueDocIndex.add(docIndex);
					}
				}

				for (int i = 0; i < uniqueDocIndex.size(); i++) {
					docIndex = uniqueDocIndex.get(i);
					System.out.println(docIndex);
				}

				for (int i = 0; i < uniqueDocIndex.size(); i++) {
					if (!uniqueURL.contains(indexedDocs.get(uniqueDocIndex.get(i)))) {
						uniqueURL.add(indexedDocs.get(uniqueDocIndex.get(i)));
						json = addJson(json, indexedDocs.get(uniqueDocIndex.get(i)), queryTerm);
					}

				}
			}
		}
		json += "]";
		response.getOutputStream().println(json);
	}

	static String addJson(String json, String newUrl, String query) {
		json += "   {\n";
		json += "       \"title\": " + "\"" + query + " " + "\"" + ",\n";
		json += "       \"description\": " + "\"Part of document from query processor " + " apt flutter \"" + ",\n";
		json += "       \"link\": " + "\"" + newUrl + "\"" + "\n";
		json += "   },\n";
		return json;
	}
}