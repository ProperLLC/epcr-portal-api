package services

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

import models._
/**
 * Created with IntelliJ IDEA.
 * User: terry
 * Date: 5/10/13
 * Time: 11:20 PM
 * To change this template use File | Settings | File Templates.
 */
object UserService {
  val db = ReactiveMongoPlugin.db
  val userCollection = db.collection[JSONCollection]("users")

  def findUserByUsername(username : String) : Future[Option[User]] = {
    userCollection.find(Json.obj("username" -> username)).one[JsValue].map {
      user =>
        if (user.isDefined) {
          val u = user.get.as[User]
          Some(u)
        }
        else
          None
    }
  }
}
