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
  def intent = {
      case r @ PUT(Path("/api/user/new")) => {
         (for {
	    body <- JsonBody(r);
	    login <- field[String]("login")(body).toOption;
	    password <- field[String]("password")(body).toOption;
	    created <- userService.create(login, password).toOption
	 } yield {
	    println("user created")
	    JsonContent ~> ResponseString(write(created))
	 }).getOrElse({
	     println("bad request")
	     BadRequest
	 })
      }
  }
}

object Jetty extends App {
   unfiltered.jetty.Http.local(8080).filter(new Object with PublicAPI with ServicesImpl).run   
}

