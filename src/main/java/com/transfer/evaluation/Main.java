package com.transfer.evaluation;

import com.transfer.evaluation.gui.SystemInfoGUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SystemInfoGUI::createAndShowGUI);
    }
}
