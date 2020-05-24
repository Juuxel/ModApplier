package juuxel.modapplier;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

final class ModApplier extends JPanel {
    private final JFileChooser fileChooser = new JFileChooser();
    private File gameFile;
    private File modFile;
    private File targetFile;

    ModApplier() {
        setLayout(new MigLayout());
        fileChooser.setFileFilter(new FileNameExtensionFilter("JAR files", "jar"));

        JLabel gameFileLabel = new JLabel("<unselected>");
        JLabel modFileLabel = new JLabel("<unselected>");
        JLabel targetFileLabel = new JLabel("<unselected>");
        JButton chooseGameButton = new JButton("Choose game");
        JButton chooseModButton = new JButton("Choose mod");
        JButton chooseTargetButton = new JButton("Choose output location");

        chooseGameButton.addActionListener(e -> {
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                gameFile = fileChooser.getSelectedFile();
                gameFileLabel.setText(gameFile.getPath());
            }
        });

        chooseModButton.addActionListener(e -> {
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                modFile = fileChooser.getSelectedFile();
                modFileLabel.setText(modFile.getPath());
            }
        });

        chooseTargetButton.addActionListener(e -> {
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                targetFile = fileChooser.getSelectedFile();
                targetFileLabel.setText(targetFile.getPath());
            }
        });

        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> apply());

        add(new JLabel("<html><h1>Mod Applier</h1>"), "span");
        add(new JLabel("<html><b>Game:</b>"));
        add(gameFileLabel, "wrap");
        add(chooseGameButton, "span");
        add(new JLabel("<html><b>Mod:</b>"));
        add(modFileLabel, "wrap");
        add(chooseModButton, "span");
        add(new JLabel("<html><b>Output:</b>"));
        add(targetFileLabel, "wrap");
        add(chooseTargetButton, "span");
        add(Box.createVerticalStrut(10), "span");
        add(applyButton, "span");
    }

    private void apply() {
        if (gameFile == null) {
            JOptionPane.showMessageDialog(this, "Choose the game first!", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (modFile == null) {
            JOptionPane.showMessageDialog(this, "Choose the mod first!", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (targetFile == null) {
            JOptionPane.showMessageDialog(this, "Choose the output first!", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            new Thread(() -> {
                try {
                    execApply();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, String.format("%s: %s", e.getClass().getName(), e.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }).start();
        }
    }

    private void execApply() throws Exception {
        if (Files.exists(targetFile.toPath())) {
            int result = JOptionPane.showConfirmDialog(this, "The output JAR already exists. Do you want to replace it?", "File already exists", JOptionPane.YES_NO_OPTION);
            if (result != JOptionPane.YES_OPTION) return;
            Files.deleteIfExists(targetFile.toPath());
        }

        Files.copy(modFile.toPath(), targetFile.toPath()); // copy from mod to target

        URI targetUri = URI.create("jar:" + targetFile.toURI());
        URI originUri = URI.create("jar:" + gameFile.toURI());

        try (FileSystem targetFs = FileSystems.newFileSystem(targetUri, Collections.emptyMap())) {
            try (FileSystem originFs = FileSystems.newFileSystem(originUri, Collections.emptyMap())) {
                for (Path root : originFs.getRootDirectories()) {
                    Files.walk(root).filter(Files::isRegularFile)
                        .forEach(it -> {
                            String path = it.toString();
                            Path targetPath = targetFs.getPath(path);

                            if (Files.notExists(targetPath)) {
                                try {
                                    Path parent = targetPath.getParent();
                                    if (parent != null) {
                                        Files.createDirectories(parent);
                                    }
                                    Files.copy(it, targetPath);
                                } catch (IOException e) {
                                    throw new UncheckedIOException(e);
                                }
                            }
                        });
                }
            }
        }

        JOptionPane.showMessageDialog(this, "Done!");
    }
}
