package indexer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import indexer.Indexer.TFdata;
import javafx.util.Pair;

public class Indexer_Main {

	static int[] docSize;
	static int docNum;
	static int wordsNum;
	static int indexedDocsCount = 0;
	static String content;
	static int numberOfPastDocs;
	static int lastDocIndex;
	

	public static void main(String[] args) throws IOException {

		ArrayList<Thread> threads = new ArrayList<Thread>();
		Indexer.init1();
		
		
		long time_all = (long) System.currentTimeMillis();
		
		long time = (long) System.currentTimeMillis();
		ArrayList<String> indexedDocs = new ArrayList<String>();
        try {
        	
		    BufferedReader in = new BufferedReader(new FileReader((String)(System.getProperty("user.dir") + "\\inventory\\indexedDocs.txt")));
		    String url;
		    while ((url = in.readLine()) != null) 
		    	indexedDocs.add(url);
		    in.close();
		    
		    Indexer.invertedIndex = new SortedVector_InvertedIndex(
					Files.readString(Paths.get((String)(System.getProperty("user.dir") + "\\inventory\\invertedIndex.txt")))
					);
		    
		    Indexer.TF_IDFmatrix = new SortedVector_IDFandTF(
					Files.readString(Paths.get((String)(System.getProperty("user.dir") + "\\inventory\\IDF_TF.txt")))
					);
		    
		    lastDocIndex = indexedDocs.size();
		    numberOfPastDocs = lastDocIndex;
		    
		    System.out.println("Will continue on the past indexes.");
		    
		    File theDir = new File((String)(System.getProperty("user.dir") + "\\inventory\\bin"));
	    	if (theDir.exists())
	    		deleteDirectory(theDir);
    		theDir.mkdirs();
    		
    		
    		Files.copy
			(Paths.get((String)(System.getProperty("user.dir") + "\\inventory\\indexedDocs.txt"))
			,Paths.get((String)(System.getProperty("user.dir") + "\\inventory\\bin\\indexedDocs.txt"))
			,java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    		
		    
		} catch (IOException e) {
			File theDir = new File((String)(System.getProperty("user.dir") + "\\inventory\\bin"));
	    	if (theDir.exists())
	    		deleteDirectory(theDir);
    		theDir.mkdirs();
    		
    		
			System.out.println("No previous index saved.");
			numberOfPastDocs = 0;
			indexedDocs.clear();
			
			lastDocIndex = 0;
			
			Indexer.init2();
		}
        time = (long) System.currentTimeMillis() - time;
        System.out.println("Time taken to load old data: " + time + " mSec.");
		
		
		time = (long) System.currentTimeMillis();
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
			System.out.println("Can not read CrawledData file");
			return;
		}
        time = (long) System.currentTimeMillis() - time;
        System.out.println("Time taken to read the documents file: " + time + " mSec.\n");
        
        docNum = URL_doc.size();
        docSize = new int[docNum+numberOfPastDocs];
        
        time = (long) System.currentTimeMillis();

    	for(int i=0; i<docNum;++i) {
    		if(indexedDocs.contains(URL_doc.get(i).getKey())) {
    			System.out.println("Aready indexed url : " + URL_doc.get(i).getKey());
    			continue;
    		}
    		threads.add(new Thread(new DocIndexer(URL_doc.get(i).getValue(),lastDocIndex++)));
    		
    		indexedDocs.add(URL_doc.get(i).getKey());
    		FileWriter fw = new FileWriter((String)(System.getProperty("user.dir") + "\\inventory\\bin\\indexedDocs.txt"), true);
    	    BufferedWriter bw = new BufferedWriter(fw);
    	    bw.write(URL_doc.get(i).getKey());
    	    bw.newLine();
    	    bw.close();
    		
    		threads.get(threads.size()-1).start();
        	++indexedDocsCount;
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
        indexedDocs.clear();
        URL_doc.clear();
        
        time = (long) System.currentTimeMillis() - time;
        System.out.println("\nTime taken for building the invertedIndex: " + time + " mSec.");
        
        wordsNum = Indexer.invertedIndex.size();  
        
        time = (long) System.currentTimeMillis();
        
		for(int i=numberOfPastDocs; i<lastDocIndex; ++i) {
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
        
        Indexer.TF_IDFmatrix.sortTFs();
        Indexer.TF_IDFmatrix.calcTheIDF();    
         
    	time = (long) System.currentTimeMillis() - time;
        
        System.out.println("Time taken for building the TF_IDF matrix: " + time + " mSec., words = " + Indexer.TF_IDFmatrix.size());
                
         
         

		////////////////////////////////////////////////
		/////////// Serializing TF_IDF Index ///////////
		////////////////////////////////////////////////
        time = (long) System.currentTimeMillis();
             		
        if(indexedDocsCount > 0) {
		    BufferedWriter writer = new BufferedWriter(new FileWriter((String)(System.getProperty("user.dir") + "\\inventory\\bin\\IDF_TF.txt")));
			writer.write(Indexer.TF_IDFmatrix.toString());
	        writer.close();
        }

		time = (long) System.currentTimeMillis() - time;
		System.out.println("Time taken to serialize TF_IDF matrix: " + time + " mSec.");
		////////////////////////////////////////////////
			
				

		//////////////////////////////////////////////////
		/////////// Serializing inverted Index ///////////
		//////////////////////////////////////////////////
			
		time = (long) System.currentTimeMillis();
		
		if(indexedDocsCount > 0) {
			BufferedWriter writer = new BufferedWriter(new FileWriter((String)(System.getProperty("user.dir") + "\\inventory\\bin\\InvertedIndex.txt")));
			writer.write(Indexer.invertedIndex.toString());  
			writer.close();
		}

		time = (long) System.currentTimeMillis() - time;
		System.out.println("Time taken to serialize invertedIndex matrix: " + time + " mSec.");
		//////////////////////////////////////////////////
		
		
		if(indexedDocsCount > 0) {
			
			new File((String)(System.getProperty("user.dir")) + "\\inventory\\InvertedIndex.txt").delete();
			
			Files.move
	            (Paths.get((String)(System.getProperty("user.dir") + "\\inventory\\bin\\InvertedIndex.txt")), 
	            Paths.get((String)(System.getProperty("user.dir") + "\\inventory\\InvertedIndex.txt")));
			
			new File((String)(System.getProperty("user.dir")) + "\\inventory\\IDF_TF.txt").delete();
			
			Files.move
	            (Paths.get((String)(System.getProperty("user.dir") + "\\inventory\\bin\\IDF_TF.txt")), 
	            Paths.get((String)(System.getProperty("user.dir") + "\\inventory\\IDF_TF.txt")));
			
			new File((String)(System.getProperty("user.dir")) + "\\inventory\\indexedDocs.txt").delete();
			
			Files.move
	            (Paths.get((String)(System.getProperty("user.dir") + "\\inventory\\bin\\indexedDocs.txt")), 
	            Paths.get((String)(System.getProperty("user.dir") + "\\inventory\\indexedDocs.txt")));
			
			
			// Creating a backup
			File theDir = new File((String)(System.getProperty("user.dir") + "\\inventory\\backup"));
			if (theDir.exists())
	    		deleteDirectory(theDir);
    		theDir.mkdirs();
    		
    		Files.copy
    			(Paths.get((String)(System.getProperty("user.dir") + "\\inventory\\indexedDocs.txt"))
    			,Paths.get((String)(System.getProperty("user.dir") + "\\inventory\\backup\\indexedDocs.txt"))
    			,java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    		
    		Files.copy
				(Paths.get((String)(System.getProperty("user.dir") + "\\inventory\\IDF_TF.txt"))
				,Paths.get((String)(System.getProperty("user.dir") + "\\inventory\\backup\\IDF_TF.txt"))
				,java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    		
    		Files.copy
				(Paths.get((String)(System.getProperty("user.dir") + "\\inventory\\InvertedIndex.txt"))
				,Paths.get((String)(System.getProperty("user.dir") + "\\inventory\\backup\\InvertedIndex.txt"))
				,java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		}
		
		File file = new File((String)(System.getProperty("user.dir")) + "\\inventory\\bin");
		deleteDirectory(file);
		
		
		time_all = (long) System.currentTimeMillis() - time_all;
		System.out.println("Time taken for the whole proccess: " + time_all/1000 + " Seconds");
		System.out.println("Number of new indexed documents: " + indexedDocsCount + " document");
		
		
		
		
		//SortedVector_InvertedIndex mat1 = new SortedVector_InvertedIndex(
		//		Files.readString(Paths.get((String)(System.getProperty("user.dir") + "\\inventory\\invertedIndex.txt")))
		//		);
		
		//SortedVector_IDFandTF mat2 = new SortedVector_IDFandTF(
		//		Files.readString(Paths.get((String)(System.getProperty("user.dir") + "\\inventory\\IDF_TF.txt")))
		//		);
	    

	}

	
	private static void deleteDirectory(File file) {
        // store all the paths of files and folders present
        // inside directory
        for (File subfile : file.listFiles()) {
  
            // if it is a subfolder,e.g Rohan and Ritik,
            // recursiley call function to empty subfolder
            if (subfile.isDirectory()) {
                deleteDirectory(subfile);
            }
  
            // delete files and empty sub folders
            subfile.delete();
        }
        file.delete();
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
