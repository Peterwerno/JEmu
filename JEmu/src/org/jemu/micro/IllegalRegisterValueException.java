/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jemu.micro;

/**
 *
 * @author peter
 */
public class IllegalRegisterValueException extends IllegalRegisterException {

    /**
     * Creates a new instance of <code>IllegalRegisterValueException</code>
     * without detail message.
     */
    public IllegalRegisterValueException() {
    }

    /**
     * Constructs an instance of <code>IllegalRegisterValueException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public IllegalRegisterValueException(String msg) {
        super(msg);
    }
}
