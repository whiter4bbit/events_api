package info.whiter4bbit.events.unf.example

import info.whiter4bbit.events._
import unfiltered.oauth._
import unfiltered.request._
import unfiltered.response._
import unfiltered._
import com.mongodb.casbah.Imports._

case class OAuthUser(val id: String) extends UserLike

trait Host extends unfiltered.oauth.UserHost { self: Sessions =>
   def current[T](r: HttpRequest[T]): Option[UserLike] = r match { 
      case Cookies(cookies) => cookies("sessionId") match {
         case Some(Cookie(_, sessionId, _, _, _, _)) => get(sessionId)
	 case _ => None
      }
      case _ => None
   }
   def accepted[T](token: String, r: HttpRequest[T]) = true
   def denied[T](token: String, r: HttpRequest[T]) = true
   def requestAcceptance(token: String, consumer: Consumer) = Html(
      <html>
         <form action="/oauth/authorize" method="POST">
	    <input type="hidden" name="token" value={token}/>
	    <input type="submit" name="submit" value="Allow"/>
	 </form>
      </html>
   )
   def deniedConfirmation(consumer: Consumer) = Html(
      <html>
         Access was denied
      </html>
   )
   def oobResponse(verifier: String) = Html(
      <p>{verifier}</p>
   )
   def login(token: String) = Html(
      <html>
        <form action="/authenticate" method="POST">
	   <input type="hidden" name="token" value={token}/>
	   Login:<input type="text" name="login"/><br/>
	   Password:<input type="password" name="password"/><br/>
	   <input type="submit" name="submit" value="login"/>
	</form>
      </html>
   )
}   
