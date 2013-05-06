package services

// Reactive Mongo Imports
import reactivemongo.api._
import play.Logger

// Reactive Mongo plugin
import play.modules.reactivemongo._
import play.modules.reactivemongo.json.collection.JSONCollection

// Play Json imports
import play.api.libs.json._
import play.api.Play.current

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import org.joda.time._

/**
 * Created with IntelliJ IDEA.
 * User: terry
 * Date: 5/5/13
 * Time: 7:19 PM
 *
 * This service performs actions on the UserSession entity.
 */

object UserSessionService {
  val db : DefaultDB = ReactiveMongoPlugin.db
  lazy val collection = db.collection[JSONCollection]("userSessions")

  def validateToken(token : String, remoteAddress : String, userAgent : String) = {
    val futureResults = collection.find(Json.obj("token" -> token, "remoteAddress" -> remoteAddress, "userAgent" -> userAgent)).projection(Json.obj("username" -> 1, "expires" -> 1)).cursor[JsValue].headOption

    val results = futureResults.map { session =>
      val expiresDate = (session.get \ "expires").as[Int]
      val now = DateTime.now().getMillis()
      if (expiresDate > now) {
        Some((session.get \ "username").as[String])
      } else {
        Logger.debug(s"Token expired => expiry time: $expiresDate vs now: $now")
        None
      }
    } recover {
      case t =>
        val error = t.getMessage
        Logger.debug(s"UserSessionService.findUserSession Error: $error")
        None
    }
    results
  }
}
