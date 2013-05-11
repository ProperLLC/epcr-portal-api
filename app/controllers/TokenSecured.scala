package controllers

import play.api.mvc._
import play.api.libs.json.Json
import services.{UserService, UserSessionService}

import scala.concurrent._
import scala.concurrent.duration._

import ExecutionContext.Implicits.global

import models._

/**
 * Created with IntelliJ IDEA.
 * User: terry
 * Date: 5/4/13
 * Time: 11:50 PM
 *
 * Trait and supporting case classes to implement the token authorization
 *
 */
trait TokenSecured {
  def Authenticated[A](p: BodyParser[A])(f: AuthenticatedRequest[A] => Result) = {
    Action(p) { request =>
      val result = for {
        token <- findTokenInHeader(request)
        user <- tokenLookup(token, request).value
      } yield f(AuthenticatedRequest(user.get.get, request))  // this is kinda hokey - need to figure out how to do this properly
      result getOrElse onUnauthorized(request)
    }
  }

  /**
   * Decode the basic auth header, if it exists.
   * @param auth
   * @return
   */
  private def parseAuthHeader(auth: String) : Option[String] = {
    // Header takes the form of: Authorization: Bearer <token>
    // NOTE - this is almost what OAuth2 expects - however we're not quite implementing that...yet
    auth.split(" ").toList match {
      case b :: t :: Nil =>
        if (b == "Bearer")
          Some(t)
        else
          None
      case _ => None
    }
  }

  /**
   * Redirect to login if the user in not authorized.
   */
  // TODO - determine if I want to return a header, or just some JSON
  private def onUnauthorized(request: RequestHeader) = Results.Unauthorized(Json.obj("error" -> "Unauthorized Access!"))

  /**
   * Function that will lookup the user session in the db given the token, ip address & user agent.
   *
   * @param token
   * @return
   */
  def tokenLookup(token : String, request : RequestHeader) = {
    val futureResults = UserSessionService.validateToken(token, request.remoteAddress, request.headers.get("User-Agent").getOrElse("NOT_DEFINED")).map {
      session =>
        UserService.findUserByUsername(session.getOrElse(throw new Exception("No username found for session")))
    }
    // NOTE - I couldn't figure out how to get the for comprehension to work the way I wanted it to above using Futures.  This is only until I can sort that out because this is not optimal.
    Await.result(futureResults, 10 seconds)
  }

  /**
   *
   * @param request
   * @return
   */
  private def findTokenInHeader(request: RequestHeader) : Option[String] = {
    request.headers.get("Authorization").map{ headerValue =>
      parseAuthHeader(headerValue)
    }.getOrElse(None)
  }
}

case class AuthenticatedRequest[A](user: User, private val request: Request[A]) extends WrappedRequest(request)
