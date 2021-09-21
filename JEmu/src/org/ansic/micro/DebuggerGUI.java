/*
 * Copyright (C) 2021 peter.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.ansic.micro;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;

/**
 *
 * @author peter
 */
public class DebuggerGUI extends JFrame implements ActionListener, ListSelectionListener {
    public static final int DISPLAY_DECIMAL = 0;
    public static final int DISPLAY_HEX = 1;
    public static final int DISPLAY_BINARY = 2;
    
    public static final int DEBUGGER_LINES = 600;
     
    Debugger debugger;
    long startAddress = 0x1800L;
    int displayType = DISPLAY_HEX;
    String[] addresses = new String[DEBUGGER_LINES];
    String[] opcodes = new String[DEBUGGER_LINES];
    String[] mnemonics = new String[DEBUGGER_LINES];
    String[] registerNames;
    String[] registerValues;
    
    TableModel registerModel;
    TableModel dataModel;
    
    JTable opCodeTable;
    JTable registerTable;
    JButton runStepButton;
    javax.swing.Timer timer;
    JButton runContinuous;
    JButton runStop;
    JTextField addressTextField;
    JButton goToAddressButton;
    JTextField contentTextField;
    JButton contentChangeButton;
    JTextField opCodeTextField;
    JButton opCodeChangeButton;
    JTextField registerNameTextField;
    JTextField registerValueTextField;
    JButton registerChangeButton;
    JButton breakPointButton;
    
    List<Integer> breakPoints = new ArrayList<>();
    
    public DebuggerGUI(Debugger debugger) {
        this.debugger = debugger;
        
        initialize();
    }
    
    public final void initialize() {
        createOpCodeTable();
        createRegisterTable();
        
        this.startAddress = debugger.getProgramCounter();
        
        this.dataModel = new AbstractTableModel() {
            public int getColumnCount() { return 3; }
            public int getRowCount() { return DEBUGGER_LINES;}
            public Object getValueAt(int row, int col) { 
                switch (col) {
                    case 0:
                        return addresses[row];
                    case 1:
                        return opcodes[row];
                    case 2:
                        return mnemonics[row];
                }
                
                return "";
            }
        };
        opCodeTable = new JTable(this.dataModel);
        JScrollPane scrollpane = new JScrollPane(opCodeTable);
        scrollpane.setBounds(20,20,550,500);
        opCodeTable.setBounds(20,20,550,500);
        opCodeTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        opCodeTable.getSelectionModel().addListSelectionListener(this);
        scrollpane.setVisible(true);
        
        add(scrollpane);
        //add(table);
      
        this.registerModel = new AbstractTableModel() {
            public int getColumnCount() { return 2; }
            public int getRowCount() { return registerNames.length; }
            public Object getValueAt(int row, int col) {
                switch (col) {
                    case 0:
                        return registerNames[row];
                        
                    case 1:
                        return registerValues[row];
                }
                return "";
            }
        };
        registerTable = new JTable(this.registerModel);
        JScrollPane scrollpane2 = new JScrollPane(registerTable);
        scrollpane2.setBounds(590,20,550,500);
        registerTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        registerTable.getSelectionModel().addListSelectionListener(this);
        scrollpane2.setVisible(true);
        registerTable.setBounds(590, 20, 550, 500);
        add(scrollpane2);
        
        setSize(1170, 610);
        setTitle("Seiko Debugger GUI");
        setLayout(null);
        setVisible(true);
        
        runStepButton = new JButton("Run OpCode");
        runStepButton.setBounds(10,530,100,20);
        runStepButton.addActionListener(this);
        add(runStepButton);
        
        timer = new javax.swing.Timer(1, this);
        
        runContinuous = new JButton("Run every 1 sec");
        runContinuous.setBounds(120, 530, 100, 20);
        runContinuous.addActionListener(this);
        add(runContinuous);
        
        runStop = new JButton("Stop");
        runStop.setBounds(230, 530, 100, 20);
        runStop.addActionListener(this);
        add(runStop);
        
        addressTextField = new JTextField();
        addressTextField.setBounds(10,560,80,20);
        addressTextField.addActionListener(this);
        add(addressTextField);
        
        goToAddressButton = new JButton("Goto");
        goToAddressButton.setBounds(90,560,50,20);
        goToAddressButton.addActionListener(this);
        add(goToAddressButton);
        
        breakPointButton = new JButton("Break");
        breakPointButton.setBounds(140,560,50,20);
        breakPointButton.addActionListener(this);
        add(breakPointButton);
        
        contentTextField = new JTextField();
        contentTextField.setBounds(200,560,130,20);
        contentTextField.addActionListener(this);
        add(contentTextField);
        
        contentChangeButton = new JButton("Store");
        contentChangeButton.setBounds(330,560,50,20);
        contentChangeButton.addActionListener(this);
        add(contentChangeButton);
        
        opCodeTextField = new JTextField();
        opCodeTextField.setBounds(390,560,130,20);
        opCodeTextField.addActionListener(this);
        add(opCodeTextField);
        
        opCodeChangeButton = new JButton("Store");
        opCodeChangeButton.setBounds(520,560,50,20);
        opCodeChangeButton.addActionListener(this);
        add(opCodeChangeButton);

        registerNameTextField = new JTextField();
        registerNameTextField.setBounds(590,560,250,20);
        registerNameTextField.addActionListener(this);
        add(registerNameTextField);
        
        registerValueTextField = new JTextField();
        registerValueTextField.setBounds(850,560,230,20);
        registerValueTextField.addActionListener(this);
        add(registerValueTextField);
        
        registerChangeButton = new JButton("Change");
        registerChangeButton.setBounds(1090,560,60,20);
        registerChangeButton.addActionListener(this);
        add(registerChangeButton);
    }
    
