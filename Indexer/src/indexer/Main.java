package indexer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javafx.util.Pair;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import indexer.Indexer.TFdata;

public class Main {

	static int failedURLS = 0;
	static int[] docSize;
	static int docNum;
	static int wordsNum;
	
	
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
        
        System.out.println("\nTime taken: " + time + " mSec.");
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
        
        System.out.println("\nTime taken: " + time + " mSec.");
                
        
        
        Indexer.TF_IDFmatrix.calcTheIDF();     
        
        //Indexer.TF_IDFmatrix.printMe();
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