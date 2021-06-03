package indexer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import indexer.Indexer;
import indexer.Indexer.TFdata;
import indexer.Indexer.indexData;
import indexer.SortedVector_IDFandTF.nameThr;
import javafx.util.Pair;



public class SortedVector_InvertedIndex extends Vector<Pair<String, Vector<indexData>>> implements Serializable {
	
	public class SortbyKeyString implements Comparator<Pair<String, Vector<indexData>>> {

	    public int compare(Pair<String, Vector<indexData>> a, Pair<String, Vector<indexData>> b)
	    {
	        return (a.getKey()).compareTo(b.getKey());
	    }
	}
  
    private final Vector<Pair<String, Vector<indexData>>> list = new Vector<Pair<String, Vector<indexData>>>();
    private final SortbyKeyString comparator = new SortbyKeyString();
      
    public SortedVector_InvertedIndex() {}
    
    public SortedVector_InvertedIndex(String str) {
    	ArrayList<String> mainArr = Stream.of((str).split("\n"))
	            .collect(Collectors.toCollection(ArrayList<String>::new));

    	for(int i=0; i<mainArr.size(); ++i) {
    		ArrayList<String> row = Stream.of((mainArr.get(i)).split("/"))
    				.collect(Collectors.toCollection(ArrayList<String>::new));

    		Vector<indexData> iDatas = new Vector<indexData>();
    		for(int j=0; j<row.size()-1; ++j) {
    			ArrayList<String> idss = Stream.of((row.get(j+1)).split(","))
        	            .collect(Collectors.toCollection(ArrayList<String>::new));
    			iDatas.add(new indexData(Integer.parseInt(idss.get(0)), idss.get(1),
    					Integer.parseInt(idss.get(2)),Integer.parseInt(idss.get(3))));
    		}
    		addingRow(row.get(0), iDatas);
    	}
    }
    
    public SortedVector_InvertedIndex(List<String> str) {
    	
    	for(int i=0; i<str.size(); ++i) {
    		ArrayList<String> row = Stream.of((str.get(i)).split("/"))
    	            .collect(Collectors.toCollection(ArrayList<String>::new));
    		
    		Vector<indexData> iDatas = new Vector<indexData>();
     		for(int j=0; j<row.size()-1; ++j) {
    			ArrayList<String> idss = Stream.of((row.get(j+1)).split(","))
        	            .collect(Collectors.toCollection(ArrayList<String>::new));
    			iDatas.add(new indexData(Integer.parseInt(idss.get(0)), idss.get(1),
    					Integer.parseInt(idss.get(2)),Integer.parseInt(idss.get(3))));
    		}
    		addingRow(row.get(0), iDatas);
    	}
    }
    
    private static List<String> strList = Collections.synchronizedList(new ArrayList<String>());

    class nameThr implements Runnable{
    	
    	private Pair<String, Vector<indexData>> row;
    	
    	public nameThr (Pair<String, Vector<indexData>> row) {
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
    
    
  
    // method for adding elements in data
    // member of 'SortedVector'
  
    public void addingElement(String key, indexData indData)
    {
    	
    	int index = getKeyIndex(key);
    	
    	if(index != -1) {
    		list.get(index).getValue().add(indData);
    	}
    	else {
    		
	        list.add(new Pair<String, Vector<indexData>>(key, new Vector<indexData>()));
	        list.lastElement().getValue().add(indData);
	        
	        Indexer.TF_IDFmatrix.addingElement(key, null); // adding new entry in this matrix
	        
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
	  
	            	Pair<String, Vector<indexData>> recent = list.lastElement();
	                list.removeElementAt(list.size() - 1);
	                Pair<String, Vector<indexData>> val;
	  
	            	val = recent;
	            	list.add(val);
	            	list.sort(comparator);
	            }
	        }
	  
	        addingElementsInSortedVector();
    	}
    }
    
    public void addingRow(String key, Vector<indexData> iDatas)
    {

        list.add(new Pair<String, Vector<indexData>>(key, iDatas));
  
        if (list.size() > 1) {
   
            try {
  
                list.sort(comparator);
            }
            catch (Exception e) {
  
            	Pair<String, Vector<indexData>> recent = list.lastElement();
                list.removeElementAt(list.size() - 1);
                Pair<String, Vector<indexData>> val;
  
            	val = recent;
            	list.add(val);
            	list.sort(comparator);
            }
        }
  
        addingElementsInSortedVector();
    }
  
    // adding element in object of 'SortedVector'
  
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
    
    private static int binarySearch(Vector<Pair<String, Vector<indexData>>> vec, int l, int r, String x)
    {
        if (r >= l) {
            int mid = l + (r - l) / 2;
 
            // If the element is present at the
            // middle itself
            if (vec.get(mid).getKey().equals(x))
                return mid;
 
            // If element is smaller than mid, then
            // it can only be present in left subarray
            if (stringCompare(vec.get(mid).getKey() ,x) > 0)
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
  
        // Edge case for strings like
        // String 1="Geeks" and String 2="Geeksforgeeks"
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
    	int s = this.size();
    	for(int i=0; i<s; ++i) {
            
        	System.out.print(this.get(i).getKey() + ":- ");
        	for(int j=0; j<this.get(i).getValue().size(); ++j) {
        		System.out.print(this.get(i).getValue().get(j).getData());
        	}
        	System.out.println();
        }
    }
}

