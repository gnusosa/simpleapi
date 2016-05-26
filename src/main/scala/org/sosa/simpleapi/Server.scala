package org.sosa

import org.http4s.server.blaze.BlazeBuilder

object BlazeExample extends App {
  BlazeBuilder.bindHttp(8080)
    .mountService(TodoService.httpService, "/")
    .run
    .awaitShutdown()
}
