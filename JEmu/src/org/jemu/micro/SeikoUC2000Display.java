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
package org.jemu.micro;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * This class implements the display driver of the Seiko UC 2000 watch.
 * 
 * @author peter
 */
public class SeikoUC2000Display extends JFrame implements IO, ActionListener {
    
    // Character rom: 5x7 pixels
    public static final byte[][] characterRom = new byte[][] {
        {0,0,0,0,0,0,0},    // chr(0)
        {0,0,0,0,0,0,0},    // chr(1)
        {0,0,0,0,0,0,0},    // chr(2)
        {0,0,0,0,0,0,0},    // chr(3)
        {0,0,0,0,0,0,0},    // chr(4)
        {0,0,0,0,0,0,0},    // chr(5)
        {0,0,0,0,0,0,0},    // chr(6)
        {0,0,0,0,0,0,0},    // chr(7)
        {0,0,0,0,0,0,0},    // chr(8)
        {0,0,0,0,0,0,0},    // chr(9)
        {0,0,0,0,0,0,0},    // chr(10)
        {0,0,0,0,0,0,0},    // chr(11)
        {0,0,0,0,0,0,0},    // chr(12)
        {0,0,0,0,0,0,0},    // chr(13)
        {0,0,0,0,0,0,0},    // chr(14)
        {0,0,0,0,0,0,0},    // chr(15)
        {0,0,0,0,0,0,0},    // chr(16)
        {0,0,0,0,0,0,0},    // chr(17)
        {0,0,0,0,0,0,0},    // chr(18)
        {0,0,0,0,0,0,0},    // chr(19)
        {0,0,0,0,0,0,0},    // chr(20)
        {0,0,0,0,0,0,0},    // chr(21)
        {0,0,0,0,0,0,0},    // chr(22)
        {0,0,0,0,0,0,0},    // chr(23)
        {0,0,0,0,0,0,0},    // chr(24)
        {0,0,0,0,0,0,0},    // chr(25)
        {0,0,0,0,0,0,0},    // chr(26)
        {0,0,0,0,0,0,0},    // chr(27)
        {0,0,0,0,0,0,0},    // chr(28)
        {0,0,0,0,0,0,0},    // chr(29)
        {0,0,0,0,0,0,0},    // chr(30)
        {0,0,0,0,0,0,0},    // chr(31)
        {0,0,0,0,0,0,0},    // chr(32) = ' '
        {0x04,0x04,0x04,0x04,0x04,0x00,0x04},   // chr(33) = '!'
        {0x0A,0x0A,0x0A,0x00,0x00,0x00,0x00},   // chr(34) = '"'
        {0x00,0x0A,0x1F,0x0A,0x1F,0x0A,0x00},   // chr(35) = '#'
        {0x04,0x1E,0x05,0x0E,0x14,0x0F,0x04},   // chr(36) = '$'
        {0x03,0x13,0x08,0x04,0x02,0x19,0x18},   // chr(37) = '%'
        {0x06,0x09,0x05,0x02,0x15,0x09,0x16},   // chr(38) = '&'
        {0x06,0x04,0x02,0x00,0x00,0x00,0x00},   // chr(39) = '\''
        {0x08,0x04,0x02,0x02,0x02,0x04,0x08},   // chr(40) = '('
        {0x02,0x04,0x08,0x08,0x08,0x04,0x02},   // chr(41) = ')'
        {0x00,0x04,0x15,0x0E,0x15,0x04,0x00},   // chr(42) = '*'
        {0x00,0x04,0x04,0x1F,0x04,0x04,0x00},   // chr(43) = '+'
        {0x00,0x00,0x00,0x00,0x06,0x04,0x02},   // chr(44) = ','
        {0x00,0x00,0x00,0x1F,0x00,0x00,0x00},   // chr(45) = '-'
        {0x00,0x00,0x00,0x00,0x00,0x03,0x03},   // chr(46) = '.'
        {0x00,0x10,0x08,0x04,0x02,0x01,0x00},   // chr(47) = '/'
        {0x0E,0x11,0x19,0x15,0x13,0x11,0x0E},   // chr(48) = '0'
        {0x04,0x06,0x04,0x04,0x04,0x04,0x04},   // chr(49) = '1'
        {0x0E,0x11,0x10,0x0C,0x02,0x01,0x1F},   // chr(50) = '2'
        {0x0E,0x11,0x10,0x0C,0x10,0x11,0x0E},   // chr(51) = '3'
        {0x08,0x0C,0x0A,0x09,0x1F,0x08,0x08},   // chr(52) = '4'
        {0x1F,0x01,0x0F,0x10,0x10,0x11,0x0E},   // chr(53) = '5'
        {0x0E,0x11,0x01,0x0F,0x11,0x11,0x0E},   // chr(54) = '6'
        {0x1F,0x10,0x08,0x04,0x04,0x04,0x04},   // chr(55) = '7'
        {0x0E,0x11,0x11,0x0E,0x11,0x11,0x0E},   // chr(56) = '8'
        {0x0E,0x11,0x11,0x1E,0x10,0x11,0x0E},   // chr(57) = '9'
        {0x00,0x06,0x06,0x00,0x06,0x06,0x00},   // chr(58) = ':'
        {0x00,0x06,0x06,0x00,0x06,0x04,0x02},   // chr(59) = ';'
        {0x08,0x04,0x02,0x01,0x02,0x04,0x08},   // chr(60) = '<'
        {0x00,0x00,0x1F,0x00,0x1F,0x00,0x00},   // chr(61) = '='
        {0x02,0x04,0x08,0x10,0x08,0x04,0x02},   // chr(62) = '>'
        {0x0E,0x11,0x10,0x08,0x04,0x00,0x04},   // chr(63) = '?'
        {0x0E,0x11,0x10,0x16,0x15,0x15,0x0E},   // chr(64) = '@'
        {0x0E,0x11,0x11,0x1F,0x11,0x11,0x11},   // chr(65) = 'A'
        {0x0F,0x11,0x11,0x0F,0x11,0x11,0x0F},   // chr(66) = 'B'
        {0x0E,0x11,0x01,0x01,0x01,0x11,0x0E},   // chr(67) = 'C'
        {0x07,0x09,0x11,0x11,0x11,0x09,0x07},   // chr(68) = 'D'
        {0x1F,0x01,0x01,0x0F,0x01,0x01,0x1F},   // chr(69) = 'E'
        {0x1F,0x01,0x01,0x0F,0x01,0x01,0x01},   // chr(70) = 'F'
        {0x0E,0x11,0x01,0x1D,0x11,0x11,0x1E},   // chr(71) = 'G'
        {0x11,0x11,0x11,0x1F,0x11,0x11,0x11},   // chr(72) = 'H'
        {0x0E,0x04,0x04,0x04,0x04,0x04,0x0E},   // chr(73) = 'I'
        {0x1C,0x08,0x08,0x08,0x08,0x09,0x06},   // chr(74) = 'J'
        {0x11,0x09,0x05,0x03,0x05,0x09,0x11},   // chr(75) = 'K'
        {0x01,0x01,0x01,0x01,0x01,0x01,0x1F},   // chr(76) = 'L'
        {0x11,0x1B,0x15,0x15,0x11,0x11,0x11},   // chr(77) = 'M'
        {0x11,0x13,0x15,0x19,0x11,0x11,0x11},   // chr(78) = 'N'
        {0x0E,0x11,0x11,0x11,0x11,0x11,0x0E},   // chr(79) = 'O'
        {0x0F,0x11,0x11,0x0F,0x01,0x01,0x01},   // chr(80) = 'P'
        {0x0E,0x11,0x11,0x11,0x15,0x19,0x16},   // chr(81) = 'Q'
        {0x0F,0x11,0x11,0x0F,0x05,0x09,0x11},   // chr(82) = 'R'
        {0x0E,0x11,0x01,0x0E,0x10,0x11,0x0E},   // chr(83) = 'S'
        {0x1F,0x04,0x04,0x04,0x04,0x04,0x04},   // chr(84) = 'T'
        {0x11,0x11,0x11,0x11,0x11,0x11,0x0E},   // chr(85) = 'U'
        {0x11,0x11,0x11,0x11,0x11,0x0A,0x04},   // chr(86) = 'V'
        {0x11,0x11,0x15,0x15,0x15,0x0A,0x0A},   // chr(87) = 'W'
        {0x11,0x0A,0x04,0x04,0x04,0x0A,0x11},   // chr(88) = 'X'
        {0x11,0x11,0x0A,0x04,0x04,0x04,0x04},   // chr(89) = 'Y'
        {0x1F,0x10,0x08,0x04,0x02,0x01,0x1F},   // chr(90) = 'Z'
        {0x07,0x01,0x01,0x01,0x01,0x01,0x07},   // chr(91) = '['
        {0x11,0x0A,0x1F,0x04,0x1F,0x04,0x04},   // chr(92) = 'Yen' ??
        {0x1C,0x10,0x10,0x10,0x10,0x10,0x1C},   // chr(93) = ']'
        {0x00,0x00,0x04,0x0A,0x11,0x00,0x00},   // chr(94) = '^'
        {0x00,0x00,0x00,0x00,0x00,0x00,0x1F},   // chr(95) = '_'
        {0x04,0x0E,0x15,0x04,0x0E,0x11,0x0E},   // chr(96) = 'male' ??
        {0x00,0x00,0x0E,0x10,0x1E,0x11,0x1E},   // chr(97) = 'a'
        {0x01,0x01,0x0D,0x13,0x11,0x11,0x0F},   // chr(98) = 'b'
        {0x00,0x00,0x0E,0x01,0x01,0x11,0x0E},   // chr(99) = 'c'
        {0x10,0x10,0x16,0x19,0x11,0x11,0x1E},   // chr(100) = 'd'
        {0x00,0x00,0x0E,0x11,0x1F,0x01,0x0E},   // chr(101) = 'e'
        {0x0C,0x12,0x02,0x07,0x02,0x02,0x02},   // chr(102) = 'f'
        {0x00,0x1E,0x11,0x11,0x1E,0x10,0x0E},   // chr(103) = 'g'
        {0x01,0x01,0x0D,0x13,0x11,0x11,0x11},   // chr(104) = 'h'
        {0x04,0x00,0x06,0x04,0x04,0x04,0x0E},   // chr(105) = 'i'
        {0x08,0x00,0x0C,0x08,0x08,0x09,0x06},   // chr(106) = 'j'
        {0x01,0x01,0x09,0x05,0x03,0x05,0x09},   // chr(107) = 'k'
        {0x06,0x04,0x04,0x04,0x04,0x04,0x0E},   // chr(108) = 'l'
        {0x00,0x00,0x0B,0x15,0x15,0x15,0x15},   // chr(109) = 'm'
        {0x00,0x00,0x0D,0x13,0x11,0x11,0x11},   // chr(110) = 'n'
        {0x00,0x00,0x0E,0x11,0x11,0x11,0x0E},   // chr(111) = 'o'
        {0x00,0x00,0x0F,0x11,0x0F,0x01,0x01},   // chr(112) = 'p'
        {0x00,0x00,0x16,0x19,0x1E,0x10,0x10},   // chr(113) = 'q'
        {0x00,0x00,0x0D,0x13,0x01,0x01,0x01},   // chr(114) = 'r'
        {0x00,0x00,0x0E,0x01,0x0E,0x10,0x0F},   // chr(115) = 's'
        {0x02,0x02,0x07,0x02,0x02,0x12,0x0C},   // chr(116) = 't'
        {0x00,0x00,0x11,0x11,0x11,0x19,0x16},   // chr(117) = 'u'
        {0x00,0x00,0x11,0x11,0x11,0x0A,0x04},   // chr(118) = 'v'
        {0x00,0x00,0x11,0x15,0x15,0x15,0x0A},   // chr(119) = 'w'
        {0x00,0x00,0x11,0x0A,0x04,0x0A,0x11},   // chr(120) = 'x'
        {0x00,0x00,0x11,0x11,0x1E,0x10,0x0E},   // chr(121) = 'y'
        {0x00,0x00,0x1F,0x08,0x04,0x02,0x1F},   // chr(122) = 'z'
        {0x00,0x11,0x0A,0x04,0x0A,0x11,0x00},   // chr(123) = 'x mittig'
        {0x0E,0x11,0x0E,0x04,0x1F,0x04,0x04},   // chr(124) = 'female' ??
        {0x00,0x04,0x00,0x1F,0x00,0x04,0x00},   // chr(125) = 'geteilt'
        {0x1E,0x12,0x1E,0x12,0x1E,0x12,0x19},   // chr(126) = '???'
        {0x1E,0x12,0x12,0x1E,0x12,0x12,0x1E},   // chr(127) = 'kleine 8'??
        {0x00,0x00,0x00,0x0E,0x11,0x11,0x11},   // chr(128) = '0 - upper half'
        {0x11,0x11,0x11,0x11,0x11,0x11,0x0E},   // chr(129) = '0 - lower half'
        {0x00,0x00,0x00,0x04,0x06,0x04,0x04},   // chr(130) = '1 - upper half'
        {0x04,0x04,0x04,0x04,0x04,0x04,0x04},   // chr(131) = '1 - lower half'
        {0x00,0x00,0x00,0x0E,0x11,0x10,0x10},   // chr(132) = '2 - upper half'
        {0x10,0x08,0x04,0x02,0x01,0x01,0x1F},   // chr(133) = '2 - lower half'
        {0x00,0x00,0x00,0x0E,0x11,0x10,0x10},   // chr(134) = '3 - upper half'
        {0x0C,0x10,0x10,0x10,0x10,0x11,0x0E},   // chr(135) = '3 - lower half'
        {0x00,0x00,0x00,0x08,0x0C,0x0C,0x0A},   // chr(136) = '4 - upper half'
        {0x09,0x09,0x09,0x1F,0x08,0x08,0x08},   // chr(137) = '4 - lower half'
        {0x00,0x00,0x00,0x1F,0x01,0x01,0x01},   // chr(138) = '5 - upper half'
        {0x0F,0x10,0x10,0x10,0x10,0x11,0x0E},   // chr(139) = '5 - lower half'
        {0x00,0x00,0x00,0x0E,0x11,0x01,0x01},   // chr(140) = '6 - upper half‘
        {0x0F,0x11,0x11,0x11,0x11,0x11,0x0E},   // chr(141) = '6 - lower half'
        {0x00,0x00,0x00,0x1F,0x10,0x10,0x10},   // chr(142) = '7 - upper half'
        {0x08,0x04,0x04,0x04,0x04,0x04,0x04},   // chr(143) = '7 - lower half'
        {0x00,0x00,0x00,0x0E,0x11,0x11,0x11},   // chr(144) = '8 - upper half'
        {0x0E,0x11,0x11,0x11,0x11,0x11,0x0E},   // chr(145) = '8 - lower half'
        {0x00,0x00,0x00,0x0E,0x11,0x11,0x11},   // chr(146) = '9 - upper half'
        {0x1E,0x10,0x10,0x10,0x10,0x11,0x0E},   // chr(147) = '9 - lower half'
        {0x00,0x00,0x00,0x00,0x06,0x06,0x00},   // chr(148) = '. - middle'
        {0x00,0x00,0x00,0x0C,0x0C,0x00,0x00},   // chr(149) = '. - middle'
        {0x04,0x0E,0x0E,0x0E,0x1F,0x04,0x00},   // chr(150) = 'bell'
        {0x00,0x04,0x0E,0x1F,0x1F,0x04,0x0E},   // chr(151) = 'spades'
        {0x00,0x00,0x1B,0x1F,0x1F,0x0E,0x04},   // chr(152) = 'hearts'
        {0x00,0x04,0x0E,0x1F,0x1F,0x0E,0x04},   // chr(153) = 'diamonds'
        {0x00,0x04,0x0E,0x15,0x1F,0x04,0x0E},   // chr(154) = 'clubs'
        {0x0C,0x1C,0x18,0x18,0x18,0x1C,0x0C},   // chr(155) = 'phone'
        {0x04,0x04,0x0E,0x1F,0x15,0x04,0x0E},   // chr(156) = 'plane'
        {0x00,0x1F,0x1F,0x0E,0x04,0x04,0x0E},   // chr(157) = 'cup'
        {0x04,0x1D,0x17,0x04,0x0D,0x09,0x19},   // chr(158) = 'runner'
        {0x11,0x1B,0x1B,0x1B,0x15,0x11,0x1B},   // chr(159) = '???'
        {0x00,0x1F,0x00,0x1F,0x04,0x04,0x04},   // chr(160) = '???'
        {0x00,0x00,0x00,0x00,0x07,0x05,0x07},   // chr(161) = '???'
        {0x1C,0x04,0x04,0x00,0x00,0x00,0x00},   // chr(162) = '???'
        {0x00,0x00,0x00,0x00,0x04,0x04,0x07},   // chr(163) = '???'
        {0x00,0x00,0x00,0x00,0x01,0x02,0x04},   // chr(164) = '???'
        {0x00,0x00,0x00,0x00,0x00,0x00,0x00},   // chr(165) = ' '
        {0x1F,0x10,0x1F,0x10,0x10,0x08,0x04},   // chr(166) = '???kanji???'
        {0x00,0x00,0x1F,0x10,0x0C,0x04,0x02},   // chr(167) = '???kanji???'
        {0x00,0x00,0x08,0x04,0x06,0x05,0x04},   // chr(168) = '???kanji???'
        {0x00,0x00,0x04,0x1F,0x11,0x10,0x08},   // chr(169) = '???kanji???'
        {0x00,0x00,0x00,0x1F,0x04,0x04,0x1F},   // chr(170) = '???kanji???'
        {0x00,0x00,0x08,0x1F,0x0C,0x0A,0x09},   // chr(171) = '???kanji???'
        {0x00,0x00,0x02,0x1F,0x12,0x0A,0x02},   // chr(172) = '???kanji???'
        {0x00,0x00,0x00,0x0E,0x08,0x08,0x1F},   // chr(173) = '???kanji???'
        {0x00,0x00,0x0F,0x08,0x0F,0x08,0x0F},   // chr(174) = '???kanji???'
        {0x00,0x00,0x00,0x15,0x15,0x10,0x0C},   // chr(175) = '???kanji???'
        {0x00,0x00,0x00,0x1F,0x00,0x00,0x00},   // chr(176) = '???kanji???'
        {0x1F,0x10,0x14,0x0C,0x04,0x04,0x02},   // chr(177) = '???kanji???'
        {0x10,0x08,0x04,0x06,0x05,0x04,0x04},   // chr(178) = '???kanji???'
        {0x04,0x1F,0x11,0x11,0x10,0x08,0x04},   // chr(179) = '???kanji???'
        {0x1F,0x04,0x04,0x04,0x04,0x04,0x1F},   // chr(180) = 'I - ???kanji???'
        {0x08,0x1F,0x08,0x0C,0x0A,0x09,0x08},   // chr(181) = '???kanji???'
        {0x02,0x1F,0x12,0x12,0x12,0x12,0x09},   // chr(182) = '???kanji???'
        {0x04,0x1F,0x04,0x1F,0x04,0x04,0x04},   // chr(183) = '???kanji??? - Antenne'
        {0x1E,0x12,0x11,0x10,0x08,0x04,0x02},   // chr(184) = '???kanji???'
        {0x02,0x1E,0x09,0x08,0x08,0x08,0x04},   // chr(185) = '???kanji???'
        {0x1F,0x10,0x10,0x10,0x10,0x10,0x1F},   // chr(186) = '] - ???kanji???'
        {0x0A,0x1F,0x0A,0x0A,0x08,0x04,0x02},   // chr(187) = '???kanji???'
        {0x03,0x00,0x13,0x10,0x10,0x08,0x07},   // chr(188) = '???kanji???'
        {0x1F,0x10,0x10,0x08,0x04,0x0A,0x11},   // chr(189) = '???kanji???'
        {0x02,0x1F,0x12,0x0A,0x02,0x02,0x1C},   // chr(190) = '???kanji???'
        {0x11,0x11,0x12,0x10,0x08,0x04,0x02},   // chr(191) = '???kanji???'
        {0x1E,0x12,0x15,0x18,0x08,0x04,0x02},   // chr(192) = '???kanji???'
        {0x08,0x07,0x04,0x1F,0x04,0x04,0x02},   // chr(193) = '???kanji???'
        {0x15,0x15,0x15,0x10,0x10,0x08,0x04},   // chr(194) = '???kanji???'
        {0x0E,0x00,0x1F,0x04,0x04,0x04,0x02},   // chr(195) = '???kanji???'
        {0x02,0x02,0x02,0x06,0x0A,0x02,0x02},   // chr(196) = '???kanji???'
        {0x04,0x04,0x1F,0x04,0x04,0x04,0x02},   // chr(197) = '???kanji???'
        {0x0E,0x00,0x00,0x00,0x00,0x00,0x1F},   // chr(198) = '???kanji??? - ober + unter strich'
        {0x1F,0x10,0x10,0x0A,0x04,0x0A,0x02},   // chr(199) = '???kanji???'
        {0x04,0x1F,0x08,0x04,0x0E,0x15,0x04},   // chr(200) = '???kanji???'
        {0x08,0x08,0x08,0x08,0x08,0x04,0x02},   // chr(201) = '???kanji???'
        {0x04,0x08,0x10,0x10,0x11,0x11,0x11},   // chr(202) = '???kanji???'
        {0x01,0x01,0x1F,0x01,0x01,0x01,0x1E},   // chr(203) = '???kanji???'
        {0x1F,0x10,0x10,0x10,0x10,0x08,0x06},   // chr(204) = '???kanji???'
        {0x00,0x02,0x05,0x08,0x10,0x10,0x00},   // chr(205) = '???kanji???'
        {0x04,0x1F,0x04,0x04,0x15,0x15,0x15},   // chr(206) = '???kanji???'
        {0x1F,0x10,0x10,0x10,0x0A,0x04,0x08},   // chr(207) = '???kanji???'
        {0x06,0x08,0x02,0x04,0x08,0x02,0x0C},   // chr(208) = '???kanji???'
        {0x04,0x02,0x01,0x01,0x11,0x1F,0x10},   // chr(209) = '???kanji???'
        {0x10,0x10,0x10,0x0A,0x04,0x0A,0x01},   // chr(210) = '???kanji???'
        {0x1F,0x02,0x1F,0x02,0x02,0x02,0x1C},   // chr(211) = '???kanji???'
        {0x02,0x02,0x1F,0x12,0x0A,0x02,0x02},   // chr(212) = '???kanji???'
        {0x0E,0x08,0x08,0x08,0x08,0x08,0x1F},   // chr(213) = '???kanji???'
        {0x1F,0x10,0x10,0x1F,0x10,0x10,0x1F},   // chr(214) = '???kanji??? umgedrehtes E'
        {0x0E,0x00,0x1F,0x10,0x10,0x08,0x06},   // chr(215) = '???kanji???'
        {0x09,0x09,0x09,0x09,0x08,0x04,0x02},   // chr(216) = '???kanji???'
        {0x04,0x05,0x05,0x05,0x05,0x15,0x0D},   // chr(217) = '???kanji???'
        {0x01,0x01,0x01,0x11,0x09,0x05,0x03},   // chr(218) = '???kanji???'
        {0x0F,0x11,0x11,0x11,0x11,0x11,0x1F},   // chr(219) = '???kanji??? - O eckig'
        {0x1F,0x11,0x11,0x10,0x10,0x08,0x04},   // chr(220) = '???kanji???'
        {0x03,0x00,0x10,0x10,0x10,0x08,0x07},   // chr(221) = '???kanji???'
        {0x04,0x09,0x02,0x00,0x00,0x00,0x00},   // chr(222) = '???kanji???'
        {0x07,0x05,0x07,0x00,0x00,0x00,0x00},   // chr(223) = '???kanji???'
        {0x1F,0x11,0x02,0x04,0x02,0x11,0x1F},   // chr(224) = 'Epsilon'
        {0x18,0x04,0x04,0x04,0x04,0x04,0x03},   // chr(225) = 'sigma'
        {0x00,0x10,0x18,0x14,0x12,0x1F,0x00},   // chr(226) = 'Delta'
        {0x00,0x1C,0x04,0x04,0x05,0x02,0x00},   // chr(227) = 'Root'
        {0x00,0x1F,0x0A,0x0A,0x0A,0x19,0x00},   // chr(228) = 'pi'
        {0x1C,0x1C,0x1C,0x1C,0x1C,0x1C,0x1C},   // chr(229) = 'gfx3r 246‘
        {0x07,0x07,0x07,0x07,0x07,0x07,0x07},   // chr(230) = 'gfx3r 135'
        {0x1F,0x1F,0x1F,0x00,0x00,0x00,0x00},   // chr(231) = 'gfx1r 12'
        {0x00,0x00,0x00,0x00,0x1F,0x1F,0x1F},   // chr(232) = 'gfx2r 34‘
        {0x07,0x07,0x07,0x00,0x00,0x00,0x00},   // chr(233) = 'gfx3r 1'
        {0x07,0x07,0x07,0x07,0x07,0x00,0x00},   // chr(234) = 'gfx3r 13'
        {0x1C,0x1C,0x1C,0x00,0x00,0x00,0x00},   // chr(235) = 'gfx3r 2'
        {0x1C,0x1C,0x1C,0x1C,0x1C,0x00,0x00},   // chr(236) = 'gfx3r 24'
        {0x00,0x00,0x00,0x00,0x07,0x07,0x07},   // chr(237) = 'gfx3r 5'
        {0x00,0x00,0x07,0x07,0x07,0x07,0x07},   // chr(238) = 'gfx3r 35'
        {0x00,0x00,0x00,0x00,0x1C,0x1C,0x1C},   // chr(239) = 'gfx3r 6'
        {0x00,0x00,0x1C,0x1C,0x1C,0x1C,0x1C},   // chr(240) = 'gfx3r 46'
        {0x00,0x00,0x1F,0x1F,0x1F,0x00,0x00},   // chr(241) = 'gfx3r 34'
        {0x1C,0x1C,0x1F,0x1F,0x1F,0x00,0x00},   // chr(242) = 'gfx3r 234'
        {0x07,0x07,0x1F,0x1F,0x1F,0x00,0x00},   // chr(243) = 'gfx3r 134'
        {0x00,0x00,0x1F,0x1F,0x1F,0x1C,0x1C},   // chr(244) = 'gfx3r 346'
        {0x00,0x00,0x1F,0x1F,0x1F,0x07,0x07},   // chr(245) = 'gfx3r 345'
        {0x00,0x00,0x01,0x03,0x07,0x0E,0x1C},   // chr(246) = 'gfx \'
        {0x07,0x0E,0x1C,0x18,0x10,0x00,0x00},   // chr(247) = 'gfx \2'
        {0x1C,0x0E,0x07,0x03,0x01,0x00,0x00},   // chr(248) = 'gfx /'
        {0x00,0x00,0x10,0x18,0x1C,0x0E,0x07},   // chr(249) = 'gfx /2'
        {0x00,0x00,0x0E,0x0E,0x0E,0x00,0x00},   // chr(250) = 'gfx middle point'
        {0x01,0x03,0x07,0x0E,0x1C,0x18,0x10},   // chr(251) = 'gfx \3'
        {0x10,0x18,0x1C,0x0E,0x07,0x03,0x01},   // chr(252) = 'gfx /3'
        {0x1C,0x1C,0x1F,0x1F,0x1F,0x1C,0x1C},   // chr(253) = 'gfx3r 2346'
        {0x07,0x07,0x1F,0x1F,0x1F,0x07,0x07},   // chr(254) = 'gfx3r 1345'
        {0x1F,0x1F,0x1F,0x1F,0x1F,0x1F,0x1F},   // chr(255) = 'gfx2r 1234'
    };
    
