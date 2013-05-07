package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.Json

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
object DataApi extends Controller with TokenSecured {

  def getCollection(collection : String) = Action {
    Ok(Json.obj("error" -> "Not Yet Implemented")).as(JSON)
  }

  def getEntity(collection : String, id : String) = Action {
    Ok(Json.obj("error" -> "Not Yet Implemented")).as(JSON)
  }

  def test() = Authenticated(parse.anyContent) { request =>
    val username = request.user
    Ok(Json.obj("status" -> s"OK $username"))
  }

}
