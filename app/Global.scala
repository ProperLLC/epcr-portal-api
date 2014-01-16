
import play.api.{Play, GlobalSettings}
import play.api.Play.current
import play.api.mvc.{SimpleResult, RequestHeader, Filter, WithFilters}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by terry on 12/15/13.
 */

object Global extends WithFilters(Cors) with GlobalSettings

object Cors extends Filter {

  lazy val config = Play.configuration
  lazy private val allowedOrigins = config.getString("auth.cors.host").getOrElse("http://localhost:8000")

  def apply(f: (RequestHeader) => Future[SimpleResult])(rh: RequestHeader): Future[SimpleResult] = {
    val result = f(rh)
    val origin = rh.headers.get("Origin")
    val defaultAllowed = "http://localhost:8000"
    val hostsAllowed = allowedOrigins.split(", ").toList
    val allowedOrigin = if (origin.isDefined && hostsAllowed.contains(origin.get)) origin.get else defaultAllowed
    // NOTE - the header Access-Control-Allow-Origin won't allow a list of origins - it must be one and only one, so we had to do some magic above...
    result.map(_.withHeaders("Access-Control-Allow-Origin" -> allowedOrigin, "Access-Control-Expose-Headers" -> "WWW-Authenticate, Server-Authorization"))
  }

}