    protected void createRegisterTable() {
        List<String> regNamList = this.debugger.getRegisterNames();
        
        this.registerNames = new String[regNamList.size()];
        this.registerValues = new String[regNamList.size()];
        
        int pos = 0;
        for(String regName : regNamList) {
            this.registerNames[pos] = regName;
            try {
                long value = this.debugger.getRegisterValue(regName);
                
                switch (this.displayType) {
                    case DISPLAY_DECIMAL:
                        this.registerValues[pos] = Long.toString(value);
                        break;
                        
                    case DISPLAY_HEX:
                        String hexVal = Long.toHexString(value);
                        int size = this.debugger.getRegisterSize(regName) / 4;
                        
                        while(hexVal.length() < size) {
                            hexVal = "0" + hexVal;
                        }
                        this.registerValues[pos] = "$" + hexVal;
                        break;
                        
                    case DISPLAY_BINARY:
                        String  binVal = Long.toBinaryString(value);
                        int bitSize = this.debugger.getRegisterSize(regName);
                        
                        while(binVal.length() < bitSize) {
                            binVal = "0" + binVal;
                        }
                        this.registerValues[pos] = "b" + binVal;
                        break;
                        
                    default:
                        throw new AssertionError();
                }
            }
            catch (Exception ex) {
                
            }
            
            pos++;
        }
    }
    
    protected void createOpCodeTable() {
        long address = this.startAddress;
        for(int i=0; i<DEBUGGER_LINES; i++) {
            try {
                Debugger.CodeAndLength cal = debugger.getCodeAndLength(address);
                
                String addrHex = Long.toHexString(address);
                while(addrHex.length() < 4)
                    addrHex = "0" + addrHex;
                
                String bytes = "";
                String txt = "";
                for(int x=0; x<cal.getCodeLength(); x++) {
                    byte memVal = this.debugger.readMemoryByte(address + x);
                    String opCode = Integer.toHexString(Byte.toUnsignedInt(memVal));
                    
                    while(opCode.length() < 2)
                        opCode = "0" + opCode;
                    
                    bytes += opCode;
                    txt += (char)(memVal);
                }
                
                this.addresses[i] = "$"+addrHex;
                this.opcodes[i] = "$" + bytes + " (" + txt + ")";
                this.mnemonics[i] = cal.getCode();
                
                address += cal.getCodeLength();
            }
            catch (Exception ex) {
                ex.printStackTrace();
                this.addresses[i] = "ERR";
                this.opcodes[i] = "ERR";
                this.mnemonics[i] = "ERR";
                address++;
            }
        }
    }
    
