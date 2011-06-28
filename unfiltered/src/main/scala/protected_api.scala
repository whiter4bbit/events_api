package info.whiter4bbit.events.unf.example 

import scalaz._
import Scalaz._
import unfiltered.Cookie
import unfiltered.request._
import unfiltered.response._

import net.liftweb.json._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.scalaz.JsonScalaz._
import net.liftweb.json.Serialization.write
import info.whiter4bbit.events._
import info.whiter4bbit.events.json._

trait ProtectedAPI extends unfiltered.filter.Plan with JsonSupport with Services { 
  val host: unfiltered.oauth.UserHost

  def intent = {
     case GET(Path("/protected/secret")) & request => host.current(request) match {
       case Some(user) => ResponseString("some secret for " + user)
       case _ => BadRequest 
     }     
  }

}