    byte[][] screen = new byte[4][10];
    byte[][] attributes = new byte[4][10];
    int xPos = 0;
    int yPos = 0;
    int contrast = 158;   // 0-196
    int pixelWidth = 8;
    int pixelHeight = 8;
    int ioPortStart = 0;
    IRQHandler handler = null;
    
    int command = 0;
    boolean blink = false;
    boolean paintLCDOnly = false;
    
    JButton leftButton;
    JButton modeButton;
    JButton transmitButton;
    JButton rightButton;
    javax.swing.Timer timer;
    JButton timerButton;
    JButton startTimer;
    JButton stopTimer;
    JButton redrawButton;
    
    java.awt.Graphics g;
    
    public SeikoUC2000Display() {
        initialize();
    }
    
    public SeikoUC2000Display(int ioPortStart) {
        this();
        this.ioPortStart = ioPortStart;
    }
    
    public final void initialize() {
        print("Copyright (c)2021 byP. Werno  Ok");
        setSize(800, 800); // 500 x 350 dislay
        setTitle("Seiko UC 2000");
        setLayout(null);
        setResizable(false);
        setVisible(true);
        
        java.awt.Color orangeColor = new java.awt.Color(230,60,20);
        java.awt.Color brownishColor = new java.awt.Color(60,60,30);
        
        leftButton = new JButton("L");
        leftButton.setBounds(100, 650, 120, 60);
        leftButton.setBackground(orangeColor);
        leftButton.setOpaque(true);
        leftButton.setForeground(orangeColor);
        leftButton.setBorderPainted(false);
        leftButton.addActionListener(this);
        
        modeButton = new JButton("");
        modeButton.setBounds(260,650,120,60);
        modeButton.setBackground(brownishColor);
        modeButton.setOpaque(true);
        modeButton.setForeground(brownishColor);
        modeButton.setBorderPainted(false);
        modeButton.addActionListener(this);
        
        transmitButton = new JButton("");
        transmitButton.setBounds(420,650,120,60);
        transmitButton.setBackground(brownishColor);
        transmitButton.setOpaque(true);
        transmitButton.setForeground(brownishColor);
        transmitButton.setBorderPainted(false);
        transmitButton.addActionListener(this);
        
        rightButton = new JButton("R");
        rightButton.setBounds(580,650,120,60);
        rightButton.setBackground(orangeColor);
        rightButton.setOpaque(true);
        rightButton.setForeground(orangeColor);
        rightButton.setBorderPainted(false);
        rightButton.addActionListener(this);
        
        timer = new javax.swing.Timer(1000, this);
        
        add(leftButton);
        add(modeButton);
        add(transmitButton);
        add(rightButton);
        
        timerButton = new JButton("Timer");
        timerButton.setBounds(100, 720, 120, 60);
        timerButton.setBackground(orangeColor);
        timerButton.setOpaque(true);
        timerButton.setForeground(orangeColor);
        timerButton.setBorderPainted(false);
        timerButton.addActionListener(this);

        startTimer = new JButton("");
        startTimer.setBounds(260,720,120,60);
        startTimer.setBackground(brownishColor);
        startTimer.setOpaque(true);
        startTimer.setForeground(brownishColor);
        startTimer.setBorderPainted(false);
        startTimer.addActionListener(this);
        
        stopTimer = new JButton("");
        stopTimer.setBounds(420,720,120,60);
        stopTimer.setBackground(brownishColor);
        stopTimer.setOpaque(true);
        stopTimer.setForeground(brownishColor);
        stopTimer.setBorderPainted(false);
        stopTimer.addActionListener(this);
        
        redrawButton = new JButton("Redraw");
        redrawButton.setBounds(580,720,120,60);
        redrawButton.setBackground(orangeColor);
        redrawButton.setOpaque(true);
        redrawButton.setForeground(orangeColor);
        redrawButton.setBorderPainted(false);
        redrawButton.addActionListener(this);
        
        add(timerButton);
        add(startTimer);
        add(stopTimer);
        add(redrawButton);
    }
    
