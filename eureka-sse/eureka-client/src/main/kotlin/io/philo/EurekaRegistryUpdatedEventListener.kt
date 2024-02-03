package io.philo

import com.netflix.discovery.EurekaClient
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.aop.framework.Advised
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.cloud.netflix.eureka.CloudEurekaClient
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible


/**
 * Update the registry list when Eureka is registered or unregistered.
 * This is not done when the target of the event is the instance itself.
 *
 * @author philo
 */
@Component
class EurekaRegistryUpdatedEventListener(
    private val eurekaClient: EurekaClient, // CloudEurekaClient
    private val discoveryClient: DiscoveryClient, // CompositeDiscoveryClient
    private val environment: Environment,
    @Value("\${spring.application.name}") private val thisApplicationName: String,
) {

    private val log = KotlinLogging.logger { }
    private val restClient = RestClient.create()

    @RabbitListener(queues = [RabbitConfig.QUEUE_NAME])
    fun handEvent(event: EurekaRegistryUpdatedEvent) {

        log.info { "event: $event" }

        val instanceInfo = event.instanceInfo
        val appName: String = instanceInfo.appName

        if (isThisApplication(appName))
            return

        try {
            val realDiscoveryClient = ((eurekaClient as Advised).targetSource.target as CloudEurekaClient) as com.netflix.discovery.DiscoveryClient

            val refreshRegistryMethod = com.netflix.discovery.DiscoveryClient::class.declaredMemberFunctions
                .firstOrNull { it.name == "refreshRegistry" && it.parameters.size == 1}?.apply { isAccessible = true }

            refreshRegistryMethod!!.call(realDiscoveryClient)

        } catch (exception: Exception) {
            log.error { exception }
            return
        }
    }

    /**
     * Instance self information does not need to be registered.
     */
    private fun isThisApplication(appName: String) = thisApplicationName.lowercase() == appName.lowercase()
}