package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.{JsValue, Json}
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

/**
 * Created with IntelliJ IDEA.
 * User: terry
 * Date: 5/4/13
 * Time: 11:40 PM
 *
 * Controller for the Data API.  Handles the requests for Collections and Entities in an abstract manner so we don't have
 * to continually write boilerplate methods for grabbing data.
 *
 */
object DataApi extends Controller with TokenSecured with MongoController {

  def getCollection(name : String) = Authenticated(parse.anyContent) {
    request =>
      Async {
        val collection = db.collection[JSONCollection](name)

        collection.find(Json.obj()).cursor[JsValue].toList map {
          results =>
            Ok(Json.toJson(results)).as(JSON)
        } recover {
          case t => NotFound(Json.obj("error" -> t.getMessage))
        }
      }
  }

  def getEntity(name : String, id : String) = Authenticated(parse.anyContent) {
    request =>
      Async {
        val collection = db.collection[JSONCollection](name)

        collection.find(Json.obj("_id" -> Json.obj("$oid" -> id))).cursor[JsValue].toList map {
          results =>
            Ok(Json.toJson(results)).as(JSON)
        } recover {
          case t => NotFound(Json.obj("error" -> t.getMessage))
        }
      }
  }

  def test() = Authenticated(parse.anyContent) { request =>
    val username = request.user
    Ok(Json.obj("status" -> s"OK $username"))
  }

}
