package io.philo

import com.netflix.appinfo.InstanceInfo
import com.netflix.discovery.shared.Application

data class EurekaRegistryUpdatedEvent(val instanceInfo: InstanceInfo, val application: Application? = null) {
}
