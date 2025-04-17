package demo;

import javax.swing.SwingUtilities;

public class App{

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InterfaceLogin().setVisible(true));
    }
    
}




