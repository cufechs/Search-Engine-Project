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

public class Main {

	static int failedURLS = 0;
	static int[] docSize;
	static int docNum;
	static int wordsNum;
	static String content;
	
	
	public static void main(String[] args) throws IOException {

		ArrayList<Thread> threads = new ArrayList<Thread>();
		Indexer.init();
		
		
		//String tst = "Hi I am Adham going to carfully say somethings adham";
		//ArrayList<String> tstNew = Indexer.removeStopwordsAndStem(tst);
		//System.out.println(tst);
		
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
        
        
        //Indexer.invertedIndex.printMe();
        
        System.out.println("\nTime taken for building the invertedIndex: " + time + " mSec.");
        System.out.println("\nNumber of failed urls: " + failedURLS);        

        
        
        time = (long) System.currentTimeMillis();
        for(int j=0; j<wordsNum; ++j) {
    		for(int i=0; i<Main.docNum; ++i) {
    		
	    		String word = Indexer.invertedIndex.get(j).getKey();
	    		
	    		Pair<String,Integer> pr = new Pair<String,Integer>(word, i);
	    		
				if (Indexer.map.containsKey(pr)) {
					Indexer.TF_IDFmatrix.addingElement(word ,new TFdata(i,(double)Indexer.map.get(pr)/Main.docSize[i]));
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
        
	    BufferedWriter writer = null;
	      
        content = Indexer.TF_IDFmatrix.toString();
        
        //String fileCont = Files.readString(Paths.get((String)(System.getProperty("user.dir") + "\\inventory\\IDF_TF.txt")));

        //To make sure the serialization is correct
		ArrayList<String> ars = (ArrayList<String>) Stream.of((content).split("\n"))
	            .collect(Collectors.toCollection(ArrayList<String>::new)); 
		int size = ars.size();
		
		int s1 = Indexer.TF_IDFmatrix.size();
		ArrayList<Thread> threads1 = new ArrayList<Thread>();
		

		SortedVector_IDFandTF mat = null;
		while(size != s1) {
			mat = new SortedVector_IDFandTF(content);
			
			for(int i=0; i<s1; ++i) {
				try {
					if(!Indexer.TF_IDFmatrix.get(i).getKey().getKey().equals(mat.get(i).getKey().getKey())) {
						
						mat.addingRow(Indexer.TF_IDFmatrix.get(i).getKey(), Indexer.TF_IDFmatrix.get(i).getValue());
						
						Thread t = new Thread(new nameThr_IDFandTF(Indexer.TF_IDFmatrix.get(i)));
			    		t.start();
			    		threads1.add(t);

					}
				}
				catch(ArrayIndexOutOfBoundsException e) {
					mat.addingRow(Indexer.TF_IDFmatrix.get(i).getKey(), Indexer.TF_IDFmatrix.get(i).getValue());
					Thread t = new Thread(new nameThr_IDFandTF(Indexer.TF_IDFmatrix.get(i)));
		    		t.start();
		    		threads1.add(t);    	
				}
			}
			
			for(Thread t : threads1) {
	        	try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	        } 
			threads1.clear();
			ars = (ArrayList<String>) Stream.of((content).split("\n"))
					.collect(Collectors.toCollection(ArrayList<String>::new)); 
			size = ars.size(); 
		}
		writer = new BufferedWriter(new FileWriter((String)(System.getProperty("user.dir") + "\\inventory\\IDF_TF.txt")));
		writer.write(content);
        writer.close();

		    
		time = (long) System.currentTimeMillis() - time;
		System.out.println("\nTime taken to serialize TF_IDF matrix: " + time + " mSec., Words count: " + mat.size() );
		////////////////////////////////////////////////
			
				
			
			
		//////////////////////////////////////////////////
		/////////// Serializing inverted Index ///////////
		//////////////////////////////////////////////////
			
		time = (long) System.currentTimeMillis();
		content = Indexer.invertedIndex.toString();

		//To make sure the serialization is correct
	 	ars = (ArrayList<String>) Stream.of((content).split("\n"))
				.collect(Collectors.toCollection(ArrayList<String>::new)); 
		size = ars.size();  

		s1 = Indexer.invertedIndex.size();
		SortedVector_InvertedIndex mat1 = null;

		while(size != s1) {
			mat1 = new SortedVector_InvertedIndex(content);
			
			
			for(int i=0; i<s1; ++i) {
				try {
									
					if(!Indexer.invertedIndex.get(i).getKey().equals(mat1.get(i).getKey())) {
						mat1.addingRow(Indexer.invertedIndex.get(i).getKey(), Indexer.invertedIndex.get(i).getValue());	
						
						Thread t = new Thread(new nameThr_InvertedIndex(Indexer.invertedIndex.get(i)));
			    		t.start();
			    		threads1.add(t);
	
					}
				}catch(ArrayIndexOutOfBoundsException e) {
					mat1.addingRow(Indexer.invertedIndex.get(i).getKey(), Indexer.invertedIndex.get(i).getValue());	
					Thread t = new Thread(new nameThr_InvertedIndex(Indexer.invertedIndex.get(i)));
		    		t.start();
		    		threads1.add(t);
				}
			}
			
			for(Thread t : threads1) {
	        	try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	        } 
			
			ars = (ArrayList<String>) Stream.of((content).split("\n"))
					.collect(Collectors.toCollection(ArrayList<String>::new)); 
			size = ars.size(); 
		}

		writer = new BufferedWriter(new FileWriter((String)(System.getProperty("user.dir") + "\\inventory\\InvertedIndex.txt")));
		writer.write(content);  
		writer.close();

		time = (long) System.currentTimeMillis() - time;
		System.out.println("\nTime taken to serialize invertedIndex matrix: " + time + " mSec., Words count: " + mat1.size() );
		//////////////////////////////////////////////////

		//Indexer.invertedIndex.printMe();
		//SortedVector_InvertedIndex mat = new SortedVector_InvertedIndex(
		//		Files.readString(Paths.get((String)(System.getProperty("user.dir") + "\\inventory\\invertedIndex.txt")))
		//		);
		//mat.printMe();
	    
	    
	    
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
            ++Main.failedURLS;
            return;
        }
    	
		ArrayList<Thread> threads = new ArrayList<Thread>();
		
		String title = doc.getElementsByTag("title").text();
		if(!title.equals("")) {
			ArrayList<String> str = Indexer.removeStopwordsAndStem(title);
			threads.add(new Thread(new Indexer(docIndex, "title", 0, str)));
			threads.get(threads.size()-1).start();
			Main.docSize[docIndex] += str.size();
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
    			Main.docSize[docIndex] += str.size();
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
			Main.docSize[docIndex] += str.size();
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

class nameThr_IDFandTF implements Runnable{

	private Pair<Pair<String,Double>, Vector<TFdata>> row;
	
	public nameThr_IDFandTF (Pair<Pair<String,Double>, Vector<TFdata>> row) {
		this.row = row;
	}

	public void run() {
		String tempStr; 
		tempStr = row.getKey().getKey() + "/" + row.getKey().getValue() + "/";
		
		int s = row.getValue().size();
		for(int j=0; j<s ; ++j) {
			tempStr += row.getValue().get(j) + "/";
		}
		tempStr = tempStr.substring(0, tempStr.length() - 1);
		tempStr += "\n";
		
		synchronized(Main.content) {
			Main.content += tempStr;
		}    		
	}
}

class nameThr_InvertedIndex implements Runnable{
	
	private Pair<String, Vector<indexData>> row;
	
	public nameThr_InvertedIndex (Pair<String, Vector<indexData>> row) {
		this.row = row;
	}

	public void run() {
		String tempStr; 
		tempStr = row.getKey() + "/";
		for(int j=0; j<row.getValue().size(); ++j) {
			tempStr += row.getValue().get(j) + "/";
		}
		tempStr = tempStr.substring(0, tempStr.length() - 1);
		tempStr += "\n";
		
		synchronized(Main.content) {
			Main.content += tempStr;
		}    		
	}
}
