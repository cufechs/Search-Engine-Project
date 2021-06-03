package indexer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.util.Pair;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import indexer.Indexer.TFdata;
import indexer.Indexer.indexData;
import indexer.SortedVector_IDFandTF.nameThr;

public class Indexer_Main {

	static int failedURLS = 0;
	static int[] docSize;
	static int docNum;
	static int wordsNum;
	static String content;
	
	
	public static void main(String[] args) throws IOException {

		ArrayList<Thread> threads = new ArrayList<Thread>();
		Indexer.init();
		
		StringBuilder contentBuilder = new StringBuilder();
		try {
		    BufferedReader in = new BufferedReader(new FileReader((String)(System.getProperty("user.dir") + "\\inventory\\ht.html")));
		    String str;
		    while ((str = in.readLine()) != null) {
		        contentBuilder.append(str);
		    }
		    in.close();
		} catch (IOException e) {}
			
		String html_str = contentBuilder.toString();
		
		Document docu = Jsoup.parse(html_str);
		ArrayList<String> URL = new ArrayList<String>();
        try {
		    BufferedReader in = new BufferedReader(new FileReader((String)(System.getProperty("user.dir") + "\\inventory\\hyperlinks.txt")));
		    String url;
		    while ((url = in.readLine()) != null) {
		    	URL.add(url);
		    }
		    in.close();
		} catch (IOException e) {
			System.out.println("Can not read hyberlinks file");
			return;
		}
        
        /*
        java.util.List<String> stopWords = null;
        String path = (String)(System.getProperty("user.dir") + "\\src\\inventory\\english_stopwords.txt");
		try {
			stopWords = (java.util.List<String>) Files.readAllLines(Paths.get(path));
		}
		catch(IOException IOe){
			System.out.println("error reading " + path);
		}
        
        String str= "This#string\" %هاي^special*characters&.";   
        str = str.replaceAll("[^a-zA-Z0-9]", " ");  
        System.out.println(str);  
        ArrayList<String> allWo = (Stream.of(str.split(" ")).collect(Collectors.toCollection(ArrayList<String>::new)));
        allWo.removeAll(stopWords);
        allWo.removeAll(Arrays.asList("", null));

        for(int i=0; i<allWo.size(); ++i) {
        	System.out.println(allWo.get(i));  
        }
        */
        
        docNum = URL.size();//URL.size();
        docSize = new int[docNum];
        
        long time = (long) System.currentTimeMillis();
        
    	for(int i=0; i<docNum;++i) {
    		//doc = Jsoup.parse(html_str);
    		threads.add(new Thread(new DocIndexer(URL.get(i),i)));
        	threads.get(threads.size()-1).start();

        	try {
				TimeUnit.MILLISECONDS.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }

        for(Thread t : threads) {
        	try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        } 
        threads.clear();
        
        time = (long) System.currentTimeMillis() - time;
        
        wordsNum = Indexer.invertedIndex.size();
        
        
        
        System.out.println("\nTime taken for building the invertedIndex: " + time + " mSec.");
        System.out.println("\nNumber of failed urls: " + failedURLS + ", From: "+ URL.size());        

        
        time = (long) System.currentTimeMillis();
        for(int j=0; j<wordsNum; ++j) {
    		for(int i=0; i<Indexer_Main.docNum; ++i) {
    		
	    		String word = Indexer.invertedIndex.get(j).getKey();
	    		
	    		Pair<String,Integer> pr = new Pair<String,Integer>(word, i);
	    		
				if (Indexer.map.containsKey(pr)) {
					Indexer.TF_IDFmatrix.addingElement(word ,new TFdata(i,(double)Indexer.map.get(pr)/Indexer_Main.docSize[i]));
					Indexer.TF_IDFmatrix.incElementDF(word);
				}
    		}
        }
    	time = (long) System.currentTimeMillis() - time;
        
        System.out.println("\nTime taken for building the TF_IDF matrix: " + time + " mSec., words = " + Indexer.TF_IDFmatrix.size());
                
        Indexer.TF_IDFmatrix.calcTheIDF();     
         

        
		////////////////////////////////////////////////
		/////////// Serializing TF_IDF Index ///////////
		////////////////////////////////////////////////
        time = (long) System.currentTimeMillis();
             		
	    BufferedWriter writer = new BufferedWriter(new FileWriter((String)(System.getProperty("user.dir") + "\\inventory\\IDF_TF.txt")));
		writer.write(Indexer.TF_IDFmatrix.toString());
        writer.close();

		time = (long) System.currentTimeMillis() - time;
		
		String[] lines = Indexer.invertedIndex.toString().split("\r\n|\r|\n");
		System.out.println("\nTime taken to serialize TF_IDF matrix: " + time + " mSec., Words count: " + lines.length );
		////////////////////////////////////////////////
			
				

		//////////////////////////////////////////////////
		/////////// Serializing inverted Index ///////////
		//////////////////////////////////////////////////
			
		time = (long) System.currentTimeMillis();
		
		writer = new BufferedWriter(new FileWriter((String)(System.getProperty("user.dir") + "\\inventory\\InvertedIndex.txt")));
		writer.write(Indexer.invertedIndex.toString());  
		writer.close();

		time = (long) System.currentTimeMillis() - time;
		
		lines = Indexer.invertedIndex.toString().split("\r\n|\r|\n");
		System.out.println("\nTime taken to serialize invertedIndex matrix: " + time + " mSec., Words count: " + lines.length );
		//////////////////////////////////////////////////

		
		//SortedVector_InvertedIndex mat1 = new SortedVector_InvertedIndex(
		//		Files.readString(Paths.get((String)(System.getProperty("user.dir") + "\\inventory\\invertedIndex.txt")))
		//		);
		
		//SortedVector_IDFandTF mat2 = new SortedVector_IDFandTF(
		//		Files.readString(Paths.get((String)(System.getProperty("user.dir") + "\\inventory\\IDF_TF.txt")))
		//		);
	    
	    
	    
	}
	

}

class DocIndexer implements Runnable{
	
