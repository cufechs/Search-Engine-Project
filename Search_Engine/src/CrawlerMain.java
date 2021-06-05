import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.io.FileWriter;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CrawlerMain implements Runnable {
	
	static BlockingQueue<String> URLQueue;
	static FileWriter SerializedData;
	
	static int Counter = 0;
	final static int crawlsCount = 5000;

	private void Crawl()
	{	
		while(Counter < crawlsCount)
		{
			if(URLQueue.size() != 0)
			{
				String URL = "";
				synchronized(URLQueue)
				{
					URL = URLQueue.remove();
				}
				
				try {
					Document document = null;
					document = Jsoup.connect(URL).ignoreContentType(true).get();
;
					synchronized(SerializedData)
					{
						if(Counter >= crawlsCount)
							return;
						
						SerializedData.write(URL + " AdhamNoice ");
						SerializedData.write(shrinkDoc(document).replace("\n", "").replace("\r", "").replace(System.getProperty("line.separator"), "").replace("\r\n", "") + "\n");
						
						System.out.println(Counter++);
					}
		
			        //3. Parse the HTML to extract links to other URLs
			        Elements linksOnPage = document.select("a[href]");
			
			        //5. For each extracted URL... go back to Step 4.
			        for (Element page : linksOnPage)
			        {
		        		if(URLQueue.contains(page.attr("abs:href")))
		        			continue;
		        		URLQueue.add(page.attr("abs:href"));
			        }
				}
				catch(Exception e)
				{}
			}
		}
	}
	
	//Adham
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
	
	public static void main(String[] args) throws InterruptedException, IOException
	{			
		
		URLQueue = new ArrayBlockingQueue<>(crawlsCount);
		
        URLQueue.add("https://www.deepcrawl.com/blog/releases/log-file-integration/");
        URLQueue.add("https://washingtonpress.com/");
        URLQueue.add("https://proxycrawl.com/how-to-scrape-reddit");
        URLQueue.add("https://www.quora.com/What-are-some-interesting-sites-to-web-crawl");


        Thread[] THREADS = new Thread[25];
        
        SerializedData = new FileWriter((String)(System.getProperty("user.dir") + "\\inventory\\CrawledData.json"));

        for(int i=0; i<25; i++)
        {
        	while(URLQueue.size() == 0)
        	{}
        	
        	Runnable TH = new CrawlerMain();
        	THREADS[i] = new Thread(TH);
        	THREADS[i].start();
        }
        
        for(int i=0; i<25; i++)
        	THREADS[i].join();
                
        SerializedData.close();
		
        SerializedData = null;
        
    }

	@Override
	public void run() {
		Crawl();
	}
}
