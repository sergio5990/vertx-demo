package com.example.starter

import io.vertx.core.json.Json
import io.vertx.core.json.jackson.DatabindCodec.mapper
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.await

class ApiHandler(private val repository: Repository) {
    suspend fun all(rc: RoutingContext) {
      val data = repository.findAll()
      rc.response().send(Json.encodeToBuffer(data)).await()
    }

    suspend fun getById(rc: RoutingContext) {
      val id = rc.pathParam("id").toLong()
      val data = repository.findById(id)
      rc.response().send(Json.encodeToBuffer(data)).await()
    }

    suspend fun save(rc: RoutingContext) {
      val command = rc.body().asPojo(PostCommand::class.java)
      val savedId = repository.save(command)

      rc.response()
        .putHeader("Location", "/posts/$savedId")
        .setStatusCode(201)
        .end()
        .await()

    }
}