	private String url;
	private int docIndex;
	
	public DocIndexer (String url, int docIndex) {
		this.url = url;
		this.docIndex = docIndex;
	}

	public void run() {
		
		Document doc = null;
		//doc = Jsoup.parse(html_str);
    	
    	try {
		doc = Jsoup.connect(url).timeout(100000).get(); 
    	} catch (IOException e) {
            System.out.println("io - "+e + ",	at " + docIndex);
            ++Indexer_Main.failedURLS;
            return;
        }
    	
		ArrayList<Thread> threads = new ArrayList<Thread>();
		
		String title = doc.getElementsByTag("title").text();
		if(!title.equals("")) {
			ArrayList<String> str = Indexer.removeStopwordsAndStem(title);
			threads.add(new Thread(new Indexer(docIndex, "title", 0, str)));
			threads.get(threads.size()-1).start();
			Indexer_Main.docSize[docIndex] += str.size();
		}
		
		int j;
		org.jsoup.select.Elements ele; 

		for(int h=1; h<7; ++h) {
    		ele = doc.select("h"+h);
    		j = 0;
    		for(Element s : ele) {
    			ArrayList<String> str = Indexer.removeStopwordsAndStem(s.text());
    			Indexer ind = new Indexer(docIndex, "h"+h, j, str);
    			Runnable r = ind;
    			threads.add(new Thread(r));
    			threads.get(threads.size()-1).start();
    			++j;
    			Indexer_Main.docSize[docIndex] += str.size();
    		}
		}
		
		ele = doc.select("p");
		j = 0;
		for(Element s : ele) {
			ArrayList<String> str = Indexer.removeStopwordsAndStem(s.text());
			Indexer ind = new Indexer(docIndex, "p", j, str);
			Runnable r = ind;
			threads.add(new Thread(r));
			threads.get(threads.size()-1).start();
			++j;
			Indexer_Main.docSize[docIndex] += str.size();
		}
		
		for(Thread t : threads) {
        	try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
	}

}
 