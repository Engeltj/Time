/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.exceptions;

/**
 *
 * @author Tim
 */
public class HomeFailedToCreateException extends Exception {
    public HomeFailedToCreateException() {}

    //Constructor that accepts a message
    public HomeFailedToCreateException(String message) {
       super(message);
    }
}
