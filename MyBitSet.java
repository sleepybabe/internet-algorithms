package bit;

import java.io.Serializable;
import java.util.*;

public class MyBitSet implements Serializable {
    
    private int lengthOfBitSet;
    private int lastDocId;
    public int length;
    public BitSet bit;
    private BitEncoding bitencoding = new BitEncoding();
    
    public MyBitSet(int id){
        this.lastDocId = id;
        List<Integer> res = new LinkedList();
        res.add(id);
        this.bit = (bitencoding.listBitEncode(res));
//        System.out.print("LOOOOOK ");
//        System.out.println(this.bit);
        this.length = 1;
        this.lengthOfBitSet = bitencoding.lengthOfBitSet;  
    }

    public void AddDocId(int id){
        if(this.lastDocId != id){
            
            int dif = id - this.lastDocId;
            System.out.printf("%d %d",this.lastDocId,dif);
//            System.out.printf("%d, %d\n",lastDocId,dif);
            this.lastDocId = id;
           
            
            System.out.print(this.bit);
            LinkedList<Integer> res = new LinkedList<>();
//            res = bitencoding.bitDecode(this);
            
            res.add(dif);
            this.length++;
//            System.out.println(res);
//            this.bit = bitencoding.listBitEncode(res);
//            System.out.println(bitencoding.bitDecode(this));
            BitSet bitset = new BitSet();
            bitset = bitencoding.listBitEncode(res);
//            bitencoding.length++;
            System.out.print(bitset);
            this.lengthOfBitSet = bitencoding.lengthOfBitSet + this.lengthOfBitSet; 
            System.out.printf("%d %d",bitencoding.lengthOfBitSet, this.lengthOfBitSet);
            int tmp = 0;
            for(int i = bitencoding.lengthOfBitSet-1; i<this.lengthOfBitSet; i++){
                if(bitset.nextSetBit(tmp)==-1)
                    break;
                this.bit.set(bitset.nextSetBit(tmp)+i);
                tmp++;
            }
            System.out.println(this.bit);
            
        }
    }
    
    public BitSet getBitSet(){
        return this.bit;
    }
}
