package info.whiter4bbit.events.unf.client

import dispatch._
import dispatch.oauth._
import OAuth._
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

object UfClient {
   case class UfUser(login: String, consumerKey: String, consumerSecret: String)
   implicit def formats = DefaultFormats

   val testConsumer: Consumer = Consumer("DqUaVkaQdGV0JP0cEX8IFwEl1bf6YB7c","kueDTB0EGGNGBW2x5vDK1cmkYJrcNVXf")

   val host = :/("localhost", 8080)
   val h = new Http

   implicit def userToConsumer(user: UfUser) = Consumer(user.consumerKey, user.consumerSecret)
   
   def createUser(login: String, password: String) = {
      val u = Map("login" -> login, "password" -> password)
      parse(h(host / "public" / "user" / "new" <<< compact(render(u)) as_str)).extract[UfUser]
   }

   def getAuthURL(consumer: Consumer) = {
      val token = h(host.POST / "oauth" / "request_token" <@ (consumer, oob) as_token)      
      token match {
         case t @ Token(key, _) => Some((t, "http://localhost:8080/oauth/authorize?oauth_token=%s" format key))
	 case _ => None
      }
   }

   def getUserInfo(consumer: Consumer, verifier: String, token: Token) = {
       val access_token = h(host.POST / "oauth" / "access_token" <@ (consumer, token, verifier) as_token)
       println("got access token:")
       h(host / "protected" / "user" / "info" <@ (consumer, access_token, verifier) as_str)
   }

}
