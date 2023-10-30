package bit;

import java.io.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;


public class BitEncoding implements Serializable {
    
    public int length;
    public int lengthOfBitSet;
    
    public BitEncoding(){
        this.length = 0;
    }
    public List<Integer> toIntervals(List<Integer> list){
        this.length = list.size();
        if(list.contains(0)){
            list.clear();
            return list;
        }
        
        
        for(int i=list.size()-1; i>0; i--){  //преобразование списка cловопозиций в список интервалов
            list.set(i, list.get(i)-list.get(i-1));
        }
        
        return list;
    }
    
    public List<Integer> toDocIds(List<Integer> list){
        for(int i=1; i<list.size(); i++){
            list.set(i, list.get(i)+list.get(i-1));
//            System.out.printf("%d and %d;   ",list.get(i),list.get(i-1));
        }
       
        return list;
    }
    
    public BitSet listBitEncode(List<Integer> list){ 
//        toIntervals(list);
//        this.length = list.size();
        BitSet bit = new BitSet();
        if(list.isEmpty())
            return bit;
//        if(this.length ==0)
//            return bit;
        String binaryNumber;
        int unarSize;
        char[] tmp;
        this.lengthOfBitSet = 0;
        for(int i=0; i<list.size(); i++){
            binaryNumber = Integer.toBinaryString(list.get(i));
            binaryNumber = binaryNumber.substring(1); //перевод числа в бинарную СС, удаляя первую единицу
//            System.out.printf("%s ;     ",binaryNumber);
            unarSize = binaryNumber.length();
            tmp = new char[unarSize]; //унарный код из единиц по длину бинарного числа 
            binaryNumber = new String(tmp).replace("\0","1") + "0" + binaryNumber; //объединение бинарного числа и унарного кода
            System.out.printf("binary: %s",binaryNumber);
            for(int j=0; j<binaryNumber.length(); j++){ // полученный код вставляем в последовательность типа BitSet
                if (binaryNumber.charAt(j)=='1')
                    bit.set(this.lengthOfBitSet, this.lengthOfBitSet+1, true);
                else 
                    bit.set(this.lengthOfBitSet,this.lengthOfBitSet+1,false);
                this.lengthOfBitSet++;
            }
        }    
        
        return bit;
    }
    
    public LinkedList<Integer> bitDecode(MyBitSet list){
        this.length = list.length;
        BitSet list2 = list.getBitSet();
//        System.out.println(list2);
        LinkedList<Integer> res = new LinkedList<>();
        if(list2.isEmpty() && this.length==0)
            return res;
        int unarSize = 0;
        String number = "";
        int start = list2.nextClearBit(0)+1; //начало числа в бинарной СС
        unarSize = start - unarSize - 1; //длина этого числа
        
        while(true){
            number+="1";
            for(int i=start; i<start+unarSize; i++){ //выделение числа из последовательности BitSet
                if(list2.get(i))
                    number+="1";
                else
                    number+="0";
            }
//            System.out.println(number);
            res.add(Integer.parseInt(number,2));
//            System.out.println(res.get(res.size()-1));
            if(res.size()==length)
                break;
            unarSize = start;
            start = list2.nextClearBit(start+number.length()-1)+1;
            unarSize = start - unarSize - number.length();
            number="";
            
        }
//        System.out.println(res);
        toDocIds(res);
        return res;
    }
    
}
