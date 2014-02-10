package controllers.support

import play.api.http.HeaderNames
import play.api.mvc._

import play.modules.hawk.IdentityService

import scala.concurrent.Future

/**
 * Created by terry on 1/2/14.
 *
 * This ActionBuilder will create actions that are secured via HTTP Basic Auth.
 *
 */
object BasicAuthSecured extends ActionBuilder[AuthorizedRequest] with HeaderNames with Results {
  protected def invokeBlock[A](request: Request[A], block: (AuthorizedRequest[A]) => Future[SimpleResult]): Future[SimpleResult] = {
    BasicAuthService.authorize(request.headers.get(AUTHORIZATION)).map {
      identity =>
        block(AuthorizedRequest(identity, request))
    } getOrElse (Future.successful(onUnauthorized))
  }

  /**
   * Redirect to login if the user in not authorized.
   */
  def onUnauthorized = Unauthorized.withHeaders(WWW_AUTHENTICATE -> "Basic realm=\"xymox.net\"")
}

object BasicAuthService {

  def authorize(maybeHeader : Option[String]) = {
    userCredentials(maybeHeader).flatMap {
      credentials =>
        IdentityService.findByEmailPassword(credentials.email, credentials.password).map(_.email)
    }
  }

  /**
   * Decode the basic auth header, if it exists.
   * @param auth
   * @return
   */
  private def decodeBasicAuth(auth: String) : Option[UserCredentials] = {
    auth.split(" ").drop(1).headOption.flatMap { encoded =>
      new String(org.apache.commons.codec.binary.Base64.decodeBase64(encoded.getBytes)).split(":").toList match {
        case u :: p :: Nil => Some(UserCredentials(u,p))
        case _ => None
      }
    }
  }

  /**
   * Function that handled looking up user credentials in the db. For now, we just return the username if we find a match.
   *
   * @param creds
   * @return
   */
  def userLookup(creds : UserCredentials)  = {
    IdentityService.findByEmailPassword(creds.email, creds.password)
  }

  /**
   *
   * @param header
   * @return
   */
  private def userCredentials(header: Option[String]) : Option[UserCredentials] = {
    header.map{ basicAuth =>
      decodeBasicAuth(basicAuth)
    }.getOrElse(None)
  }
}

case class UserCredentials(email : String, password : String)

case class AuthorizedRequest[A](email : String, request : Request[A]) extends WrappedRequest[A](request)