package demo;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.sql.*;

public class InterfaceChef extends JFrame {
    private DefaultTableModel modeleTableau;

    public InterfaceChef() {
        setTitle("Gestionnaire de cahier de texte");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Menu principal
        JMenuBar barreMenu = new JMenuBar();
        barreMenu.setBackground(new Color(30, 30, 47));
        barreMenu.setBorder(BorderFactory.createLineBorder(new Color(0, 180, 216)));

        JMenu menuFichier = new JMenu("\uD83D\uDCC1 Fichier");
        JMenu menuAjout = new JMenu("➕ Ajouter");
        JMenu menuActions = new JMenu("⚙️ Actions");
        JMenu menuAide = new JMenu("❓ Aide");

        for (JMenu menu : new JMenu[]{menuFichier, menuAjout, menuActions, menuAide}) {
            menu.setForeground(new Color(241, 241, 241));
        }

        JMenuItem ouvrirPDF = new JMenuItem("📂 Ouvrir PDF");
        ouvrirPDF.addActionListener(e -> ouvrirPDF());
        JMenuItem quitter = new JMenuItem("❌ Quitter");
        quitter.addActionListener(e -> System.exit(0));
        menuFichier.add(ouvrirPDF);
        menuFichier.addSeparator();
        menuFichier.add(quitter);

        JMenuItem ajoutUtilisateur = new JMenuItem("👤 Ajouter Enseignant/Responsable");
        ajoutUtilisateur.addActionListener(e -> ajouterUtilisateur());
        JMenuItem assignerCours = new JMenuItem("📘 Assigner un cours");
        assignerCours.addActionListener(e -> ajouterEnseignant());
        menuAjout.add(ajoutUtilisateur);
        menuAjout.add(assignerCours);

        JMenuItem genererPDF = new JMenuItem("📄 Générer PDF");
        genererPDF.addActionListener(e -> genererPDF());
        menuActions.add(genererPDF);

        JMenuItem aPropos = new JMenuItem("ℹ️ À propos");
        aPropos.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Application Cahier de Texte\nDéveloppée par Licence 2 Informatique\nUFR SET - Université Iba Der Thiam de Thiès",
                "À propos", JOptionPane.INFORMATION_MESSAGE));
        menuAide.add(aPropos);

        barreMenu.add(menuFichier);
        barreMenu.add(menuAjout);
        barreMenu.add(menuActions);
        barreMenu.add(menuAide);
        setJMenuBar(barreMenu);

        JPanel panneauPrincipal = new PanneauDegrade();
        panneauPrincipal.setLayout(new BorderLayout(10, 10));
        panneauPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titre = new JLabel("\uD83D\uDCDA Gestionnaire de cahier de texte", SwingConstants.CENTER);
        titre.setForeground(Color.WHITE);
        panneauPrincipal.add(titre, BorderLayout.NORTH);

        JPanel panneauStats = new JPanel(new GridLayout(1, 3, 15, 15));
        panneauStats.setOpaque(false);
        panneauStats.add(creerPanneauStatistique("📚 Cours", "10", new Color(52, 152, 219)));
        panneauStats.add(creerPanneauStatistique("👨‍🏫 Enseignants", "5", new Color(46, 204, 113)));
        panneauStats.add(creerPanneauStatistique("🏫 Classes", "3", new Color(241, 196, 15)));
        panneauPrincipal.add(panneauStats, BorderLayout.SOUTH);

        modeleTableau = new DefaultTableModel(new String[]{"Cours", "Classe", "Enseignant"}, 0);
        JTable tableau = new JTable(modeleTableau);
        tableau.setRowHeight(28);
        tableau.setForeground(new Color(230, 230, 230));
        tableau.setBackground(new Color(44, 44, 60));
        tableau.setSelectionBackground(new Color(0, 180, 216));
        tableau.setSelectionForeground(Color.BLACK);
        tableau.setGridColor(new Color(60, 60, 80));
        tableau.setShowVerticalLines(false);

        tableau.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int lig, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, lig, col);
                if (!sel) {
                    c.setBackground(lig % 2 == 0 ? new Color(47, 58, 74) : new Color(37, 40, 54));
                }
                return c;
            }
        });

        JTableHeader entete = tableau.getTableHeader();
        entete.setBackground(new Color(30, 35, 50));
        entete.setForeground(new Color(255, 255, 255));
        entete.setReorderingAllowed(false);

        JScrollPane defilement = new JScrollPane(tableau);
        defilement.setBorder(BorderFactory.createLineBorder(new Color(0, 180, 216)));
        panneauPrincipal.add(defilement, BorderLayout.CENTER);

        setContentPane(panneauPrincipal);
        chargerDonnees();
    }

    private void chargerDonnees() {
        try (Connection connexion = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestionnairedecahierdetextes", "root", "")) {
            String requete = "SELECT s.nom AS seance, c.nom AS filiere, u.nom AS utilisateur FROM seance s JOIN filiere c ON s.filiere_id = c.id JOIN utilisateur u ON s.utilisateur_id = u.id";
            PreparedStatement stmt = connexion.prepareStatement(requete);
            ResultSet resultat = stmt.executeQuery();
            modeleTableau.setRowCount(0);
            while (resultat.next()) {
                modeleTableau.addRow(new Object[]{
                        resultat.getString("seance"),
                        resultat.getString("filiere"),
                        resultat.getString("utilisateur")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur de connexion à la base de données", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ajouterEnseignant() {
        JTextField champNomSeance = new JTextField();
        JTextField champCours = new JTextField();
        JTextField champEnseignant = new JTextField();
        Object[] champs = {"Nom de la séance :", champNomSeance, "filiere :", champCours, "Enseignant :", champEnseignant};
        int choix = JOptionPane.showConfirmDialog(null, champs, "Ajouter un enseignant", JOptionPane.OK_CANCEL_OPTION);
        if (choix == JOptionPane.OK_OPTION) {
            ajouterDansBase(champNomSeance.getText(), champCours.getText(), champEnseignant.getText());
            chargerDonnees();
        }
    }

    private void ajouterDansBase(String nomSeance, String nomfiliere, String nomEnseignant) {
        try (Connection connexion = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestionnairedecahierdetextes", "root", "")) {
            int idEnseignant = getIdUtilisateur(connexion, nomEnseignant);
            if (idEnseignant == -1) {
                JOptionPane.showMessageDialog(this, "L'utilisateur n'existe pas. Ajoutez-le d'abord.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int idCours = getId(connexion, "filiere", nomfiliere, idEnseignant);
            if (idCours == -1) {
                idCours = insererEtRecupererId(connexion, "filiere", nomfiliere, idEnseignant);
            }

            PreparedStatement stmt = connexion.prepareStatement("INSERT INTO seance (nom, etat, utilisateur_id, filiere_id) VALUES (?, ?, ?, ?)");
            stmt.setString(1, nomSeance);
            stmt.setNull(2, Types.VARCHAR);
            stmt.setInt(3, idEnseignant);
            stmt.setInt(4, idCours);
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'ajout", "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private int getIdUtilisateur(Connection connexion, String nom) throws SQLException {
        PreparedStatement stmt = connexion.prepareStatement("SELECT id FROM utilisateur WHERE nom = ?");
        stmt.setString(1, nom);
        ResultSet resultat = stmt.executeQuery();
        return resultat.next() ? resultat.getInt("id") : -1;
    }

    private int getId(Connection connexion, String table, String nom, int idUtilisateur) throws SQLException {
        PreparedStatement stmt = connexion.prepareStatement("SELECT id FROM " + table + " WHERE nom = ? AND utilisateur_id = ?");
        stmt.setString(1, nom);
        stmt.setInt(2, idUtilisateur);
        ResultSet resultat = stmt.executeQuery();
        return resultat.next() ? resultat.getInt("id") : -1;
    }

    private int insererEtRecupererId(Connection connexion, String table, String nom, int idUtilisateur) throws SQLException {
        PreparedStatement stmt = connexion.prepareStatement("INSERT INTO " + table + " (nom, utilisateur_id) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, nom);
        stmt.setInt(2, idUtilisateur);
        stmt.executeUpdate();
        ResultSet resultat = stmt.getGeneratedKeys();
        return resultat.next() ? resultat.getInt(1) : -1;
    }

    private void ajouterUtilisateur() {
        JTextField champNom = new JTextField();
        JTextField champEmail = new JTextField();
        JPasswordField champMotDePasse = new JPasswordField();
        String[] roles = {"enseignant", "responsable"};
        JComboBox<String> boiteRoles = new JComboBox<>(roles);

        Object[] champs = {"Nom :", champNom, "Email :", champEmail, "Mot de passe :", champMotDePasse, "Rôle :", boiteRoles};

        int choix = JOptionPane.showConfirmDialog(null, champs, "Ajouter un utilisateur", JOptionPane.OK_CANCEL_OPTION);
        if (choix == JOptionPane.OK_OPTION) {
            String nom = champNom.getText();
            String email = champEmail.getText();
            String motDePasse = new String(champMotDePasse.getPassword());
            String role = (String) boiteRoles.getSelectedItem();

            if (nom.isEmpty() || email.isEmpty() || motDePasse.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tous les champs sont obligatoires.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection connexion = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestionnairedecahierdetextes", "root", "")) {
                PreparedStatement stmt = connexion.prepareStatement("INSERT INTO utilisateur (nom, email, mot_de_passe, role) VALUES (?, ?, ?, ?)");
                stmt.setString(1, nom);
                stmt.setString(2, email);
                stmt.setString(3, motDePasse);
                stmt.setString(4, role);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Utilisateur ajouté avec succès !");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erreur lors de l'ajout", "Erreur", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void genererPDF() {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("CahierDeTexte.pdf"));
            document.open();
            document.add(new Paragraph("Cahier de Texte", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            PdfPTable tableauPDF = new PdfPTable(modeleTableau.getColumnCount());
            for (int i = 0; i < modeleTableau.getColumnCount(); i++) {
                tableauPDF.addCell(modeleTableau.getColumnName(i));
            }
            for (int i = 0; i < modeleTableau.getRowCount(); i++) {
                for (int j = 0; j < modeleTableau.getColumnCount(); j++) {
                    tableauPDF.addCell(modeleTableau.getValueAt(i, j).toString());
                }
            }
            document.add(tableauPDF);
            document.close();
            JOptionPane.showMessageDialog(this, "PDF généré avec succès !");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur de génération du PDF", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ouvrirPDF() {
        try {
            File fichier = new File("CahierDeTexte.pdf");
            if (fichier.exists()) Desktop.getDesktop().open(fichier);
            else JOptionPane.showMessageDialog(this, "Le fichier PDF n'existe pas encore.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Impossible d'ouvrir le fichier.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    class PanneauDegrade extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            GradientPaint degrade = new GradientPaint(0, 0, new Color(30, 30, 47), 0, getHeight(), new Color(18, 18, 30));
            g2d.setPaint(degrade);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private JPanel creerPanneauStatistique(String titre, String valeur, Color couleurAccent) {
        JPanel panneau = new JPanel(new BorderLayout(5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        panneau.setOpaque(false);
        panneau.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel etiquetteTitre = new JLabel(titre, SwingConstants.CENTER);
        etiquetteTitre.setForeground(couleurAccent);

        JLabel etiquetteValeur = new JLabel(valeur, SwingConstants.CENTER);
        etiquetteValeur.setForeground(Color.WHITE);

        panneau.add(etiquetteTitre, BorderLayout.NORTH);
        panneau.add(etiquetteValeur, BorderLayout.CENTER);

        return panneau;
    }

   
}
