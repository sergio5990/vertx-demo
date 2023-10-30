package com.example.starter

import io.vertx.kotlin.coroutines.await
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.Tuple
import io.vertx.sqlclient.templates.SqlTemplate


class Repository(private val client: PgPool) {
  suspend fun findAll(): List<Post> {
    return SqlTemplate.forQuery(client, "SELECT * FROM posts")
      .mapTo(PostRowMapper.INSTANCE)
      .execute(emptyMap())
      .await()
      .toList()
  }


  suspend fun findById(id: Long): Post {
    val post = SqlTemplate.forQuery(client, "SELECT * FROM posts WHERE id=#{id}")
      .mapTo(PostRowMapper.INSTANCE)
      .execute(mapOf("id" to id))
      .await()
      .first()
    return post
  }


  suspend fun save(data: PostCommand): Long {
    return client.preparedQuery("INSERT INTO posts (message) VALUES ($1) RETURNING (id)")
      .execute(Tuple.of(data.message))
      .await()
      .first()
      .getLong("id")
  }
}
