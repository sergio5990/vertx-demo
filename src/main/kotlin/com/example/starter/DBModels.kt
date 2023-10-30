package com.example.starter

import io.vertx.codegen.annotations.DataObject
import io.vertx.sqlclient.templates.annotations.RowMapped

@DataObject
@RowMapped
class Post(var id: Long = -1, var message: String = "")

@DataObject
@RowMapped
class Comment(var id: Long = -1, var postId: Long = -1, var text: String = "")
