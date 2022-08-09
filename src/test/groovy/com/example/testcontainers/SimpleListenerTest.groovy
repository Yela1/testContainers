package com.example.testcontainers

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.AmazonS3Exception
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate
import org.springframework.messaging.support.GenericMessage
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.spock.Testcontainers
import org.testcontainers.utility.DockerImageName
import static org.awaitility.Awaitility.given
import spock.lang.Specification


import static java.util.concurrent.TimeUnit.SECONDS
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS


@Testcontainers
@SpringBootTest
class SimpleListenerTest extends Specification{

    static queueName = "order-event-test-queue"

    static bucketName = "order-event-test-bucket"

    static LocalStackContainer localStack =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.13.0"))
                    .withServices(S3, SQS)


    def setupSpec(){
        localStack.start()
        localStack.execInContainer("awslocal", "sqs", "create-queue", "--queue-name", queueName)
        localStack.execInContainer("awslocal", "s3", "mb", "s3://" + bucketName)
    }

    def cleanupSpec(){
        localStack.stop()
    }


    @DynamicPropertySource
    static void overrideConfiguration(DynamicPropertyRegistry registry) {
        registry.add("event-processing.order-event-queue", () -> queueName)
        registry.add("event-processing.order-event-bucket", () -> bucketName)
        registry.add("cloud.aws.sqs.endpoint", () -> localStack.getEndpointOverride(SQS))
        registry.add("cloud.aws.s3.endpoint", () -> localStack.getEndpointOverride(S3))
        registry.add("cloud.aws.credentials.access-key", localStack::getAccessKey)
        registry.add("cloud.aws.credentials.secret-key", localStack::getSecretKey)
    }

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;

    def "message should upload to bucket when they received sqs message"() {
        when:
            queueMessagingTemplate.send(queueName, new GenericMessage<>("""
            {
               "id": "42",
               "message": "Please delivery ASAP",
               "product": "MacBook Pro",
               "orderedAt": "2021-11-11 12:00:00",
               "expressDelivery": true
            }
          """, Map.of("contentType", "application/json")))

        then:
            Thread.sleep(5 * 1000)
        and:
            amazonS3.getObject(bucketName, "42") != null
    }
}
