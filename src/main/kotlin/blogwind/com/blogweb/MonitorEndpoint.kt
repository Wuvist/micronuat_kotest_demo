package blogwind.com.blogweb

import io.micronaut.health.HealthStatus
import io.micronaut.management.endpoint.annotation.Endpoint
import io.micronaut.management.endpoint.annotation.Read
import io.micronaut.management.endpoint.health.HealthLevelOfDetail
import io.micronaut.management.health.aggregator.HealthAggregator
import io.micronaut.management.health.indicator.HealthIndicator
import io.micronaut.management.health.indicator.HealthResult
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import org.reactivestreams.Publisher
import java.util.*
import java.util.stream.Collectors
import javax.inject.Singleton

@Endpoint(id = "monitor", defaultEnabled = true, defaultSensitive = false)
class MonitorEndpoint(private val healthAggregator: HealthAggregator<HealthResult>,
                      private val apis: Array<HTTPIndicator>,
                      private val healthIndicators: Array<HealthIndicator>) {
    @Read
    fun getMonitorData(): Single<MonitoringResult> {
        val result = healthAggregator.aggregate(healthIndicators, HealthLevelOfDetail.STATUS_DESCRIPTION_DETAILS)
        val result2 = Flowable.fromPublisher(aggregateHTTP(apis)).toList()

        return Single.fromPublisher(result).zipWith(result2, { it, apiResult ->
            MonitoringResult(it.name, it.status, apiResult, it.details)
        })
    }

    private fun aggregateHTTP(indicators: Array<HTTPIndicator>): Flowable<CheckResult> {
        val results = Arrays.stream(indicators)
                .map { it.check() }
                .collect(Collectors.toList())
        return Flowable.merge(results)
    }
}

@Singleton
class BackendApiIndicator(private val backendApi: BackendApi) : HTTPIndicator("backendApi", backendApi) {}

@Singleton
class BackendApi2Indicator(private val backendApi: BackendApi2) : HTTPIndicator("backendApi2", backendApi) {}

open class HTTPIndicator(private val name: String, private val client: MonitorApi) {
    fun check(): Publisher<CheckResult> {
        return client.checkHealth().map {
            CheckResult(name, true)
        }.onErrorReturn {
            CheckResult(name, false)
        }.toFlowable()
    }
}

data class CheckResult(
        val name: String,
        val status: Boolean
)

data class MonitoringResult(
        val name: String,
        val status: HealthStatus,
        val data: Any,
        val details: Any
)