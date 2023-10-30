package com.example.starter

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.http.HttpServer
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await

class MainVerticle(private val httpServer: HttpServer) : CoroutineVerticle() {

  override suspend fun start() {
    httpServer
      .listen(8088)
      .onComplete { println("HttpSever started at ${it.result().actualPort()}") }
      .await()
  }
}
