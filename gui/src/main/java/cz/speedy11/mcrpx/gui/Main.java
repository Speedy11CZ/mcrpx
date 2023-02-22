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

package cz.speedy11.mcrpx.gui;

import cz.speedy11.mcrpx.gui.component.ExtractorPanel;
import cz.speedy11.mcrpx.gui.util.ImageUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Main class of the GUI application.
 *
 * @author Michal Spišak (Speedy11CZ)
 * @since 1.1.0
 */
public class Main {

    public static final int PANEL_MARGIN = 10;
    public static final int COMPONENT_MARGIN = 5;
    public static final Image ICON = ImageUtil.getImageFromBase64("iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAMAAABEpIrGAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAA5UExURb2EQLB+O72LRJltORoVDR4YDqJzOLosaJQjUz5OYjZDVC9TIyZFHItfL2RSHH1nImRkZE1NTZFmMlLMa8oAAAAJcEhZcwAADsMAAA7DAcdvqGQAAACoSURBVDhPpZLbEoMgDEQT0uI1ovz/xzZAW7QPWWd6nhY8MxIWImImCl+uWeSO8DjxrPQUIxaYh2Ecp2mel2Vdf7P9AgoinqCKBaJtS2nfjyOlzbhm+wwF/5B2iVCI0RPsoqCgRjZEci65UNafHSy0ess0zLXjE2QHuCPUnt+0knuCD8aEEMCYUFD1hHoPQAjBq9tGhcL/78EXbEwotHJbyVIpq149EFRfT0ohib4Q+AcAAAAASUVORK5CYII=");
    public static final Image BANNER = ImageUtil.getImageFromBase64("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");

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        JFrame frame = new JFrame();
        frame.setContentPane(new ExtractorPanel());
        frame.setTitle("MCRPX (" + (Main.class.getPackage().getSpecificationVersion()) + ")");
        frame.setIconImage(ICON);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
    }
}
