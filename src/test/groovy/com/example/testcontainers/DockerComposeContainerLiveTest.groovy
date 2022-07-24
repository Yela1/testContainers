package com.example.testcontainers

import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.utility.DockerImageName
import spock.lang.Specification

class DockerComposeContainerLiveTest extends Specification{

    static final DockerComposeContainer compose =
            new DockerComposeContainer(
                    new File("src/test/resources/test-compose.yml"))
                    .withExposedService("simpleWebServer_1", 8081)

    static final GenericContainer simpleWebServer
            = new GenericContainer("alpine:3.2")
            .withExposedPorts(90)
            .withCommand("/bin/sh", "-c", "while true; do echo "
                    + "\"HTTP/1.1 200 OK\n\nHello World!\" | nc -l -p 90; done")
//    static final GenericContainer container = new GenericContainer<>(
//            DockerImageName.parse("testcontainers/helloworld:1.1.0")
//    )
//            .withExposedPorts(9090)

    def setupSpec(){
//        container.start()
        compose.start()
        simpleWebServer.start()
    }

    def "testing compose connections"(){
        given:
            def address = "http://" + compose.getServiceHost("simpleWebServer_1", 8081) + ":" + compose.getServicePort("simpleWebServer_1", 8081);
            def response = getRequest(address)

            def simpleWebServerAddress = "http://" + simpleWebServer.getContainerIpAddress() + ":" + simpleWebServer.getMappedPort(90)
            def simpleWebServerResponse = getRequest(simpleWebServerAddress)

        expect:
            response == "Hello World!"
        and:
            simpleWebServerResponse == "Hello World!"
    }

    def getRequest(String address){
        def url = new URL(address)
        def con = (HttpURLConnection) url.openConnection()
        con.setRequestMethod("GET")

        def reader = new BufferedReader(new InputStreamReader(con.getInputStream()))
        def inputLine
        def content = new StringBuffer()
        while ((inputLine = reader.readLine()) != null) {
            content.append(inputLine);
        }
        reader.close()

        content.toString()
    }

//    def "test porting"(){
//            def first = container.getMappedPort(9090)
//            def second = container.getMappedPort(9091)
//            def ipAddress = container.getHost()
//            print("firstPort: " + first)
//            print("secondPort " + second)
//            print("ipAddress: " + ipAddress)
//        expect:
//            second != null
//
//    }
}