    public void print(String str) {
        for(int i=0; i<str.length(); i++) {
            char c = str.charAt(i);
            this.screen[yPos][xPos] = (byte)c;
            this.xPos++;
            if(xPos >= 10) {
                this.yPos ++;
                this.xPos = 0;
                if(this.yPos >= 4) {
                    this.yPos = 0;
                }
            }
        }
    }
    
    public void paintLCD(java.awt.Graphics g) {
        if(g == null) return ;
        
        java.awt.Color lcdOn = new java.awt.Color(196-contrast, 196-contrast, 196-contrast);
        java.awt.Color lcdOff= new java.awt.Color(contrast, contrast, contrast);
        for(int row=0;row<4;row++) {
            for(int col=0;col<10;col++) {
                byte blinkByte = this.attributes[row][col];
                for(int y=0; y<7; y++) {
                    byte rowBits = characterRom[Byte.toUnsignedInt(this.screen[row][col])][y];
                    for(int x=0; x<5; x++) {
                        if((rowBits & 0x01) == 0x01) {
                            if((blinkByte == 0) || blink)
                                g.setColor(lcdOn);
                            else
                                g.setColor(lcdOff);
                        }
                        else {
                            g.setColor(lcdOff);
                        }
                        g.fillRect(160 + col * pixelWidth * 6 + x*pixelWidth, 250 + row * pixelHeight * 8 + y*pixelHeight, pixelWidth * 8 / 10, pixelHeight * 8/10);
                        rowBits >>= 1;
                    }
                }
            }
        }
    }
    
