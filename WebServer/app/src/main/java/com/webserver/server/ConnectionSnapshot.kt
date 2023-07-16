package com.webserver.server

data class ConnectionSnapshot(
    val uuid: String,
    val request: Request,
    val response: Response
)

data class Request(
    val method: Method,
    val endpoint: String,
    val acceptHeaders: AcceptHeader,
    val timestamp: Long,
)

sealed class Response(val headers: String, val timestamp: Long, val body: String) {
    class JsonResponse(
        headers: String,
        timestamp: Long,
        body: String,
    ) : Response(headers, timestamp, body)

    class HtmlResponse(
        headers: String,
        timestamp: Long,
        body: String
    ) : Response(headers, timestamp, body)
}