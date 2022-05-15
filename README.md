

#### Mock Server
http://localhost:8087/api/v1/info/countryInfo?a1=cm&a2=abdullah

> java -jar MockServer-0.0.1-SNAPSHOT.jar
> kill -9 $(lsof -ti:8087)
* default running
  * server.port=8087 
  * app.duration=100
  * --server.port=8086 --app.duration=500
  *  java -jar MockServer-0.0.1-SNAPSHOT.jar --server.port=8086 --app.duration=500
