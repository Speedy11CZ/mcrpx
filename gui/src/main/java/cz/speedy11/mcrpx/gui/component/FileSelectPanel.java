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

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

import static cz.speedy11.mcrpx.gui.util.ComponentUtil.componentAlignedToCenter;

/**
 * Panel used for selecting file or directory and displaying it.
 *
 * @author Michal Spišak (Speedy11CZ)
 * @since 1.1.0
 */
public class FileSelectPanel extends JPanel {

    private File file;

    /**
     * Creates new panel with file selector.
     *
     * @param text      Text displayed before file selector
     * @param directory If true, only directories can be selected
     */
    public FileSelectPanel(String text, boolean directory) {
        super();
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        JTextField filePath = new JTextField();
        filePath.setEditable(false);

        JButton button = new JButton("...");
        button.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setFileHidingEnabled(true);
            fileChooser.setFileSelectionMode(directory ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
            if (!directory) {
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Minecraft Assets File (*.zip;*.jar)", "zip", "jar"));
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Zip File Format (*.zip)", "zip", "jar"));
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Jar File (*.jar)", "jar"));
                fileChooser.setAcceptAllFileFilterUsed(true);
            }
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                filePath.setText(file.getAbsolutePath());
            }
        });

        this.add(componentAlignedToCenter(new JLabel(text)));
        this.add(componentAlignedToCenter(filePath));
        this.add(componentAlignedToCenter(button));
    }

    /**
     * Returns selected file.
     *
     * @return Selected file
     */
    public File getFile() {
        return file;
    }
}
