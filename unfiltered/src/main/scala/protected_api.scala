package info.whiter4bbit.events.unf.example 

import scalaz._
import Scalaz._
import unfiltered.Cookie
import unfiltered.request._
import unfiltered.response._
import unfiltered.oauth.OAuth.XAuthorizedIdentity

import net.liftweb.json._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.scalaz.JsonScalaz._
import net.liftweb.json.Serialization.write
import info.whiter4bbit.events._
import info.whiter4bbit.events.json._

trait ProtectedAPI extends unfiltered.filter.Plan with JsonSupport with Services { 

  def obligatory[A](test: Option[A], msg: String): Validation[String, A] = test.map(_.success).getOrElse(msg.fail)

  def intent = {
     case GET(Path("/protected/user/info")) & request => {
        val user = request.underlying.getAttribute(XAuthorizedIdentity).asInstanceOf[String]
	userService.findById(user).map((user) => {
	   JsonContent ~> ResponseString(write(user))
	}) ||| (fail => BadRequest ~> ResponseString(fail.toString))
     }
     case PUT(Path("/protected/events/attend")) & request => {
        val userId = request.underlying.getAttribute(XAuthorizedIdentity).asInstanceOf[String]
	(for {
	  body <- obligatory(JsonBody(request), "can't parse json body");
	  eventId <- field[String]("id")(body);
	  attended <- eventService.attend(eventId, userId)
	} yield {
	   JsonContent ~> ResponseString(write(attended))
	}) ||| (fail => BadRequest ~> ResponseString(fail.toString))
     }    
     case PUT(Path("/protected/events/add")) & request => {
        val userId = request.underlying.getAttribute(XAuthorizedIdentity).asInstanceOf[String]
	val proto = Function.uncurried(Event.curried(None));	
	(for {
	   body <- obligatory(JsonBody(request), "can't parse json body");
	   event <- proto.applyJSON(field("name"), field("description"), field("startDate"), field("endDate"))(body);
	   inserted <- eventService.add(event, userId)	
        } yield {
           JsonContent ~> ResponseString(write(inserted))
        }) ||| (fail => BadRequest ~> ResponseString(fail.toString))
     }
     case GET(Path("/protected/events/mine")) & request => {
        val userId = request.underlying.getAttribute(XAuthorizedIdentity).asInstanceOf[String]
	eventService.findByUser(userId).map((events) => {
	   JsonContent ~> ResponseString(write(events))
	}) ||| (fail => BadRequest ~> ResponseString(fail.toString))
     }
  }

}

