import org.apache.commons.validator.routines.UrlValidator;
import org.tartarus.snowball.ext.*;
import java.util. *;
import java.io.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.net.*;

public class InvertedIndex implements Serializable {
    
    private List<String> documents;
    private Map<String, LinkedList<Integer>> index;
    private String stopFile;
    private Set<String> stopwords;
    private englishStemmer stemmer = new englishStemmer();
    
//    public InvertedIndex(){
//        documents = new ArrayList<String>();
//        index = new HashMap<>();
//    }
    
    public InvertedIndex(String stopFile) throws FileNotFoundException{
        this.documents = new ArrayList<String>();
        this.index = new HashMap<>();
        this.stopFile = stopFile;
        this.stopwords = new HashSet<String>();
        
        File stop = new File(this.stopFile);
        Scanner instop = new Scanner(stop);
        String line;
        while (instop.hasNextLine()){
                line = instop.nextLine();
                stopwords.add(line);
           }
        
    }
    
    private void indexCycle(String line, int id){
        LinkedList<Integer> count;
        line = line.toLowerCase();
        String[] words = (line.split("\\W+"));
        
        
        for (int i = 0; i<words.length; i++){
            if(stopwords.contains(words[i]))
                continue;
            
            stemmer.setCurrent(words[i]);
            stemmer.stem();
            String word = stemmer.getCurrent();
            
            count = new LinkedList<Integer>();
            if (index.containsKey(word)){
                count = index.get(word); 
                if (count.getLast()!=id)
                    count.add(id);
            }
            else 
                count.add(id);
            index.put(word,count);
            }
    }
    
    public void indexDocument(String path) throws FileNotFoundException, IOException {
        if (documents.indexOf(path)!= -1)
            return;
        documents.add(path);
        int id = documents.size()-1;        
        String line="";
        Scanner infile;
        File file;
        UrlValidator urlVal= new UrlValidator();

        if(urlVal.isValid(path)){
            Document doc = Jsoup.connect(path).get();
            line = doc.body().text();
            indexCycle(line,id);
            System.out.printf("|%7d | %-150s|%10d |\n", id, path, index.size());
            return;
        }
        
        else{
            file = new File(path);
            path = file.getAbsolutePath();
            file = new File(path);
            infile = new Scanner(file);
        }
        
        String mimeType = URLConnection.guessContentTypeFromName(file.getName());
        
        if(mimeType.equals("text/plain")){
           while (infile.hasNextLine()){
                line = infile.nextLine();
                indexCycle(line,id);
           }
        }
        else if(mimeType.equals("text/html")){
            Document doc = Jsoup.parse(file, "UTF-8");
            line = doc.body().text();
            indexCycle(line,id);
        }
        System.out.printf("|%7d | %-150s|%10d |\n", id, path, index.size());
    }
        
    