    @Override
    public void paint(java.awt.Graphics g) {
        if(this.g == null) this.g = g;
        if(!this.paintLCDOnly) {
            super.paint(g);

            java.awt.Color brownish = new java.awt.Color(30,30,15);
            java.awt.Color beige = new java.awt.Color(128,128,77);
            java.awt.Color lightGrey = new java.awt.Color(196,196,196);
            java.awt.Color black = new java.awt.Color(0, 0, 0);
            java.awt.Font font = new java.awt.Font("Courier", java.awt.Font.PLAIN, 80);
            g.setColor(brownish);
            g.fillRect(0, 0, 800, 800);
            g.setColor(beige);
            g.fillRoundRect(75, 152, 650, 450, 20, 20);
            g.setColor(lightGrey);
            g.setFont(font);
            g.drawString("SEIKO", 30, 100);
            g.drawString("UC-2000", 420, 100);
            g.fillRoundRect(130, 190, 540, 380, 10, 10);
            g.fillRect(150, 210, 500, 350);
            g.setColor(black);
        }

        paintLCD(g);
        
        if(!this.paintLCDOnly) {
            leftButton.repaint();
            modeButton.repaint();
            transmitButton.repaint();
            rightButton.repaint();
            timerButton.repaint();
            startTimer.repaint();
            stopTimer.repaint();
            redrawButton.repaint();
        }
        this.paintLCDOnly = false;
    }
    
