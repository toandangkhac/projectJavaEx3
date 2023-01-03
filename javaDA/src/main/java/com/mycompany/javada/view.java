/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.javada;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author ezernt
 */
class dataMAP {

    String query = "";
    List<Integer> docID = new ArrayList<>();
    List<Float> score = new ArrayList<>();
    List<Boolean> relevant = new ArrayList<>();
    List<Float> precision = new ArrayList<>();

    public dataMAP(String IN) {
        this.query = IN;
    }

    public void updatePrecision() {
        int countRelevantTrue = 0;
        for (int i = 0; i < relevant.size(); i++) {
            if (relevant.get(i)) {
                countRelevantTrue++;
            }
            precision.set(i, (float) countRelevantTrue / (i + 1));
            //System.out.println(precision.get(i));
        }
    }

    public void changeRelevant(int index) {
        relevant.set(index, !relevant.get(index));
    }

    public float ComputeAveragePrecision() {
        int count = 0;
        float result = 0;
        for (int i = 0; i < relevant.size(); i++) {
            if (relevant.get(i)) {
                count++;
                result += precision.get(i);
            }
        }
        if (count == 0) {
            count = 1;
        }
        //System.out.println(result / count);
        return result / count;

    }
}

public final class view extends javax.swing.JFrame {

    //int[] DocIDArr = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34};
    int[] DocIDArr = new int[34];
    float a = (float) 0.2, b = (float) 0.8;
    float g[] = {a, b};
    Doc DocID = new Doc();
    private DefaultTableModel modeDocId1;
    private DefaultTableModel modeDocId2;
    private DefaultTableModel modeScores;
    private DefaultTableModel modeMAP;
    private DefaultTableModel modeDCG1;
    private DefaultTableModel modeDCG2;
    LinkedList<Doc> q1 = new LinkedList<>();
    LinkedList<Doc> q2 = new LinkedList<>();
    LinkedList<LinkedList<Doc>> query1 = new LinkedList<>();//COSINSCORE
    LinkedList<LinkedList<Doc>> query2 = new LinkedList<>();//PRECISION and RECALL
    LinkedList<LinkedList<Doc>> query3 = new LinkedList<>();//F MEARSURE
    LinkedList<LinkedList<Doc>> query4 = new LinkedList<>();//MAP
    LinkedList<LinkedList<Doc>> query5 = new LinkedList<>();//NDCG
    static Map<Integer, Float> scores = new HashMap<>();
    static Map<Integer, Float> relevantVal = new LinkedHashMap<>();
    static Map<Integer, Float> relevantValUserRate = new LinkedHashMap<>();
    static Map<Integer, Float> scoreSort = new LinkedHashMap<>();
    Map<Integer, Float> length = new HashMap<>();
    List<String> NQuery = new ArrayList<>();
    List<dataMAP> MAP = new ArrayList<>();
    File f = new File("");
    String urlf = f.getAbsolutePath();

    /**
     * @param term
     * @param q
     * @param
     * @throws java.io.FileNotFoundException
     */
    public void InitQuery1(String term, LinkedList<Doc> q) throws FileNotFoundException {
        q.removeAll(q);
        for (int i = 0; i < DocIDArr.length; i++) {
            Doc DocId = new Doc();
            DocId.readDoc(term, DocIDArr[i]);
            if (DocId.isBody() == true || DocId.isTitle() == true) {
                q.add(DocId);
            }
        }
    }

    public void InitScores(Map<Integer, Float> obj) {
        for (int i = 0; i < DocIDArr.length; i++) {
            obj.put(DocIDArr[i], (float) 0);
        }
    }

    public void InitQuery2(Map<String, Integer> temps, LinkedList<LinkedList<Doc>> queryIN) throws FileNotFoundException {
        for (Map.Entry<String, Integer> entry : temps.entrySet()) {

            LinkedList<Doc> q = new LinkedList<Doc>();
            for (int i = 0; i < DocIDArr.length; i++) {
                Doc doc1 = new Doc();
                try {
                    doc1.readDoc(entry.getKey(), DocIDArr[i]);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if (doc1.getCount() != 0) {
                    q.add(doc1);
                }
            }
            queryIN.add(q);
        }
    }

    // chua chac dung
    public float WEIGHTEDZONE(Doc p1, Doc p2) {
        float s1, s2;
        if (p1.isTitle() && p2.isTitle()) {
            s1 = (float) 1;
        } else {
            s1 = 0;
        }
        if (p1.isBody() && p2.isBody()) {
            s2 = (float) 1;
        } else {
            s2 = 0;
        }
        return (s1 * g[0] + s2 * g[1]);
    }

    public void ZONESCORE() {
        Doc docbf = new Doc();
        q1.add(docbf);
        q2.add(docbf);
        int iq1 = 0, iq2 = 0;
        Doc p1 = q1.get(iq1);
        Doc p2 = q2.get(iq2);
        while ((p1.getDocID() != 0 && p2.getDocID() != 0)) {
            if (p1.getDocID() == p2.getDocID()) {
                scores.replace(p1.getDocID(), WEIGHTEDZONE(p1, p2));
                p1 = q1.get(++iq1);
                p2 = q2.get(++iq2);
            } else {
                if (p1.getDocID() < p2.getDocID()) {
                    p1 = q1.get(++iq1);
                } else {
                    p2 = q2.get(++iq2);
                }
            }
        }
        q1.remove(q1.size() - 1);
        q2.remove(q2.size() - 1);
    }

    public void COSINESCORE(Map<String, Integer> temps, LinkedList<LinkedList<Doc>> queryIN) {
        float[] wtq = new float[queryIN.size()];
        float[][] wtd = new float[queryIN.size()][DocIDArr.length];
        float temp1 = 0;
        float lengthQuery = 0;

        for (LinkedList<Doc> term : queryIN) {
            if (!term.isEmpty()) {
                lengthQuery += Math.pow(temps.get(term.get(0).getTerms()), 2);
            }
        }
        int i = 0;
        for (LinkedList<Doc> term : queryIN) {
            if (!term.isEmpty()) {
                //wtq[i] = (float) Math.log10((float)(10 / term.size()));
                //normalization length
                wtq[i] = (float) (temps.get(term.get(0).getTerms()) / Math.sqrt(lengthQuery));
                //wtq[i] = (float) (temps.get(term.get(0).getTerms()) / Math.sqrt(lengthQuery));

                for (Doc d : term) {
                    //tftd
                    wtd[i][d.getDocID() - 1] = d.getCount();
                    //wtd[i][d.getDocID() - 1] = (float) (1 + Math.log10(wtd[i][d.getDocID() - 1]));
                    //chua normalization length
                    //System.out.print(wtd[i][d.getDocID() - 1]+" " +i + String.valueOf(d.getDocID() - 1) + "\t");
                }
                System.out.println();
            }
            i++;

        }

        for (LinkedList<Doc> term : queryIN) {
            for (Doc d : term) {
                temp1 = (float) (length.get(d.getDocID()) + Math.pow(d.getCount(), 2));
                length.replace(d.getDocID(), temp1);
            }
        }

        for (i = 0; i < queryIN.size(); i++) {
            for (int j = 0; j < DocIDArr.length; j++) {
                wtd[i][j] = (float) (wtd[i][j] / (Math.sqrt(length.get(DocIDArr[j]))));
                System.out.print(wtd[i][j] + " " + i + String.valueOf(j + 1) + "\t");
                scores.replace(DocIDArr[j], scores.get(DocIDArr[j]) + wtd[i][j] * wtq[i]);
            }
            System.out.println();
        }
    }

    /**
     * Creates new form view view
     */
    public view() {
        for (int i = 0; i < 34; i++) {
            DocIDArr[i]=i+1;
        }
        initComponents();        
        this.setLocationRelativeTo(null);
        this.jText1.setText("");
        this.jText2.setText("");
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel16 = new javax.swing.JLabel();
        jScrollPane9 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        buttonGroup1 = new javax.swing.ButtonGroup();
        tab1 = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        txtKeyword1 = new javax.swing.JTextField();
        btnSearch1 = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        tblScore4 = new javax.swing.JTable();
        jLabel19 = new javax.swing.JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        txtArea2 = new javax.swing.JTextArea();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        btnUseful1 = new javax.swing.JButton();
        btnUnUseful1 = new javax.swing.JButton();
        jScrollPane10 = new javax.swing.JScrollPane();
        tblRelevant = new javax.swing.JTable();
        jLabel23 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        txtPrecision1 = new javax.swing.JLabel();
        txtRecall1 = new javax.swing.JLabel();
        txtFMeasure = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        txtT2 = new javax.swing.JTextField();
        ButT2 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblScores2 = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        jK = new javax.swing.JTextField();
        txtTermsRetrieved = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        btnSearch = new javax.swing.JButton();
        txtKeyword = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtNumberOfSearchResult = new javax.swing.JTextField();
        jScrollPane5 = new javax.swing.JScrollPane();
        tblScore3 = new javax.swing.JTable();
        jScrollPane6 = new javax.swing.JScrollPane();
        txtArea1 = new javax.swing.JTextArea();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        btnUseful = new javax.swing.JButton();
        btnUnUseful = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        txtPrecision = new javax.swing.JLabel();
        txtRecall = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        InputMAP = new javax.swing.JTextField();
        ButAddQuery = new javax.swing.JButton();
        ComboBoxMAP = new javax.swing.JComboBox<>();
        jLabel24 = new javax.swing.JLabel();
        jScrollPane11 = new javax.swing.JScrollPane();
        tblMAP = new javax.swing.JTable();
        jLabel26 = new javax.swing.JLabel();
        jScrollPane12 = new javax.swing.JScrollPane();
        txtAreaMAP = new javax.swing.JTextArea();
        ButtonSMap = new javax.swing.JButton();
        btnMAP = new javax.swing.JButton();
        jLMAP = new javax.swing.JLabel();
        RESETMAP = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jText1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jText2 = new javax.swing.JTextField();
        Start = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblDocID1 = new javax.swing.JTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDocID2 = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblScores = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel29 = new javax.swing.JLabel();
        txtNDCG = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jScrollPane14 = new javax.swing.JScrollPane();
        NDCGtbl1 = new javax.swing.JTable();
        ttNDCG2 = new javax.swing.JLabel();
        ttNDCG1 = new javax.swing.JLabel();
        jScrollPane15 = new javax.swing.JScrollPane();
        DCGtbl2 = new javax.swing.JTable();
        jLabel30 = new javax.swing.JLabel();
        jRadioB0 = new javax.swing.JRadioButton();
        jRadioB2 = new javax.swing.JRadioButton();
        jRadioB1 = new javax.swing.JRadioButton();
        jRadioB3 = new javax.swing.JRadioButton();
        jRadioB4 = new javax.swing.JRadioButton();
        jButton2 = new javax.swing.JButton();
        labelNdcg = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        txtKeyword2 = new javax.swing.JTextField();
        tbnSearch2 = new javax.swing.JButton();
        jScrollPane13 = new javax.swing.JScrollPane();
        tblScoreUser1 = new javax.swing.JTable();
        jScrollPane16 = new javax.swing.JScrollPane();
        txtArea3 = new javax.swing.JTextArea();
        jLabel32 = new javax.swing.JLabel();
        jScrollPane17 = new javax.swing.JScrollPane();
        tblAssessingRelevance = new javax.swing.JTable();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        tbnComputeKappa = new javax.swing.JButton();
        jLabel36 = new javax.swing.JLabel();
        txtPA = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        txtPE = new javax.swing.JLabel();
        txtKappa = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();

        jLabel16.setText("jLabel16");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane9.setViewportView(jTable1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        tab1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel17.setText("Keyword:");

        btnSearch1.setText("Search");
        btnSearch1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearch1ActionPerformed(evt);
            }
        });

