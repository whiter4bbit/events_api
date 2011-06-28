package info.whiter4bbit.events.unf.example

import info.whiter4bbit.events._
import unfiltered.request._
import unfiltered.response._
import unfiltered.oauth._
import com.mongodb.casbah.Imports._

trait UfOAuthCollections extends UserMongoCollection {
   val requests: MongoCollection
   val nonces: MongoCollection
   val sessions: MongoCollection
}

case class EConsumer(key: String, secret: String) extends Consumer

trait Sessions { self: UfOAuthCollections =>
   def store(key: String, user: OAuthUser) = {
      val session = MongoDBObject.newBuilder
      session += "key" -> key
      session += "userId" -> user.id
      sessions.insert(session.result)
   }
   def get(key: String): Option[OAuthUser] = {
      sessions.findOne(MongoDBObject("key" -> key)).flatMap((user) => {
         for {
	    userId <- user.getAs[String]("userId")
	 } yield {
	    OAuthUser(userId)
	 }
      })
   }
}

trait Consumers extends ConsumerStore { self: UserMongoCollection =>
   def get(consumerKey: String) = {
      users.findOne(MongoDBObject("consumerKey" -> consumerKey)).flatMap((user) => {
         for {
	   key <- user.getAs[String]("consumerKey");
	   secret <- user.getAs[String]("consumerSecret")
	 } yield {
	   EConsumer(key, secret)
	 }
      })
   }
}

trait Nonces extends NonceStore { self: UfOAuthCollections =>
   def put(consumer: String, timestamp: String, nonce: String) = {
      val o = MongoDBObject("consumer" -> consumer,
                            "timestamp" -> timestamp,
	   		    "nonce" -> nonce)
      nonces.findOne(o) match {
        case Some(_) => false
	case None => {
	  nonces.insert(o)
	  true
	}
      }
   }
}

trait Tokens extends DefaultTokenStore { self: UfOAuthCollections =>
   val AuthorizedType = "authorized"
   val AccessType = "access"
   val RequestType = "request"
   def put(token: Token) = {
      val obj = token match {      
         case RequestToken(key, secret, consumerKey, callback) => {
	    val b = MongoDBObject.newBuilder
	    b += "key" -> key
	    b += "secret" -> secret
	    b += "consumerKey" -> consumerKey
	    b += "callback" -> callback
	    b += "type" -> RequestType 
	    b
         }
         case AuthorizedRequestToken(key, secret, consumerKey, user, verifier) => {
	    val b = MongoDBObject.newBuilder
	    b += "key" -> key
	    b += "secret" -> secret
	    b += "consumerKey" -> consumerKey
	    b += "user" -> user
	    b += "verifier" -> verifier
	    b += "type" -> AuthorizedType
	    b
	 }
         case AccessToken(key, secret, user, consumerKey) =>  {
	    val b = MongoDBObject.newBuilder
	    b += "key" -> key
	    b += "secret" -> secret
	    b += "consumerKey" -> consumerKey
	    b += "user" -> user
	    b += "type" -> AccessType 
	    b
	 }
      }
      requests.insert(obj.result)
      token
   }
   def get(tokenId: String) = {
      val found = requests.find(MongoDBObject("key" -> tokenId))      
              .sort(MongoDBObject("_id" -> -1)).limit(1)
      found.toList.headOption.flatMap( request => 
         request.getAs[String]("type") match {
	    case Some(RequestType) => for {
	       key <- request.getAs[String]("key");
	       secret <- request.getAs[String]("secret");
	       consumerKey <- request.getAs[String]("consumerKey");
	       callback <- request.getAs[String]("callback")
	    } yield {
	       RequestToken(key, secret, consumerKey, callback)
	    }
	    case Some(AccessType) => for {
	       key <- request.getAs[String]("key");
	       secret <- request.getAs[String]("secret");
	       consumerKey <- request.getAs[String]("consumerKey");
	       user <- request.getAs[String]("user")
	    } yield {
	       AccessToken(key, secret, user, consumerKey)
	    }
	    case Some(AuthorizedType) => for {
	      key <- request.getAs[String]("key");
	      secret <- request.getAs[String]("secret");
	      consumerKey <- request.getAs[String]("consumerKey");
	      user <- request.getAs[String]("user");
	      verifier <- request.getAs[String]("verifier")
	    } yield {
	      AuthorizedRequestToken(key, secret, consumerKey, user, verifier)
	    }
	    case _ => None
	 }
      )
   }
   def delete(tokenId: String) {
      requests.remove(MongoDBObject("key" -> tokenId))
   }
}

trait Stores extends OAuthStores { 
  val nonces: Nonces
  val consumers: Consumers
  val tokens: Tokens
  val users: Host
}
