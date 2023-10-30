package com.example.starter

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.core.json.jackson.DatabindCodec.*
import io.vertx.core.json.jackson.VertxModule
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.LoggerHandler
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.lang.Compiler.disable


suspend fun main() {
  val vertx = Vertx.vertx()
  configureObjectMapper(mapper())

  val pgPool = pgPool(vertx)
  val repository = Repository(pgPool)
  val apiHandler = ApiHandler(repository)

  val router = Router.router(vertx)
  routes(router, apiHandler)


  runServer(vertx, router)
}

fun configureObjectMapper(mapper: com.fasterxml.jackson.databind.ObjectMapper) {
  mapper.apply {
    registerModule(kotlinModule())
    enable(SerializationFeature.INDENT_OUTPUT)
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
  }
}

private suspend fun routes(router: Router, apiHandler: ApiHandler) {
  router.route().handler(LoggerHandler.create())
  router.route().handler(BodyHandler.create())
  router.get("/posts")
    .coroutineHandler {
      apiHandler.all(it)
    }

  router.post("/posts")
    .coroutineHandler {
      apiHandler.save(it)
    }

  router.get("/posts/:id")
    .coroutineHandler {
      apiHandler.getById(it)
    }

  router.route().failureHandler {
    println(it.failure())
    it.failure().printStackTrace()
      it.response()
        .setStatusCode(500)
        .end("error")
  }
}

private suspend fun runServer(vertx: Vertx, router: Router?) {
  val httpServer = vertx.createHttpServer().requestHandler(router)
  val mainVerticle = MainVerticle(httpServer)
  vertx.deployVerticle(mainVerticle).await()
}

fun Route.coroutineHandler(requestHandler: suspend (RoutingContext) -> Unit): Route {
  return handler { ctx ->
    val dispatcher = ctx.vertx().dispatcher()
    CoroutineScope(dispatcher).launch {
      try {
        requestHandler(ctx)
      } catch (e: RuntimeException) {
        ctx.fail(e)
      }
    }
  }
}

private fun pgPool(vertx: Vertx): PgPool {
  val connectOptions = PgConnectOptions()
    .setPort(5432)
    .setHost("localhost")
    .setDatabase("postdb")
    .setUser("user")
    .setPassword("password")

  // Pool Options
  val poolOptions = PoolOptions().setMaxSize(5)

  // Create the pool from the data object
  return PgPool.pool(vertx, connectOptions, poolOptions)
}