    public static void main(String args[]) throws Exception {
        Memory mem = new RAMMemoryLittleEndian(8192);
        SeikoUC2000Display display = new SeikoUC2000Display();
        
        // fill memory with something
        java.io.InputStream is = Class.class.getResourceAsStream("/tetris.bin");
        int fileByte = is.read();
        int pos = 0x1800;
        while(fileByte >= 0) {
            mem.setByte(pos, (byte)(fileByte));
            fileByte = is.read();
            pos++;
        }
            
        is.close();
        
        SeikoUC2000Debugger uc2000 = new SeikoUC2000Debugger(mem, display);
//        uc2000.setProgramCounter(0x1800);
        
        DebuggerGUI dg = new DebuggerGUI(uc2000);
        display.setHandler((IRQHandler)uc2000);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == this.runStepButton) {
            try {
                this.debugger.runNextOpCode();
                this.startAddress = this.debugger.getProgramCounter();
                this.createOpCodeTable();
                this.createRegisterTable();
                this.copyToTextFields();
                this.copyRegisterToTextField();
                this.repaint();
            } 
            catch (MemoryException | OpCodeException ex) {
                Logger.getLogger(DebuggerGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        else if(e.getSource() == this.runContinuous) {
            this.timer.start();
        }
        else if(e.getSource() == this.runStop) {
            this.timer.stop();
        }
        else if(e.getSource() == this.timer) {
            try {
                this.debugger.runNextOpCode();
                this.startAddress = this.debugger.getProgramCounter();
                this.createOpCodeTable();
                this.createRegisterTable();
                this.copyToTextFields();
                this.copyRegisterToTextField();
                this.repaint();
                
                int currPC = (int)this.debugger.getProgramCounter();
                for(Integer address : this.breakPoints) {
                    if(currPC == address) {
                        this.timer.stop();
                    }
                }
//                if(this.debugger.getProgramCounter() == 0x18F4) this.timer.stop();
            } 
            catch (MemoryException | OpCodeException ex) {
                Logger.getLogger(DebuggerGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else if(e.getSource() == this.goToAddressButton) {
            String addressText = this.addressTextField.getText();
            
            if(addressText.equalsIgnoreCase("pc")) {
                this.startAddress = this.debugger.getProgramCounter();
            }
            else if(addressText.startsWith("$")){
                try {
                    int value = Integer.parseInt(addressText.substring(1), 16);
                    
                    this.startAddress = value;
                }
                catch (NumberFormatException ex) {
                    
                }
            }
            else {
                try {
                    int value = Integer.parseInt(addressText);
                    
                    this.startAddress = value;
                }
                catch (NumberFormatException ex) {
                    
                }
            }
            
            this.createOpCodeTable();
            this.createRegisterTable();
            this.repaint();
        }
        else if(e.getSource() == this.contentChangeButton) {
            String contentText = this.contentTextField.getText();
            String addressText = this.addressTextField.getText();
            int addressValue = 0;
            int contentValue = 0;
            boolean error = false;
            
            if(addressText.equalsIgnoreCase("pc")) {
                addressValue = (int)this.debugger.getProgramCounter();
            }
            else if(addressText.startsWith("$")){
                try {
                    addressValue = Integer.parseInt(addressText.substring(1), 16);
                }
                catch (NumberFormatException ex) {
                    error = true;
                }
            }
            else {
                try {
                    addressValue = Integer.parseInt(addressText);
                }
                catch (NumberFormatException ex) {
                    error = true;
                }
            }
            
            if(contentText.startsWith("$")){
                try {
                    contentValue = Integer.parseInt(contentText.substring(1), 16);
                }
                catch (NumberFormatException ex) {
                    error = true;
                }
            }
            else {
                try {
                    contentValue = Integer.parseInt(contentText);
                }
                catch (NumberFormatException ex) {
                    error = true;
                }
            }
            
            if(!error) {
                try {
                    // TODO: This is big endian, cater for little endian in debugger interface!!
                    this.debugger.writeMemoryByte(addressValue+1, (byte)(contentValue & 0x00FF));
                    this.debugger.writeMemoryByte(addressValue, (byte)((contentValue >> 8) & 0x00FF));
                    this.createOpCodeTable();
                    this.createRegisterTable();
                    this.repaint();
                }
                catch (MemoryException ex) {
                    // TODO
                }
            }
        }
        else if(e.getSource() == this.registerChangeButton) {
            String valueText = this.registerValueTextField.getText();
            String registerName = this.registerNameTextField.getText();
            int value = 0;
            boolean error = false;
            
            if(valueText.startsWith("$")){
                try {
                    value = Integer.parseInt(valueText.substring(1), 16);
                }
                catch (NumberFormatException ex) {
                    error = true;
                }
            }
            else {
                try {
                    value = Integer.parseInt(valueText);
                }
                catch (NumberFormatException ex) {
                    error = true;
                }
            }
            
            if(!error) {
                try {
                    debugger.setRegisterValue(registerName, value);
                    this.createOpCodeTable();
                    this.createRegisterTable();
                    this.repaint();
                }
                catch (IllegalRegisterException ex) {
                    // Nothing to do here :)
                }
            }
        }
        else if(e.getSource() == this.breakPointButton) {
            String addressText = this.addressTextField.getText();
            Integer address = null; 
            boolean error = false;
            
            if(addressText.equalsIgnoreCase("pc")) {
                address = (int)this.debugger.getProgramCounter();
            }
            else if(addressText.startsWith("$")){
                try {
                    address = Integer.parseInt(addressText.substring(1), 16);
                }
                catch (NumberFormatException ex) {
                    error = true;
                }
            }
            else {
                try {
                    address = Integer.parseInt(addressText);
                }
                catch (NumberFormatException ex) {
                    error = true;
                }
            }
            if(!error) {
                if(this.breakPoints.contains(address)) {
                    // remove when it exists
                    this.breakPoints.remove(address);
                }
                else {
                    // add when new
                    this.breakPoints.add(address);
                }
            }
        }
    }

    public void copyToTextFields() {
        int row = this.opCodeTable.getSelectedRow();

        if(row >= 0) {
            this.addressTextField.setText(this.addresses[row]);
            this.contentTextField.setText(this.opcodes[row].substring(0, 5));
            this.opCodeTextField.setText(this.mnemonics[row]);
        }
    }
    
    public void copyRegisterToTextField() {
        int row = this.registerTable.getSelectedRow();
        
        if(row >= 0) {
            this.registerNameTextField.setText(this.registerNames[row]);
            this.registerValueTextField.setText(this.registerValues[row]);
        }
    }
    
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if(e.getSource() instanceof DefaultListSelectionModel) {
            DefaultListSelectionModel lsm = (DefaultListSelectionModel)e.getSource();
            
            if(lsm == this.opCodeTable.getSelectionModel()) {
                copyToTextFields();
            }
            else if(lsm == this.registerTable.getSelectionModel()) {
                copyRegisterToTextField();
            }
        }
    }
}
