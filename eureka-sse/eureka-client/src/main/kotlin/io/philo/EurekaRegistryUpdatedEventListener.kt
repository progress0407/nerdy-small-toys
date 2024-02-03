package io.philo

import com.netflix.discovery.EurekaClient
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.aop.framework.Advised
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.cloud.netflix.eureka.CloudEurekaClient
import org.springframework.stereotype.Component
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible
import com.netflix.discovery.DiscoveryClient as NetflixDiscoveryDiscoveryClient


/**
 * Update the registry list when Eureka is registered or unregistered.
 * This is not done when the target of the event is the instance itself.
 *
 * @author philo
 */
@Component
class EurekaRegistryUpdatedEventListener(

    private val eurekaClient: EurekaClient, // CloudEurekaClient injected in runtime

    private val discoveryClient: DiscoveryClient, // CompositeDiscoveryClient injected in runtime

    @Value("\${spring.application.name}")
    private val thisApplicationName: String,
) {

    private val log = KotlinLogging.logger { }

    private val realDiscoveryClient = extractDiscoveryClientFromAopProxy()
    private val refreshRegistryMethod = extractRefreshRegistryMethod()

    @RabbitListener(queues = [RabbitConfig.QUEUE_NAME])
    fun handEvent(event: EurekaRegistryUpdatedEvent) {

        log.info { "event: $event" }

        val instanceInfo = event.instanceInfo
        val appName: String = instanceInfo.appName

        if (isThisApplication(appName))
            return

        try {
            refreshRegistryMethod.call(realDiscoveryClient)

        } catch (exception: Exception) {
            log.error { exception }
            return
        }
    }

    /**
     * Retrieve the target object from the CloudEurekaClient AOP object that dependency injected at runtime
     * and upcasts it to the com.netflix.discovery.DiscoveryClient.
     */
    private fun extractDiscoveryClientFromAopProxy() =
        ((eurekaClient as Advised).targetSource.target as CloudEurekaClient) as NetflixDiscoveryDiscoveryClient

    /**
     * Extract `refreshRegistry` as a feature of Java Reflection.
     */
    private fun extractRefreshRegistryMethod(): KFunction<*> {
        return NetflixDiscoveryDiscoveryClient::class.declaredMemberFunctions
            .firstOrNull { it.name == "refreshRegistry" && it.parameters.size == 1 }?.apply { isAccessible = true }!!
    }

    /**
     * Instance self information does not need to be registered.
     *
     * todo Currently, it compares the same name, but there can be multiple instances within an application. Let's change to comparing actual instance IDs.
     */
    private fun isThisApplication(appName: String) = thisApplicationName.lowercase() == appName.lowercase()
}