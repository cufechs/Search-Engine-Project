package indexer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.util.Pair;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import indexer.Indexer.TFdata;

public class Indexer_Main {

	static int[] docSize;
	static int docNum;
	static int wordsNum;
	static String content;
	
	
	public static void main(String[] args) throws IOException {

		ArrayList<Thread> threads = new ArrayList<Thread>();
		Indexer.init();
		
		long time = (long) System.currentTimeMillis();
		long time_all = time;
		
		ArrayList<Pair<String,String>> URL_doc = new ArrayList<Pair<String,String>>();
        try {
		    BufferedReader in = new BufferedReader(new FileReader((String)(System.getProperty("user.dir") + "\\inventory\\CrawledData.json")));
		    String url;
		    while ((url = in.readLine()) != null) {
		    	ArrayList<String> miniArr = Stream.of((url).split(" AdhamNoice "))
			            .collect(Collectors.toCollection(ArrayList<String>::new));
		    	URL_doc.add(new Pair<String,String>(miniArr.get(0),miniArr.get(1)));
		    }
		    in.close();
		} catch (IOException e) {
			System.out.println("Can not read hyberlinks file");
			return;
		}
        time = (long) System.currentTimeMillis() - time;
        System.out.println("\nTime taken to read the documents file: " + time + " mSec.");
        
        docNum = URL_doc.size();
        docSize = new int[docNum];
        
        time = (long) System.currentTimeMillis();

    	for(int i=0; i<docNum;++i) {
    		threads.add(new Thread(new DocIndexer(URL_doc.get(i).getValue(),i)));
        	threads.get(threads.size()-1).start();
        	if(threads.size() >= 1000) {
        		for(int j=0; j<500; ++j) {
                	try {
                		threads.get(j).join(200);
                		if(!threads.get(j).isAlive())
                			threads.remove(j);
        			} catch (InterruptedException e) {
        				e.printStackTrace();
        			}
                } 
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
        System.out.println("\nTime taken for building the invertedIndex: " + time + " mSec.");
        
        wordsNum = Indexer.invertedIndex.size();  
        
        time = (long) System.currentTimeMillis();
        
        
		for(int i=0; i<Indexer_Main.docNum; ++i) {
			threads.add(new Thread(new DocTF_IDF(i)));
			threads.get(threads.size()-1).start();
			if(threads.size() >= 1000) {
        		for(int j=0; j<500; ++j) {
                	try {
                		threads.get(j).join(200);
                		if(!threads.get(j).isAlive())
                			threads.remove(j);
        			} catch (InterruptedException e) {
        				e.printStackTrace();
        			}
                } 
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
		System.out.println("\nTime taken to serialize TF_IDF matrix: " + time + " mSec.");
		////////////////////////////////////////////////
			
				

		//////////////////////////////////////////////////
		/////////// Serializing inverted Index ///////////
		//////////////////////////////////////////////////
			
		time = (long) System.currentTimeMillis();
		
		writer = new BufferedWriter(new FileWriter((String)(System.getProperty("user.dir") + "\\inventory\\InvertedIndex.txt")));
		writer.write(Indexer.invertedIndex.toString());  
		writer.close();

		time = (long) System.currentTimeMillis() - time;
		System.out.println("\nTime taken to serialize invertedIndex matrix: " + time + " mSec.");
		//////////////////////////////////////////////////
		
		
		time_all = (long) System.currentTimeMillis() - time_all;
		System.out.println("\nTime taken for the whole proccess: " + time_all/1000 + " Seconds");

		
		//SortedVector_InvertedIndex mat1 = new SortedVector_InvertedIndex(
		//		Files.readString(Paths.get((String)(System.getProperty("user.dir") + "\\inventory\\invertedIndex.txt")))
		//		);
		
		//SortedVector_IDFandTF mat2 = new SortedVector_IDFandTF(
		//		Files.readString(Paths.get((String)(System.getProperty("user.dir") + "\\inventory\\IDF_TF.txt")))
		//		);
	    
		//Indexer.invertedIndex.printMe();

	}

}

class DocTF_IDF implements Runnable{
	
	private int docIndex;
	
	public DocTF_IDF ( int docIndex) {
		this.docIndex = docIndex;
	}
	
	public void run() {
		for(int j=0; j<Indexer_Main.wordsNum; ++j) {
			String word = Indexer.invertedIndex.get(j).getKey();
    		
    		Pair<String,Integer> pr = new Pair<String,Integer>(word, docIndex);
    		
			if (Indexer.map.containsKey(pr)) {
				synchronized(Indexer.map) {
					Indexer.TF_IDFmatrix.addingElement(word ,new TFdata(docIndex,(double)Indexer.map.get(pr)/Indexer_Main.docSize[docIndex]));
					Indexer.TF_IDFmatrix.incElementDF(word);
				}
			}
		}
	}
}

class DocIndexer implements Runnable{
	
	private String docText;
	private int docIndex;
	
	public DocIndexer (String docText, int docIndex) {
		this.docText = docText;
		this.docIndex = docIndex;
	}

	public void run() {
		
		Document doc = Jsoup.parse(docText);
    	
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
 