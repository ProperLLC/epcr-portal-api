package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.{JsValue, Json}
import play.api.Play.current
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.QueryOpts

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
  val config = Play.configuration
  val defaultPageSize = config.getInt("data.defaults.pageSize").getOrElse(25)

  private def toInt(valStr : String) : Option[Int] = {
    try {
      Some(valStr.toInt)
    } catch {
      case e: Exception => None
    }
  }

  def getCollection(name : String, filter : String, limit : Int, skip : Int) = Authenticated(parse.anyContent) {
    request =>
      // TODO - make sure data is constrained to data only the user can see (should I also look up organization that the user belongs to and put it on the request too?)
      Async {
        val collection = db.collection[JSONCollection](name)
        Logger.debug(s"filter: $filter, skip: $skip, limit: $limit")
        collection.find(Json.obj()).options(QueryOpts(skip, limit)).cursor[JsValue].toList map {
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