    @Override
    public long getLowAddress() {
        return this.ioPortStart;
    }

    @Override
    public long getHighAddress() {
        return this.ioPortStart + 2;
    }

    @Override
    public boolean isReadable() {
        return false;
    }

    @Override
    public boolean isWriteable() {
        return true;
    }

    @Override
    public boolean isLittleEndian() {
        return false;
    }

    @Override
    public int getBitSize() {
        return 8;
    }

    @Override
    public int getContent(long address) throws MemoryException {
        return getByte(address);
    }

    @Override
    public byte getByte(long address) throws MemoryException {
        switch ((int)address) {
            case 0x00:  // button pressed (todo)
                return (byte)0;
                
            case 0x01:  // reception data flag
                return (byte)0;
                
            case 0x02:  // transfer error flag
                return (byte)0;
                
            case 0x05:  // senior nibble to reel
                return (byte)0;
                
            case 0x06:  // low nibble to reel
                return (byte)0;
                
            case 0x07:  // button interrupt flag;
                return (byte)0x0F;
                
            case 0x08:  // Flags of the pressed buttons (todo)
                return (byte)0;
                
            case 0x0E:  // 1/16 second counter
                return (byte)((System.currentTimeMillis() / 16000L) & 0x0F);  // 1/16th second counter
                
            default:
                throw new MemoryException("Cannot read from LCD Screen");
        }
    }

