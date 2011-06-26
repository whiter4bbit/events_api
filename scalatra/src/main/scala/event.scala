package info.whiter4bbit.oauth.scalatra.example

import com.mongodb.casbah.Imports._
import org.slf4j.LoggerFactory
import java.util.Date
import org.joda.time.DateTime
import scalaz._
import Scalaz._

trait EventMongoCollection {
   val events: MongoCollection
}

case class Event(val id: Option[String], val name: String, val description: String, val startDate: DateTime, val endDate: DateTime)

trait EventService { self: EventMongoCollection => 
   val logger = LoggerFactory.getLogger(getClass)
   def add(event: Event, id: String): Validation[String, Event] = {
      if (self.events.find(MongoDBObject("name" -> event.name)).size == 0) {
          val obj = MongoDBObject(
	    "name" -> event.name, 
	    "description" -> event.description, 
	    "startDate" -> event.startDate.toDate, 
	    "userId" -> id, 
	    "endDate" -> event.endDate.toDate)
          self.events.insert(obj)
          self.events.last.getAs[ObjectId]("_id").map((id) => {
	       Event(Some(id.toString), event.name, event.description, event.startDate, event.endDate).success
	  }).getOrElse("Can't resolve last object id".fail)
      } else {
          ("There are exists event with name %s" format event.name).fail
      }
   }

   def findByUser(id: String): Validation[String, List[Event]] = {
      self.events.find(MongoDBObject("userId" -> id)).map((event) => {
         for {
	    id <- event.getAs[ObjectId]("_id");
	    name <- event.getAs[String]("name");
	    description <- event.getAs[String]("description");
	    startDate <- event.getAs[java.util.Date]("startDate");
	    endDate <- event.getAs[java.util.Date]("endDate")
	 } yield {
	    Event(Some(id.toString), name, description, new DateTime(startDate.getTime), new DateTime(endDate.getTime))
	 }
      }).toList.filter(_.isDefined).map(_.get).success
   }

   def objectId(eventId: String): Option[ObjectId] = {
      try {
         Some(new ObjectId(eventId))
      } catch {
         case _ => None
      }
   }

   def attend(eventId: String, userId: String): Validation[String, String] = {   
      (for {
         id <- objectId(eventId);
	 modified <- self.events.findAndModify(MongoDBObject("_id" -> id), 
	                                       MongoDBObject("$addToSet" -> MongoDBObject("attendees" -> userId)))
      } yield {
         eventId.success
      }).getOrElse("Error while updating event".fail)
   }

   def latest(limit: Int): Validation[String, List[Event]] = {
      self.events.find("startDate" $gte new Date()).limit(limit).map( (obj) => {
         for {
	    id <- obj.getAs[ObjectId]("_id");
	    name <- obj.getAs[String]("name");
	    description <- obj.getAs[String]("description");
	    startDate <- obj.getAs[Date]("startDate").map(new DateTime(_));
	    endDate <- obj.getAs[Date]("endDate").map(new DateTime(_))
	 } yield {
	    Event(Some(id.toString), name, description, startDate, endDate)
	 }
      }).toList.filter(_.isDefined).map(_.get).success
   }

   def attendees(eventId: String): Validation[String, List[String]] = {
      (for {
         id <- objectId(eventId);
	 event <- self.events.findOne(MongoDBObject("_id" -> id))	 
      } yield {
         event.getAs[BasicDBList]("attendees").map(_.toList.map(_.toString)).getOrElse(List()).success	 
      }).getOrElse(("Can't get list of attendees for event %s" format eventId).fail)     
   }
}
