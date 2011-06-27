package info.whiter4bbit.events.unf.example 

import scalaz._
import Scalaz._
import unfiltered.request._
import unfiltered.response._

import net.liftweb.json._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.scalaz.JsonScalaz._
import net.liftweb.json.Serialization.write
import info.whiter4bbit.events._
import info.whiter4bbit.events.json._

trait PublicAPI extends unfiltered.filter.Plan with JsonSupport with Services { 
  object int {
      def unapply(s: String): Option[Int] = {
         try {
	   val i = Integer.parseInt(s)	   
           if (i > 0 && i < 100) Some(i) else None
	 } catch {
	   case _ => None
	 }
      }
  }
  def intent = {
      case r @ PUT(Path("/api/user/new")) => {
         (for {
	    body <- JsonBody(r);
	    login <- field[String]("login")(body).toOption;
	    password <- field[String]("password")(body).toOption;
	    created <- userService.create(login, password).toOption
	 } yield {
	    JsonContent ~> ResponseString(write(created))
	 }).getOrElse({
	    BadRequest
	 })
      }
      case GET(Path(Seg("api" :: "events" :: "latest" :: int(num) :: Nil))) => {         
         eventService.latest(num).map((events) => {
	    JsonContent ~> ResponseString(write(events))
	 }) ||| ((e: String) => {	 
	    BadRequest ~> ResponseString(e)
	 })
      }
      case GET(Path(Seg("api" :: "users" :: "public" :: id :: Nil))) => {
         userService.findPublic(id).map((user) => {
	    JsonContent ~> ResponseString(write(user))
	 }) ||| ((e: String) => {
	    BadRequest ~> ResponseString(e)
	 })
      }
      case GET(Path(Seg("api" :: "events" :: "attendees" :: id :: Nil))) => {
         eventService.attendees(id).map((attendees) => {
	    JsonContent ~> ResponseString(write(attendees))	    
	 }) ||| ((e: String) => {
	    BadRequest ~> ResponseString(e)
	 })
      }
  }
}

object Jetty extends App {
   unfiltered.jetty.Http.local(8080).filter(new Object with PublicAPI with ServicesImpl).run   
}

