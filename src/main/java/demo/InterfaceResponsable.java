package demo;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import java.awt.*;

import java.sql.*;

public class InterfaceResponsable extends JFrame {

    private DefaultTableModel modele;
    private JTextArea blocNotesArea;

    public InterfaceResponsable() {
        setTitle("🧑‍🏫 Espace Responsable");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(false);

        setJMenuBar(creerMenu());

        JPanel contenu = new PanneauDegradeFonce();
        contenu.setLayout(new BorderLayout(15, 15));
        contenu.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(contenu);

        // Statistiques
        JPanel panneauStats = new JPanel(new GridLayout(1, 3, 15, 15));
        panneauStats.setOpaque(false);
        panneauStats.add(creerPanneauStatistique("📚 Cours", "12", new Color(52, 152, 219)));
        panneauStats.add(creerPanneauStatistique("👨‍🏫 Enseignants", "6", new Color(46, 204, 113)));
        panneauStats.add(creerPanneauStatistique("🏫 Classes", "4", new Color(241, 196, 15)));
        contenu.add(panneauStats, BorderLayout.NORTH);

        // Contenu principal
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.7);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setLeftComponent(creerTableauCours());
        splitPane.setRightComponent(creerBlocNotes());

        contenu.add(splitPane, BorderLayout.CENTER);

        chargerDonnees();
        chargerInfosSeances();
    }

    private JMenuBar creerMenu() {
        JMenuBar barre = new JMenuBar();
        barre.setBackground(new Color(44, 62, 80));

        JMenu menu = new JMenu("☰ Menu");
        menu.setForeground(Color.WHITE);

        JMenuItem rafraichir = new JMenuItem("🔄 Rafraîchir");
        rafraichir.addActionListener(e -> chargerDonnees());

        JMenuItem quitter = new JMenuItem("❌ Quitter");
        quitter.addActionListener(e -> System.exit(0));

        menu.add(rafraichir);
        menu.addSeparator();
        menu.add(quitter);

        barre.add(menu);
        return barre;
    }

    private JScrollPane creerTableauCours() {
        modele = new DefaultTableModel(new String[]{"Séance", "Cours", "Enseignant", "État", "Action"}, 0) {
            public boolean isCellEditable(int row, int col) {
                return col == 4;
            }
        };

        JTable table = new JTable(modele);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.getTableHeader().setBackground(new Color(57, 62, 70));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setBackground(new Color(50, 50, 60));
        table.setForeground(Color.WHITE);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                c.setBackground(row % 2 == 0 ? new Color(60, 63, 65) : new Color(44, 62, 80));
                c.setForeground(Color.WHITE);
                return c;
            }
        });

        table.getColumn("Action").setCellRenderer(new RenduBouton());
        table.getColumn("Action").setCellEditor(new EditeurBouton(new JCheckBox()));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        return scroll;
    }

    private JScrollPane creerBlocNotes() {
        blocNotesArea = new JTextArea("Chargement...");
        blocNotesArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        blocNotesArea.setBackground(new Color(255, 255, 255, 180));
        blocNotesArea.setForeground(new Color(40, 40, 40));
        blocNotesArea.setLineWrap(true);
        blocNotesArea.setWrapStyleWord(true);
        blocNotesArea.setEditable(false);
        blocNotesArea.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "📥 Informations à valider",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(40, 40, 40)
        ));

        JScrollPane scroll = new JScrollPane(blocNotesArea);
        scroll.setPreferredSize(new Dimension(330, 400));
        return scroll;
    }

    private void chargerDonnees() {
        modele.setRowCount(0);

        try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestionnairedecahierdetextes", "root", "")) {
            String sql = "SELECT i.nom AS infosseance, f.nom AS filiere, u.nom AS enseignant, s.etat FROM seance s JOIN filiere f ON s.filiere_id = f.id JOIN utilisateur u ON s.utilisateur_id = u.id JOIN infosseance i ON i.utilisateur_id = u.id";
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String etat = rs.getString("etat");
                modele.addRow(new Object[]{
                        rs.getString("infosseance"),
                        rs.getString("filiere"),
                        rs.getString("enseignant"),
                        etat != null ? etat : "En attente",
                        "Action"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur de chargement : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void chargerInfosSeances() {
        try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestionnairedecahierdetextes", "root", "")) {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT i.*, u.nom AS utilisateur FROM infosseance i JOIN utilisateur u ON u.id = i.utilisateur_id");

            blocNotesArea.setText("");
            while (rs.next()) {
                String nom = rs.getString("nom");
                String contenu = rs.getString("contenu");
                String utilisateur = rs.getString("utilisateur");

                blocNotesArea.append("Séance : " + nom + "\nTitre : " + contenu + "\nProfesseur : " + utilisateur + "\n--------------------------\n");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement infos séance : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void changerEtatCours(String nomSeance, String nouvelEtat) {
        try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestionnairedecahierdetextes", "root", "")) {
            String sql = "UPDATE seance SET etat = ? WHERE nom = ?";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, nouvelEtat);
            ps.setString(2, nomSeance);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- Table Cell Components ---

    class RenduBouton extends JButton implements TableCellRenderer {
        public RenduBouton() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        }

        public Component getTableCellRendererComponent(JTable table, Object val, boolean sel, boolean focus, int row, int col) {
            String etat = (String) table.getValueAt(row, 3);
            setText(etat.equals("Validé") ? "Annuler" : "Valider");
            setBackground(etat.equals("Validé") ? new Color(231, 76, 60) : new Color(39, 174, 96));
            setForeground(Color.WHITE);
            return this;
        }
    }

    class EditeurBouton extends DefaultCellEditor {
        private final JButton bouton;
        private int ligne;

        public EditeurBouton(JCheckBox checkBox) {
            super(checkBox);
            bouton = new JButton();
            bouton.setFocusPainted(false);
            bouton.setFont(new Font("Segoe UI", Font.BOLD, 13));
            bouton.setForeground(Color.WHITE);
            bouton.setCursor(new Cursor(Cursor.HAND_CURSOR));

            bouton.addActionListener(e -> {
                fireEditingStopped();
                String nomSeance = modele.getValueAt(ligne, 0).toString();
                String etatActuel = modele.getValueAt(ligne, 3).toString();

                String nouvelEtat = etatActuel.equals("Validé") ? "En attente" : "Validé";
                changerEtatCours(nomSeance, nouvelEtat);
                modele.setValueAt(nouvelEtat, ligne, 3);
                chargerInfosSeances();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.ligne = row;
            bouton.setText(table.getValueAt(row, 3).equals("Validé") ? "Annuler" : "Valider");
            bouton.setBackground(table.getValueAt(row, 3).equals("Validé") ? new Color(231, 76, 60) : new Color(46, 204, 113));
            return bouton;
        }
    }

    private JPanel creerPanneauStatistique(String titre, String valeur, Color couleur) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(couleur, 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel titreLabel = new JLabel(titre, SwingConstants.CENTER);
        titreLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titreLabel.setForeground(couleur);

        JLabel valeurLabel = new JLabel(valeur, SwingConstants.CENTER);
        valeurLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valeurLabel.setForeground(Color.WHITE);

        panel.add(titreLabel, BorderLayout.NORTH);
        panel.add(valeurLabel, BorderLayout.CENTER);
        return panel;
    }

    static class PanneauDegradeFonce extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            GradientPaint gp = new GradientPaint(0, 0, new Color(36, 37, 42), getWidth(), getHeight(), new Color(25, 26, 30));
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

   
}
