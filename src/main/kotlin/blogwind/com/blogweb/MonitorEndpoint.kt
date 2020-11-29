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
import kotlinx.coroutines.rx2.await
import org.reactivestreams.Publisher
import java.util.*
import java.util.stream.Collectors
import javax.inject.Singleton


@Endpoint(id = "monitor", defaultEnabled = true, defaultSensitive = false)
class MonitorEndpoint(private val healthAggregator: HealthAggregator<HealthResult>,
                      private val apis: Array<HTTPIndicator>,
                      private val healthIndicators: Array<HealthIndicator>) {
    @Read
    suspend fun getMonitorData(): CSMonitoring {
        val result = healthAggregator.aggregate(healthIndicators, HealthLevelOfDetail.STATUS_DESCRIPTION_DETAILS)
        val result2 = prepare(aggregateHTTP(apis))

        return Single.fromPublisher(result).map {
            CSMonitoring(it.name, it.status, result2, it.details)
        }.await()
    }

    private suspend fun prepare(result: Flowable<CheckResult>): List<CheckResult> {
        return Flowable.fromPublisher(result).toList().await()
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

data class CSMonitoring(
        val name: String,
        val status: HealthStatus,
        val data: Any,
        val details: Any
)