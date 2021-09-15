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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.*;

/**
 *
 * @author peter
 */
public class DebuggerGUI extends JFrame implements ActionListener {
    public static final int DISPLAY_DECIMAL = 0;
    public static final int DISPLAY_HEX = 1;
    public static final int DISPLAY_BINARY = 2;
    
    public static final int DEBUGGER_LINES = 60;
     
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
        scrollpane2.setVisible(true);
        registerTable.setBounds(590, 20, 550, 500);
        add(scrollpane2);
        
        setSize(1170, 600);
        setTitle("Seiko Debugger GUI");
        setLayout(null);
        setVisible(true);
        
        runStepButton = new JButton("Run OpCode");
        runStepButton.setBounds(10,530,100,20);
        runStepButton.addActionListener(this);
        add(runStepButton);
        
        timer = new javax.swing.Timer(10, this);
        
        runContinuous = new JButton("Run every 1 sec");
        runContinuous.setBounds(120, 530, 100, 20);
        runContinuous.addActionListener(this);
        add(runContinuous);
        
        runStop = new JButton("Stop");
        runStop.setBounds(230, 530, 100, 20);
        runStop.addActionListener(this);
        add(runStop);
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
                for(int x=0; x<cal.getCodeLength(); x++) {
                    byte memVal = this.debugger.readMemoryByte(address + x);
                    String opCode = Integer.toHexString(Byte.toUnsignedInt(memVal));
                    
                    while(opCode.length() < 2)
                        opCode = "0" + opCode;
                    
                    bytes += "$" + opCode + " (" + (char)(memVal) + ") ";
                }
                
                this.addresses[i] = "$"+addrHex;
                this.opcodes[i] = bytes;
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
            System.out.println("pos: " + pos + ", value: " + fileByte);
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
                this.repaint();
                System.out.println("Run one step " + this.debugger.getProgramCounter());
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
                this.repaint();
                System.out.println("Run one step " + this.debugger.getProgramCounter());
            } 
            catch (MemoryException | OpCodeException ex) {
                Logger.getLogger(DebuggerGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }
}
