package io.philo

import com.netflix.appinfo.InstanceInfo
import com.netflix.discovery.shared.Application
import io.philo.RabbitConfig.Companion.EXCHANGE_NAME
import io.philo.RabbitConfig.Companion.ROUTING_KEY
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class RabbitSignalSender(private val rabbitTemplate: RabbitTemplate) {

    /**
     * 레지스트리 업데이트 신호 브로드캐스트
     */
    fun sendSignal() {

//        val event = EurekaRegistryUpdatedEvent()

//        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, event)
    }

    fun publishEvent(instanceInfo: InstanceInfo, application: Application?) {

        val event = EurekaRegistryUpdatedEvent(instanceInfo, application)

        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, event)
    }
}