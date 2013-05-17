package services

import play.api.{Play, Logger}

// Reactive Mongo Imports
import reactivemongo.api._

// Reactive Mongo plugin
import play.modules.reactivemongo._
import play.modules.reactivemongo.json.collection.JSONCollection

// Play Json imports
import play.api.libs.json._
import play.api.Play.current

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

import org.joda.time._
import java.util.UUID
import org.apache.commons.codec.binary.Base64

/**
 * Created with IntelliJ IDEA.
 * User: terry
 * Date: 5/5/13
 * Time: 7:19 PM
 *
 * This service performs actions on the UserSession entity.
 */

object UserSessionService {
  val config = Play.configuration
  val tokenTimeout = config.getInt("auth.token.timeout")  // NOTE - ultimately we may want to get this from the db per department, but for now, configured for all
  val randomSeed = config.getLong("auth.token.seed")

  val db : DefaultDB = ReactiveMongoPlugin.db
  lazy val userCollection = db.collection[JSONCollection]("users")
  lazy val sessionCollection = db.collection[JSONCollection]("userSessions")

  def createToken(username : String, password : String, remoteAddress: String, userAgent : String) = {
    val futureUser = userCollection.find(Json.obj("username" -> username, "password" -> password)).one[JsValue]
    val results = futureUser.filter(_.isDefined).flatMap { user =>
      val token = buildToken(user.get, remoteAddress, userAgent)
      val insertResults = sessionCollection.insert(token).map {
        lastError =>
          val message = lastError.errMsg
          Logger.debug(s"UserSessionService.createToken (insert Token) results: $message")
          if (lastError.ok) {
            val response = Json.obj(
              "auth_token" -> (token \ "authToken").as[String],
              "type" -> "Bearer",
              "expires" -> (token \ "expires").as[Long]
            )
            Some(response)
          } else {
            None
          }
      }
      insertResults
    }
    results
  }

  def validateToken(token : String, remoteAddress : String, userAgent : String) = {
    Logger.debug(s"token: $token | remoteAddr: $remoteAddress | userAgent: $userAgent")
    val futureResults = sessionCollection.find(Json.obj("authToken" -> token, "remoteAddress" -> remoteAddress, "userAgent" -> userAgent)).projection(Json.obj("username" -> 1, "expires" -> 1)).cursor[JsValue].headOption

    val results = futureResults.map { session =>
      val expiresDate = (session.get \ "expires").as[Long]
      val now = DateTime.now().getMillis()
      if (expiresDate > now) {
        Some((session.get \ "username").as[String])
      } else {
        Logger.debug(s"Token expired => expiry time: $expiresDate vs now: $now")
        // TODO - remove token from db?
        None
      }
    } recover {
      case t => handleRecovery(t, "validateToken")
    }
    results
  }

  def invalidateTokensForUser(username : String) = {
    Logger.debug(s"Invalidating tokens for $username")
    // NOTE - this will invalidate ALL tokens...
    sessionCollection.remove(Json.obj("username" -> username)).map {
      lastError =>
        Logger.debug(s"Invalidate token results ${lastError.errMsg.getOrElse("OK")}")
        if (lastError.ok) {
          OperationResults(message = "success")
        } else {
          OperationResults(error = true, lastError.errMsg.get)
        }
    }
  }

  def buildToken(user : JsValue, remoteAddress : String, userAgent : String) = {
    val username = (user \ "username").as[String]
    val password = (user \ "password").as[String] // is this wise?
    val expires = DateTime.now().getMillis() + tokenTimeout.getOrElse(36000000) // default to 1 hour if not otherwise configured
    val randomNumber = new Random(randomSeed.getOrElse(572392734l)).nextLong()
    val token = new String(Base64.encodeBase64(s"$username:$password:$remoteAddress:$userAgent:$expires:$randomNumber".getBytes))  // probably should put a psuedo-random number in here...but expires is close enough(?)
    Json.obj(
        "username" -> username,
        "userAgent" -> userAgent,
        "remoteAddress" -> remoteAddress,
        "authToken" -> token,
        "expires" -> expires
     )
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
    None
  }

  case class OperationResults(error : Boolean = false, message : String)
}
