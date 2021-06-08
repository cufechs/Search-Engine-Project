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
import indexer.Indexer.indexData;
import indexer.SortedVector_InvertedIndex;
import indexer.Stemmer;

public class QueryProcessor_Main {

	public static void main(String[] args) throws IOException {

		// get stop words
		java.util.List<String> stopWords;
		String path = (String) (System.getProperty("user.dir") + "\\inventory\\english_stopwords.txt");
		stopWords = (java.util.List<String>) Files.readAllLines(Paths.get(path));

		// read indexedDocs.txt file
		ArrayList<String> indexedDocs = new ArrayList<String>();

		BufferedReader in = new BufferedReader(
				new FileReader((String) (System.getProperty("user.dir") + "\\inventory\\indexedDocs.txt")));
		String url;
		while ((url = in.readLine()) != null)
			indexedDocs.add(url);
		in.close();

		// read invertedIndex.txt
		SortedVector_InvertedIndex mat1 = new SortedVector_InvertedIndex(Files
				.readString(Paths.get((String) (System.getProperty("user.dir") + "\\inventory\\InvertedIndex.txt"))));

		///// Dummy query to be replaced with interface response/////
		String query = "why not replace me with a more relevant query?";
		/////////////////////////////////////////////////////////////

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

		///// Print all query tokens
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

			String queryTerm = allWords.get(j);

			//System.out.println(queryTerm);

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
				}

				for (int i = 0; i < uniqueDocIndex.size(); i++) {

					if (!uniqueURL.contains(indexedDocs.get(uniqueDocIndex.get(i)))) {
						uniqueURL.add(indexedDocs.get(uniqueDocIndex.get(i)));
						System.out.print(indexedDocs.get(uniqueDocIndex.get(i)) + "\n");
					}
				}
			}
		}
	}
}
