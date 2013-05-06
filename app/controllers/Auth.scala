package controllers

import play.api.mvc._
import play.api.{Play, Logger}
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.json.{JsValue, Json}

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
object Auth extends Controller with MongoController {

  lazy val userCollection = db.collection[JSONCollection]("users")
  lazy val sessionCollection = db.collection[JSONCollection]("userSessions")

  def login(username : String) = Action(parse.json) { request =>
    val password = (request.body \ "password").as[String]
    val remoteAddress = request.remoteAddress
    val userAgent = request.headers.get("User-Agent").getOrElse("NOT_DEFINED")
    Logger.debug(s"Login with: $username / $password : $remoteAddress : $userAgent")
    val futureSession = UserSessionService.createToken(username, password, remoteAddress, userAgent)

    Async {
      futureSession.map {
        session =>
          if (session.isDefined) {
            Ok(session.get).as(JSON)
          } else {
            Unauthorized(Json.obj("error" -> "Invalid Credentials")).as(JSON)
          }
      } recover {
        case t =>
          val error = t.getMessage()
          Logger.debug(s"Error : $error")
          InternalServerError(Json.obj("error" -> error)).as(JSON)
      }
    }
  }

  def logout(username : String) = Action {
    Ok("Not Yet Implemented")
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
