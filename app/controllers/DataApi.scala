package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.{JsValue, Json}
import play.api.Play.current
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.QueryOpts

import java.net.URLDecoder
import scala.concurrent.Future

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
  val restrictedCollections = config.getStringList("data.auth.restrictedCollections").get

  private def toInt(valStr : String) : Option[Int] = {
    try {
      Some(valStr.toInt)
    } catch {
      case e: Exception => None
    }
  }

  def getCollection(name : String, query: String, filter : String, limit : Int, skip : Int) = Authenticated(parse.anyContent) {
    request =>
      // TODO - make sure data is constrained to data only the user can see (should I also look up organization that the user belongs to and put it on the request too?)
      // IDEA - need a collection that identifies the collections that admin users only can see; plus identify what field must be screened (i.e. username/departmentCode, etc)
      // ? - do we need to be concerned with injection attacks?  If so which ones and how best to shield from them?
      Async {
        if (! restrictedCollections.contains(name)) {
          val collection = db.collection[JSONCollection](name)
          val queryObj = if (query.length > 0) Json.parse(query) else Json.obj()
          val filterObj = if (filter.length > 0) Json.parse(filter) else Json.obj()
          Logger.debug(s"filter: $filter, query: $queryObj : skip: $skip, limit: $limit")
          // NOTE - batchN on QueryOpts is basically useless as a limit as you would think of one from SQL due to how Mongo works (it tells the cursor how many records to retrieve at a time, hence batch and not limit)
          //      - so this is why we put the limit on toList
          //      https://groups.google.com/forum/#!msg/reactivemongo/GNgR2yHN8pA/SdjXFzkFctQJ
          collection.find(queryObj, filterObj).options(QueryOpts().skip(skip)).cursor[JsValue].toList(limit) map {
            results =>
              Ok(Json.toJson(results)).as(JSON)
          } recover {
            case t => NotFound(Json.obj("error" -> t.getMessage))
          }
        } else {
          Future(Unauthorized(Json.obj("error" -> "Attempt to gain access to collection denied.")))
        }
      }
  }

  def getEntity(name : String, id : String) = Authenticated(parse.anyContent) {
    request =>
      Async {
        val collection = db.collection[JSONCollection](name)

        collection.find(Json.obj("_id" -> Json.obj("$oid" -> id))).one[JsValue] map {
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
