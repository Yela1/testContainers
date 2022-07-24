package com.example.testcontainers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

@Testcontainers
@SpringBootTest
class TestContainer extends Specification{
    @Autowired
    private CustomerDao customerDao
    @Shared
    MySQLContainer container = (MySQLContainer) new MySQLContainer("mysql:latest")
            .withReuse(true)

    def setupSpec(){
        container.start()
    }

    def "verify mysql save and get"(){
        when:
            def customers = customerDao.findAll()
        then:
            customers.size() == 2
    }

    def "verify mysql get"(){
        when:
            def customer = customerDao.findCustomer("trisha")
        then:
            customer != null
            customer.getLastName() == "gee"
    }
}
