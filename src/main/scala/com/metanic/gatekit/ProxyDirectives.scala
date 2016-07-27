package com.metanic.gatekit

import scala.collection._

import akka.http.scaladsl.model._
import akka.http.scaladsl.server._

import Directives._
import Uri._

trait ProxyDirectives {
  import ProxyDirectives._

  /** Strips off outbound request headers not allowed by akka-http. */
  def stripInvalidHttpRequestHeaders: Directive0 = {
    mapRequestHeaders(_.filterNot(header ⇒ {
      InvalidHttpRequestHeaders.contains(header.name) || (header.is("expect") && header.value.toLowerCase == "100-continue")
    }))
  }

  /** Strips off inbound response headers not allowed by akka-http. */
  def stripInvalidHttpResponseHeaders: Directive0 = {
    mapResponseHeaders(_.filterNot(header ⇒ InvalidHttpResponseHeaders.contains(header.name)))
  }

  def adjustRequestHostHeader(host: String, port: Int = 80, scheme: String = "http"): Directive0 = {
    mapRequest { request ⇒
      request.copy(
        uri = request.uri.copy(
          authority = Authority(Host(host), port),
          scheme = scheme
        )
      )
    }
  }

  private def mapRequestHeaders(f: immutable.Seq[HttpHeader] ⇒ immutable.Seq[HttpHeader]): Directive0 = {
    mapRequest(_.mapHeaders(f))
  }
}

object ProxyDirectives {
  /**
   * Headers not allowed by akka-http to be set on outbound (to the target) requests
   * since they are calculated by akka-htt itself.
   */
  val InvalidHttpRequestHeaders = Set(
    "Content-Type",
    "Content-Length",
    "Host",
    "Timeout-Access"
  )

  /**
   * Headers not allowed by akka-http to be set on outbound (to the requesting client)
   * responses since they are calculated by akka-http itself.
   */
  val InvalidHttpResponseHeaders = Set(
    "Content-Type",
    "Content-Length",
    "Transfer-Encoding",
    "Server",
    "Date",
    "Connection"
  )
}