    public void indexCollection(String folder) throws FileNotFoundException, IOException{
        UrlValidator urlVal= new UrlValidator();
        if(urlVal.isValid(folder)){
            indexDocument(folder);
        }
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

    public LinkedList<Integer> executeQuery(String query) throws FileNotFoundException{
        LinkedList<Integer> res = new LinkedList<Integer>();
        query = query.toLowerCase();
        
        String [] words = query.split(" ");
        String word;
        
        if (words.length == 1){
            if(stopwords.contains(words[0]))
                return res;
            stemmer.setCurrent(words[0]);
            stemmer.stem();
            word = stemmer.getCurrent();
            if (index.get(word)!=null)
                res = index.get(word);
        }
        
        if(Arrays.asList(words).contains("or")){
            int ind=-1;
            for (int i = 0; i<=words.length; i=i+2){
                if(stopwords.contains(words[i]))
                    continue;
                stemmer.setCurrent(words[i]);
                stemmer.stem();
                word = stemmer.getCurrent();
                
                if (index.get(word) != null){
                    res = index.get(word);
                    ind = i;
                    break;
                }
            }
            if(ind==-1){
                return res;
            }
            for (int i=ind+2; i<=words.length; i=i+2){
                if(stopwords.contains(words[i]))
                    continue;
                stemmer.setCurrent(words[i]);
                stemmer.stem();
                word = stemmer.getCurrent();
                
                if (index.get(word) != null)
                    res = getUnion(res,index.get(word));
            }
            
        }

        if(Arrays.asList(words).contains("and")){
            int ind=-1;
            for (int i = 0; i<words.length; i=i+2){
                if(stopwords.contains(words[i]))
                    continue;
                stemmer.setCurrent(words[i]);
                stemmer.stem();
                word = stemmer.getCurrent();
                
                if (ind==-1)
                    ind=i;
                if (index.get(word) == null)
                    return res;
            }
            if(ind>=0){
                stemmer.setCurrent(words[ind]);
                stemmer.stem();
                word = stemmer.getCurrent();
                res = index.get(word);
            }
            else{
                return res;
            }
            if(ind != words.length-1)
                for (int i =ind + 2; i<words.length; i=i+2){
                    if(stopwords.contains(words[i]))
                        continue;
                    stemmer.setCurrent(words[i]);
                    stemmer.stem();
                    word = stemmer.getCurrent(); 
                    res = getIntersection(res,index.get(word));
                }  
        } 
        return res;
    }

    public static void main(String[] args) throws FileNotFoundException,IOException,ClassNotFoundException {
        String pathTEXT = "collection";
        String pathHTML = "collection_html";
        String pathURL = "https://isu.ru/en/about_irkutsk/";
        String stopFile = "stop_words.txt";
        File file = new File("index.out");        
        InvertedIndex invIndex;

        if (file.exists()){
            FileInputStream fin = new FileInputStream("index.out");
            ObjectInputStream fin2 = new ObjectInputStream(fin);
            invIndex = (InvertedIndex) fin2.readObject();
            invIndex.indexCollection(pathTEXT);
        }
        else{
            FileOutputStream fout = new FileOutputStream("index.out");
            ObjectOutputStream fout2 = new ObjectOutputStream(fout);
            invIndex = new InvertedIndex(stopFile);
            invIndex.indexCollection(pathTEXT);
            fout2.writeObject(invIndex);
            fout2.flush();
            fout2.close();
        }


//        System.out.printf("%7s%-150s%10s\n", "+--------",
//        "+-------------------------------------------------------------------------------------------------------------------------------------------------------+",
//        "-----------+");
//        

      
        System.out.print("dresses ");
        System.out.println(invIndex.executeQuery("dresses"));
        
        System.out.print("dress ");
        System.out.println(invIndex.executeQuery("dress"));

        System.out.print("he ");
        System.out.println(invIndex.executeQuery("he"));
        
        System.out.print("Calpurnia ");
        System.out.println(invIndex.executeQuery("Calpurnia"));
        
        System.out.print("Brutus ");
        System.out.println(invIndex.executeQuery("Brutus"));
        
        ////
        System.out.print("Brutus AND Caesar ");
        System.out.println(invIndex.executeQuery("Brutus AND Caesar"));
        
        System.out.print("his AND your AND you ");
        System.out.println(invIndex.executeQuery("his AND your AND you"));
        
        System.out.print("dress AND you AND his AND Caesar");
        System.out.println(invIndex.executeQuery("dress AND you AND his AND Caesar"));
        
        System.out.print("he AND Brutus AND yourself AND Caesar");
        System.out.println(invIndex.executeQuery("he AND Brutus AND yourself AND Caesar"));
        ////
        
        System.out.print("Brutus OR Caesar ");
        System.out.println(invIndex.executeQuery("Brutus OR Caesar "));

        System.out.print("your OR she OR he ");
        System.out.println(invIndex.executeQuery("your OR she OR he"));

        System.out.print("Brutus OR dress OR Caesar OR you");
        System.out.println(invIndex.executeQuery("Brutus OR dress OR Caesar OR you"));
        
        System.out.print("she OR dresses OR Caesar OR Brutus OR Brutus ");
        System.out.println(invIndex.executeQuery("she OR dresses OR Caesar OR Brutus OR Brutus"));

        
    }
}


