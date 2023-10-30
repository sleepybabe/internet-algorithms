import java.util. *;
import java.io.*;


public class InvertedIndex implements Serializable {
    
    private List<String> documents;
    private Map<String, LinkedList<Integer>> index;
    
    public InvertedIndex(){
        documents = new ArrayList<String>();
        index = new HashMap<>();
    }

    public void indexDocument(String path) throws FileNotFoundException {
        
        
        File file = new File(path);
        path = file.getAbsolutePath();
        file = new File(path);
        Scanner infile = new Scanner(file);
        
        if (documents.indexOf(path)!= -1)
            return;
     
        documents.add(path);
        int id = documents.size()-1;
        
        LinkedList<Integer> count;
        
        while (infile.hasNextLine()){
            String line = infile.nextLine();
            
            line = line.toLowerCase();
            String[] words = (line.split("\\W+"));
            for (int i = 0; i<words.length; i++){
                count = new LinkedList<Integer>();
                if (index.containsKey(words[i])){
                    count = index.get(words[i]); 
                    if (count.getLast()!=id)
                        count.add(id);
                }
                else 
                    count.add(id);
                index.put(words[i],count);
                
            }
        }
        System.out.printf("|%7d | %-150s|%10d |\n", id, path, index.size());
    }

    public void indexCollection(String folder) throws FileNotFoundException{
        File file = new File(folder);
        File[] files = file.listFiles();
        if(files!=null)
            for (File val:files){ 
                if(val.isDirectory())
                    indexCollection(val.toString());
                else indexDocument(val.toString());
                
            }  
    }
    
    
    
    public LinkedList<Integer> getIntersection(LinkedList<Integer> list1, LinkedList<Integer> list2){
        ListIterator<Integer> it1 = list1.listIterator();
        ListIterator<Integer> it2 = list2.listIterator();
        LinkedList<Integer> res = new LinkedList<Integer>();
        
        int n = it1.next();
        int m = it2.next();

        while (true){
            if (n == m){
                res.add(n);
                if(!it1.hasNext() | !it2.hasNext())
                    break;
                n = it1.next();
                m = it2.next();
            }
            else if(n > m){
                if(!it2.hasNext())
                    break;
                m = it2.next();
            }
            else{
                if(!it1.hasNext())
                    break;
                n = it1.next();
            }
            
        }
        return res;
    }
    
    public LinkedList<Integer> getUnion(LinkedList<Integer> list1, LinkedList<Integer> list2){
        LinkedList<Integer> res = new LinkedList<Integer>();
        ListIterator<Integer> it1 = list1.listIterator();
        ListIterator<Integer> it2 = list2.listIterator();

        int n = it1.next();
        int m = it2.next();

        while (true) {
            if (n == m){
                res.add(n);
                if(!it1.hasNext() & !it2.hasNext())
                    break;
                if(!it1.hasNext()){
                    m = it2.next();
                    n = m;
                    continue;
                }
                if(!it2.hasNext()){
                    n = it1.next();
                    m = n;
                    continue;
                }
                n = it1.next();
                m = it2.next();   
            }
            else if(n > m){
                res.add(m);
                if(!it2.hasNext()){
                    m = n;
                    continue;
                }
                m = it2.next();
            }
            else {
                res.add(n);
                if(!it1.hasNext()){
                    n = m;
                    continue;
                }
                n = it1.next();
            }  
        }
        return res;
    }



    public LinkedList<Integer> executeQuery(String query){
        LinkedList<Integer> res = new LinkedList<Integer>();
        query = query.toLowerCase();
        String [] words = query.split(" ");
        if (words.length == 1)
            if (index.get(query)!=null)
                res = index.get(query);
        
        if(Arrays.asList(words).contains("or")){
            int ind=0;
            for (int i = 0; i<=words.length; i=i+2){
                if (index.get(words[i]) != null){
                    res = index.get(words[i]);
                    ind = i;
                    break;
                }
            }
            for (int i=ind+2; i<=words.length; i=i+2)
                if (index.get(words[i]) != null)
                    res = getUnion(res,index.get(words[i]));
            
        }

        if(Arrays.asList(words).contains("and")){
            for (int i = 0; i<=words.length; i=i+2)
                if (index.get(words[i]) == null)
                    return res;
            res = index.get(words[0]);
            for (int i = 2; i<=words.length; i=i+2)
                res = getIntersection(res,index.get(words[i]));
                
        }
        return res;
    }

    public static void main(String[] args) throws FileNotFoundException,IOException,ClassNotFoundException {
        String pathfolder = "collection";
        File file = new File("index.out");        
        InvertedIndex invIndex;
        
//        System.out.printf("%7s%-150s%10s\n", "+--------",
//        "+-------------------------------------------------------------------------------------------------------------------------------------------------------+",
//        "-----------+");
//        System.out.printf("|%7s | %-150s|%10s |\n", "docID", "file", "indexsize");
//        System.out.printf("%7s%-150s%10s\n", "+--------",
//        "+-------------------------------------------------------------------------------------------------------------------------------------------------------+",
//        "-----------+");
        
        if (file.exists()){
            FileInputStream fin = new FileInputStream("index.out");
            ObjectInputStream fin2 = new ObjectInputStream(fin);
            invIndex = (InvertedIndex) fin2.readObject();
            invIndex.indexCollection(pathfolder);
        }
        else {
            FileOutputStream fout = new FileOutputStream("index.out");
            ObjectOutputStream fout2 = new ObjectOutputStream(fout);
            invIndex = new InvertedIndex();
            
            invIndex.indexCollection(pathfolder);
            fout2.writeObject(invIndex);
            fout2.flush();
            fout2.close();
        }
        
//        System.out.printf("%7s%-150s%10s\n", "+--------",
//        "+-------------------------------------------------------------------------------------------------------------------------------------------------------+",
//        "-----------+");
//        

      
        System.out.print("Brutus ");
        System.out.println(invIndex.executeQuery("Brutus"));

        System.out.print("Caesar ");
        System.out.println(invIndex.executeQuery("Caesar"));
        
        System.out.print("Calpurnia ");
        System.out.println(invIndex.executeQuery("Calpurnia"));

        System.out.print("Brutus AND Brutus ");
        System.out.println(invIndex.executeQuery("Brutus AND Brutus"));

        System.out.print("Brutus AND Caesar ");
        System.out.println(invIndex.executeQuery("Brutus AND Caesar"));

        System.out.print("Brutus AND Caesar AND Calpurnia ");
        System.out.println(invIndex.executeQuery("Brutus AND Caesar AND Calpurnia"));

        System.out.print("Brutus OR Brutus ");
        System.out.println(invIndex.executeQuery("Brutus OR Brutus"));
        
        System.out.print("Brutus OR Caesar ");
        System.out.println(invIndex.executeQuery("Brutus OR Caesar"));

        System.out.print("Brutus OR Caesar OR Calpurnia ");
        System.out.println(invIndex.executeQuery("Brutus OR Caesar OR Calpurnia"));

        System.out.print("SpiderMan ");
        System.out.println(invIndex.executeQuery("SpiderMan"));

        System.out.print("Brutus AND SpiderMan ");
        System.out.println(invIndex.executeQuery("Brutus AND SpiderMan"));

        System.out.print("Caesar OR SpiderMan ");
        System.out.println(invIndex.executeQuery("Caesar OR SpiderMan"));

        
        
    }
}


