import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.*;

public class CrawlerMain implements Runnable {
	
	final static BlockingQueue<String> DataQueue = new ArrayBlockingQueue<>(5000);
	final static BlockingQueue<String> URLQueue = new ArrayBlockingQueue<>(5000);
	
	private static void Crawl()
	{		
		if(DataQueue.size() >= 5000)
			return;

		
		
		String URL = "";
		synchronized(URLQueue)
    	{
			URL = URLQueue.remove();
    	}
		
		try {
			Document document = null;
			try {
				document = Jsoup.connect(URL).ignoreContentType(true).get();
				
				synchronized(DataQueue)
	        	{
					DataQueue.add(document.html());
					System.out.println(DataQueue.size());
	        	}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        //3. Parse the HTML to extract links to other URLs
	        Elements linksOnPage = document.select("a[href]");
	
	        //5. For each extracted URL... go back to Step 4.
	        Thread[] THREADS = new Thread[linksOnPage.size()];
	        int i=0;
	        for (Element page : linksOnPage)
	        {
	        	if(DataQueue.size() >= 5000)
	    			break;
	        	
	        	synchronized(URLQueue)
	        	{
	        		if(URLQueue.contains(page.attr("abs:href")))
	        			break;
	        		URLQueue.add(page.attr("abs:href"));
	        	}
	        
	        	
	        	Runnable thread = new CrawlerMain();
	        	Thread TH = new Thread(thread);
	        	THREADS[i++] = TH;
	        	TH.start();
	        	
	        	Thread.sleep(50);
	        }
	        
	        for(int j=0; j<THREADS.length; j++)
	        	THREADS[j].join();
		}
		catch(Exception e)
		{}
	}
	
	public static void main(String[] args)
	{
        URLQueue.add("https://en.wikipedia.org/wiki/Keleri");
        
        Crawl();
		
        
		//System.out.println(URLQueue.size());
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Crawl();
	}

}
