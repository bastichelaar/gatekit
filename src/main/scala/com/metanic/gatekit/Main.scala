package com.metanic.gatekit

import akka.actor._
import akka.stream._

import akka.http.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._

import Directives._

object Main extends App with Routes {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  Http().bindAndHandle(routes, "0.0.0.0", 5000)
}

trait Routes extends ProxyDirectives {
  implicit val system: ActorSystem
  implicit val materializer: Materializer

  val routes = {
    stripInvalidHttpRequestHeaders {
      stripInvalidHttpResponseHeaders {
        adjustRequestHostHeader("www.google.com") {
          extractRequest { request ⇒
            complete(
              Http().singleRequest(request)
            )
          }
        }
      }
    }
  }
}
