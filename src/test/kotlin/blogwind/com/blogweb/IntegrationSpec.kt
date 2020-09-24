package blogwind.com.blogweb

import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Tags("Integration")
class IntegrationSpec : FunSpec({
    tags(Integration)

    test("fail") {
        "1" shouldBe "2"
    }
})