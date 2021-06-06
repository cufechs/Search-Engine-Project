import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CrawlerMain implements Runnable {
	
	static BlockingQueue<String> URLQueue;
	static FileWriter SerializedData;
	static HashMap<String, ArrayList<String>> RobotContainer = new HashMap<String, ArrayList<String>>();
	
	static int Counter = 0;
	static AtomicInteger DisallowedURLs = new AtomicInteger();

	private void Crawl()
	{	
		while(Counter < 5000)
		{
			String URL = "";
			synchronized(URLQueue)
			{
				while(URLQueue.size() == 0)
				{}
				
				URL = URLQueue.remove();
			}
			
			try {
				if(HandleRobot(URL))
					return;
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			
			try {
				Document document = null;
				document = Jsoup.connect(URL).ignoreContentType(true).get();

				synchronized(SerializedData)
				{
					if(Counter >= 5000)
						return;
					
					SerializedData.write(URL + " AdhamNoice ");
					SerializedData.write(shrinkDoc(document).replace("\n", "").replace("\r", "").replace(System.getProperty("line.separator"), "").replace("\r\n", "") + "\n");
					
					System.out.println(((Counter++)/5000.0f)*100 + "%");
				}
	
		        //3. Parse the HTML to extract links to other URLs
		        Elements linksOnPage = document.select("a[href]");
		
		        //5. For each extracted URL... go back to Step 4.
		        for (Element page : linksOnPage)
		        {
		        	synchronized(URLQueue)
		        	{
		        		if(URLQueue.contains(page.attr("abs:href")))
		        			continue;
		        	}
	        		URLQueue.add(page.attr("abs:href"));
		        }
			}
			catch(Exception e)
			{}
		}
	}
	
	static public String shrinkDoc(Document doc) 
	{
		org.jsoup.select.Elements ele;
		StringBuilder sb = new StringBuilder();		
		
		sb.append("<!DOCTYPE html><html> <body>");
		
		ele = doc.select("title");
		sb.append(ele.outerHtml());
		
		for(int h=1; h<7; ++h) {
    		ele = doc.select("h"+h);
    		sb.append(ele.outerHtml());
		}
		
		ele = doc.select("p");
		sb.append(ele.outerHtml());
		sb.append("</body> </html>");

		return sb.toString();
	}
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws InterruptedException, IOException
	{	
		/*
		// Handling Interruption
        Runtime.getRuntime().addShutdownHook(new Thread() {

		    @Override
		    public void run() {
		        // place your code here
		    	System.out.println("Exiting");
		    }

		});
		*/
		
		URLQueue = new ArrayBlockingQueue<>(5000);
		
        URLQueue.add("https://stackoverflow.com/");					//Programming
		URLQueue.add("https://www.theverge.com/tech"); 				//Tech
        URLQueue.add("https://www.espn.com/");     					//Sports
        URLQueue.add("https://www.health.com/");     				//Health
        URLQueue.add("https://edition.cnn.com/politics");			//Politics
        URLQueue.add("https://www.allrecipes.com/");				//Cooking
        URLQueue.add("https://www.bbc.com/news/business/economy");	//Economy
        URLQueue.add("https://www.imdb.com/");						//Entertainment
        URLQueue.add("https://www.amazon.com/");					//Online Shopping
        URLQueue.add("https://www.pcgamer.com/news/");				//Gaming
        
        Thread[] THREADS = new Thread[50];
        
		SerializedData = new FileWriter("/C:/APT/Project/CrawledData.json");

        for(int i=0; i<50; i++)
        {
        	while(URLQueue.size() == 0)
        	{}
        	
        	Runnable TH = new CrawlerMain();
        	THREADS[i] = new Thread(TH);
        	THREADS[i].start();
        }
        
        for(int i=0; i<50; i++)
        	THREADS[i].join();
                
        SerializedData.close();
		
        SerializedData = null;
    }

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Crawl();
	}
	
	public boolean HandleRobot(String URL) throws MalformedURLException
	{
		String Host = new URL(URL).getHost();

		synchronized(RobotContainer)
		{
			if(!RobotContainer.containsKey(Host))
				RobotContainer.put(Host, ParseRobot(Host));
		}

		ArrayList<String> URLs = RobotContainer.get(Host);
		
		for (String DisallowedURL : URLs)
			if(DisallowedURL.equals(new URL(URL).getPath()))
				return true;
				
		return false;
	}
	
	//This bit of code is fetched from stackoverflow (Modified to suit my needs)
	//URL: https://stackoverflow.com/questions/25731346/reading-robot-txt-with-jsoup-line-by-line
	public ArrayList<String> ParseRobot(String URL) {
		
		ArrayList<String> DisallowedURLs = new ArrayList<String>();	  
		
	    try(BufferedReader in = new BufferedReader(new InputStreamReader(new URL("https://" + URL + "/robots.txt").openStream()))) 
	    {
	        String line = null;
	        boolean FoundUserAgent = false;

	        while((line = in.readLine()) != null)
	        {
	            String[] SplittedLine = line.split(" ");
	            
	            if(SplittedLine.length <= 1)
	            	continue;
	            
	            if(FoundUserAgent || (SplittedLine[0].equals("User-agent:") && SplittedLine[1].equals("*")))
	            {	
	            	if(!FoundUserAgent)
	            	{
	            		FoundUserAgent = true;
	            		continue;
	            	}
	            	
	            	if(SplittedLine[0].equals("Disallow:"))
	            		DisallowedURLs.add(SplittedLine[1]);
	            	else if(SplittedLine[0].equals("Allow:"))
	            		continue;
	            	else
	            		break;
	            }
	        }	        
	    }
	    catch (IOException e) 
	    {
	        //e.printStackTrace();
	    }
	    
        return DisallowedURLs;
	}
}
