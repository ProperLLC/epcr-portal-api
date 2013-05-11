package models

import play.api.libs.json.Json

/**
 * User class used during user session requests.  The entity is ultimately serialized from the db.
 *
 * User: terry
 * Date: 5/10/13
 * Time: 11:03 PM
 */
object User {
   implicit val UserFormats = Json.format[User]
}

case class User(username : String, organizationCode : String, roles : Seq[String]) {
  def isAdmin() : Boolean = {
    roles.contains("ROLE_ADMIN")
  }
}
