package io.philo

import com.netflix.appinfo.InstanceInfo
import com.netflix.appinfo.InstanceInfo.InstanceStatus.DOWN
import com.netflix.appinfo.InstanceInfo.InstanceStatus.UP
import com.netflix.discovery.EurekaClient
import com.netflix.discovery.shared.Application
import com.netflix.discovery.shared.Applications
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.lang.reflect.Field


@Component
class EurekaRegistryUpdatedEventListener(
    private val eurekaClient: EurekaClient,
    private val discoveryClient: DiscoveryClient,
    private val environment: Environment,
    @Value("\${spring.application.name}") private val thisApplicationName: String,
) {

    private val log = KotlinLogging.logger { }
    private val restClient = RestClient.create()

    @RabbitListener(queues = [RabbitConfig.QUEUE_NAME])
    fun handEvent(event: EurekaRegistryUpdatedEvent) { // 자기 자신의 정보는 등록할 필요 없다(유레카 등록시 즉시 fetch한다), 예외처리할 것. 타인의 정보만 등록하면 된다

        log.info { "event: $event" }

        val instanceInfo = event.instanceInfo
        val appName: String = instanceInfo.appName
        val status: InstanceInfo.InstanceStatus = event.instanceInfo.status

        if (isThisApplication(appName)) {
            return
        }

        try {

            // UP or DOWN Application, 로컬 캐시에 존재하지 않는 app이 들어올때가 있다
//            val application: Application = eurekaClient.getApplication(appName) ?: return

//            val application = event.application

            val eurekaClientApplicationManager = eurekaClient.applications
            val application = Application(appName, listOf(instanceInfo))

            if (status == UP) {
                eurekaClientApplicationManager.addApplication(application)
            } else if (status == DOWN) {
                eurekaClientApplicationManager.removeApplication(application)
            }
        } catch (exception: Exception) {
            log.error { exception }
            return
        }
    }

    private fun isThisApplication(appName: String) = thisApplicationName.lowercase() == appName.lowercase()


    //    @RabbitListener(queues = [RabbitConfig.QUEUE_NAME])
    fun handEvent_2(event: EurekaRegistryUpdatedEvent) {

        log.info { "event: $event" }

        val instanceInfo = event.instanceInfo

        val applications: Applications = eurekaClient.applications

        eurekaClient.getApplication(event.instanceInfo.appName)

        // Step 1: Access the field
        // AbstractQueue<Application>
        val field: Field = Applications::class.java.getDeclaredField("applications")

        // Step 2: Make the field accessible
        field.isAccessible = true
        field.get(applications)

//        val field: Field = Applications::class.java.getDeclaredField("applications")
//        field.isAccessible = true
//        val app = field.get(applications)

//        val field2: Field = Application::class.java.getDeclaredField("instances")
//        field2.isAccessible = true
//        val app2 = field2.get(app)
//        app2

        // Step 3: Modify the field value
//        field.set(example, "New Secret")

        // Verification
//        val modifiedValue = field.get(example)
//        println("Modified secret value: $modifiedValue")
    }

    /*
        fun handEvent_1(event: EurekaRegistryUpdatedEvent) {

            log.info { "event: $event" }

    //        val applications = eurekaClient.applications
            val services = discoveryClient.services
            val result =
                services.associateWith { serviceId ->
    //                discoveryClient.getInstances(serviceId)
                    eurekaClient.getApplication(serviceId)
                }
            val serverPort = environment.getProperty("server.port")


        val instanceInfo = event.instanceInfo!!
        val applications = eurekaClient.applications


    //        println("event = [${event}]")

    //        val actionResult = restClient.get()
    //            .uri("localhost:$serverPort")
    //            .retrieve()

    //        println("actionResult = ${actionResult}")

    //        refreshRegistry();
        }
    */

    private fun refreshRegistry() {
        val applications = eurekaClient.applications
        println("applications = ${applications}")
    }
}