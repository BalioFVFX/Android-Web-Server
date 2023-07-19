package com.webserver.server

import com.webserver.Logger
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.UUID

class ClientWorker(
    private val endpointHandler: EndpointHandler
) {

    companion object {
        private const val TAG = "ClientWorker"
    }

    fun serve(client: Socket): ConnectionSnapshot? {
        var snapshot: ConnectionSnapshot? = null
        val timestamp = System.currentTimeMillis()

        try {
            val inputStream = BufferedReader(InputStreamReader(client.getInputStream()))
            val outputStream = client.getOutputStream()

            val buffer = StringBuilder()

            var line = try {
                inputStream.readLine()
            } catch (timeout: SocketTimeoutException) {
                // Nothing to do. Check if the obtained data is enough to be processed
            }

            while (line != "") {
                buffer.append(line)
                line = try {
                    inputStream.readLine()
                } catch (timeout: SocketTimeoutException) {
                    // Nothing to do. Check if the obtained data is enough to be processed
                    break
                }
            }

            val input = buffer.toString()

            if (!input.contains("GET /")) {
                Logger.d(TAG, "GET / was not found")
                return null
            }

            val endpoint = getEndpoint(input)
            val acceptHeader = getAcceptHeader(input) ?: throw IllegalArgumentException()

            val response = endpointHandler.handle(endpoint, acceptHeader)

            val request = Request(
                method = Method.GET,
                endpoint = endpoint,
                acceptHeaders = acceptHeader,
                timestamp = timestamp
            )

            snapshot = ConnectionSnapshot(
                uuid = UUID.randomUUID().toString(),
                request = request,
                response = response
            )

            outputStream.write(response.headers.toByteArray())
            outputStream.write(response.body.toByteArray())
            outputStream.flush()

            inputStream.silentClose()
            outputStream.silentClose()
        } catch (exception: Exception) {
            snapshot = null
            Logger.d(TAG, "Exception")
        } finally {
            client.silentClose()
        }


        return snapshot
    }

    private fun getEndpoint(input: String): String {
        return find(
            input = input,
            searchAfter = "GET /",
            stopBefore = charArrayOf(' ')
        ).lowercase()
    }

    private fun getAcceptHeader(input: String): AcceptHeader? {
        val found = find(
            input = input,
            searchAfter = "Accept: ",
            stopBefore = charArrayOf(' ', ',')
        ).lowercase()

        if (found.contains(AcceptHeader.JSON.value)) {
            return AcceptHeader.JSON
        } else if (found.contains(AcceptHeader.TEXT_HTML.value)) {
            return AcceptHeader.TEXT_HTML
        }

        return null
    }

    private fun find(input: String, searchAfter: String, stopBefore: CharArray): String {
        val start = input.indexOf(searchAfter)
        var result = ""
        var i = searchAfter.length

        while (input.length > start + i && !stopBefore.contains(input[start + i])) {
            result += input[start + i]
            i++
        }

        return result
    }
}