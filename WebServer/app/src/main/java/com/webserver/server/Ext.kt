package com.webserver.server

import java.io.BufferedReader
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.net.Socket

fun Socket.silentClose() {
    try {
        close()
    } catch (ex: Exception) {

    }
}

fun InputStream.silentClose() {
    try {
        close()
    } catch (ex: Exception) {

    }
}

fun OutputStream.silentClose() {
    try {
        close()
    } catch (ex: Exception) {

    }
}

fun BufferedReader.silentClose() {
    try {
        close()
    } catch (ex: Exception) {

    }
}