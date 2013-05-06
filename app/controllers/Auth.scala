package controllers

import play.api.mvc._
import play.api.Logger

/**
 * Created with IntelliJ IDEA.
 * User: terry
 * Date: 5/5/13
 * Time: 6:39 PM
 * To change this template use File | Settings | File Templates.
 */
object Auth extends Controller {

  def login(username : String) = Action(parse.json) { request =>
    // pull payload
    // send to auth service for validation
    // return results
    Unauthorized("Not Yet Implemented")
  }

  def logout(username : String) = Action {
    Ok("Not Yet Implemented")
  }

  def headers = Action { request =>
    Logger.info(s"Remote Address: ${request.remoteAddress}")
    Ok(request.headers.toSimpleMap.toString).as(JSON)
  }
}