        jLabel18.setText("Result:");

        tblScore4.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "DocID", "Score", "Relevant", "Retrieved"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Float.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblScore4.setColumnSelectionAllowed(true);
        tblScore4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblScore4MouseClicked(evt);
            }
        });
        jScrollPane7.setViewportView(tblScore4);
        tblScore4.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        if (tblScore4.getColumnModel().getColumnCount() > 0) {
            tblScore4.getColumnModel().getColumn(0).setPreferredWidth(10);
            tblScore4.getColumnModel().getColumn(1).setPreferredWidth(10);
            tblScore4.getColumnModel().getColumn(2).setPreferredWidth(30);
            tblScore4.getColumnModel().getColumn(3).setPreferredWidth(30);
        }

        jLabel19.setText("Content :");

        txtArea2.setEditable(false);
        txtArea2.setColumns(20);
        txtArea2.setRows(5);
        jScrollPane8.setViewportView(txtArea2);

        jLabel20.setFont(new java.awt.Font("Segoe UI Symbol", 3, 18)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(102, 255, 102));
        jLabel20.setText("Give feedback:");

        jLabel21.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(0, 246, 245));
        jLabel21.setText("Was this resource helpful?");

        btnUseful1.setText("Useful");
        btnUseful1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUseful1ActionPerformed(evt);
            }
        });

        btnUnUseful1.setText("UnUseFul");
        btnUnUseful1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUnUseful1ActionPerformed(evt);
            }
        });

        tblRelevant.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Retrieved", "0", "0"},
                {"Not retrieved", "0", "0"},
                {null, null, null}
            },
            new String [] {
                "", "Relevant", "Nonrelevant"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane10.setViewportView(tblRelevant);

        jLabel23.setText("P = ");

        jLabel25.setText("R = ");

        jLabel27.setText("F");

        jLabel28.setText("β=1");

        txtPrecision1.setText("????");

        txtRecall1.setText("????");

        txtFMeasure.setText("????");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(txtKeyword1, javax.swing.GroupLayout.PREFERRED_SIZE, 263, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnSearch1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addGap(24, 24, 24)
                                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel4Layout.createSequentialGroup()
                                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jLabel23)
                                                    .addComponent(jLabel25))
                                                .addGap(67, 67, 67)
                                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                    .addComponent(txtRecall1)
                                                    .addComponent(txtPrecision1)))
                                            .addGroup(jPanel4Layout.createSequentialGroup()
                                                .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jLabel28)
                                                .addGap(48, 48, 48)
                                                .addComponent(txtFMeasure)))))
                                .addGap(62, 62, 62)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addComponent(jLabel21))
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addGap(114, 114, 114)
                                        .addComponent(btnUseful1)
                                        .addGap(47, 47, 47)
                                        .addComponent(btnUnUseful1))))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 296, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel19))))
                        .addContainerGap(334, Short.MAX_VALUE))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel17)
                .addGap(9, 9, 9)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtKeyword1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch1))
                .addGap(26, 26, 26)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane8)))
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(52, 52, 52)
                        .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel21)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnUseful1)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                .addComponent(btnUnUseful1)
                                .addGap(9, 9, 9))))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel23)
                            .addComponent(txtPrecision1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel25)
                            .addComponent(txtRecall1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel27)
                            .addComponent(jLabel28)
                            .addComponent(txtFMeasure))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tab1.addTab("F Mearsure", jPanel4);

        jPanel3.setBorder(new javax.swing.border.MatteBorder(null));

        jLabel4.setText("Input");

        txtT2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtT2MouseClicked(evt);
            }
        });
        txtT2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtT2ActionPerformed(evt);
            }
        });

        ButT2.setText("Go");
        ButT2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButT2ActionPerformed(evt);
            }
        });

        jLabel5.setText("Scores :");

        tblScores2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "        DocID", "         Score"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Float.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane4.setViewportView(tblScores2);

        jLabel6.setText("Input K");

        txtTermsRetrieved.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtTermsRetrieved.setText("Terms is used to retrieve: ");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addComponent(jLabel5))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 368, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jK, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtTermsRetrieved, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtT2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE))
                        .addGap(33, 33, 33)
                        .addComponent(ButT2)))
                .addContainerGap(484, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtT2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ButT2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jK, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(141, 141, 141)
                        .addComponent(jLabel5))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(44, 44, 44)
                        .addComponent(txtTermsRetrieved)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(354, Short.MAX_VALUE))
        );

        tab1.addTab("COSINESCORE", jPanel3);

        jLabel7.setText("Keyword:");

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        jLabel8.setText("Enter K:");
        jLabel8.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        tblScore3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "DocID", "Score", "Relevant"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Float.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblScore3.setColumnSelectionAllowed(true);
        tblScore3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblScore3MouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(tblScore3);
        tblScore3.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        txtArea1.setColumns(20);
        txtArea1.setRows(5);
        jScrollPane6.setViewportView(txtArea1);

        jLabel9.setText("Result:");

        jLabel10.setText("Content :");

        jLabel11.setText("(K is the number of search result)");

        jLabel12.setFont(new java.awt.Font("Segoe UI Symbol", 3, 18)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(102, 255, 102));
        jLabel12.setText("Give feedback:");

        jLabel13.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(0, 246, 245));
        jLabel13.setText("Was this resource helpful?");

        btnUseful.setText("Useful");
        btnUseful.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUsefulActionPerformed(evt);
            }
        });

        btnUnUseful.setText("UnUseful");
        btnUnUseful.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUnUsefulActionPerformed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(0, 255, 204));
        jLabel14.setText("Precision = ");

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(0, 255, 204));
        jLabel15.setText("Recall = ");

        txtPrecision.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtPrecision.setForeground(new java.awt.Color(0, 255, 204));
        txtPrecision.setText("???");

        txtRecall.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtRecall.setForeground(new java.awt.Color(0, 255, 204));
        txtRecall.setText("???");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnSearch)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(txtNumberOfSearchResult, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(txtKeyword, javax.swing.GroupLayout.PREFERRED_SIZE, 263, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(66, 66, 66)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel10)
                                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnUseful)
                                        .addGap(18, 18, 18)
                                        .addComponent(btnUnUseful)
                                        .addGap(23, 23, 23))))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jLabel15)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtRecall, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jLabel14)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtPrecision, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addGap(122, 122, 122)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addComponent(jLabel13)))
                                .addGap(131, 131, 131)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtKeyword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNumberOfSearchResult, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSearch)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(57, 57, 57)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnUseful)
                            .addComponent(btnUnUseful)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel14)
                                    .addComponent(txtPrecision))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel15)
                                    .addComponent(txtRecall))))))
                .addGap(209, 209, 209))
        );

        tab1.addTab("Precision & Recall", jPanel2);

        jLabel22.setText("Input");

        InputMAP.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                InputMAPMouseClicked(evt);
            }
        });

        ButAddQuery.setText("Add Query");
        ButAddQuery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButAddQueryActionPerformed(evt);
            }
        });

        ComboBoxMAP.setEditable(true);
        ComboBoxMAP.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                ComboBoxMAPComponentResized(evt);
            }
        });
        ComboBoxMAP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ComboBoxMAPActionPerformed(evt);
            }
        });

        jLabel24.setText("Result:");

        tblMAP.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "DocID", "Score", "Relevant", "Precision"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Float.class, java.lang.String.class, java.lang.Float.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblMAP.setColumnSelectionAllowed(true);
        tblMAP.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblMAPMouseClicked(evt);
            }
        });
        jScrollPane11.setViewportView(tblMAP);
        tblMAP.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        if (tblMAP.getColumnModel().getColumnCount() > 0) {
            tblMAP.getColumnModel().getColumn(0).setResizable(false);
            tblMAP.getColumnModel().getColumn(1).setResizable(false);
            tblMAP.getColumnModel().getColumn(3).setResizable(false);
        }

        jLabel26.setText("Content :");

        txtAreaMAP.setEditable(false);
        txtAreaMAP.setColumns(20);
        txtAreaMAP.setRows(5);
        jScrollPane12.setViewportView(txtAreaMAP);

        ButtonSMap.setText("Search");
        ButtonSMap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButtonSMapActionPerformed(evt);
            }
        });

        btnMAP.setText(" Compute MAP: ");
        btnMAP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMAPActionPerformed(evt);
            }
        });

        jLMAP.setText("Result Mean Average Precision");

        RESETMAP.setText("RESET");
        RESETMAP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RESETMAPActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(181, 181, 181))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                                    .addComponent(ComboBoxMAP, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(ButtonSMap)))
                            .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel26))
                        .addGap(299, 299, 299))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addComponent(InputMAP, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40)
                        .addComponent(ButAddQuery)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(85, 85, 85)
                .addComponent(btnMAP)
                .addGap(45, 45, 45)
                .addComponent(jLMAP, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(587, 587, 587))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(RESETMAP)
                .addGap(265, 265, 265))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(RESETMAP)
                .addGap(20, 20, 20)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel22)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(InputMAP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(ButAddQuery)))
                .addGap(97, 97, 97)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ComboBoxMAP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ButtonSMap))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel24)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel26)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(23, 23, 23)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnMAP)
                    .addComponent(jLMAP))
                .addContainerGap(166, Short.MAX_VALUE))
        );

        tab1.addTab(" MAP", jPanel5);

        jLabel1.setText("Terms 1");

        jText1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jText1MouseClicked(evt);
            }
        });

        jLabel2.setText("Terms 2");

        jText2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jText2MouseClicked(evt);
            }
        });

        Start.setText("Start");
        Start.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StartActionPerformed(evt);
            }
        });

        tblDocID1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "DocID", "Title ", "Body"
            }
        ));
        jScrollPane2.setViewportView(tblDocID1);

        tblDocID2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "DocID", "Title ", "Body"
            }
        ));
        jScrollPane1.setViewportView(tblDocID2);

        tblScores.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "        DocID", "         Score"
            }
        ));
        jScrollPane3.setViewportView(tblScores);

        jLabel3.setText("Scores: ");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(98, 98, 98)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(99, 99, 99)
                                .addComponent(jText1, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(107, 107, 107)
                                .addComponent(jText2, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(48, 48, 48)
                        .addComponent(Start))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(114, 114, 114)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(32, 32, 32)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 368, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(46, 46, 46)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(140, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jText1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jText2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addComponent(Start)))
                .addGap(41, 41, 41)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(81, Short.MAX_VALUE))
        );

        tab1.addTab("ZONESCORE", jPanel1);

        jLabel29.setText("Input");

        txtNDCG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNDCGActionPerformed(evt);
            }
        });

        jButton1.setText("Search");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        NDCGtbl1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "DocID", "DCG"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane14.setViewportView(NDCGtbl1);

        ttNDCG2.setText("Total DCG");

        ttNDCG1.setText("Total DCG");

        DCGtbl2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "DocID", "DCG", "Relevant"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Float.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        DCGtbl2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                DCGtbl2MouseClicked(evt);
            }
        });
        jScrollPane15.setViewportView(DCGtbl2);

        jLabel30.setText("user rate");

        buttonGroup1.add(jRadioB0);
        jRadioB0.setText("0 - non relevent");
        jRadioB0.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioB0ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioB2);
        jRadioB2.setText("2 - medium relevant");
        jRadioB2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioB2ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioB1);
        jRadioB1.setText("1 - low relevant");
        jRadioB1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioB1ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioB3);
        jRadioB3.setText("3 - relevant");
        jRadioB3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioB3ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioB4);
        jRadioB4.setText("4 - high relevant");
        jRadioB4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioB4ActionPerformed(evt);
            }
        });

        jButton2.setText(" CACULATE : ");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        labelNdcg.setText("NDCG VALUE");
        labelNdcg.setBorder(new javax.swing.border.MatteBorder(null));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel6Layout.createSequentialGroup()
                            .addGap(40, 40, 40)
                            .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(txtNDCG, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(53, 53, 53)
                            .addComponent(jButton1))
                        .addGroup(jPanel6Layout.createSequentialGroup()
                            .addGap(121, 121, 121)
                            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(ttNDCG2, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(labelNdcg, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel6Layout.createSequentialGroup()
                                    .addGap(51, 51, 51)
                                    .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel6Layout.createSequentialGroup()
                                    .addGap(145, 145, 145)
                                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jButton2)
                                        .addComponent(ttNDCG1, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane15, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(49, 49, 49)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jRadioB2)
                        .addComponent(jRadioB3, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jRadioB4, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(jRadioB0)
                    .addComponent(jRadioB1))
                .addContainerGap(356, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29)
                    .addComponent(txtNDCG, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 178, Short.MAX_VALUE)
                .addComponent(jLabel30)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane15, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(jScrollPane14, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(jRadioB0)
                        .addGap(18, 18, 18)
                        .addComponent(jRadioB1)
                        .addGap(18, 18, 18)
                        .addComponent(jRadioB2)
                        .addGap(18, 18, 18)
                        .addComponent(jRadioB3)
                        .addGap(18, 18, 18)
                        .addComponent(jRadioB4)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ttNDCG2)
                    .addComponent(ttNDCG1))
                .addGap(27, 27, 27)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(labelNdcg))
                .addGap(101, 101, 101))
        );

        tab1.addTab("NDCG", jPanel6);

        jLabel31.setText("Keyword");

        txtKeyword2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtKeyword2ActionPerformed(evt);
            }
        });

        tbnSearch2.setText("Search");
        tbnSearch2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbnSearch2ActionPerformed(evt);
            }
        });

        tblScoreUser1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "DocID", "User1", "User2"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblScoreUser1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblScoreUser1MouseClicked(evt);
            }
        });
        jScrollPane13.setViewportView(tblScoreUser1);

        txtArea3.setColumns(20);
        txtArea3.setRows(5);
        jScrollPane16.setViewportView(txtArea3);

        jLabel32.setText("Content :");

        tblAssessingRelevance.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Yes", null, null, null},
                {"No", null, null, null},
                {"Total", null, null, null}
            },
            new String [] {
                "", "Yes", "No", "Total"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane17.setViewportView(tblAssessingRelevance);

        jLabel33.setText("Judge 2 Relevant");

        jLabel34.setText("Judge 1");

        jLabel35.setText("Relevant");

        tbnComputeKappa.setText("Conpute");
        tbnComputeKappa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbnComputeKappaActionPerformed(evt);
            }
        });

        jLabel36.setText("P(A)=");

        txtPA.setText("???");

        jLabel38.setText("P(B)=");

        txtPE.setText("???");

        txtKappa.setText("???");

        jLabel41.setText("Kappa=");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(101, 101, 101)
                        .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(txtKeyword2, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(84, 84, 84)
                        .addComponent(tbnSearch2))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(49, 49, 49)
                        .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(72, 72, 72)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel32)
                            .addComponent(jScrollPane16, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(94, 94, 94)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel34)
                            .addComponent(jLabel35))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel33))
                        .addGap(85, 85, 85)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(tbnComputeKappa)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel36)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtPA))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel7Layout.createSequentialGroup()
                                        .addComponent(jLabel41)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(jLabel38)
                                        .addGap(30, 30, 30)))
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtKappa, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(txtPE, javax.swing.GroupLayout.Alignment.TRAILING))))))
                .addContainerGap(302, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel31)
                    .addComponent(txtKeyword2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tbnSearch2))
                .addGap(26, 26, 26)
                .addComponent(jLabel32)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane16, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 133, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel33)
                    .addComponent(tbnComputeKappa))
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(jLabel34)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel35))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel36)
                            .addComponent(txtPA))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel38)
                            .addComponent(txtPE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtKappa)
                            .addComponent(jLabel41))))
                .addGap(23, 23, 23))
        );

        tab1.addTab("Kappa", jPanel7);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tab1, javax.swing.GroupLayout.PREFERRED_SIZE, 770, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tab1, javax.swing.GroupLayout.PREFERRED_SIZE, 593, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void StartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StartActionPerformed
        // TODO add your handling code here:
        this.InitScores(scores);
        String term1 = "";
        String term2 = "";
        if (!this.jText1.getText().contains(" ") || !this.jText1.getText().contains(" ")) {
            if (!this.jText1.getText().equals("") && !this.jText2.getText().equals("")) {
                try {
                    term1 = " " + this.jText1.getText().toLowerCase().trim() + " ";
                    term2 = " " + this.jText2.getText().toLowerCase().trim() + " ";
                    this.InitQuery1(term1, q1);
                    this.InitQuery1(term2, q2);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(view.class.getName()).log(Level.SEVERE, null, ex);
                }
                modeDocId1 = (DefaultTableModel) tblDocID1.getModel();
                this.showData(q1, modeDocId1);
                modeDocId2 = (DefaultTableModel) tblDocID2.getModel();
                this.showData(q2, modeDocId2);
                this.ZONESCORE();
                modeScores = (DefaultTableModel) tblScores.getModel();
                this.showScores(scores, modeScores, scores.size());
            } else {
                JOptionPane.showMessageDialog(rootPane, "thông tin truyen vào bi thiêu", "ERROR", HEIGHT);
            }
        } else {

            JOptionPane.showMessageDialog(rootPane, "thông tin truyen vào chua dung", "ERROR", HEIGHT);
        }

    }//GEN-LAST:event_StartActionPerformed

    private void ButT2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButT2ActionPerformed
        // TODO a        query.removeAll(LinkedList<>);dd your handling code here:
        query1.clear();
        String tuTruyXuat = "";
        this.InitScores(scores);
        this.InitScores(length);
        scoreSort.clear();
        int K = Integer.valueOf(this.jK.getText());
        String input = this.txtT2.getText().toLowerCase().trim();
        input = input.replaceAll("\\p{Punct}", " ");
        while(input.contains("  ")) input=input.replaceAll("  ", " ");
        String[] terms = input.split(" ");
        for (int i = 0; i < terms.length; i++) {
            terms[i] = " " + terms[i] + " ";
        }
        Map<String, Integer> MapTerm = new HashMap<>();
        for (int i = 0; i < terms.length; i++) {
            if (!MapTerm.containsKey(terms[i])) {
                MapTerm.put(terms[i], 1);
            } else {
                MapTerm.put(terms[i], MapTerm.get(terms[i]) + 1);
            }

        }

        if ((!input.equals("")) && (K > 0)) {
            try {
                this.InitQuery2(MapTerm, query1);
                for (int i = 0; i < query1.size(); i++) {
                    if (!query1.get(i).isEmpty()) {
                        tuTruyXuat += query1.get(i).get(0).getTerms();
                    }

                }
                this.COSINESCORE(MapTerm, query1);
// Getting Collection of values from HashMap
                Collection<Float> values = scores.values();
                List<Float> list = new ArrayList<>(values);
                Collections.sort(list);
                Collections.reverse(list);

                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i) <= 1 && list.get(i) >= 0) {
                        for (Map.Entry<Integer, Float> entry : scores.entrySet()) {
                            if (entry.getValue() == list.get(i)) {
                                scoreSort.put(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                }
                modeScores = (DefaultTableModel) tblScores2.getModel();
                this.showScores(scoreSort, modeScores, K);
                txtTermsRetrieved.setText("Terms is used to retrieve: " + tuTruyXuat);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(view.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            JOptionPane.showMessageDialog(rootPane, "thông tin truyen vào bi thiêu", "ERROR", HEIGHT);
        }
    }//GEN-LAST:event_ButT2ActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        // TODO a        query.removeAll(LinkedList<>);dd your handling code here:
        query2.clear();
        this.InitScores(scores);
        this.InitScores(length);
        scoreSort.clear();
        int K = Integer.valueOf(this.txtNumberOfSearchResult.getText());
        String input = this.txtKeyword.getText().toLowerCase().trim();
        input = input.replaceAll("\\p{Punct}", " ");
        while(input.contains("  ")) input=input.replaceAll("  ", " ");
        String[] terms = input.split(" ");
        for (int i = 0; i < terms.length; i++) {
            terms[i] = " " + terms[i] + " ";
        }
        Map<String, Integer> MapTerm = new HashMap<>();
        for (int i = 0; i < terms.length; i++) {
            if (!MapTerm.containsKey(terms[i])) {
                MapTerm.put(terms[i], 1);
            } else {
                MapTerm.put(terms[i], MapTerm.get(terms[i]) + 1);
            }

        }

        if ((!input.equals("")) && (K > 0)) {
            try {

                this.InitQuery2(MapTerm, query2);
                this.COSINESCORE(MapTerm, query2);
// Getting Collection of values from HashMap
                Collection<Float> values = scores.values();
                List<Float> list = new ArrayList<>(values);
                Collections.sort(list);
                Collections.reverse(list);
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i) <= 1 && list.get(i) >= 0) {
                        for (Map.Entry<Integer, Float> entry : scores.entrySet()) {
                            if (entry.getValue() == list.get(i)) {
                                scoreSort.put(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                }
                modeScores = (DefaultTableModel) tblScore3.getModel();
                this.showScores1(scoreSort, modeScores, K);
                txtPrecision.setText("0");
                txtRecall.setText("0");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(view.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            JOptionPane.showMessageDialog(rootPane, "thông tin truyen vào bi thiêu", "ERROR", HEIGHT);
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void tblScore3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblScore3MouseClicked
        // TODO add your handling code here:
        int column = 0;
        int row = tblScore3.getSelectedRow();
        String docID = tblScore3.getModel().getValueAt(row, column).toString();
        try {
            FileReader fr = new FileReader(urlf + "\\src\\main\\java\\com\\mycompany\\javada\\document\\" + docID + ".txt");
            BufferedReader reader = new BufferedReader(fr);
            while (reader.ready()) {
                txtArea1.read(reader, "txtArea1");
            }
        } catch (IOException ioe) {
            System.err.println(ioe);
            System.exit(1);
        }

    }//GEN-LAST:event_tblScore3MouseClicked

    private void btnUsefulActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUsefulActionPerformed
        // TODO add your handling code here:
        int column = 2;
        int row = tblScore3.getSelectedRow();
        if (tblScore3.getValueAt(row, 2).toString() == "false") {
            tblScore3.getModel().setValueAt(true, row, column);
            float recall = Float.valueOf(txtRecall.getText()) * 10 + 1;// I assume Relative items = 10
            recall /= 10;
            float precision = Float.valueOf(txtPrecision.getText()) * Integer.valueOf(tblScore3.getRowCount()) + 1;
            precision /= Integer.valueOf(tblScore3.getRowCount());
            txtPrecision.setText(String.valueOf(precision));
            txtRecall.setText(String.valueOf(recall));
        }


    }//GEN-LAST:event_btnUsefulActionPerformed

    private void btnUnUsefulActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUnUsefulActionPerformed
        // TODO add your handling code here:
        int column = 2;
        int row = tblScore3.getSelectedRow();
        if (tblScore3.getValueAt(row, 2).toString() == "true") {
            tblScore3.getModel().setValueAt(false, row, column);
            float recall = Float.valueOf(txtRecall.getText()) * 10 - 1;// I assume Relative items = 10
            recall /= 10;
            float precision = Float.valueOf(txtPrecision.getText()) * Integer.valueOf(tblScore3.getRowCount()) - 1;
            precision /= Integer.valueOf(tblScore3.getRowCount());
            txtPrecision.setText(String.valueOf(precision));
            txtRecall.setText(String.valueOf(recall));
        }


    }//GEN-LAST:event_btnUnUsefulActionPerformed

    private void btnSearch1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearch1ActionPerformed
        // TODO add your handling code here:
        // TODO a        query.removeAll(LinkedList<>);dd your handling code here:
        query3.clear();
        this.InitScores(scores);
        this.InitScores(length);
        scoreSort.clear();
        int K = 34;
        String input = this.txtKeyword1.getText().toLowerCase().trim();
        input = input.replaceAll("\\p{Punct}", " ");
        while(input.contains("  ")) input=input.replaceAll("  ", " ");
        String[] terms = input.split(" ");
        for (int i = 0; i < terms.length; i++) {
            terms[i] = " " + terms[i] + " ";
        }
        Map<String, Integer> MapTerm = new HashMap<>();
        for (int i = 0; i < terms.length; i++) {
            if (!MapTerm.containsKey(terms[i])) {
                MapTerm.put(terms[i], 1);
            } else {
                MapTerm.put(terms[i], MapTerm.get(terms[i]) + 1);
            }

        }

        if ((!input.equals("")) && (K > 0)) {
            try {

                this.InitQuery2(MapTerm, query3);
                this.COSINESCORE(MapTerm, query3);
// Getting Collection of values from HashMap
                Collection<Float> values = scores.values();
                List<Float> list = new ArrayList<>(values);
                Collections.sort(list);
                Collections.reverse(list);
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i) <= 1 && list.get(i) >= 0) {
                        for (Map.Entry<Integer, Float> entry : scores.entrySet()) {
                            if (entry.getValue() == list.get(i)) {
                                scoreSort.put(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                }
                modeScores = (DefaultTableModel) tblScore4.getModel();
                this.showScores2(scoreSort, modeScores, K);
                int tp, fp, fn, tn;
                tp = Integer.valueOf(tblRelevant.getValueAt(0, 1).toString());
                fp = Integer.valueOf(tblRelevant.getValueAt(0, 2).toString());
                fn = Integer.valueOf(tblRelevant.getValueAt(1, 1).toString());
                tn = Integer.valueOf(tblRelevant.getValueAt(1, 2).toString());
                float precision, recall, fMeasure;
                if (tp + fp == 0) {
                    precision = 0;
                } else {
                    precision = (float)tp / (tp + fp);
                }
                if (tp + fn == 0) {
                    recall = 0;
                } else {
                    recall = (float)tp / (tp + fn);
                }
                if (recall + precision == 0) {
                    fMeasure = 0;
                } else {
                    fMeasure = (float)2 * precision * recall / (precision + recall);
                }

                txtPrecision1.setText(String.valueOf(precision));
                txtRecall1.setText(String.valueOf(recall));
                txtFMeasure.setText(String.valueOf(fMeasure));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(view.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            JOptionPane.showMessageDialog(rootPane, "thông tin truyen vào bi thiêu", "ERROR", HEIGHT);
        }
    }//GEN-LAST:event_btnSearch1ActionPerformed

    private void tblScore4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblScore4MouseClicked
        // TODO add your handling code here:
        int column = 0;
        int row = tblScore4.getSelectedRow();
        String docID = tblScore4.getModel().getValueAt(row, column).toString();
        try {
            FileReader fr = new FileReader(urlf + "\\src\\main\\java\\com\\mycompany\\javada\\document\\" + docID + ".txt");
            BufferedReader reader = new BufferedReader(fr);
            while (reader.ready()) {
                txtArea2.read(reader, "txtArea2");
            }
        } catch (IOException ioe) {
            System.err.println(ioe);
            System.exit(1);
        }
    }//GEN-LAST:event_tblScore4MouseClicked

    private void btnUseful1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUseful1ActionPerformed
        // TODO add your handling code here:
        int column = 2;
        int row = tblScore4.getSelectedRow();
        if (tblScore4.getValueAt(row, 2).toString() == "false") {
            tblScore4.getModel().setValueAt(true, row, column);
            if (tblScore4.getValueAt(row, 3).toString() == "true") {
                int tp = Integer.valueOf(tblRelevant.getValueAt(0, 1).toString()) + 1;
                tblRelevant.setValueAt(tp, 0, 1);
                int fp = Integer.valueOf(tblRelevant.getValueAt(0, 2).toString()) - 1;
                tblRelevant.setValueAt(fp, 0, 2);
                //tp and fp are changed -> precision, recall, fmeasure are changed
                int fn, tn;
                fn = Integer.valueOf(tblRelevant.getValueAt(1, 1).toString());
                tn = Integer.valueOf(tblRelevant.getValueAt(1, 2).toString());
                float precision, recall, fMeasure;
                if (tp + fp == 0) {
                    precision = 0;
                } else {
                    precision = (float)tp / (tp + fp);
                }
                if (tp + fn == 0) {
                    recall = 0;
                } else {
                    recall = (float)tp / (tp + fn);
                }
                if (recall + precision == 0) {
                    fMeasure = 0;
                } else {
                    fMeasure = (float)2 * precision * recall / (precision + recall);
                }
                txtPrecision1.setText(String.valueOf(precision));
                txtRecall1.setText(String.valueOf(recall));
                txtFMeasure.setText(String.valueOf(fMeasure));
            } else {
                int fn = Integer.valueOf(tblRelevant.getValueAt(1, 1).toString()) + 1;
                tblRelevant.setValueAt(fn, 1, 1);
                int tn = Integer.valueOf(tblRelevant.getValueAt(1, 2).toString()) - 1;
                tblRelevant.setValueAt(tn, 1, 2);
                // fn, tn are changed ->
                int tp, fp;
                tp = Integer.valueOf(tblRelevant.getValueAt(0, 1).toString());
                fp = Integer.valueOf(tblRelevant.getValueAt(0, 2).toString());

                float precision, recall, fMeasure;
                if (tp + fp == 0) {
                    precision = 0;
                } else {
                    precision = (float)tp / (tp + fp);
                }
                if (tp + fn == 0) {
                    recall = 0;
                } else {
                    recall = (float)tp / (tp + fn);
                }
                if (recall + precision == 0) {
                    fMeasure = 0;
                } else {
                    fMeasure = (float)2 * precision * recall / (precision + recall);
                }
                txtRecall1.setText(String.valueOf(recall));
                txtFMeasure.setText(String.valueOf(fMeasure));
            }
        }
    }//GEN-LAST:event_btnUseful1ActionPerformed

    private void btnUnUseful1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUnUseful1ActionPerformed
// TODO add your handling code here:
        int column = 2;
        int row = tblScore4.getSelectedRow();
        if (tblScore4.getValueAt(row, 2).toString() == "true") {
            tblScore4.getModel().setValueAt(false, row, column);
            if (tblScore4.getValueAt(row, 3).toString() == "true") {
                int fp = Integer.valueOf(tblRelevant.getValueAt(0, 2).toString()) + 1;
                tblRelevant.setValueAt(fp, 0, 2);
                int tp = Integer.valueOf(tblRelevant.getValueAt(0, 1).toString()) - 1;
                tblRelevant.setValueAt(tp, 0, 1);
                int fn, tn;
                fn = Integer.valueOf(tblRelevant.getValueAt(1, 1).toString());
                tn = Integer.valueOf(tblRelevant.getValueAt(1, 2).toString());
                float precision, recall, fMeasure;
                if (tp + fp == 0) {
                    precision = 0;
                } else {
                    precision = (float)tp / (tp + fp);
                }
                if (tp + fn == 0) {
                    recall = 0;
                } else {
                    recall = (float)tp / (tp + fn);
                }
                if (recall + precision == 0) {
                    fMeasure = 0;
                } else {
                    fMeasure = (float)2 * precision * recall / (precision + recall);
                }
                txtPrecision1.setText(String.valueOf(precision));
                txtRecall1.setText(String.valueOf(recall));
                txtFMeasure.setText(String.valueOf(fMeasure));

            } else {
                int fn = Integer.valueOf(tblRelevant.getValueAt(1, 1).toString()) - 1;
                tblRelevant.setValueAt(fn, 1, 1);
                int tn = Integer.valueOf(tblRelevant.getValueAt(1, 2).toString()) + 1;
                tblRelevant.setValueAt(tn, 1, 2);
                int tp, fp;
                tp = Integer.valueOf(tblRelevant.getValueAt(0, 1).toString());
                fp = Integer.valueOf(tblRelevant.getValueAt(0, 2).toString());

                float precision, recall, fMeasure;
                if (tp + fp == 0) {
                    precision = 0;
                } else {
                    precision = (float)tp / (tp + fp);
                }
                if (tp + fn == 0) {
                    recall = 0;
                } else {
                    recall = (float)tp / (tp + fn);
                }
                if (recall + precision == 0) {
                    fMeasure = 0;
                } else {
                    fMeasure = (float)2 * precision * recall / (precision + recall);
                }
                txtRecall1.setText(String.valueOf(recall));
                txtFMeasure.setText(String.valueOf(fMeasure));

            }
        }
    }//GEN-LAST:event_btnUnUseful1ActionPerformed

    private void txtT2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtT2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtT2ActionPerformed

    private void txtT2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtT2MouseClicked
        this.txtT2.setText("");
        // TODO add your handling code here:
    }//GEN-LAST:event_txtT2MouseClicked

    private void tblMAPMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblMAPMouseClicked
        // TODO add your handling code here:
        int row = tblMAP.getSelectedRow();
        int column = tblMAP.getSelectedColumn();
        if (column == 2) {
            String index = ComboBoxMAP.getSelectedItem().toString();
            modeMAP = (DefaultTableModel) tblMAP.getModel();
            System.out.println(MAP.size());
            for (int i = 0; i < MAP.size(); i++) {
                if (MAP.get(i).query.equals(index)) {
                    MAP.get(i).changeRelevant(row);
                    MAP.get(i).updatePrecision();
                    showDataMAP(MAP.get(i), modeMAP);
                }
//            
            }
        }
        if (column == 0) {
            String docID = tblMAP.getModel().getValueAt(row, column).toString();
            try {
                FileReader fr = new FileReader(urlf + "\\src\\main\\java\\com\\mycompany\\javada\\document\\" + docID + ".txt");
                BufferedReader reader = new BufferedReader(fr);
                while (reader.ready()) {
                    txtAreaMAP.read(reader, "txtAreaMAP");
                }
            } catch (IOException ioe) {
                System.err.println(ioe);
                System.exit(1);
            }
        }
    }//GEN-LAST:event_tblMAPMouseClicked

    private void ComboBoxMAPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ComboBoxMAPActionPerformed
        // TODO add your handling code here:         
        if (ComboBoxMAP.getItemCount() != 0) {
            String index = ComboBoxMAP.getSelectedItem().toString();
            modeMAP = (DefaultTableModel) tblMAP.getModel();
            System.out.println(MAP.size());

            for (int i = 0; i < MAP.size(); i++) {
                if (MAP.get(i).query.equals(index)) {
                    showDataMAP(MAP.get(i), modeMAP);
                }
//            System.out.println(MAP.get(i).query);
//            for (int j = 0; j < MAP.get(i).docID.size(); j++) {
//                System.out.println(MAP.get(i).docID.get(j));
//                System.out.println(MAP.get(i).score.get(j));
//                System.out.println(MAP.get(i).relevant.get(j));
//                System.out.println(MAP.get(i).precision.get(j));
//            }
                //       }
            }
        }
    }//GEN-LAST:event_ComboBoxMAPActionPerformed

    private void ComboBoxMAPComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_ComboBoxMAPComponentResized
        // TODO add your handling code here:
    }//GEN-LAST:event_ComboBoxMAPComponentResized

    private void ButAddQueryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButAddQueryActionPerformed
        // TODO add your handling code here:
        String inputMAP = InputMAP.getText();
        inputMAP = inputMAP.replaceAll("\\p{Punct}", " ").strip().toLowerCase();
        while(inputMAP.contains("  ")) inputMAP=inputMAP.replaceAll("  ", " ");
        if (!inputMAP.equals("")) {
            NQuery.add(inputMAP);
        }
    }//GEN-LAST:event_ButAddQueryActionPerformed

    private void jText1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jText1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jText1MouseClicked

    private void jText2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jText2MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jText2MouseClicked

    private void InputMAPMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_InputMAPMouseClicked
        // TODO add your handling code here:
        InputMAP.setText("");
    }//GEN-LAST:event_InputMAPMouseClicked

    private void ButtonSMapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButtonSMapActionPerformed

        int k = 34;         //max
        MAP.clear();
        dataMAP dataMap = null;
        ComboBoxMAP.removeAllItems();
        for (String str : NQuery) {
            ComboBoxMAP.addItem(str);
        }
        for (int q = 0; q < NQuery.size(); q++) {
            dataMap = new dataMAP(NQuery.get(q));
            query4.clear();
            this.InitScores(scores);
            this.InitScores(length);
            scoreSort.clear();
            String[] terms = NQuery.get(q).split(" ");
            for (int i = 0; i < terms.length; i++) {
                terms[i] = " " + terms[i] + " ";
                System.out.println(terms[i]);
            }
            Map<String, Integer> MapTerm = new HashMap<>();
            for (int i = 0; i < terms.length; i++) {
                if (!MapTerm.containsKey(terms[i])) {
                    MapTerm.put(terms[i], 1);
                } else {
                    MapTerm.put(terms[i], MapTerm.get(terms[i]) + 1);
                }

            }

            if ((!NQuery.get(q).equals("")) && (k > 0)) {
                try {

                    this.InitQuery2(MapTerm, query4);
                    this.COSINESCORE(MapTerm, query4);
// Getting Collection of values from HashMap
                    Collection<Float> values = scores.values();
                    List<Float> list = new ArrayList<>(values);
                    Collections.sort(list);
                    Collections.reverse(list);
                    for (int i = 0; i < list.size(); i++) {

                        if (list.get(i) <= 1 && list.get(i) >= 0) {
                            for (Map.Entry<Integer, Float> entry : scores.entrySet()) {
                                if (entry.getValue() == list.get(i)) {
                                    //scoreSort.put(entry.getKey(), entry.getValue());
                                    dataMap.docID.add(entry.getKey());
                                    dataMap.score.add(entry.getValue());
                                    dataMap.relevant.add(false);
                                    dataMap.precision.add((float) 0);
                                }
                            }
                        }
                    }

                    MAP.add(dataMap);
                    // System.out.println(MAP.size());
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(view.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                JOptionPane.showMessageDialog(rootPane, "thông tin truyen vào bi thiêu", "ERROR", HEIGHT);
            }
        }
//        for (int i = 0; i < MAP.size(); i++) {
//            System.out.println(MAP.get(i).query);
//            for (int j = 0; j < MAP.get(i).docID.size(); j++) {
//                System.out.println(MAP.get(i).docID.get(j));
//                System.out.println(MAP.get(i).score.get(j));
//                System.out.println(MAP.get(i).relevant.get(j));
//                System.out.println(MAP.get(i).precision.get(j));
//            }
//            
//        }
        // System.out.println(MAP.get(0).docID.get(0));
        ButtonSMap.setVisible(false);
    }//GEN-LAST:event_ButtonSMapActionPerformed

    private void btnMAPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMAPActionPerformed
        // TODO add your handling code here:
        List<Float> Average = new ArrayList<>();
        float resultSum = 0;
        for (int i = 0; i < MAP.size(); i++) {
            Average.add(MAP.get(i).ComputeAveragePrecision());
        }
        for (Float value : Average) {
            resultSum += value;
        }
        jLMAP.setText(Float.toString(resultSum / Average.size()));
    }//GEN-LAST:event_btnMAPActionPerformed

    private void jRadioB1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioB1ActionPerformed
        // TODO add your handling code here:
        int row = DCGtbl2.getSelectedRow();
        int DocId = Integer.parseInt(DCGtbl2.getValueAt(row, 0).toString());
        relevantVal.replace(DocId, (float) 1);
        relevantValUserRate.replace(DocId, (float) 1);
        DCGtbl2.setValueAt(1, row, 2);
    }//GEN-LAST:event_jRadioB1ActionPerformed

    private void jRadioB0ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioB0ActionPerformed
        // TODO add your handling code here:
        int row = DCGtbl2.getSelectedRow();
        int DocId = Integer.parseInt(DCGtbl2.getValueAt(row, 0).toString());
        relevantVal.replace(DocId, (float) 0);
        relevantValUserRate.replace(DocId, (float) 0);
        DCGtbl2.setValueAt(0, row, 2);
    }//GEN-LAST:event_jRadioB0ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        query5.clear();
        String tuTruyXuat = "";
        this.InitScores(scores);
        this.InitScores(length);
        relevantVal.clear();
        relevantValUserRate.clear();
        int K = 34;//max
        String input = txtNDCG.getText().toLowerCase().trim();
        input = input.replaceAll("\\p{Punct}", " ");
        while(input.contains("  ")) input=input.replaceAll("  ", " ");
        String[] terms = input.split(" ");
        for (int i = 0; i < terms.length; i++) {
            terms[i] = " " + terms[i] + " ";
        }
        Map<String, Integer> MapTerm = new HashMap<>();
        for (int i = 0; i < terms.length; i++) {
            if (!MapTerm.containsKey(terms[i])) {
                MapTerm.put(terms[i], 1);
            } else {
                MapTerm.put(terms[i], MapTerm.get(terms[i]) + 1);
            }

        }

        if ((!input.equals("")) && (K > 0)) {
            try {
                this.InitQuery2(MapTerm, query5);
                for (int i = 0; i < query5.size(); i++) {
                    if (!query5.get(i).isEmpty()) {
                        tuTruyXuat += query5.get(i).get(0).getTerms();
                    }

                }
                this.COSINESCORE(MapTerm, query5);
// Getting Collection of values from HashMap
                Map<Integer, Float> relevantValTemp = new HashMap<Integer, Float>();
                Collection<Float> values = scores.values();
                List<Float> list = new ArrayList<>(values);
                Collections.sort(list);
                Collections.reverse(list);

                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i) <= 1 && list.get(i) >= 0) {
                        for (Map.Entry<Integer, Float> entry : scores.entrySet()) {
                            if (entry.getValue() == list.get(i)) {
                                relevantVal.put(entry.getKey(), (float) 0);
                                relevantValTemp.put(entry.getKey(), (float) 0);
                            }
                        }
                    }
                }
                relevantValUserRate.putAll(relevantValTemp);
                modeDCG1 = (DefaultTableModel) NDCGtbl1.getModel();
                modeDCG2 = (DefaultTableModel) DCGtbl2.getModel();
                this.showRelevant(relevantVal, modeDCG1, K);
                this.showRelevantUserRate(relevantValUserRate, modeDCG2, K);

            } catch (FileNotFoundException ex) {
                Logger.getLogger(view.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            JOptionPane.showMessageDialog(rootPane, "thông tin truyen vào bi thiêu", "ERROR", HEIGHT);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void DCGtbl2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_DCGtbl2MouseClicked
        // TODO add your handling code here:
        int row = DCGtbl2.getSelectedRow();
        int valRelevant = (int) Float.parseFloat(DCGtbl2.getValueAt(row, 2).toString());
        switch (valRelevant) {
            case 0 ->
                jRadioB0.setSelected(true);
            case 1 ->
                jRadioB1.setSelected(true);
            case 2 ->
                jRadioB2.setSelected(true);
            case 3 ->
                jRadioB3.setSelected(true);
            case 4 ->
                jRadioB4.setSelected(true);
            default -> {
            }
        }
    }//GEN-LAST:event_DCGtbl2MouseClicked

    private void jRadioB2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioB2ActionPerformed
        int row = DCGtbl2.getSelectedRow();
        int DocId = Integer.parseInt(DCGtbl2.getValueAt(row, 0).toString());
        relevantVal.replace(DocId, (float) 2);
        relevantValUserRate.replace(DocId, (float) 2);
        DCGtbl2.setValueAt(2, row, 2);
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioB2ActionPerformed

    private void jRadioB3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioB3ActionPerformed
        // TODO add your handling code here:
        int row = DCGtbl2.getSelectedRow();
        int DocId = Integer.parseInt(DCGtbl2.getValueAt(row, 0).toString());
        relevantVal.replace(DocId, (float) 3);
        relevantValUserRate.replace(DocId, (float) 3);
        DCGtbl2.setValueAt(3, row, 2);
    }//GEN-LAST:event_jRadioB3ActionPerformed

    private void jRadioB4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioB4ActionPerformed
        // TODO add your handling code here:
        int row = DCGtbl2.getSelectedRow();
        int DocId = Integer.parseInt(DCGtbl2.getValueAt(row, 0).toString());
        relevantVal.replace(DocId, (float) 4);
        relevantValUserRate.replace(DocId, (float) 4);
        DCGtbl2.setValueAt(4, row, 2);
    }//GEN-LAST:event_jRadioB4ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:

        int k = 34;
        int rowCount = NDCGtbl1.getRowCount();
        Map<Integer, Float> relevantValUserRateTemp = new LinkedHashMap<>();
        Collection<Float> values = relevantValUserRate.values();
        List<Float> list = new ArrayList<>(values);
        Collections.sort(list);
        Collections.reverse(list);

        for (int i = 0; i < list.size(); i++) {
            for (Map.Entry<Integer, Float> entry : relevantValUserRate.entrySet()) {
                if (entry.getValue() == list.get(i)) {
                    relevantValUserRateTemp.put(entry.getKey(), entry.getValue());
                }
            }
        }
        relevantValUserRate.clear();
        relevantValUserRate.putAll(relevantValUserRateTemp);
        relevantValUserRateTemp.clear();
        modeDCG1 = (DefaultTableModel) NDCGtbl1.getModel();
        modeDCG2 = (DefaultTableModel) DCGtbl2.getModel();
        showRelevantUserRate(relevantValUserRate, modeDCG2, k);
        // tinh toan NDCG
        float DCG1 = ((float) (Math.pow(2, relevantVal.get(NDCGtbl1.getValueAt(0, 0))) - 1));
        NDCGtbl1.setValueAt(DCG1, 0, 1);
        for (int i = 1; i < rowCount; i++) {
            DCG1 = (float) ((float) (Math.pow(2, relevantVal.get(NDCGtbl1.getValueAt(i, 0))) - 1) / (Math.log(i + 2) / Math.log(2)));
            DCG1 += Float.parseFloat(NDCGtbl1.getValueAt(i - 1, 1).toString());
            NDCGtbl1.setValueAt(DCG1, i, 1);
        }
        // tinh dcg user rate
        DCG1 = ((float) (Math.pow(2, relevantVal.get(DCGtbl2.getValueAt(0, 0))) - 1));
        DCGtbl2.setValueAt(DCG1, 0, 1);
        for (int i = 1; i < rowCount; i++) {
            DCG1 = (float) ((float) (Math.pow(2, relevantVal.get(DCGtbl2.getValueAt(i, 0))) - 1) / (Math.log(i + 2) / Math.log(2)));
            DCG1 += Float.parseFloat(DCGtbl2.getValueAt(i - 1, 1).toString());
            DCGtbl2.setValueAt(DCG1, i, 1);
        }
        // final
        float NDCG = Float.parseFloat(NDCGtbl1.getValueAt(rowCount - 1, 1).toString()) / DCG1;
        labelNdcg.setText(String.valueOf(NDCG));
    }//GEN-LAST:event_jButton2ActionPerformed

    private void txtNDCGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNDCGActionPerformed
        // TODO add your handling code here:
        txtNDCG.setText("");
    }//GEN-LAST:event_txtNDCGActionPerformed

    private void RESETMAPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RESETMAPActionPerformed
        // TODO add your handling code here:
        InputMAP.setText("");
        NQuery.clear();
        dataMAP non = new dataMAP("");
        MAP.clear();
        ButtonSMap.setVisible(true);
        ComboBoxMAP.removeAllItems();
        modeMAP = (DefaultTableModel) tblMAP.getModel();
        showDataMAP(non, modeMAP);
    }//GEN-LAST:event_RESETMAPActionPerformed

    private void txtKeyword2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtKeyword2ActionPerformed

    }//GEN-LAST:event_txtKeyword2ActionPerformed

    private void tbnSearch2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbnSearch2ActionPerformed
        // TODO add your handling code here:
        // TODO add your handling code here:
        int K = 34;// set default K = 10
        String input = this.txtKeyword2.getText().toLowerCase().trim();
        input = input.replaceAll("\\p{Punct}", " ");
        while(input.contains("  ")) input=input.replaceAll("  ", " ");
        if ((!input.equals("")) && (K > 0)) {
            modeScores = (DefaultTableModel) tblScoreUser1.getModel();
            this.showdata1(modeScores, K);

        } else {
            JOptionPane.showMessageDialog(rootPane, "thông tin truyen vào bi thiêu", "ERROR", HEIGHT);
        }
    }//GEN-LAST:event_tbnSearch2ActionPerformed

    private void tbnComputeKappaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbnComputeKappaActionPerformed
        // TODO add your handling code here:
        int yy, nn, total;
            yy = Integer.valueOf(tblAssessingRelevance.getValueAt(0, 1).toString());
            nn = Integer.valueOf(tblAssessingRelevance.getValueAt(1, 2).toString());
            total = Integer.valueOf(tblAssessingRelevance.getValueAt(2, 3).toString());
            float pa;
            pa= (float)(yy + nn) / total;
            float pn, pr;
            pn = (float)(Integer.valueOf(tblAssessingRelevance.getValueAt(1, 3).toString()) + Integer.valueOf(tblAssessingRelevance.getValueAt(2, 2).toString())) / (2 * total);
            pr = (float)(Integer.valueOf(tblAssessingRelevance.getValueAt(0, 3).toString()) + Integer.valueOf(tblAssessingRelevance.getValueAt(2, 1).toString())) / (2 * total);
            System.out.println(pn);
            System.out.println(pr);
            float pe = (float) (Math.pow(pn, 2) + Math.pow(pr, 2));
            float kappa = (float)(pa - pe) / (1 - pe);
            txtPA.setText(String.valueOf(pa));
            txtPE.setText(String.valueOf(pe));
            txtKappa.setText(String.valueOf(kappa));
    }//GEN-LAST:event_tbnComputeKappaActionPerformed

    private void tblScoreUser1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblScoreUser1MouseClicked
        // TODO add your handling code here:
        int row = tblScoreUser1.getSelectedRow();
        int column = tblScoreUser1.getSelectedColumn();
        if (column >= 1 && column <= 2) {
            if (tblScoreUser1.getValueAt(row, column).toString() == "true") {
                tblScoreUser1.setValueAt(false, row, column);
                if (column == 1) {
                    if (tblScoreUser1.getValueAt(row, 2).toString() == "false") {// yn ->nn
                        int yy, nn, total, yn;
                        yy = Integer.valueOf(tblAssessingRelevance.getValueAt(0, 1).toString());
                        yn = Integer.valueOf(tblAssessingRelevance.getValueAt(0, 2).toString()) - 1;
                        nn = Integer.valueOf(tblAssessingRelevance.getValueAt(1, 2).toString()) + 1;
                        tblAssessingRelevance.setValueAt(yn, 0, 2);
                        tblAssessingRelevance.setValueAt(nn, 1, 2);
                        tblAssessingRelevance.setValueAt(yy + yn, 0, 3);
                        tblAssessingRelevance.setValueAt(Integer.valueOf(tblAssessingRelevance.getValueAt(1, 1).toString()) + nn, 1, 3);
                        
                    } else {//yy -> ny
                        int yy, nn, total, ny, yn;
                        yy = Integer.valueOf(tblAssessingRelevance.getValueAt(0, 1).toString()) -1;
                        yn = Integer.valueOf(tblAssessingRelevance.getValueAt(0, 2).toString());
                        ny = Integer.valueOf(tblAssessingRelevance.getValueAt(1, 1).toString()) + 1;
                        nn = Integer.valueOf(tblAssessingRelevance.getValueAt(1, 2).toString());
                        tblAssessingRelevance.setValueAt(yy, 0, 1);
                        tblAssessingRelevance.setValueAt(ny, 1, 1);
                        tblAssessingRelevance.setValueAt(yy + yn, 0, 3);
                        tblAssessingRelevance.setValueAt(Integer.valueOf(tblAssessingRelevance.getValueAt(1, 1).toString()) + nn, 1, 3);
                        
                    }
                } else {
                    if (tblScoreUser1.getValueAt(row, 1).toString() == "false") {//ny -> nn
                        int yy, nn, total, ny, yn;
                        yy = Integer.valueOf(tblAssessingRelevance.getValueAt(0, 1).toString());
                        yn = Integer.valueOf(tblAssessingRelevance.getValueAt(0, 2).toString());
                        ny = Integer.valueOf(tblAssessingRelevance.getValueAt(1, 1).toString())-1;
                        nn = Integer.valueOf(tblAssessingRelevance.getValueAt(1, 2).toString()) + 1;
                        tblAssessingRelevance.setValueAt(nn, 1, 2);
                        tblAssessingRelevance.setValueAt(ny, 1, 1);
                        tblAssessingRelevance.setValueAt(ny + yy, 2, 1);
                        tblAssessingRelevance.setValueAt(yn + nn, 2, 2);
                        
                    } else {//yy->yn
                        int yy, nn, total, ny, yn;
                        yy = Integer.valueOf(tblAssessingRelevance.getValueAt(0, 1).toString()) -1;
                        yn = Integer.valueOf(tblAssessingRelevance.getValueAt(0, 2).toString()) +1;
                        ny = Integer.valueOf(tblAssessingRelevance.getValueAt(1, 1).toString());
                        nn = Integer.valueOf(tblAssessingRelevance.getValueAt(1, 2).toString());
                        tblAssessingRelevance.setValueAt(yy, 0, 1);
                        tblAssessingRelevance.setValueAt(yn, 0, 2);
                        tblAssessingRelevance.setValueAt(ny + yy, 2, 1);
                        tblAssessingRelevance.setValueAt(yn + nn, 2, 2);
                        
                    }
                }

            } else {
                tblScoreUser1.setValueAt(true, row, column);
                if (column == 1) {
                    if (tblScoreUser1.getValueAt(row, 2).toString() == "false") {// nn ->  yn
                        int yy, nn, total, ny, yn;
                        yy = Integer.valueOf(tblAssessingRelevance.getValueAt(0, 1).toString());
                        yn = Integer.valueOf(tblAssessingRelevance.getValueAt(0, 2).toString())+1;
                        ny = Integer.valueOf(tblAssessingRelevance.getValueAt(1, 1).toString());
                        nn = Integer.valueOf(tblAssessingRelevance.getValueAt(1, 2).toString())-1;
                        tblAssessingRelevance.setValueAt(nn, 1, 2);
                        tblAssessingRelevance.setValueAt(yn, 0, 2);
                        tblAssessingRelevance.setValueAt(ny + nn, 1, 3);
                        tblAssessingRelevance.setValueAt(yn + yy, 0, 3);
                        
                    } else { // ny -> yy
                        int yy, nn, total, ny, yn;
                        yy = Integer.valueOf(tblAssessingRelevance.getValueAt(0, 1).toString())+1;
                        yn = Integer.valueOf(tblAssessingRelevance.getValueAt(0, 2).toString());
                        ny = Integer.valueOf(tblAssessingRelevance.getValueAt(1, 1).toString())-1;
                        nn = Integer.valueOf(tblAssessingRelevance.getValueAt(1, 2).toString());
                        tblAssessingRelevance.setValueAt(ny, 1, 1);
                        tblAssessingRelevance.setValueAt(yy, 0, 1);
                        tblAssessingRelevance.setValueAt(ny + nn, 1, 3);
                        tblAssessingRelevance.setValueAt(yn + yy, 0, 3);
                        
                    }
                } else {
                    if (tblScoreUser1.getValueAt(row, 1).toString() == "false") {// nn -> ny
                        int yy, nn, total, ny, yn;
                        yy = Integer.valueOf(tblAssessingRelevance.getValueAt(0, 1).toString());
                        yn = Integer.valueOf(tblAssessingRelevance.getValueAt(0, 2).toString());
                        ny = Integer.valueOf(tblAssessingRelevance.getValueAt(1, 1).toString())+1;
                        nn = Integer.valueOf(tblAssessingRelevance.getValueAt(1, 2).toString())-1;
                        tblAssessingRelevance.setValueAt(ny, 1, 1);
                        tblAssessingRelevance.setValueAt(nn, 1, 2);
                        tblAssessingRelevance.setValueAt(ny + yy, 2, 1);
                        tblAssessingRelevance.setValueAt(yn + nn, 2, 2);
                        
                    } else { // yn -> yy
                        int yy, nn, total, ny, yn;
                        yy = Integer.valueOf(tblAssessingRelevance.getValueAt(0, 1).toString())+1;
                        yn = Integer.valueOf(tblAssessingRelevance.getValueAt(0, 2).toString())-1;
                        ny = Integer.valueOf(tblAssessingRelevance.getValueAt(1, 1).toString());
                        nn = Integer.valueOf(tblAssessingRelevance.getValueAt(1, 2).toString());
                        tblAssessingRelevance.setValueAt(yn, 0, 2);
                        tblAssessingRelevance.setValueAt(yy, 0, 1);
                        tblAssessingRelevance.setValueAt(ny + yy, 2, 1);
                        tblAssessingRelevance.setValueAt(yn + nn, 2, 2);
                        
                    }
                }
            }
        }
        if(column==0){
            String docID = tblScoreUser1.getModel().getValueAt(row, 0).toString();
        try {
            FileReader fr = new FileReader(urlf + "\\src\\main\\java\\com\\mycompany\\javada\\document\\" + docID + ".txt");
            BufferedReader reader = new BufferedReader(fr);
            while (reader.ready()) {
                txtArea3.read(reader, "txtArea1");
            }
        } catch (IOException ioe) {
            System.err.println(ioe);
            System.exit(1);
        }
        }
    }//GEN-LAST:event_tblScoreUser1MouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(view.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(view.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(view.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(view.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new view().setVisible(true);
        });

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ButAddQuery;
    private javax.swing.JButton ButT2;
    private javax.swing.JButton ButtonSMap;
    private javax.swing.JComboBox<String> ComboBoxMAP;
    private javax.swing.JTable DCGtbl2;
    private javax.swing.JTextField InputMAP;
    private javax.swing.JTable NDCGtbl1;
    private javax.swing.JButton RESETMAP;
    private javax.swing.JButton Start;
    private javax.swing.JButton btnMAP;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnSearch1;
    private javax.swing.JButton btnUnUseful;
    private javax.swing.JButton btnUnUseful1;
    private javax.swing.JButton btnUseful;
    private javax.swing.JButton btnUseful1;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JTextField jK;
    private javax.swing.JLabel jLMAP;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JRadioButton jRadioB0;
    private javax.swing.JRadioButton jRadioB1;
    private javax.swing.JRadioButton jRadioB2;
    private javax.swing.JRadioButton jRadioB3;
    private javax.swing.JRadioButton jRadioB4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane15;
    private javax.swing.JScrollPane jScrollPane16;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jText1;
    private javax.swing.JTextField jText2;
    private javax.swing.JLabel labelNdcg;
    private javax.swing.JTabbedPane tab1;
    private javax.swing.JTable tblAssessingRelevance;
    private javax.swing.JTable tblDocID1;
    private javax.swing.JTable tblDocID2;
    private javax.swing.JTable tblMAP;
    private javax.swing.JTable tblRelevant;
    private javax.swing.JTable tblScore3;
    private javax.swing.JTable tblScore4;
    private javax.swing.JTable tblScoreUser1;
    private javax.swing.JTable tblScores;
    private javax.swing.JTable tblScores2;
    private javax.swing.JButton tbnComputeKappa;
    private javax.swing.JButton tbnSearch2;
    private javax.swing.JLabel ttNDCG1;
    private javax.swing.JLabel ttNDCG2;
    private javax.swing.JTextArea txtArea1;
    private javax.swing.JTextArea txtArea2;
    private javax.swing.JTextArea txtArea3;
    private javax.swing.JTextArea txtAreaMAP;
    private javax.swing.JLabel txtFMeasure;
    private javax.swing.JLabel txtKappa;
    private javax.swing.JTextField txtKeyword;
    private javax.swing.JTextField txtKeyword1;
    private javax.swing.JTextField txtKeyword2;
    private javax.swing.JTextField txtNDCG;
    private javax.swing.JTextField txtNumberOfSearchResult;
    private javax.swing.JLabel txtPA;
    private javax.swing.JLabel txtPE;
    private javax.swing.JLabel txtPrecision;
    private javax.swing.JLabel txtPrecision1;
    private javax.swing.JLabel txtRecall;
    private javax.swing.JLabel txtRecall1;
    private javax.swing.JTextField txtT2;
    private javax.swing.JLabel txtTermsRetrieved;
    // End of variables declaration//GEN-END:variables
       public void showDataMAP(dataMAP data, DefaultTableModel model) {
        model.setRowCount(0);
        for (int i = 0; i < data.docID.size(); i++) {
            model.addRow(new Object[]{
                data.docID.get(i), data.score.get(i), data.relevant.get(i).toString(), data.precision.get(i)
            });

        }
    }

    public <T> void showData(List<T> list, DefaultTableModel model) {
        model.setRowCount(0);
        String tit, bod;
        for (T t : list) {
            if (t instanceof Doc) {
                Doc d = (Doc) t;
                if (d.isTitle()) {
                    tit = "+";
                } else {
                    tit = "";
                }
                if (d.isBody()) {
                    bod = "+";
                } else {
                    bod = "";
                }
                model.addRow(new Object[]{
                    d.getDocID(), tit, bod
                });
            }
        }
    }

    public <K, V> void showScores(Map<K, V> map, DefaultTableModel model, int K) {
        model.setRowCount(0);
        int i = 1;

        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (!(i > K)) {
                model.addRow(new Object[]{
                    entry.getKey(), entry.getValue()
                });
                i++;
            }
        }
    }

    public <K, V> void showScores1(Map<K, V> map, DefaultTableModel model, int K) {// A Toan lam
        model.setRowCount(0);
        int i = 1;

        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (!(i > K)) {
                model.addRow(new Object[]{
                    entry.getKey(), entry.getValue(), false
                });
                i++;
            }
        }
    }

    public <K, V, T> void showRelevant(Map<K, V> map, DefaultTableModel model, int K) {
        model.setRowCount(0);
        int i = 1;

        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (!(i > K)) {
                model.addRow(new Object[]{
                    entry.getKey(), 0
                });
                i++;
            }
        }
    }

    public <K, V, T> void showRelevantUserRate(Map<K, V> map, DefaultTableModel model, int K) {
        model.setRowCount(0);
        int i = 1;

        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (!(i > K)) {
                model.addRow(new Object[]{
                    entry.getKey(), 0, entry.getValue()
                });
                i++;
            }
        }
    }

    public <K, V> void showScores2(Map<K, V> map, DefaultTableModel model, int K) {
        model.setRowCount(0);
        tblRelevant.setValueAt(0, 0, 1);
        tblRelevant.setValueAt(0, 0, 2);
        tblRelevant.setValueAt(0, 1, 1);
        tblRelevant.setValueAt(0, 1, 2);
        int i = 1;
        int fp, tn;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (!(i > K)) {
                if ((Float) entry.getValue() >= 0.75) {
                    model.addRow(new Object[]{
                        entry.getKey(), entry.getValue(), false, true
                    });
                    fp = Integer.valueOf(tblRelevant.getValueAt(0, 2).toString()) + 1;
                    tblRelevant.setValueAt(fp, 0, 2);
                } else {
                    model.addRow(new Object[]{
                        entry.getKey(), entry.getValue(), false, false
                    });
                    tn = Integer.valueOf(tblRelevant.getValueAt(1, 2).toString()) + 1;
                    tblRelevant.setValueAt(tn, 1, 2);
                }
                i++;
            }
        }
    }

    public <K, V> void showdata1(DefaultTableModel model, int K) {
        model.setRowCount(0);
        int i = 1;
        for (int docid : DocIDArr) {
            if (!(i > K)) {
                model.addRow(new Object[]{
                    docid, false, false
                });
                i++;
            }
        }
        tblAssessingRelevance.setValueAt(0, 0, 1);
        tblAssessingRelevance.setValueAt(0, 0, 2);
        tblAssessingRelevance.setValueAt(0, 0, 3);
        tblAssessingRelevance.setValueAt(0, 1, 1);
        tblAssessingRelevance.setValueAt(10, 1, 2);
        tblAssessingRelevance.setValueAt(10, 1, 3);
        tblAssessingRelevance.setValueAt(0, 2, 1);
        tblAssessingRelevance.setValueAt(10, 2, 2);

        tblAssessingRelevance.setValueAt(10, 2, 3);
    }
}
