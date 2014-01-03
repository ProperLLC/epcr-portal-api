package controllers.support

import play.modules.hawk.Identity
import models.User

/**
 * Created by terry on 1/2/14.
 *
 * Trait for Controllers that need authorization support.  Contains various helper methods.
 *
 */
trait Authz {

   def hasEntityCreatorRole(identity : Identity, entityName : String) = identity match {
    case u:User => u.roles.contains(s"ROLE_${entityName.toUpperCase}_CREATOR")
    case _ => false
  }

   def isAdmin(identity : Identity) = identity match {
    case u:User => u.isAdmin()
    case _ => false
  }
}
