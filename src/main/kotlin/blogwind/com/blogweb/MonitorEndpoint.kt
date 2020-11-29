package blogwind.com.blogweb

import io.micronaut.health.HealthStatus
import io.micronaut.management.endpoint.annotation.Endpoint
import io.micronaut.management.endpoint.annotation.Read
import io.micronaut.management.endpoint.health.HealthEndpoint
import io.micronaut.management.health.aggregator.HealthAggregator
import io.micronaut.management.health.indicator.HealthIndicator
import io.micronaut.management.health.indicator.HealthResult
import io.reactivex.Single

@Endpoint(id = "monitor", defaultEnabled = true, defaultSensitive = false)
class MonitorEndpoint(healthAggregator: HealthAggregator<HealthResult>, healthIndicators: Array<HealthIndicator>) : HealthEndpoint(healthAggregator, healthIndicators) {
    @Read
    fun getMonitorData(): Single<CSMonitoring> {
        val result = this.getHealth(null)
        return result.map {
            CSMonitoring(it.name, it.status)
        }
    }
}

data class CSMonitoring(
        val name: String,
        val status: HealthStatus
)