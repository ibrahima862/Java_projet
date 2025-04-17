// ligne 1 - même package
package demo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class InterfaceLogin extends JFrame {

    public InterfaceLogin() {
        setTitle("Connexion - Cahier de Texte");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setContentPane(new DarkGradientPanel());

        JPanel glassPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(245, 245, 245, 245));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            }
        };
        glassPanel.setOpaque(false);
        glassPanel.setPreferredSize(new Dimension(420, 500));
        glassPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 20, 15, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel title = new JLabel("Connexion");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 40, 40));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        glassPanel.add(title, gbc);

        gbc.gridy++;
        JTextField email = createStyledTextField("Email");
        glassPanel.add(email, gbc);

        gbc.gridy++;
        JPasswordField password = new JPasswordField(20);
        stylePasswordField(password, "Mot de passe");
        glassPanel.add(password, gbc);
        gbc.gridy++;
        JCheckBox showPassword = new JCheckBox("Afficher le mot de passe");
        showPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        showPassword.setOpaque(false);
        showPassword.setForeground(new Color(60, 60, 60));
        showPassword.addActionListener(e -> password.setEchoChar(showPassword.isSelected() ? '\u0000' : '•'));
        glassPanel.add(showPassword, gbc);

        gbc.gridy++;
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"chef", "enseignant", "responsable"});
        styleComboBox(roleBox, "Statut");
        glassPanel.add(roleBox, gbc);

        gbc.gridy++;
        JLabel messageLabel = new JLabel(" ");
        messageLabel.setForeground(Color.RED);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        glassPanel.add(messageLabel, gbc);

        gbc.gridy++;
        JButton loginBtn = new loginButton("Connexion");
        glassPanel.add(loginBtn, gbc);

        gbc.gridy++;
        JButton addButton = new addButton("S'inscrire");
        addButton.addActionListener(e -> ajouterChef());
        glassPanel.add(addButton, gbc);

        loginBtn.addActionListener(e -> {
            String emails = email.getText().trim();
            String mdp = new String(password.getPassword()).trim();
            String selectedRole = (String) roleBox.getSelectedItem();

            if (emails.isEmpty() || mdp.isEmpty()) {
                messageLabel.setText("Veuillez remplir tous les champs.");
                return;
            }

            if (mdp.length() < 4) {
                messageLabel.setText("Mot de passe trop court.");
                return;
            }

            messageLabel.setForeground(new Color(50, 50, 50));
            messageLabel.setText("Connexion en cours...");

            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/gestionnairedecahierdetextes", "root", "");
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT * FROM utilisateur WHERE email=? AND mot_de_passe=? AND role=?")) {

                stmt.setString(1, emails);
                stmt.setString(2, mdp);
                stmt.setString(3, selectedRole);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    messageLabel.setForeground(new Color(0, 140, 80));
                    messageLabel.setText("Connexion réussie !");
                    dispose();
                    switch (selectedRole) {
                        case "chef": new InterfaceChef().setVisible(true); break;
                        case "enseignant": new InterfaceEnseignants().setVisible(true); break;
                        case "responsable": new InterfaceResponsable().setVisible(true); break;
                    }
                } else {
                    messageLabel.setForeground(Color.RED);
                    messageLabel.setText("Email ou mot de passe incorrect.");
                }

            } catch (SQLException ex1) {
                ex1.printStackTrace();
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("Erreur de base de données.");
            }
        });

        setLayout(new GridBagLayout());
        add(glassPanel);
    }

    private void ajouterChef() {
        String clef = JOptionPane.showInputDialog(this, "Veuillez entrer la clé d'autorisation :", "Clé d'Autorisation", JOptionPane.PLAIN_MESSAGE);
        String clefCorrecte = "clé_secrète";

        if (clef != null && clef.equals(clefCorrecte)) {
            JTextField nomField = new JTextField();
            JTextField emailField = new JTextField();
            JTextField passField = new JTextField();
            JTextField roleField = new JTextField();
            Object[] fields = {"Nom:", nomField, "Email:", emailField, "Mot de passe:", passField, "Rôle: chef/enseignant/responsable", roleField};
            int option = JOptionPane.showConfirmDialog(null, fields, "Ajouter un Utilisateur", JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                ajouterDansBase(nomField.getText(), emailField.getText(), passField.getText(), roleField.getText());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Clé incorrecte. Vous n'êtes pas autorisé à ajouter un utilisateur.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ajouterDansBase(String nom, String email, String motDePasse, String role) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestionDeCahierDeTexte", "root", "")) {
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO utilisateur (nom, email, mot_de_passe, role) VALUES (?, ?, ?, ?)");
            pstmt.setString(1, nom);
            pstmt.setString(2, email);
            pstmt.setString(3, motDePasse);
            pstmt.setString(4, role);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Utilisateur ajouté avec succès !");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'ajout", "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private JTextField createStyledTextField(String title) {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        field.setCaretColor(Color.BLACK);
        field.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                title, 0, 0, null, Color.DARK_GRAY));
        return field;
    }

    private void stylePasswordField(JPasswordField field, String title) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        field.setCaretColor(Color.BLACK);
        field.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                title, 0, 0, null, Color.DARK_GRAY));
    }

    private void styleComboBox(JComboBox<String> comboBox, String title) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(Color.BLACK);
        comboBox.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                title, 0, 0, null, Color.DARK_GRAY));
    }

    static class DarkGradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            GradientPaint gp = new GradientPaint(
                0, 0, new Color(30, 30, 47),
                getWidth(), getHeight(), new Color(20, 20, 30));
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    static class loginButton extends JButton {
        public loginButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 16));
            setBackground(new Color(0, 181, 136));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setContentAreaFilled(false);
            setOpaque(true);

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent evt) {
                    setBackground(new Color(0, 158, 137));
                }

                public void mouseExited(MouseEvent evt) {
                    setBackground(new Color(0, 191, 166));
                }
            });
        }
    }

    static class addButton extends JButton {
        public addButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setBackground(new Color(45, 31, 36));
            setForeground(Color.WHITE);
            setFocusPainted(true);
            setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setContentAreaFilled(false);
            setOpaque(true);

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent evt) {
                    setBackground(new Color(0, 98, 17));
                }

                public void mouseExited(MouseEvent evt) {
                    setBackground(new Color(45, 31, 36));
                }
            });
        }
    }
  
}
