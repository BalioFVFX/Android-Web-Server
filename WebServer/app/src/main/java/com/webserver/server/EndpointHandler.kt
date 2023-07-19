package com.webserver.server

import com.webserver.DeviceManager
import com.webserver.EndpointStorage
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL


class EndpointHandler(
    private val deviceManager: DeviceManager,
    private val endpointStorage: EndpointStorage,
) {

    fun handle(endpoint: String, acceptHeader: AcceptHeader): Response {
        // Default endpoints
        val response = when (endpoint) {
            "home" -> handleHome(acceptHeader)
            "about" -> handleAbout(acceptHeader)
            else -> null
        }

        if (response == null) {
            // Default endpoint
            if (endpoint.startsWith("weather")) {
                return handleWeather(
                    endpoint = endpoint,
                    location = getQueryParam(endpoint, "location"),
                    acceptHeader = acceptHeader
                )
            }

            // User-defined endpoints
            val content = endpointStorage.getEndpointContent(endpoint)

            if (content != null) {
                endpointStorage.markViewed(endpoint)
                return handleUserDefinedEndpoint(content)
            }

            return handleUnknown(endpoint, acceptHeader)
        } else {
            return response
        }
    }

    private fun handleUserDefinedEndpoint(content: String) : Response {
        return Response.HtmlResponse(
            createHeaders(content.length, AcceptHeader.TEXT_HTML),
            System.currentTimeMillis(),
            content
        )
    }

    private fun handleHome(acceptHeader: AcceptHeader): Response {
        if (acceptHeader == AcceptHeader.JSON) {
            return handleNotSupported()
        }

        val html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Home</title>\n" +
                "    <link rel=\"icon\" href=\"data:,\">\n" +
                "    <style>\n" +
                "        body {\n" +
                "            background-color: black;\n" +
                "            display: flex;\n" +
                "            flex-direction: column;\n" +
                "            align-items: center;\n" +
                "            justify-content: center;\n" +
                "            height: 100vh;\n" +
                "            margin: 0;\n" +
                "            color: white;\n" +
                "        }\n" +
                "        \n" +
                "        h1 {\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        \n" +
                "        .button-container {\n" +
                "            display: flex;\n" +
                "            justify-content: center;\n" +
                "            margin-top: 20px;\n" +
                "        }\n" +
                "        \n" +
                "        button {\n" +
                "            margin: 0 10px;\n" +
                "        }\n" +
                "\n" +
                "        * {\n" +
                "            font-family: Verdana, Geneva, Tahoma, sans-serif;\n" +
                "        }\n" +
                "\n" +
                "        a {\n" +
                "            all: unset;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>Hello, World!</h1>\n" +
                "    <div class=\"button-container\">\n" +
                "        <button><a href=\"home\">Home</a></button>\n" +
                "        <button><a href=\"about\">About</a></button>\n" +
                "        <button><a href=\"weather?location=sofia\">Weather</a></button>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>"

        return Response.HtmlResponse(
            createHeaders(html.length, AcceptHeader.TEXT_HTML),
            System.currentTimeMillis(),
            html
        )
    }

    private fun handleAbout(acceptHeader: AcceptHeader): Response {
        val batteryLevel = deviceManager.getBatteryLevel()
        val deviceName = deviceManager.getDeviceName()

        if (acceptHeader == AcceptHeader.JSON) {
            val json = "{\"server_battery\": \"${batteryLevel}\"}"
            return Response.JsonResponse(
                createHeaders(json.length, AcceptHeader.JSON),
                System.currentTimeMillis(),
                json
            )
        }

        val html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Home</title>\n" +
                "    <link rel=\"icon\" href=\"data:,\">\n" +
                "    <style>\n" +
                "        body {\n" +
                "            background-color: black;\n" +
                "            display: flex;\n" +
                "            flex-direction: column;\n" +
                "            align-items: center;\n" +
                "            justify-content: center;\n" +
                "            height: 100vh;\n" +
                "            margin: 0;\n" +
                "            color: white;\n" +
                "        }\n" +
                "        \n" +
                "        h1 {\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        \n" +
                "        .button-container {\n" +
                "            display: flex;\n" +
                "            justify-content: center;\n" +
                "            margin-top: 20px;\n" +
                "        }\n" +
                "        \n" +
                "        button {\n" +
                "            margin: 0 10px;\n" +
                "        }\n" +
                "\n" +
                "        * {\n" +
                "            font-family: Verdana, Geneva, Tahoma, sans-serif;\n" +
                "        }\n" +
                "\n" +
                "        a {\n" +
                "            all: unset;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>About</h1>\n" +
                "    <p>This web page is hosted on an Android device (${deviceName}) \uD83D\uDCF1.</p>\n" +
                "    <p>The server has ${batteryLevel}% battery left. \uD83D\uDE09</p>\n" +
                "    <div class=\"button-container\">\n" +
                "        <button><a href=\"home\">Home</a></button>\n" +
                "        <button><a href=\"about\">About</a></button>\n" +
                "        <button><a href=\"weather?location=sofia\">Weather</a></button>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>"

        return Response.HtmlResponse(
            createHeaders(html.length, AcceptHeader.TEXT_HTML),
            System.currentTimeMillis(),
            html
        )
    }

    private fun handleWeather(
        endpoint: String,
        location: String?,
        acceptHeader: AcceptHeader
    ): Response {
        if (location == null || location != "sofia") {
            return handleUnknown(endpoint, acceptHeader)
        }

        val displayLocation = location[0].uppercase() + location.substring(1)

        val temperature = try {
            val url = URL("https://api.open-meteo.com/v1/forecast?latitude=42.6975&longitude=23.3241&current_weather=true")
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "GET"
            con.setRequestProperty("Content-Type", "application/json")
            con.readTimeout = 3000
            con.connectTimeout = 3000

            val reader = BufferedReader(InputStreamReader(con.inputStream))
            val buffer = StringBuilder()
            var line = reader.readLine()

            while (line != null) {
                buffer.append(line)
                line = reader.readLine()
            }

            val json = JSONObject(buffer.toString())

            json.getJSONObject("current_weather").getDouble("temperature").toString()
        } catch (exception: Exception) {
            ""
        }

        if (acceptHeader == AcceptHeader.JSON) {
            val json = "{\"location\": \"${displayLocation}\"," +
                    "\"temperature\": \"${temperature}\"," +
                    "\"credits\": \"Weather data by Open-Meteo.com https://open-meteo.com/\""

            return Response.JsonResponse(
                createHeaders(json.length, AcceptHeader.JSON),
                System.currentTimeMillis(),
                json
            )
        } else {
            val html = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <title>Home</title>\n" +
                    "    <link rel=\"icon\" href=\"data:,\">\n" +
                    "    <style>\n" +
                    "        body {\n" +
                    "            background-color: black;\n" +
                    "            display: flex;\n" +
                    "            flex-direction: column;\n" +
                    "            align-items: center;\n" +
                    "            justify-content: center;\n" +
                    "            height: 100vh;\n" +
                    "            margin: 0;\n" +
                    "            color: white;\n" +
                    "        }\n" +
                    "        \n" +
                    "        h1 {\n" +
                    "            text-align: center;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .button-container {\n" +
                    "            display: flex;\n" +
                    "            justify-content: center;\n" +
                    "            margin-top: 20px;\n" +
                    "        }\n" +
                    "        \n" +
                    "        button {\n" +
                    "            margin: 0 10px;\n" +
                    "        }\n" +
                    "\n" +
                    "        * {\n" +
                    "            font-family: Verdana, Geneva, Tahoma, sans-serif;\n" +
                    "        }\n" +
                    "\n" +
                    "        button a {\n" +
                    "            all: unset;\n" +
                    "        }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <h1>Weather in ${displayLocation}: ${temperature}</h1>\n" +
                    "    <p><a href=\"https://open-meteo.com/\">Weather data by Open-Meteo.com</a></p>\n" +
                    "    <div class=\"button-container\">\n" +
                    "        <button><a href=\"home\">Home</a></button>\n" +
                    "        <button><a href=\"about\">About</a></button>\n" +
                    "        <button><a href=\"weather?location=sofia\">Weather</a></button>\n" +
                    "    </div>\n" +
                    "</body>\n" +
                    "</html>"

            return Response.HtmlResponse(
                createHeaders(html.length, AcceptHeader.TEXT_HTML),
                System.currentTimeMillis(),
                html
            )
        }
    }

    private fun handleUnknown(endpoint: String, acceptHeader: AcceptHeader): Response {
        if (acceptHeader == AcceptHeader.JSON) {
            val json = "{\"message\": \"Unknown endpoint: ${endpoint}\"}"

            return Response.JsonResponse(
                createHeaders(json.length, AcceptHeader.JSON, 404),
                System.currentTimeMillis(),
                json
            )
        }

        val html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Home</title>\n" +
                "    <link rel=\"icon\" href=\"data:,\">\n" +
                "    <style>\n" +
                "        body {\n" +
                "            background-color: black;\n" +
                "            display: flex;\n" +
                "            flex-direction: column;\n" +
                "            align-items: center;\n" +
                "            justify-content: center;\n" +
                "            height: 100vh;\n" +
                "            margin: 0;\n" +
                "            color: white;\n" +
                "        }\n" +
                "        \n" +
                "        h1 {\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        \n" +
                "        .button-container {\n" +
                "            display: flex;\n" +
                "            justify-content: center;\n" +
                "            margin-top: 20px;\n" +
                "        }\n" +
                "        \n" +
                "        button {\n" +
                "            margin: 0 10px;\n" +
                "        }\n" +
                "\n" +
                "        * {\n" +
                "            font-family: Verdana, Geneva, Tahoma, sans-serif;\n" +
                "        }\n" +
                "\n" +
                "        a {\n" +
                "            all: unset;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>Unknown endpoint: ${endpoint}</h1>\n" +
                "    <div class=\"button-container\">\n" +
                "        <button><a href=\"home\">Home</a></button>\n" +
                "        <button><a href=\"about\">About</a></button>\n" +
                "        <button><a href=\"weather?location=sofia\">Weather</a></button>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>"

        return Response.HtmlResponse(
            createHeaders(html.length, AcceptHeader.TEXT_HTML),
            System.currentTimeMillis(),
            html
        )
    }

    private fun handleNotSupported(): Response {
        val html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Home</title>\n" +
                "    <link rel=\"icon\" href=\"data:,\">\n" +
                "    <style>\n" +
                "        body {\n" +
                "            background-color: black;\n" +
                "            display: flex;\n" +
                "            flex-direction: column;\n" +
                "            align-items: center;\n" +
                "            justify-content: center;\n" +
                "            height: 100vh;\n" +
                "            margin: 0;\n" +
                "            color: white;\n" +
                "        }\n" +
                "        \n" +
                "        h1 {\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        \n" +
                "        .button-container {\n" +
                "            display: flex;\n" +
                "            justify-content: center;\n" +
                "            margin-top: 20px;\n" +
                "        }\n" +
                "        \n" +
                "        button {\n" +
                "            margin: 0 10px;\n" +
                "        }\n" +
                "\n" +
                "        * {\n" +
                "            font-family: Verdana, Geneva, Tahoma, sans-serif;\n" +
                "        }\n" +
                "\n" +
                "        a {\n" +
                "            all: unset;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>Operation not supported!</h1>\n" +
                "    <div class=\"button-container\">\n" +
                "        <button><a href=\"home\">Home</a></button>\n" +
                "        <button><a href=\"about\">About</a></button>\n" +
                "        <button><a href=\"weather?location=sofia\">Weather</a></button>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>"

        return Response.HtmlResponse(
            createHeaders(html.length, AcceptHeader.TEXT_HTML, 400),
            System.currentTimeMillis(),
            html
        )
    }

    private fun createHeaders(
        bodyLength: Int,
        contentType: AcceptHeader,
        statusCode: Int = 200
    ): String {
        val code = when (statusCode) {
            200 -> {
                "200 OK"
            }
            404 -> {
                "404 Not Found"
            }
            400 -> {
                "400 Bad Request"
            }
            else -> {
                throw IllegalArgumentException()
            }
        }

        return "HTTP/1.0 ${code}\r\n" +
                "Connection: close\r\n" +
                "Content-Length: ${bodyLength}\r\n" +
                "Content-Type: ${contentType.value}; charset=utf-8\r\n\r\n"
    }

    private fun getQueryParam(endpoint: String, searchedParam: String): String? {
        return endpoint.substringAfter("?", "")
            .substringAfter("$searchedParam=", "")
            .substringBefore("&")
    }
}