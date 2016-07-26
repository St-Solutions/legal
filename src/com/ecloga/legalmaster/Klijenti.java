package com.ecloga.legalmaster;

import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class Klijenti {
    private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private int width = (int) (screenSize.getWidth() * 0.75);
    private int height = (int) (screenSize.getHeight() * 0.75);
    private JPanel panel, tablePanel, menuPanel;
    private static DefaultTableModel model;
    private static JTable table;
    private JLabel lEcloga;
    private JScrollPane scrollPane;
    private JButton bDodaj, bUkloni, bIzmeni, bPredmeti, bKalendar, bStampaj;
    private static ArrayList<String> klijenti = new ArrayList<String>();
    private static int maxID = 0;
    public static boolean infoShown = false;
    public static ArrayList<String> predmetiShown = new ArrayList<String>();
    private JCheckBox cbKlijenti, cbPredmeti;

    public Klijenti() {
        panel = new JPanel();
        tablePanel = new JPanel();
        menuPanel = new JPanel();

        lEcloga = new JLabel("Copyright © Ecloga Apps");
        lEcloga.setForeground(Color.GRAY);

        model = new DefaultTableModel();

        table = new JTable(model) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String[] columns = {"ID", "Ime i prezime", "Broj telefona", "Email", "Adresa", "Napomena"};

        for(String value : columns) {
            model.addColumn(value);
        }

        table.setPreferredScrollableViewportSize(new Dimension(width, (int) (height * 0.8)));
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(true);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if(!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.decode("#ecf0f1") : Color.WHITE);
                }

                if(column == 0) {
                    this.setHorizontalAlignment(JLabel.CENTER);
                }else {
                    this.setHorizontalAlignment(JLabel.LEFT);
                }

                if(column < 2) {
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                }

                table.getColumnModel().getColumn(0).setCellRenderer(this);

                return c;
            }
        });

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setMaxWidth(75);
        columnModel.getColumn(1).setPreferredWidth(200);
        columnModel.getColumn(2).setPreferredWidth(100);
        columnModel.getColumn(3).setPreferredWidth(200);
        columnModel.getColumn(4).setPreferredWidth(300);
        columnModel.getColumn(5).setMinWidth(300);

        refresh();

        scrollPane = new JScrollPane(table);

        bDodaj = new JButton("Dodaj");
        bDodaj.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!infoShown) {
                    infoShown = true;

                    KlijentInfo dodaj = new KlijentInfo();
                    dodaj.show();
                }else {
                    JOptionPane.showMessageDialog(null, "Prozor je aktivan", "Poruka", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        bUkloni = new JButton("Ukloni");
        bUkloni.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = table.getSelectedRow();

                if(selectedIndex != -1) {
                    String id = String.valueOf(table.getValueAt(selectedIndex, 0));

                    model.removeRow(selectedIndex);
                    klijenti.remove(id);

                    String cmd = "SELECT * FROM predmeti";

                    ResultSet rs = null;

                    try {
                        rs = Main.s.executeQuery(cmd);

                        while(rs.next()){
                            if(String.valueOf(rs.getInt("klijent")).equals(id)) {
                                File media = new File(Main.directoryName + File.separator + "media" + File.separator + rs.getInt("id"));

                                try {
                                    FileUtils.deleteDirectory(media);
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }

                                Main.executeDB("DELETE FROM tok WHERE predmet=" + rs.getInt("id"));
                                Main.executeDB("DELETE FROM cenovnik WHERE predmet=" + rs.getInt("id"));
                            }
                        }

                        rs.close();
                    }catch(SQLException e1) {
                        e1.printStackTrace();
                    }

                    Main.executeDB("DELETE FROM predmeti WHERE klijent=" + id);
                    Main.executeDB("DELETE FROM klijenti WHERE id=" + id);
                }else {
                    JOptionPane.showMessageDialog(null, "Klijent nije selektovan", "Poruka", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        bIzmeni = new JButton("Izmeni");
        bIzmeni.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!infoShown) {
                    int selectedIndex = table.getSelectedRow();

                    if(selectedIndex != -1) {
                        infoShown = true;

                        HashMap<Integer, String> info = new HashMap<Integer, String>();

                        for(int i = 0; i < table.getColumnCount(); i++) {
                            info.put(i, String.valueOf(table.getValueAt(selectedIndex, i)));
                        }

                        KlijentInfo izmeni = new KlijentInfo(info);
                        izmeni.show();
                    }else {
                        JOptionPane.showMessageDialog(null, "Klijent nije selektovan", "Poruka", JOptionPane.INFORMATION_MESSAGE);
                    }
                }else {
                    JOptionPane.showMessageDialog(null, "Prozor je aktivan", "Poruka", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        bPredmeti = new JButton("Predmeti");
        bPredmeti.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = table.getSelectedRow();

                if(selectedIndex != -1) {
                    String ime = String.valueOf(table.getValueAt(selectedIndex, 1));

                    if(!predmetiShown.contains(ime)) {
                        Predmeti predmeti = new Predmeti(ime);
                        predmeti.show();

                        predmetiShown.add(ime);
                    }else {
                        JOptionPane.showMessageDialog(null, "Predmeti ovog klijenta su prikazani", "Poruka", JOptionPane.INFORMATION_MESSAGE);
                    }
                }else {
                    JOptionPane.showMessageDialog(null, "Klijent nije selektovan", "Poruka", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        bKalendar = new JButton("Kalendar");
        bKalendar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Kalendar kalendar = new Kalendar();
                kalendar.pick();
            }
        });

        cbKlijenti = new JCheckBox("Klijenti", false);
        cbPredmeti = new JCheckBox("Predmeti", false);

        bStampaj = new JButton("Stampaj");
        bStampaj.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(cbKlijenti.isSelected()) {
                    ArrayList<String> list = new ArrayList<String>();

                    String cmd = "SELECT * FROM klijenti";

                    ResultSet rs = null;

                    try {
                        rs = Main.s.executeQuery(cmd);

                        while(rs.next()) {
                            list.add(rs.getInt("id") + "   " + rs.getString("ime") + "   " + rs.getString("broj") + "   " + rs.getString("email") + "   " + rs.getString("adresa") + "   " + rs.getString("napomena"));
                        }

                        rs.close();
                    }catch(SQLException e1) {
                        e1.printStackTrace();
                    }

                    Main.writeFile(Main.directoryName + File.separator + "stampa" + File.separator + "klijenti.txt", list);
                }

                if(cbPredmeti.isSelected()) {
                    ArrayList<String> list = new ArrayList<String>();
                    ArrayList<Integer> klijentiId = new ArrayList<Integer>();
                    ArrayList<String> klijentiIme = new ArrayList<String>();

                    String cmd = "SELECT * FROM predmeti";

                    ResultSet rs = null;

                    try {
                        rs = Main.s.executeQuery(cmd);

                        while(rs.next()) {
                            String line = rs.getInt("id") + "   " + rs.getString("sifra") + "   " + rs.getString("ime") + "   " + rs.getString("napomena");
                            list.add(line);

                            klijentiId.add(rs.getInt("klijent"));
                        }

                        rs.close();
                    }catch(SQLException e1) {
                        e1.printStackTrace();
                    }

                    for(Integer klijentId : klijentiId) {
                        cmd = "SELECT * FROM klijenti WHERE id=" + klijentId;

                        try {
                            rs = Main.s.executeQuery(cmd);

                            while(rs.next()) {
                                klijentiIme.add(rs.getString("ime"));
                            }

                            rs.close();
                        }catch(SQLException e1) {
                            e1.printStackTrace();
                        }
                    }

                    for(String line : list) {
                        String id = String.valueOf(line.substring(0, line.indexOf("   ")));

                        int cena = 0;
                        int placeno = 0;

                        cmd = "SELECT * FROM cenovnik WHERE predmet=" + id;

                        try {
                            rs = Main.s.executeQuery(cmd);

                            while(rs.next()) {
                                cena += rs.getInt("cena");
                                placeno += rs.getInt("placeno");
                            }

                            list.set(list.indexOf(line), line + "   " + klijentiIme.get(list.indexOf(line)) + "   " + placeno + "/" + cena);

                            rs.close();
                        }catch(SQLException e1) {
                            e1.printStackTrace();
                        }
                    }

                    Main.writeFile(Main.directoryName + File.separator + "stampa" + File.separator + "predmeti.txt", list);
                }

                if(!cbKlijenti.isSelected() && !cbPredmeti.isSelected()) {
                    JOptionPane.showMessageDialog(null, "Lista nije selektovana", "Poruka", JOptionPane.INFORMATION_MESSAGE);
                }else {
                    openStampa(Main.directoryName + File.separator + "stampa");
                }
            }
        });
    }

    private void openStampa(String dir) {
        try {
            Desktop.getDesktop().open(new File(dir));
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void add(Object[] row) {
        maxID++;
        String id = String.valueOf(maxID);
        row[0] = id;
        Main.executeDB("INSERT INTO klijenti VALUES (" + row[0] + ", '" + row[1] + "', '" + row[2] + "', '" + row[3] + "', '" + row[4] + "', '" + row[5] + "')");
        addRow(row);
    }

    public static void addRow(Object[] row) {
        model.addRow(row);
        table.setModel(model);
        klijenti.add(String.valueOf(row[0]));
    }

    public static void update(Object[] row) {
        String id = String.valueOf(row[0]);
        Main.executeDB("UPDATE klijenti SET ime='" + row[1] + "', broj='" + row[2] + "', email='" + row[3] + "', adresa='" + row[4] + "', napomena='" + row[5] + "' WHERE id=" + id);
        refresh();
    }

    public static void refresh() {
        model.setRowCount(0);
        table.setModel(model);

        String cmd = "SELECT * FROM klijenti";

        ResultSet rs = null;

        try {
            rs = Main.s.executeQuery(cmd);

            while(rs.next()){
                addRow(new Object[] {rs.getInt("id"), rs.getString("ime"), rs.getString("broj"), rs.getString("email"), rs.getString("adresa"), rs.getString("napomena")});

                if(rs.getInt("id") > maxID) {
                    maxID = rs.getInt("id");
                }
            }

            rs.close();
        }catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void show() {
        menuPanel.add(bDodaj);
        menuPanel.add(bUkloni);
        menuPanel.add(bIzmeni);
        menuPanel.add(bPredmeti);
        menuPanel.add(bKalendar);
        menuPanel.add(cbKlijenti);
        menuPanel.add(cbPredmeti);
        menuPanel.add(bStampaj);

        tablePanel.add(scrollPane);

        panel.add(menuPanel);
        panel.add(tablePanel);
        panel.add(lEcloga);

        JFrame frame = new JFrame();
        frame.setTitle("LegalMaster");
        frame.setSize(new Dimension(width, height));
        frame.setLocation(screenSize.width / 2 - width / 2,screenSize.height / 2 - height / 2);
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
