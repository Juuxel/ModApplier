package juuxel.modapplier;

import javax.swing.*;

public final class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Mod Applier");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(new ModApplier());

        SwingUtilities.invokeLater(() -> {
            frame.setVisible(true);
            frame.pack();
        });
    }
}
