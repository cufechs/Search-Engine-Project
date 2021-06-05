package indexer;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.util.Pair;


public class Indexer implements Runnable{
	
	
    public static class indexData {
        public int docIndex;
        public String stringType;
        public int indexOfType, indexInString;
        
        indexData(int docIndex, String stringType, int indexOfType ,int indexInString){
        	this.docIndex = docIndex;
        	this.stringType = stringType;
        	this.indexOfType = indexOfType;
        	this.indexInString = indexInString;
        }
        
        public String getData() {
        	return ("{" + docIndex + ", " + stringType + ", " + indexOfType + ", " + indexInString + "}");
        }
        
        @Override
        public String toString() {
        	return  docIndex + "," + stringType + "," + indexOfType + "," + indexInString ;
        }
    }
    
    @SuppressWarnings("serial")
	public static class TFdata implements Serializable{
        public int docIndex;
        public double TF;
        
        TFdata(int docIndex, double TF){
        	this.docIndex = docIndex;
        	this.TF = TF;
        }
        
        public String getData() {
        	return ("{" + docIndex + ", " + TF + "}");
        }
        
        @Override
        public String toString() {
        	return  docIndex + "," + TF;
        }
    }
	
    public static java.util.List<String> stopWords;
	public static SortedVector_InvertedIndex invertedIndex = new SortedVector_InvertedIndex();
	public static SortedVector_IDFandTF TF_IDFmatrix = new SortedVector_IDFandTF();
	public static HashMap<Pair<String,Integer>, Integer> map = new HashMap<>();
	

	private ArrayList<String> str;
	
	private indexData indData;

	
	Indexer(int docIndex, String StrType, int StrTypeIndex, ArrayList<String> str){
		
		indData = new indexData(docIndex, StrType, StrTypeIndex, 0);
		this.str = str;
	}
	
	public static void init() {
		String path = (String)(System.getProperty("user.dir") + "\\inventory\\english_stopwords.txt");
		try {
			stopWords = (java.util.List<String>) Files.readAllLines(Paths.get(path));
		}
		catch(IOException IOe){
			System.out.println("error reading " + path);
		}
	}
	
	public static ArrayList<String> removeStopwordsAndStem(String doc) {
		
		Stemmer s = new Stemmer();
		
		ArrayList<String> allWords = 
			      Stream.of((doc.toLowerCase()).replaceAll("[^a-zA-Z0-9]", " ").split(" "))
			            .collect(Collectors.toCollection(ArrayList<String>::new));
		
	    allWords.removeAll(stopWords);
	    allWords.removeAll(Arrays.asList("", null));
	    
	    for(int i=0; i<allWords.size(); ++i) {
	    	for(int j=0; j<allWords.get(i).length(); ++j)
	    		s.add(allWords.get(i).charAt(j));
	    	
	    	s.stem();
	    	
	    	allWords.set(i, s.toString());
	    }
		
		return allWords;
	}
	
	public void run() {
		
		for(int i=0; i<str.size(); ++i) {
	
			Pair<String,Integer> pr = new Pair<String,Integer>(str.get(i), indData.docIndex);
			
			synchronized(map) {
				if (map.containsKey(pr))
					map.put(pr,map.get(pr) + 1);
				else
					map.put(pr,1);
			}
			
			
			synchronized(invertedIndex) {
				invertedIndex.addingElement(
						str.get(i), new indexData(indData.docIndex,indData.stringType, indData.indexOfType, i)
						);
			}	
		}
	}

}