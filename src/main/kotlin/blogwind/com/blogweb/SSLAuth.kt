package blogwind.com.blogweb

import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.AuthenticationException
import io.micronaut.security.authentication.AuthenticationFailed
import io.micronaut.security.filters.AuthenticationFetcher
import io.reactivex.Maybe
import org.reactivestreams.Publisher
import javax.inject.Singleton


@Singleton
class SSLAuth : AuthenticationFetcher {
    override fun fetchAuthentication(request: HttpRequest<*>): Publisher<Authentication> {
        val result = Maybe.create<Authentication> {
            println(request.uri.path)
            if (request.uri.path == "/monitor") {
                println("it.onComplete()")
                it.onComplete()
                return@create
            }
            it.onError(AuthenticationException(AuthenticationFailed()))
//            it.onSuccess(DefaultAuthentication("bingo", mapOf()))
        }

        return result.toFlowable()
    }
}