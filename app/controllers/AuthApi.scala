package controllers

import play.api.mvc._

import play.api.libs.json.Json

import play.modules.hawk.HawkSecuredAction
import play.modules.reactivemongo.MongoController

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

import controllers.support.BasicAuthSecured
import services.AppCredentialsService
import org.joda.time.DateTime

import models._

/**
 * This Controller provides the application interface to the authorization system.  It will grant credentials as
 * well as expire them.  It also contains the default value of the HTTP OPTIONS verb for CORS support.
 *
 * Created by terry on 1/2/2014.
 */
object AuthApi extends Controller with MongoController {

  def credentials = BasicAuthSecured.async {
    request =>
      AppCredentialsService.createCredentials(request.email).map {
        maybeCreds =>
          if (maybeCreds.isDefined)
            Ok(Json.toJson(maybeCreds.get))
          else
            BadRequest(Json.obj("error" -> s"could not create credentials for ${request.email}"))
      }
  }

  def expire = HawkSecuredAction.async {
    request =>
      AppCredentialsService.expire(request.clientId).map {
        case Success(status) =>
          Ok(Json.obj("results" -> "success", "status" -> status))
        case Failure(e) =>
          BadRequest(Json.obj("results" -> "error", "status" -> "500", "message" -> e.getMessage))
      }
  }

  def ping = Action {
    Ok(Json.obj("pong" -> DateTime.now().getMillis))
  }

  def options(url: String) = Action {
    Ok(Json.obj("results" -> "success")).withHeaders(
      "Access-Control-Allow-Methods" -> "GET, POST, PUT, DELETE, OPTIONS",
      "Access-Control-Allow-Headers" -> "Content-Type, X-Requested-With, Accept, Authorization, User-Agent",
      "Access-Control-Max-Age" -> (60 * 60 * 24).toString
    )
  }
}
