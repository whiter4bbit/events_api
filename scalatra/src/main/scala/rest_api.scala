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
import info.whiter4bbit.events._
import info.whiter4bbit.events.json._

trait EventsAPI extends ScalatraFilter with OAuthProviderFilter with Scalatraz with JsonSupport { 
  this: Services => 

  val logger = LoggerFactory.getLogger(getClass) 

  def storage: OAuthMongoStorage = new java.lang.Object with OAuthMongoStorage with MongoDBCollections  

  putz("/api/user/new") {
     for {
         json <- parse(request.body).success;
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

  protectedPut("/api/events/add") {  
     for {
	proto <- Function.uncurried(Event.curried(None)).success;	
	id <- userService.find(oauthRequest.consumer.consumerKey).map(_.id);
	json <- parse(request.body).success;
	event <- proto.applyJSON(field("name"), field("description"), field("startDate"), field("endDate"))(json);
	inserted <- eventService.add(event, id)	
     } yield {
        write(inserted)
     }
  }

  protectedPut("/api/events/attend") {
     for {
	eventId <- field[String]("id")(parse(request.body));
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

  getz("/api/events/attendees/:id") {
     for {
       id <- paramz("id");
       attendees <- eventService.attendees(id)
     } yield {
       write(attendees)
     }
  }

  getz("/api/users/public/:id") {
      for {
         id <- paramz("id");
	 user <- userService.findPublic(id)
      } yield {
         write(user)
      }
  }
}

class EventsAPIImpl extends EventsAPI with ServicesImpl 
