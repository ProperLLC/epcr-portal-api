/**
 * Created with IntelliJ IDEA.
 * User: terry
 * Date: 5/18/13
 * Time: 12:07 AM
 * To change this template use File | Settings | File Templates.
 */
import play.api.mvc.Results._
import play.api.mvc.SimpleResult
import controllers.CORS._

new play.core.StaticApplication(new java.io.File("."))





val corsOk = Ok("").withCors



