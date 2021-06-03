package indexer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import indexer.Indexer.TFdata;
import javafx.util.Pair;



public class SortedVector_IDFandTF extends Vector<Pair<Pair<String,Double>, Vector<TFdata>>> implements Serializable {
	
	
	public class SortbyKeyString implements Comparator<Pair<Pair<String,Double>, Vector<TFdata>>> {

	    public int compare(Pair<Pair<String,Double>, Vector<TFdata>> a, Pair<Pair<String,Double>, Vector<TFdata>> b)
	    {
	        return (a.getKey().getKey()).compareTo(b.getKey().getKey());
	    }
	}
  
    private final Vector<Pair<Pair<String,Double>, Vector<TFdata>>> list = new Vector<Pair<Pair<String,Double>, Vector<TFdata>>>();
    private final SortbyKeyString comparator = new SortbyKeyString();
      
    public SortedVector_IDFandTF() {}
    
    public SortedVector_IDFandTF(String str) {
    	ArrayList<String> mainArr = Stream.of((str).split("\n"))
	            .collect(Collectors.toCollection(ArrayList<String>::new));
    	for(int i=0; i<mainArr.size(); ++i) {
    		ArrayList<String> row = Stream.of((mainArr.get(i)).split("/"))
    	            .collect(Collectors.toCollection(ArrayList<String>::new));
    		Vector<TFdata> TFs = new Vector<TFdata>();
    		for(int j=0; j<row.size()-2; ++j) {
    			ArrayList<String> tf = Stream.of((row.get(j+2)).split(","))
        	            .collect(Collectors.toCollection(ArrayList<String>::new));
    			TFs.add(new TFdata(Integer.parseInt(tf.get(0)), Double.parseDouble(tf.get(1))));
    		}
    		addingRow(new Pair<String,Double>(row.get(0), Double.parseDouble(row.get(1))), TFs);
    	}
    }
    
    public SortedVector_IDFandTF(List<String> str) {
    	for(int i=0; i<str.size(); ++i) {
    		ArrayList<String> row = Stream.of((str.get(i)).split("/"))
    	            .collect(Collectors.toCollection(ArrayList<String>::new));
    		Vector<TFdata> TFs = new Vector<TFdata>();
    		for(int j=0; j<row.size()-2; ++j) {
    			ArrayList<String> tf = Stream.of((row.get(j+2)).split(","))
        	            .collect(Collectors.toCollection(ArrayList<String>::new));
    			TFs.add(new TFdata(Integer.parseInt(tf.get(0)), Double.parseDouble(tf.get(1))));
    		}
    		addingRow(new Pair<String,Double>(row.get(0), Double.parseDouble(row.get(1))), TFs);
    	}
    }
    
    private static List<String> strList = Collections.synchronizedList(new ArrayList<String>());
    

    class nameThr implements Runnable{
    	
    	private Pair<Pair<String,Double>, Vector<TFdata>> row;
    	
    	public nameThr (Pair<Pair<String,Double>, Vector<TFdata>> row) {
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
    		
    		strList.add(tempStr);		
    	}

    }
    
    @Override
    public String toString() {
    	
    	if(this.size() != strList.size()) 
    		toStringList();
    	
    	StringBuilder ser = new StringBuilder();
    	for (int i=0; i<strList.size(); ++i)
    		ser.append(strList.get(i));
    	
        return ser.toString();
    }
    