    @Override
    public short getShort(long address) throws MemoryException {
        throw new MemoryException("Cannot read from LCD Screen");
    }

    @Override
    public int getInt(long address) throws MemoryException {
        throw new MemoryException("Cannot read from LCD Screen");
    }

    @Override
    public long getLong(long address) throws MemoryException {
        throw new MemoryException("Cannot read from LCD Screen");
    }
    
    @Override
    public void setContent(long address, int value) throws MemoryException {
        setByte(address, (byte)value);
    }

    @Override
    public void setByte(long address, byte value) throws MemoryException {
        if(address == this.ioPortStart) {
            this.command = Byte.toUnsignedInt(value);
        }
        else if(address == (this.ioPortStart + 1)) {
            if((this.command >= 0) && (this.command <40)) {
                this.screen[(this.command / 10)][this.command % 10] = value;
                this.command++;
                if(this.command == 40)
                    this.command = 0;
            }
            else if((this.command >= 64) && (this.command < 104)) {
                this.attributes[((this.command - 0x40) / 10)][(this.command - 0x40) % 10] = value;
                this.command++;
                if(this.command == 104)
                    this.command = 64;
            }
            else {
                switch (this.command) {
                    case 0x79:
                        this.contrast += 10;
                        if(this.contrast >= 196) this.contrast = 196;
                        break;
                        
                    case 0x7A:
                        this.contrast -= 10;
                        if(this.contrast < 0) this.contrast = 0;
                        break;
                        
                    case 0x7D:
                        // clear
                        this.screen = new byte[4][10];
                        this.attributes = new byte[4][10];
                        break;
                        
                    default:
                        System.out.println("Illegal LCD Command " + this.command + " = $" + Integer.toHexString(this.command));
                        break;
                }
            }
            
            this.paintLCDOnly = true;
            this.repaint();
        }
        else {
            throw new MemoryException("IO Port out of range");
        }
    }

