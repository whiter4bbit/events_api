package info.whiter4bbit.oauth.scalatra.example

import net.liftweb.json.scalaz.JsonScalaz._
import net.liftweb.json.Serialization.write
import net.liftweb.json._
import java.util.Date

trait JsonSupport {
  implicit val formats = DefaultFormats

  implicit def dateJSON(implicit formats: Formats): JSON[Date] = new JSON[Date] {
      def read(json: JValue) = json match {
         case v@JString(x) => formats.dateFormat.parse(x).map((date) => {
            date.success
         }).getOrElse(UnexpectedJSONError(v, classOf[JString]).fail.liftFailNel)
         case x => UnexpectedJSONError(x, classOf[JString]).fail.liftFailNel
      }
      def write(date: Date) = {
         JString(formats.dateFormat.format(date))
      }
  }
}
