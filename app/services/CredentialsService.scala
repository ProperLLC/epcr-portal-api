package services

import play.api.{Logger, Application}
import play.api.libs.json.Json

import play.modules.hawk.{Credentials, CredentialsServicePlugin}
import play.modules.reactivemongo.json.collection.JSONCollection

import org.joda.time.DateTime

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random._
import scala.util.Try

import models._

/**
 * Service that will create/find/remove credentials used by the Hawk subsystem to authorize access to API endpoints.
 *
 * Created by terry on 1/2/14.
 */
class CredentialsService(application : Application) extends CredentialsServicePlugin(application) with MongoSupport {
  private def credentials = db.collection[JSONCollection]("credentials")

  def find(identifier : String) = {
    val futureCredentials = credentials.find(Json.obj("identifier" -> identifier)).one[AppCredentials]
    Await.result(futureCredentials, 10 seconds)
  }

}

private object KeyGen {

  def createClientId = genSecureRandomString(17)

  def createKey = genSecureRandomString(59)

  private def genSecureRandomString(length : Int) = {
    new java.security.SecureRandom().alphanumeric.take(length).mkString
  }
}

/**
 * Class that contains operations specific to this implementation of credentials.  The Hawk provided CredentialsService handles just the details of finding
 * the credentials in the database.  It has nothing to do with creating them, expiring them, etc.  Those details are left up to the application developer, and
 * as such are found in this class.
 */
object AppCredentialsService extends MongoSupport {

  private def credentials = db.collection[JSONCollection]("credentials")

  def createCredentials(email : String) = {
    val creds = AppCredentials(identifier = KeyGen.createClientId, key = KeyGen.createKey, algorithm="SHA256", email = email, created = DateTime.now())
    credentials.insert(Json.toJson(creds)).map {
      lastError =>
        if (lastError.ok) {
          Logger.debug("Credentials inserted successfully.")
          Some(creds)
        } else {
          Logger.debug(s"Error inserting credentials: ${lastError.err.getOrElse("No details.")}")
          None
        }
    }
  }

  def expire(clientId : String) = {
    credentials.remove(Json.obj("identifier" -> clientId)).map {
      lastError =>
        Try (
          if (lastError.ok) {
            Logger.debug(s"Credentials expired successfully for clientId ${clientId}")
            lastError.code.getOrElse(0)
          } else {
            Logger.debug(s"Error expiring credentials for clientId: ${clientId} : ${lastError.err.getOrElse("No details.")}")
            throw new Exception(s"Could not expire credentials for clientId ${clientId} : ${lastError.err.getOrElse("No details.")} ")
          }
        )
    }
  }

}

case class AppCredentials(identifier : String, key : String, algorithm : String, created : DateTime, email : String) extends Credentials