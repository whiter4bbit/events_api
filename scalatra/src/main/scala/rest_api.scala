package info.whiter4bbit.oauth.scalatra.example 

import scalaz._
import Scalaz._

import org.slf4j.LoggerFactory
import org.scalatra._
import java.net.URL
import info.whiter4bbit.oauth.scalatra._
import info.whiter4bbit.oauth._
import mongodb._

import net.liftweb.json.scalaz.JsonScalaz._
import net.liftweb.json.Serialization.write
import net.liftweb.json._
import java.util.Date
import org.joda.time.DateTime

trait EventsAPI extends ScalatraFilter with OAuthProviderFilter with Scalatraz with JsonSupport { 
  this: Services => 

  val logger = LoggerFactory.getLogger(getClass) 

  def storage: OAuthMongoStorage = new java.lang.Object with OAuthMongoStorage with MongoDBCollections  

  postz("/api/user/new") {  
     for {
         raw <- paramz("user");	 
         json <- parse(raw).success;
	 login <- field[String]("login")(json);
	 password <- field[String]("password")(json); 
	 created <- userService.create(login, password)
     } yield {
         write(created)
     }
  } 

  protectedGet("/api/user/info") {
     userService.find(oauthRequest.consumer.consumerKey).map((user) => {
        write(user)
     })
  }   

  protectedPost("/api/events/add") {  
     for {
        raw <- paramz("event");
	proto <- Function.uncurried(Event.curried(None)).success;
	id <- userService.find(oauthRequest.consumer.consumerKey).map(_.id);
	event <- proto.applyJSON(field("name"), field("description"), field("startDate"), field("endDate"))(parse(raw));
	inserted <- eventService.add(event, id)	
     } yield {
        write(inserted)
     }
  }

  protectedPost("/api/events/attend") {
     for {
        raw <- paramz("event");
	eventId <- field[String]("id")(parse(raw));
	userId <- userService.find(oauthRequest.consumer.consumerKey).map(_.id);
	attended <- eventService.attend(eventId, userId)
     } yield {
        write(attended)
     }
  }

  protectedGet("/api/events/mine") {  
     for {
        user <- userService.find(oauthRequest.consumer.consumerKey);
        events <- eventService.findByUser(user.id)
     } yield {
        write(events)
     }
  }

  def int(n: String): Validation[String, Int] = {
     try {
        Integer.parseInt(n).success
     } catch {
        case _ => ("%s is not a number" format n).fail
     }
  }

  getz("/api/events/latest/:num") {
     for {     
        num <- paramz("num") flatMap int;       
	events <- eventService.latest(num)
     } yield {
        write(events)
     }
  }
 
}

class EventsAPIImpl extends EventsAPI with ServicesImpl 
