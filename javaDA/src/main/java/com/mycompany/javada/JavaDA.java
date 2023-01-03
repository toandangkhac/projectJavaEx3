/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package com.mycompany.javada;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author ezernt
 */
public class JavaDA {
        int[] DocIDArr={1,2,3,4,5};
    float a= (float) 0.2, b = (float) 0.8;
    float g[] = {a,b};
    Doc DocID = new Doc();
    private DefaultTableModel modeDocId1;
    private DefaultTableModel modeDocId2;
    private DefaultTableModel modeScores;
    LinkedList<Doc> q1 = new LinkedList<>();
    LinkedList<Doc> q2 = new LinkedList<>();
    
    static Map<Integer, Float> scores = new LinkedHashMap<>();
    
        public void Sort(int[] key, float[] value, int left,int right){
        int pivot = left, L ,R;
        float x;
         int temp1;
        float temp2;      
        x=value[pivot]; L=left; R = right;
        while(L<R){
            while(value[L]<x&&(L<right)){ L++;}
            while((value[R]>x)&&(R>left)){R--;}
            if(L<=R){
                if(L<R) {
                     temp2 = value[L];
                    value[L] = value[R];
                    value[R]=temp2;
                    temp1=key[L];
                    key[L]=key[R];
                    key[R]=temp1;
                }
                L++;R--;
            }
            
        }
        if(left<R) Sort(key,value,left,R);
        if(L<right) Sort(key,value,L,right);
         for(int iS=0;iS<key.length;iS++){
                    scores.put(key[iS],value[iS]);
                }
    }
    public static void main(String[] args) {
        float a = (float) 0.01;
//        for (int i = 0; i < 10; i++) {
//            a = (float)(100-i);
//            scores.put(i, a);
//        }
        scores.put(1, (float)1);
        scores.put(5, (float)3);
        scores.put(2, (float)1);
        scores.put(3, (float)1);
        scores.put(1, (float)20);
        scores.put(4, Float.NaN);
        
        for (Entry<Integer, Float> entry : scores.entrySet()) {
                //scores.remove(entry.getKey());
                System.out.println(entry.getKey());
        }
        System.out.println(scores);
 
// Creating an ArrayList Of Entry objects

    }     
}
