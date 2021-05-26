package indexer;

import java.util.Comparator;
import java.util.Vector;

import indexer.Indexer;
import indexer.Indexer.indexData;
import javafx.util.Pair;



public class SortedVector_InvertedIndex extends Vector<Pair<String, Vector<indexData>>> {
	
	public class SortbyKeyString implements Comparator<Pair<String, Vector<indexData>>> {

	    public int compare(Pair<String, Vector<indexData>> a, Pair<String, Vector<indexData>> b)
	    {
	        return (a.getKey()).compareTo(b.getKey());
	    }
	}
  
    private final Vector<Pair<String, Vector<indexData>>> list = new Vector<Pair<String, Vector<indexData>>>();
    private final SortbyKeyString comparator = new SortbyKeyString();
      
    public SortedVector_InvertedIndex() {}
  
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
    	for(int i=0; i<Main.wordsNum; ++i) {
            
        	System.out.print(this.get(i).getKey() + ":- ");
        	for(int j=0; j<this.get(i).getValue().size(); ++j) {
        		System.out.print(this.get(i).getValue().get(j).getData());
        	}
        	System.out.println();
        }
    }
}

