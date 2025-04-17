package demo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Optional;

public class InterfaceEnseignants extends JFrame {

    private JTextField enseignantField;
    private JTextField nomSeanceField;
    private JTextArea contenuSeanceArea;
    private JTextArea blocNotesArea;
    private JTable table;

    public InterfaceEnseignants() {
        setTitle("Gestion des Enseignants");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setContentPane(new DarkGradientPanel());

        Color fondFormulaire = new Color(40, 40, 60);
        Color accent = new Color(0, 122, 204);

        JMenuBar barreMenu = new JMenuBar();
        barreMenu.setBackground(new Color(30, 30, 47));
        JMenu menuConsulter = new JMenu("💡Consulter");
        menuConsulter.setForeground(Color.WHITE);

        JMenuItem voirCoursItem = new JMenuItem("Voir mes cours");
        voirCoursItem.addActionListener(e -> consulterCours());
        menuConsulter.add(voirCoursItem);
        barreMenu.add(menuConsulter);
        setJMenuBar(barreMenu);

        JLabel title = new JLabel("Espace des Enseignants", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        title.setOpaque(true);
        title.setBackground(new Color(36, 37, 42));
        title.setBorder(new EmptyBorder(15, 10, 15, 10));

        JPanel formulairePanel = new JPanel(new GridBagLayout());
        formulairePanel.setBackground(fondFormulaire);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        enseignantField = new JTextField(25);
        configTextField(enseignantField, "Nom de l'enseignant");
        gbc.gridy = 0;
        formulairePanel.add(enseignantField, gbc);

        nomSeanceField = new JTextField(25);
        configTextField(nomSeanceField, "Nom de la séance");
        gbc.gridy++;
        formulairePanel.add(nomSeanceField, gbc);

        contenuSeanceArea = new JTextArea(4, 25);
        configTextArea(contenuSeanceArea, "Contenu de la séance");
        gbc.gridy++;
        formulairePanel.add(new JScrollPane(contenuSeanceArea), gbc);

        JButton addButton = new LightButton("Ajouter la séance", accent);
        addButton.addActionListener(e -> ajouterSeance());
        gbc.gridy++;
        formulairePanel.add(addButton, gbc);

        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setBackground(fondFormulaire);
        formContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Ajouter une séance",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), Color.LIGHT_GRAY));
        formContainer.add(formulairePanel, BorderLayout.CENTER);

        DefaultTableModel model = new DefaultTableModel(new String[]{"Cours", "Enseignant"}, 0);
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setBackground(new Color(35, 35, 50));
        table.setForeground(Color.WHITE);
        table.setRowHeight(30);
        table.setGridColor(new Color(70, 70, 90));
        table.setSelectionBackground(accent);

        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.getTableHeader().setBackground(new Color(50, 50, 70));
        table.getTableHeader().setForeground(Color.WHITE);
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        JScrollPane tableScroll = new JScrollPane(table);

        blocNotesArea = new JTextArea();
        blocNotesArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        blocNotesArea.setBackground(new Color(30, 30, 47));
        blocNotesArea.setForeground(new Color(200, 200, 220));
        blocNotesArea.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 122, 204)),
                "Bloc-notes",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), Color.LIGHT_GRAY));
        blocNotesArea.setEditable(false);
        blocNotesArea.setLineWrap(true);
        blocNotesArea.setWrapStyleWord(true);

        JScrollPane blocScroll = new JScrollPane(blocNotesArea);

        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, blocScroll);
        horizontalSplit.setDividerLocation(300);

        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, formContainer, horizontalSplit);
        verticalSplit.setDividerLocation(350);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(title, BorderLayout.NORTH);
        getContentPane().add(verticalSplit, BorderLayout.CENTER);

        loadCoursEtSeances();
        chargerInfosSeances();
    }

    private void consulterCours() {
        String nom = JOptionPane.showInputDialog(this, "Entrez votre nom :", "Identification Enseignant", JOptionPane.PLAIN_MESSAGE);
        if (nom == null || nom.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nom non saisi.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = getConnection()) {
            Optional<Integer> utilisateurId = getUtilisateurIdParNom(nom.trim(), conn);
            if (!utilisateurId.isPresent()) {
                JOptionPane.showMessageDialog(this, "Enseignant introuvable.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int id = utilisateurId.get();
            PreparedStatement ps = conn.prepareStatement("SELECT nom FROM seance WHERE utilisateur_id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("nom"), nom});
            }

            chargerBlocNotesPourEnseignant(id);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void ajouterSeance() {
        String nomEnseignant = enseignantField.getText().trim();
        String nomSeance = nomSeanceField.getText().trim();
        String contenu = contenuSeanceArea.getText().trim();

        if (nomEnseignant.isEmpty() || nomSeance.isEmpty() || contenu.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = getConnection()) {
            Optional<Integer> utilisateurId = getUtilisateurIdParNom(nomEnseignant, conn);
            if (!utilisateurId.isPresent()) {
                JOptionPane.showMessageDialog(this, "Cours pas encore assigné", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            PreparedStatement ps = conn.prepareStatement("INSERT INTO infosseance (nom, contenu, utilisateur_id) VALUES (?, ?, ?)");
            ps.setString(1, nomSeance);
            ps.setString(2, contenu);
            ps.setInt(3, utilisateurId.get());
            ps.executeUpdate();

            enseignantField.setText("");
            nomSeanceField.setText("");
            contenuSeanceArea.setText("");

            loadCoursEtSeances();
            chargerInfosSeances();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void chargerInfosSeances() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT i.contenu, u.nom AS enseignant FROM infosseance i JOIN utilisateur u ON i.utilisateur_id = u.id")) {

            blocNotesArea.setText("");
            while (rs.next()) {
                blocNotesArea.append("Contenu : " + rs.getString("contenu") + "\n");
                blocNotesArea.append("Auteur : " + rs.getString("enseignant") + "\n");
                blocNotesArea.append("-----------------------------\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void chargerBlocNotesPourEnseignant(int utilisateurId) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT contenu FROM infosseance WHERE utilisateur_id = ?")) {
            ps.setInt(1, utilisateurId);
            ResultSet rs = ps.executeQuery();
            blocNotesArea.setText("");
            while (rs.next()) {
                blocNotesArea.append("Contenu : " + rs.getString("contenu") + "\n");
                blocNotesArea.append("---------------------------\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCoursEtSeances() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT s.nom AS seance, u.nom AS enseignant FROM seance s JOIN utilisateur u ON s.utilisateur_id = u.id")) {

            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("seance"), rs.getString("enseignant")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Optional<Integer> getUtilisateurIdParNom(String nom, Connection conn) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT id FROM utilisateur WHERE nom = ?");
        ps.setString(1, nom);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return Optional.of(rs.getInt("id"));
        return Optional.empty();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/gestionnairedecahierdetextes?useUnicode=true&characterEncoding=utf8", "root", "");
    }

    private void configTextField(JTextField field, String title) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setBackground(new Color(60, 60, 80));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), title, TitledBorder.LEFT, TitledBorder.TOP, null, Color.LIGHT_GRAY));
    }

    private void configTextArea(JTextArea area, String title) {
        area.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(new Color(60, 60, 80));
        area.setForeground(Color.WHITE);
        area.setCaretColor(Color.WHITE);
        area.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), title, TitledBorder.LEFT, TitledBorder.TOP, null, Color.LIGHT_GRAY));
    }

    static class DarkGradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(new GradientPaint(0, 0, new Color(20, 20, 40), getWidth(), getHeight(), new Color(40, 40, 60)));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    static class LightButton extends JButton {
        public LightButton(String text, Color background) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 16));
            setBackground(background);
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    }

   
}
