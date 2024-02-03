package io.philo.app

import com.netflix.appinfo.InstanceInfo
import com.netflix.discovery.EurekaClient
import com.netflix.discovery.shared.Application
import io.philo.RabbitSignalSender
import mu.KotlinLogging
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

/**
 * When a new instance is registered on the Eureka server,
 * broadcast events to Eureka clients
 *
 * @author philo
 */
@Component
class EurekaEventListener(
    private val eventPublisher: ApplicationEventPublisher,
    private val discoveryClient: DiscoveryClient,
    private val eurekaClient: EurekaClient,
    private val rabbitSignalSender: RabbitSignalSender,
) {

    private val log = KotlinLogging.logger { }
    private val restClient = RestClient.create()

    @EventListener
    fun handEvent(event: EurekaInstanceRegisteredEvent) {

        log.info { "event: $event" }

        val instanceInfo: InstanceInfo? = event.instanceInfo

        if (instanceInfo != null) {
            val appName: String = instanceInfo.appName
            val application: Application? = eurekaClient.getApplication(appName)
            rabbitSignalSender.publishEvent(instanceInfo, application)
        }

        /**
         * todo  eureka를 제외한 모든 모듈을 강제 refresh (좋은 방법은 아니다...)
         */
    }
}