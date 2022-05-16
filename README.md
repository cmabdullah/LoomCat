

#### Mock Server

> java -jar MockServer-0.0.1-SNAPSHOT.jar

> kill -9 $(lsof -ti:8087)
* default running
  * server.port=8087 -> by default mock service app is running on port 8087
  * app.duration=100 -> mock service app return response after 100 ms delay.
  * you can change config accordingly
    * --server.port=8086 --app.duration=500
    * java -jar MockServer-0.0.1-SNAPSHOT.jar --server.port=8086 --app.duration=500

### Backend service curl
```curl
curl --location --request GET 'http://localhost:8086/api/v1/info/countryInfo?a1=cm&a2=abdullah' \
--header 'Content-Type: application/json' \
--data-raw '{

}'
```

### Run Loom App
> javac --enable-preview LoomCatApp.java
> java --enable-preview LoomCatApp

### Loom App Curl

```curl
curl --location --request GET 'http://localhost:8082/rpc?a1=cm&a2=abdullah' \
--header 'Content-Type: application/json' \
--data-raw '{

}'
```

performance

![](loomcat%20performance.png)


### TomCat App Curl

```curl
curl --location --request GET 'http://localhost:8082/rpc?a1=cm&a2=abdullah' \
--header 'Content-Type: application/json' \
--data-raw '{

}'
```

performance

![](old%20school%20tomcat.png)
