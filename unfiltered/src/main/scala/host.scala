package info.whiter4bbit.events.unf.example

import info.whiter4bbit.events._
import unfiltered.oauth._
import unfiltered.request._
import unfiltered.response._
import unfiltered._
import com.mongodb.casbah.Imports._

case class OAuthUser(val id: String) extends UserLike

trait Host extends unfiltered.oauth.UserHost { self: Sessions =>
   def current[T](r: HttpRequest[T]): Option[UserLike] = {
      /*None*/ Some(OAuthUser("1"))
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
        <form action="/oauthenticate" method="POST">
	   <input type="hidden" name="token" value={token}/>
	   Login:<input type="text" name="login"/>
	   Password:<input type="password" name="password"/>
	   <input type="submit" name="submit" value="login"/>
	</form>
      </html>
   )
}   
