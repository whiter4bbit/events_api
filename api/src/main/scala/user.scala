package info.whiter4bbit.events

import com.mongodb.casbah.Imports._
import info.whiter4bbit.oauth.TokenGenerator
import org.slf4j.LoggerFactory
import scala.util.parsing.json._
import scalaz._
import Scalaz._

trait UserMongoCollection {
   val users: MongoCollection
}

case class PublicUser(val id: String, val login: String)

case class User(val id: String, val login: String, val password: String, val consumerKey: String, val consumerSecret: String)

trait UserService extends TokenGenerator { self: UserMongoCollection with TokenGenerator => 
   val logger = LoggerFactory.getLogger(getClass)

   def generateKey = self.generateToken(32)

   def create(login: String, password: String): Validation[String, User] = {
       if (self.users.find(MongoDBObject("login" -> login)).size == 0) {       
          val consumerKey = generateKey
	  val consumerSecret = generateKey
	  val obj = MongoDBObject("login" -> login, "password" -> password, "consumerKey" -> consumerKey, "consumerSecret" -> consumerSecret) 
          self.users.insert(obj)
          self.users.last.getAs[ObjectId]("_id").map((id) => {
	     logger.info("User %s has been created" format login) 
	     User(id.toString, login, password, consumerKey, consumerSecret).success
	  }).getOrElse("Can't resolve last object id".fail)
       } else {
          logger.info("User %s already exists" format login)
	  "User %s already exists".format(login).fail
       }
   }

   def find(consumerKey: String): Validation[String, User] = {
       (for {
          found <- self.users.findOne(MongoDBObject("consumerKey" -> consumerKey));
	  id <- found.getAs[ObjectId]("_id");
	  login <- found.getAs[String]("login");
	  password <- found.getAs[String]("password");
	  conumserKey <- found.getAs[String]("consumerKey");
	  consumerSecret <- found.getAs[String]("consumerSecret")
       } yield {
          User(id.toString, login, password, consumerKey, consumerSecret).success
       }).getOrElse({
          println("Consumer with key %s not found" format consumerKey)
          "Consumer with key %s not found".fail
       }) 
   }

   def objectId(eventId: String): Option[ObjectId] = {
      try {
         Some(new ObjectId(eventId))
      } catch {
         case _ => None
      }
   }

   def findPublic(userId: String): Validation[String, PublicUser] = {
       (for {
         id <- objectId(userId);
	 user <- self.users.findOne(MongoDBObject("_id" -> id));
	 login <- user.getAs[String]("login")
       } yield {
         PublicUser(userId, login).success
       }).getOrElse(("Can't find user with id %s" format id).fail)
   }

   def find(login: String, password: String): Validation[String, User] = {
       (for {
          found <- self.users.findOne(MongoDBObject("login" -> login, "password" -> password));
	  id <- found.getAs[ObjectId]("_id");
	  login <- found.getAs[String]("login");
	  password <- found.getAs[String]("password");
	  consumerKey <- found.getAs[String]("consumerKey");
	  consumerSecret <- found.getAs[String]("consumerSecret")
       } yield {
          User(id.toString, login, password, consumerKey, consumerSecret).success
       }).getOrElse("User not found".fail)
   }

   def findById(id: String): Validation[String, User] = {
       (for {
          id <- objectId(id); 
          found <- self.users.findOne(MongoDBObject("_id" -> id));
	  login <- found.getAs[String]("login");
	  password <- found.getAs[String]("password");
	  consumerKey <- found.getAs[String]("consumerKey");
	  consumerSecret <- found.getAs[String]("consumerSecret")
       } yield {
          User(id.toString, login, password, consumerKey, consumerSecret).success
       }).getOrElse("User not found".fail)
   }
}

