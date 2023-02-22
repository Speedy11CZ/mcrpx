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

import cz.speedy11.mcrpx.common.extraction.ExtractTaskListener;
import cz.speedy11.mcrpx.common.util.ZipUtil;
import cz.speedy11.mcrpx.gui.Main;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.concurrent.Future;

/**
 * Frame for showing extraction status.
 *
 * @author Michal Spišak (Speedy11CZ)
 * @since 1.1.0
 */
public class ExtractionStatusFrame extends JFrame implements ExtractTaskListener {

    private final JLabel progressText = new JLabel();
    private final JProgressBar progressBar = new JProgressBar();
    private final JTextArea consoleArea = new JTextArea();
    private int maxSize;
    private int currentSize;

    private Future<?> task;

    /**
     * Creates new extraction status frame.
     *
     * @param fileName File name
     * @throws HeadlessException If GraphicsEnvironment.isHeadless() returns true.
     */
    public ExtractionStatusFrame(String fileName) throws HeadlessException {
        super();

        JPanel panel = new JPanel();
        setContentPane(panel);
        setTitle("Extracting " + fileName);
        setIconImage(Main.ICON);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 600, 400);
        setContentPane(panel);
        setLocationRelativeTo(null);

        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{600, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0, 200};
        gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0};
        panel.setLayout(gridBagLayout);

        GridBagConstraints gbcProgressText = new GridBagConstraints();
        gbcProgressText.insets = new Insets(10, 0, 5, 0);
        gbcProgressText.gridx = 0;
        gbcProgressText.gridy = 0;
        panel.add(progressText, gbcProgressText);

        GridBagConstraints gbcProgressBar = new GridBagConstraints();
        gbcProgressBar.insets = new Insets(0, 25, 5, 25);
        gbcProgressBar.fill = GridBagConstraints.HORIZONTAL;
        gbcProgressBar.gridx = 0;
        gbcProgressBar.gridy = 1;
        progressBar.setStringPainted(true);
        panel.add(progressBar, gbcProgressBar);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> {
            task.cancel(true);
            JOptionPane.showMessageDialog(this, "Extraction cancelled", "Extraction", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        GridBagConstraints gbcBtnCancel = new GridBagConstraints();
        gbcBtnCancel.insets = new Insets(0, 25, 5, 25);
        gbcBtnCancel.fill = GridBagConstraints.HORIZONTAL;
        gbcBtnCancel.gridx = 0;
        gbcBtnCancel.gridy = 2;
        panel.add(btnCancel, gbcBtnCancel);

        consoleArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        GridBagConstraints gbcConsoleArea = new GridBagConstraints();
        gbcConsoleArea.insets = new Insets(15, 25, 25, 25);
        gbcConsoleArea.fill = GridBagConstraints.BOTH;
        gbcConsoleArea.gridx = 0;
        gbcConsoleArea.gridy = 3;

        JScrollPane scroll = new JScrollPane(consoleArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setAutoscrolls(true);
        panel.add(scroll, gbcConsoleArea);
    }

    public void extract(File file, File destDirectory) {
        setVisible(true);
        task = ZipUtil.extract(file, destDirectory, this);
    }

    @Override
    public void bytesTotal(int bytes) {
        maxSize = bytes;
        currentSize = 0;
    }

    @Override
    public void fileProcessed(int bytes) {
        currentSize += bytes;
        progressBar.setValue((int) ((currentSize / (double) maxSize) * 100));
    }

    @Override
    public void taskCompleted() {
        JOptionPane.showConfirmDialog(this, "Extraction completed successfully!", "Extraction", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    @Override
    public void onError(Exception exception) {
        JOptionPane.showConfirmDialog(this, exception.getLocalizedMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        dispose();
    }

    @Override
    public void onMessage(String message) {
        System.out.println(message);
        progressText.setText(message);
        consoleArea.append(message + "\n");
        consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
    }
}
