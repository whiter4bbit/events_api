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
import com.mongodb.casbah.Imports._

trait EventsApp extends unfiltered.filter.Plan with JsonSupport with Services { self: Sessions =>

    import QParams._

    def intent = {
      case r @ PUT(Path("/autenticate") & Params(params)) => 
        val expected = for {
	   token <- lookup("token") is required("token is required") is
	     nonempty("token cannot be blank");	   
	   login <- lookup("login") is required("login is required") is	     
	     nonempty("login cannot be blank");	   
	   password <- lookup("password") is required("password is required") is
	     nonempty("password cannot be blank")
	} yield {	   
	   userService.find(login.get, password.get).map((user) => {
	      val key = java.util.UUID.randomUUID().toString()
	      store(key, OAuthUser(user.id)) 
	      ResponseCookies(Cookie("sessionId", key)) ~> Redirect("/oauth/authorize?oauth_token=%s" format token.get)
	   }) | BadRequest ~> ResponseString("Invalid user")
	}
	expected(params) orFail ( fail => 
	   BadRequest ~> ResponseString(fail.map(_.error) mkString(".")))
    }
}
