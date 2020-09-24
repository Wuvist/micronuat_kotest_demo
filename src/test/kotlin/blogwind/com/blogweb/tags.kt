package blogwind.com.blogweb

import io.kotest.core.Tag
import io.kotest.core.Tags
import io.kotest.core.extensions.TagExtension

object Integration : Tag()

object IntegrationExtension : TagExtension {

    override fun tags(): Tags {
        if (!System.getProperty("run").isNullOrEmpty()) {
            return Tags.include(Integration)
        }

        return Tags.exclude(Integration)
    }
}