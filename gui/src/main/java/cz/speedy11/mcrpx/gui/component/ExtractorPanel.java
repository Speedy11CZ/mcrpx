/*
 * This file is part of MCRPX, licensed under the MIT License.
 *
 * Copyright (c) Michal Spišak (Speedy11CZ) <michalspisak53@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package cz.speedy11.mcrpx.gui.component;

import cz.speedy11.mcrpx.common.util.FileUtil;
import cz.speedy11.mcrpx.common.util.ZipUtil;
import cz.speedy11.mcrpx.gui.Main;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

import static cz.speedy11.mcrpx.gui.util.ComponentUtil.componentAlignedToCenter;

/**
 * Main panel for selecting input file, output directory and extracting it.
 *
 * @author Michal Spišak (Speedy11CZ)
 * @since 1.1.0
 */
public class ExtractorPanel extends JPanel {

    public ExtractorPanel() {
        super();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(Main.PANEL_MARGIN, Main.PANEL_MARGIN, Main.PANEL_MARGIN, Main.PANEL_MARGIN));

        FileSelectPanel inputFileSelectPanel = new FileSelectPanel("Input File:             ");
        DirectorySelectPanel outputDirectorySelectPanel = new DirectorySelectPanel("Output Directory: ");

        JButton btnExtract = new JButton("Extract");
        btnExtract.addActionListener(e -> {
            if (inputFileSelectPanel.getFile() == null) {
                JOptionPane.showMessageDialog(this, "Input file is not selected", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!inputFileSelectPanel.getFile().exists()) {
                JOptionPane.showMessageDialog(this, "Input file doesn't exists", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!inputFileSelectPanel.getFile().isFile()) {
                JOptionPane.showMessageDialog(this, "Input file is not a file", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!ZipUtil.isValid(inputFileSelectPanel.getFile())) {
                JOptionPane.showMessageDialog(this, "Invalid input file! Must be resource pack or Minecraft jar file", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (outputDirectorySelectPanel.getFile() == null) {
                JOptionPane.showMessageDialog(this, "Output directory is not selected", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!outputDirectorySelectPanel.getFile().exists()) {
                JOptionPane.showMessageDialog(this, "Output directory doesn't exists", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!outputDirectorySelectPanel.getFile().isDirectory()) {
                JOptionPane.showMessageDialog(this, "Output directory is not a directory", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                if (!FileUtil.isEmpty(outputDirectorySelectPanel.getFile())) {
                    if (JOptionPane.showConfirmDialog(this, "Output directory is not empty! Do you want to continue?", "Output directory is not empty", JOptionPane.YES_NO_OPTION) == 1) {
                        return;
                    }
                }
            } catch (IOException exception) {
                JOptionPane.showMessageDialog(this, "Error while checking output directory", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ExtractionStatusFrame extractionProgressFrame = new ExtractionStatusFrame(inputFileSelectPanel.getFile().getName());
            extractionProgressFrame.extract(inputFileSelectPanel.getFile(), outputDirectorySelectPanel.getFile());
        });

        JButton btnClose = componentAlignedToCenter(new JButton("Close"));
        btnClose.addActionListener(e -> System.exit(0));

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
        controlPanel.add(btnExtract);
        controlPanel.add(Box.createRigidArea(new Dimension(Main.COMPONENT_MARGIN, 0)));
        controlPanel.add(btnClose);

        add(componentAlignedToCenter(new JLabel(new ImageIcon(Main.BANNER))));
        add(Box.createRigidArea(new Dimension(0, Main.COMPONENT_MARGIN)));
        add(componentAlignedToCenter(new JLabel("MineCraft Resource Pack eXtractor")));
        add(componentAlignedToCenter(new JLabel("by Speedy11CZ")));
        add(Box.createRigidArea(new Dimension(0, Main.COMPONENT_MARGIN)));
        add(componentAlignedToCenter(inputFileSelectPanel));
        add(Box.createRigidArea(new Dimension(0, Main.COMPONENT_MARGIN)));
        add(componentAlignedToCenter(outputDirectorySelectPanel));
        add(Box.createRigidArea(new Dimension(0, Main.COMPONENT_MARGIN)));
        add(componentAlignedToCenter(controlPanel));
    }
}