    @Override
    public void setShort(long address, short value) throws MemoryException {
        throw new MemoryException("Can only write bytes to LCD Screen");
    }

    @Override
    public void setInt(long address, int value) throws MemoryException {
        throw new MemoryException("Can only write bytes to LCD Screen");
    }

    @Override
    public void setLong(long address, long value) throws MemoryException {
        throw new MemoryException("Can only write bytes to LCD Screen");
    }
    
    public void setHandler(IRQHandler handler) {
        this.handler = handler;
    }
    
    public static void main(String[] args) throws Exception {
        SeikoUC2000Display disp = new SeikoUC2000Display();
        disp.setByte(0, (byte)0x40);
        disp.setByte(1, (byte)1);
        disp.setByte(1, (byte)1);
        disp.setByte(1, (byte)1);
        disp.setByte(1, (byte)1);
        disp.setByte(1, (byte)1);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        // TODO: Add a timer every second or so...
        try {
            if(source == this.leftButton) {
                if(this.handler != null)
                    this.handler.handleIRQ(SeikoUC2000.IRQ_SET_BUTTON);
            }
            if(source == this.rightButton) {
                if(this.handler != null)
                    this.handler.handleIRQ(SeikoUC2000.IRQ_SELECT_BUTTON);
            }
            if(source == this.modeButton) {
                if(this.handler != null)
                    this.handler.handleIRQ(SeikoUC2000.IRQ_MODE_BUTTON);
            }
            if(source == this.transmitButton) {
                if(this.handler != null)
                    this.handler.handleIRQ(SeikoUC2000.IRQ_TRANSMIT_BUTTON);
            }
            
            if((source == this.timer) || (source == this.timerButton)) {
                if(this.handler != null)
                    this.handler.handleIRQ(SeikoUC2000.IRQ_SECOND_TIMER);
                this.blink = !this.blink;
                this.paintLCDOnly = true;
                this.repaint(160, 250, 500, 350);
            }
            
            if(source == this.redrawButton) {
                if(this.handler != null)
                    this.handler.handleIRQ(SeikoUC2000.IRQ_REDRAW_SCREEN);
            }
            if(source == this.startTimer) {
                this.timer.start();
            }
            if(source == this.stopTimer) {
                this.timer.stop();
            }
        }
        catch (Exception ex) {
            
        }
    }
}
