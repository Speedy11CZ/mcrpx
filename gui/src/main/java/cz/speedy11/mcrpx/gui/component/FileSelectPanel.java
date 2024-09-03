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


import com.sun.javafx.util.Utils;
import cz.speedy11.mcrpx.gui.Main;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.FileChooserUI;
import java.awt.*;
import java.io.File;
import java.lang.annotation.Native;

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
     * Returns selected file.
     *
     * @return Selected file
     */
    public File getFile() {
        return file;
    }

    /**
     * Creates new panel with file selector.
     *
     * @param text      Text displayed before file selector
     */
    public FileSelectPanel(String text) {
        super();

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        JTextField filePath = new JTextField();
        filePath.setEditable(false);

        JButton button = new JButton("...");

        FileChooser fileChooser = new FileChooser();
        button.addActionListener(e -> {
            Platform.runLater(() -> {
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Minecraft Assets File (*.zip;*.jar)", "*.zip", "*.jar"),
                        new FileChooser.ExtensionFilter("Zip File Format (*.zip)", "*.zip"),
                        new FileChooser.ExtensionFilter("Jar File (*.jar)", "*.jar")
                );
                fileChooser.setSelectedExtensionFilter(fileChooser.getExtensionFilters().get(0));
                file = fileChooser.showOpenDialog(null);
                if (file != null) {
                    filePath.setText(file.getAbsolutePath());
                }
            });
        });

        this.add(componentAlignedToCenter(new JLabel(text)));
        this.add(componentAlignedToCenter(filePath));
        this.add(componentAlignedToCenter(button));
    }
}
