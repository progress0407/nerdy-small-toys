package io.philo.backup

import com.netflix.appinfo.InstanceInfo
import com.netflix.appinfo.InstanceInfo.InstanceStatus.DOWN
import com.netflix.appinfo.InstanceInfo.InstanceStatus.UP
import com.netflix.discovery.EurekaClient
import com.netflix.discovery.shared.Application
import io.philo.EurekaRegistryUpdatedEvent
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.core.env.Environment
import org.springframework.web.client.RestClient


/**
 *This class is currently unsuccessful in development, but we're keeping it for our records.
 *
 * Performs registry updates only for changes made when registering/unregistering with Eureka.
 *
 * @author philo
 */
//@Component
class EurekaRegistryUpdatedEventListener_JustBackup(
    private val eurekaClient: EurekaClient, // CloudEurekaClient
    private val discoveryClient: DiscoveryClient, // CompositeDiscoveryClient
    private val environment: Environment,
    @Value("\${spring.application.name}") private val thisApplicationName: String,
) {

    private val log = KotlinLogging.logger { }
    private val restClient = RestClient.create()


    //    @RabbitListener(queues = [RabbitConfig.UEUE_NAME])
    fun handlEvent_backUp_2(event: EurekaRegistryUpdatedEvent) {

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

            val eurekaClientApplicationManager = eurekaClient.applications
            val application = Application(appName, listOf(instanceInfo))

            if (status == UP) {
                discoveryClient
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


    /**
     * Actuator의 기능을 이용해서 Refresh를 한다
     *
     * 다음의 이유로 개발하지 않음
     * 1. fetch registry에 비해 훨씬 무거운 작업
     * 2. refresh할 때 eureka에 다시 등록하는 과정 또한 포함됨
     */
    @Deprecated("여러 문제 사항이 있어 개발이 중단된 메서드입니다.")
    fun handlEvent_backUp_1(event: EurekaRegistryUpdatedEvent) {

        log.info { "event: $event" }

        val serverPort = environment.getProperty("server.port")

        val actionResult = restClient.get()
            .uri("localhost:$serverPort/actuator/refresh")
            .retrieve()
    }
}