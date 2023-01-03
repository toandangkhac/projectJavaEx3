/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.javada;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author ezernt
 */


public class Doc {
    private int DocID =0;
    private String terms = null;
    private boolean title = false;
    private boolean body = false;
    private int count = 0;
    
    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }
    public Doc() {
        
    }
    
    public int getDocID() {
        return DocID;
    }

    public String getTerms() {
        return terms;
    }

    public boolean isTitle() {
        return title;
    }

    public boolean isBody() {
        return body;
    }

    public void setDocID(int DocID) {
        this.DocID = DocID;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    public void setTitle(boolean title) {
        this.title = title;
    }

    public void setBody(boolean body) {
        this.body = body;
    }

    
        public void readDoc(String term,int DocId) throws FileNotFoundException{
        String title1 = " ";
        String body1 = " ";
        File f = new File("");
        String urlf = f.getAbsolutePath();
         String url = urlf+"\\src\\main\\java\\com\\mycompany\\javada\\document\\"+String.valueOf(DocId)+".txt";
        // Đọc dữ liệu từ body File với Scanner
        FileInputStream fileInputStream = new FileInputStream(url);
        Scanner scanner = new Scanner(fileInputStream);

        try {
            title1 = title1 + scanner.nextLine().toLowerCase();
            while (scanner.hasNextLine()) {
               //body1 = body1.concat(scanner.nextLine().toLowerCase());
               body1 = body1+scanner.nextLine()+" ";
            }
        } finally {
            try {
                scanner.close();
                fileInputStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        title1 = title1.replaceAll("\\p{Punct}", " ").toLowerCase();
        body1 = body1.replaceAll("\\p{Punct}", " ").toLowerCase();
        this.setDocID(DocId);
        this.setTerms(term);
        String t = title1+' '+body1;
            
        String[] num = t.split(term);
        this.setCount(num.length-1);
        if(title1.contains(term)==true) this.setTitle(true);
        if(body1.contains(term)== true) this.setBody(true);
    }
}
