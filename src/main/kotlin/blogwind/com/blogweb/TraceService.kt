package blogwind.com.blogweb
import io.micronaut.http.HttpRequest
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory

import javax.inject.Singleton

@Singleton
class TraceService {
    private val LOG = LoggerFactory.getLogger(TraceService::class.java)

    internal fun trace(request: HttpRequest<*>): Flowable<Boolean> {
//        request.certificate.get().publicKey.
        return Flowable.fromCallable {
            if (LOG.isDebugEnabled) {
                LOG.debug("Tracing request: " + request.uri)
            }
            // trace logic here, potentially performing I/O
            true
        }.subscribeOn(Schedulers.io())
    }
}