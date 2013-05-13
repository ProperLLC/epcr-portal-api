package controllers

import play.api.mvc._
import play.api.{Play, Logger}
import play.api.Play.current

import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global

import java.security.MessageDigest

import services.UserSessionService

/**
 * Created with IntelliJ IDEA.
 * User: terry
 * Date: 5/5/13
 * Time: 6:39 PM
 *
 * Handles the endpoints around Authentication/Authorization.  The scheme is somewhat like OAuth2, but not officially.
 *
 */
object Auth extends Controller with TokenSecured {
  val config = Play.configuration
  val allowedHost = config.getString("auth.cors.host").getOrElse("http://localhost:8000")
  val digest = MessageDigest.getInstance("MD5")


  def md5(message : Option[String]) = {
    message.map( msg => digest.digest(msg.getBytes).map("%02x".format(_)).mkString)
  }

  def login = Action(parse.json) { request =>
    val username = (request.body \ "username").as[String]
    val password = md5((request.body \ "password").asOpt[String])
    val remoteAddress = request.remoteAddress
    val userAgent = request.headers.get("User-Agent").getOrElse("NOT_DEFINED")
    Logger.debug(s"Login with: $username / $password : $remoteAddress : $userAgent")
    val futureSession = UserSessionService.createToken(username, password.getOrElse(""), remoteAddress, userAgent)

    Async {
      futureSession.map {
        session =>
          if (session.isDefined) {
            Ok(session.get).withHeaders("Access-Control-Allow-Origin" -> allowedHost) // for now - let's see if we can get CORS to work..
          } else {
            Logger.warn(s"NOTICE - Invalid login for $username from ${request.remoteAddress} using $userAgent")
            Unauthorized(Json.obj("error" -> "Invalid Credentials")).withHeaders("Access-Control-Allow-Origin" -> allowedHost)
          }
      } recover {
        case e : java.util.NoSuchElementException =>
          Logger.warn(s"Auth.login - NOTICE : Attempt to login with invalid credentials: ${username}")
          Unauthorized(Json.obj("error" -> "Invalid Credentials")).withHeaders("Access-Control-Allow-Origin" -> allowedHost)
        case t =>
          Logger.debug(s"Auth.login - Error : ${t.getMessage}")
          InternalServerError(Json.obj("error" -> "An error occurred while processing your request: ${t.getMessage}")).withHeaders("Access-Control-Allow-Origin" -> allowedHost)
      }
    }
  }

  def logout = Authenticated(parse.anyContent) { request =>
    Async {
      UserSessionService.invalidateTokensForUser(request.user.username).map {
        results =>
          if (results.error) {
            InternalServerError(Json.obj("error" -> results.message))
          } else {
            Ok(Json.obj("results" -> results.message))
          }
      }
    }
  }

  // For CORS
  def options(url: String) = Action {
    Ok("").withHeaders(
      "Access-Control-Allow-Origin" -> allowedHost,
      "Access-Control-Allow-Methods" -> "GET, POST, PUT, DELETE, OPTIONS",
      "Access-Control-Allow-Headers" -> "Content-Type, X-Requested-With, Accept",
      // cache access control response for one day
      "Access-Control-Max-Age" -> (60 * 60 * 24).toString
    )
  }

  def headers = Action { request =>
    Logger.info(s"Remote Address: ${request.remoteAddress}")
    Ok(request.headers.toSimpleMap.toString).as(JSON)
  }

  /**
   * Used to recover from a failure with Mongo; logs the error and returns a None so that processing can continue.
   *
   * @param t
   * @param method
   * @return
   */
  private def handleRecovery(t: Throwable, method : String) = {
    val error = t.getMessage
    Logger.debug(s"UserSessionService.$method Error: $error")
    Json.obj("error" -> error)
  }
}
