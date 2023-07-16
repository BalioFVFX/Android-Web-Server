# Android-Web-Server
Android app that turns your device into a Web server

## How does it work?
![Overview](https://raw.githubusercontent.com/BalioFVFX/Android-Web-Server/987a983f4b3e3e8f3bb533c495b7d12b202e2f24/media/overview.svg)
The app starts a Thread that uses ServerSocket to allow connections. Each connection is then processed on a separate Thread.

Main components of the app:

#### WebServer
The WebServer uses ServerSocket and listens for connections to be made. Once a connection is made it is then delegated to the ClientWorker component.

#### ClientWorker
The ClientWorker parses the input that the connections have. More specifically, it searches for the HTTP method, the endpoint, and the Accept headers. The parsed input (Request) is then sent to the Endpoint handler which returns a Response. Finally, the ClientWorker sends the response to the client connection and returns a ConnectionSnapshot (Request, Response) object to the WebServer.

#### EndpointHandler
The EndpointHandler maps the Request to a Response object. It does so by examining the endpoint and the Accept header. The EndpointHandler component supports `text/html` and `application/json` Accept headers. Based on the Accept headers it returns a HTML page (HtmlResponse) or JSON (JsonResponse). 

This component also supports query parameters. In the case of the `/weather` endpoint, the EndpointHandler searches for the `location` param. The location param is then used for an API call to [Open-Meteo.com](https://open-meteo.com/), then a Response is returned as stated above. Note: Even though the app makes API requests to obtain the weather data, only `weather?location=sofia` is currently supported.

## How to use it?
Install the app on your device and start the WebServer by pressing the start button.

On the host device, the WebServer could be reached through the browser on the following URL `localhost:8080/home`. Other devices would need the Local IP address of the host, for example: `192.168.0.103:8080/home`.

As mentioned above, the WebServer can return JSON responses. The easiest way to test this is to use an app like Postman. Make sure that the `Accept` header has the following value `application/json`. A ready-to-be-used Postman collection can be downloaded from [here](https://github.com/BalioFVFX/Android-Web-Server/blob/main/WebServer.postman_collection.json).

## Credits
[Weather data by Open-Meteo.com](https://open-meteo.com/)
