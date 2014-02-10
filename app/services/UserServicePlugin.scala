package services

import play.api.{Logger, Application}

import play.api.libs.json.Json

import play.modules.hawk.{Identity, Credentials, IdentityServicePlugin}
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.Await
import scala.concurrent.duration._

import models.User

/**
 * Created by terry on 10/1/13.
 */
class UserServicePlugin(application: Application) extends IdentityServicePlugin(application) with MongoSupport {

  def users = db.collection[JSONCollection]("users")

  def findByEmailPassword(email : String, password : String) = {
    val futureUser = users.find(Json.obj("email" -> email, "password" -> User.md5pass(password), "suspended" -> false)).one[User]
    val results = Await.result(futureUser, 10 seconds)
    Logger.debug(s"Login results: ${results}")
    results

  }

  def create(user : User) = {
    val secureUser = user.copy(password = User.md5pass(user.password))
    users.insert(Json.toJson(secureUser)).map {
      lastError =>
        if (lastError.ok)
          Logger.debug(s"User ${user.email} saved.")
        else
          Logger.debug(s"Cannot save user: ${user.email}: ${lastError.err.getOrElse("No details")}")

    }
  }

  def findByCredentials(credentials: Credentials): Option[Identity] = {
    val futureUser = users.find(Json.obj("email" -> credentials.asInstanceOf[AppCredentials].email)).one[User]
    Await.result(futureUser, 10 seconds)
  }

}