package controllers

import play.api.mvc.SimpleResult

/**
 * Created with IntelliJ IDEA.
 * User: terry
 * Date: 5/18/13
 * Time: 12:10 AM
 *
 * Contains what we need to handle CORS.
 *
 * NOTE - I wanted this in a package class, but that was giving me grief, so I put it here.  At least having to explicitly import it means we're aware of it..
 */
object CORS extends Config {
  private val allowedHost = config.getString("auth.cors.host").getOrElse("http://localhost:8000")

  implicit class SimpleResultsWithCors[A](val result : SimpleResult[A]) {
    def withCors = {
      result.withHeaders("Access-Control-Allow-Origin" -> allowedHost)
    }
  }
}