    public List<String> toStringList() {
    	ArrayList<Thread> threads = new ArrayList<Thread>();
    	strList.clear();
    	
    	int s = this.size();
    	for(int i=0; i<s; ++i) {
    		Thread t = new Thread(new nameThr(this.get(i)));
    		t.start();
    		threads.add(t);
    	}
    	
    	for(Thread t : threads) {
        	try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    	
    	//System.out.println("RESULT: " + (strList.size() == this.size()));
    	
        return strList;
    }
    
    
    public void incElementDF(String key) {
    	
		int index = getKeyIndex(key);
    	
    	if(index != -1) {	
    		Double d = list.get(index).getKey().getValue();
    		list.set(index, new Pair<Pair<String,Double>, Vector<TFdata>>(
    				new Pair<String,Double>(key, d+1), list.get(index).getValue()));
    	}
    }
    
    public void calcTheIDF() {
    	
    	for(int i=0; i<list.size(); ++i) {
    	this.set(i, new Pair<Pair<String,Double>, Vector<TFdata>>(
				new Pair<String,Double>(list.get(i).getKey().getKey(),
						(Double)Math.log10((double)Indexer_Main.docNum/list.get(i).getValue().size()))
				, list.get(i).getValue()));
    	
    	//System.out.println("log(" + Main.docNum + " / " + list.get(i).getValue().size() + ") = " + list.get(i).getKey().getValue());//+ Math.log10((double)Main.docNum/list.get(i).getValue().size()));

    	}
    }

  
    public void addingElement(String key, TFdata tfData)
    {
    	
    	int index = getKeyIndex(key);
    	
    	if(index != -1) {
    		if(tfData != null)
    			list.get(index).getValue().add(tfData);
    	}
    	else {
    		
    		
	        list.add(new Pair<Pair<String,Double>, Vector<TFdata>>(new Pair<String,Double>(key, 0.0), new Vector<TFdata>()));
	        
	        if(tfData != null)
	        	list.lastElement().getValue().add(tfData);
	        
	        // if list size is less than or equal to one
	        // element then there is no need of sorting.
	        // here we are sorting elements
	  
	        if (list.size() > 1) {
	  
	            // If we are getting character as input then
	            // Exceptions occurs in 'Collections.sort'.
	            // So, we are type casting character to int.
	            // and sorting character as integer.
	  
	            try {
	  
	                list.sort(comparator);
	            }
	            catch (Exception e) {
	  
	            	Pair<Pair<String,Double>, Vector<TFdata>> recent = list.lastElement();
	                list.removeElementAt(list.size() - 1);
	                Pair<Pair<String,Double>, Vector<TFdata>> val;
	  
	            	val = recent;
	            	list.add(val);
	            	list.sort(comparator);
	            }
	        }
	  
	        addingElementsInSortedVector();
    	}
    }
    
    public void addingRow(Pair<String,Double> key, Vector<TFdata> tfDatas)
    {

        list.add(new Pair<Pair<String,Double>, Vector<TFdata>>(key, tfDatas));
  
        if (list.size() > 1) {
   
            try {
  
                list.sort(comparator);
            }
            catch (Exception e) {
  
            	Pair<Pair<String,Double>, Vector<TFdata>> recent = list.lastElement();
                list.removeElementAt(list.size() - 1);
                Pair<Pair<String,Double>, Vector<TFdata>> val;
  
            	val = recent;
            	list.add(val);
            	list.sort(comparator);
            }
        }
  
        addingElementsInSortedVector();
    }
  
    
    private void addingElementsInSortedVector()
    {
        // clear all values of "SortedVector's" object
        clear();
  
        // adding values in object of 'SortedVector'
        addAll(list);
    }
    
    public int getKeyIndex(String key) {
    	return binarySearch(list, 0, list.size()-1, key); 	
    }
    
    private static int binarySearch(Vector<Pair<Pair<String,Double>, Vector<TFdata>>> vec, int l, int r, String x)
    {
        if (r >= l) {
            int mid = l + (r - l) / 2;
 
            // If the element is present at the
            // middle itself
            if (vec.get(mid).getKey().getKey().equals(x))
                return mid;
 
            // If element is smaller than mid, then
            // it can only be present in left subarray
            if (stringCompare(vec.get(mid).getKey().getKey() ,x) > 0)
                return binarySearch(vec, l, mid - 1, x);
 
            // Else the element can only be present
            // in right subarray
            return binarySearch(vec, mid + 1, r, x);
        }
 
        // We reach here when element is not present
        // in array
        return -1;
    }
    
    private static int stringCompare(String str1, String str2)
    {
  
        int l1 = str1.length();
        int l2 = str2.length();
        int lmin = Math.min(l1, l2);
  
        for (int i = 0; i < lmin; i++) {
            int str1_ch = (int)str1.charAt(i);
            int str2_ch = (int)str2.charAt(i);
  
            if (str1_ch != str2_ch) {
                return str1_ch - str2_ch;
            }
        }
  
        if (l1 != l2) {
            return l1 - l2;
        }
  
        // If none of the above conditions is true,
        // it implies both the strings are equal
        else {
            return 0;
        }
    }
    
    public void printMe() {
    	for(int i=0; i<Indexer_Main.wordsNum; ++i) {
            
        	System.out.print(this.get(i).getKey().getKey() + ":- ");
        	System.out.print("DF: (" + (double)this.get(i).getKey().getValue() + ") :- ");
        	
        	for(int j=0; j<this.get(i).getValue().size(); ++j) {
        		System.out.print(this.get(i).getValue().get(j).getData());
        	}
        	System.out.println();
        }
    }
}

