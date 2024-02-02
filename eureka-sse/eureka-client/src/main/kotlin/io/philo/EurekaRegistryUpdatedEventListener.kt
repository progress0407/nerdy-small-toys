package io.philo

import com.netflix.discovery.EurekaClient
import mu.KotlinLogging
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.cloud.netflix.eureka.CloudEurekaClient
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaRegistration
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class EurekaRegistryUpdatedEventListener(private val eurekaClient: EurekaClient) {

    private val log = KotlinLogging.logger { }

    @EventListener
    fun handEvent(event: EurekaRegistryUpdatedEvent) {

        log.info { "event: $event" }
        refreshRegistry();
    }

    private fun refreshRegistry() {
        val applications = eurekaClient.applications
        println("applications = ${applications}")
    }
}