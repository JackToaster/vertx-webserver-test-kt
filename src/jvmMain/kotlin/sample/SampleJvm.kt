package sample

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver


actual class Sample {
    actual fun checkMe() = 42
}

actual object Platform {
    actual val name: String = "JVM"
}


fun main() {
    val vertx = Vertx.vertx()
    var server = vertx.createHttpServer()

    val engine = ThymeleafTemplateEngine.create(vertx)

    val templateResolver = ClassLoaderTemplateResolver()
    templateResolver.prefix = "templates/"
    templateResolver.suffix = ".html"
    engine.thymeleafTemplateEngine.setTemplateResolver(templateResolver)

    var router = Router.router(vertx)

    router.route("/vertx-webserver-test.js").handler{ routingContext ->
        var response = routingContext.response()
        response.putHeader("content-type", "application/javascript")

        // Write to the response and end it
        response.sendFile("vertx-webserver-test.js")
        response.end()
    }
//    router.route("/vertx-webserver-test.js.map").handler{ routingContext ->
//        var response = routingContext.response()
//        response.putHeader("content-type", "application/json")
//
//        // Write to the response and end it
//        response.sendFile("vertx-webserver-test.js.map")
//        response.end()
//    }

    router.route("/static/*").handler(StaticHandler.create().setDirectoryListing(true))

    router.route("/").handler { routingContext ->
        // This handler will be called for every request
        routingContext.put("checkMe", Sample().checkMe())
        engine.render(routingContext.data(), "thymeleaftest") { res ->
            if(res.succeeded()){
                routingContext.response().end(res.result())
            }else{
                routingContext.fail(res.cause())
            }
        }
    }

    server.requestHandler(router).listen(8080)
}
