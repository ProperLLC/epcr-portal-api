package models

import play.api.libs.json.Json
import java.security.MessageDigest
import play.modules.hawk.Identity

/**
 * User class used during user session requests.  The entity is ultimately serialized from the db.
 *
 * User: terry
 * Date: 5/10/13
 * Time: 11:03 PM
 */
object User {
  val digest = MessageDigest.getInstance("MD5")

  def md5pass(password : String) = {
    digest.digest(password.getBytes).map("%02x".format(_)).mkString
  }

}

case class User(email : String, password: String, firstName : String, lastName : String, suspended : Boolean = false, organizationCode : String, roles : Seq[String]) extends Identity {
  def isAdmin() : Boolean = {
    roles.contains("ROLE_ADMIN")
  }
}
