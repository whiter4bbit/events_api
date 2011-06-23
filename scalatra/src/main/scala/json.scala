package info.whiter4bbit.oauth.scalatra.example

import scalaz._
import Scalaz._

import net.liftweb.json.scalaz.JsonScalaz._
import net.liftweb.json.Serialization.write
import net.liftweb.json._
import net.liftweb.json.ext._
import java.util.Date
import org.joda.time.DateTime

trait JsonSupport {
  implicit val formats = DefaultFormats ++ JodaTimeSerializers.all

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

  implicit def dateTimeJSON(implicit formats: Formats): JSON[DateTime] = new JSON[DateTime] {
      def read(json: JValue) = json match {
         case v @ JString(x) => formats.dateFormat.parse(x).map((date) => {
	   new DateTime(date.getTime).success
	 }).getOrElse(UnexpectedJSONError(v, classOf[JString]).fail.liftFailNel)
	 case x => UnexpectedJSONError(x, classOf[JString]).fail.liftFailNel
      }
      def write(date: DateTime) = {
         JString(formats.dateFormat.format(date.toDate))
      }
  }

}
