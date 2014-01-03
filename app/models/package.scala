import play.api.libs.json.Json

/**
 * Created by terry on 1/2/14.
 *
 * Contains the formats for Play JSON Inception API
 */
package object models {

  implicit val UserFormats = Json.format[User]

}
