package controllers

import play.api.Play
import play.api.Play.current
/**
 * Created with IntelliJ IDEA.
 * User: terry
 * Date: 5/18/13
 * Time: 12:17 AM
 *
 * Contains the base items to use the config class from Play.  Honestly, I don't know why they didn't do this...
 */
trait Config {
  val config = Play.configuration
}
