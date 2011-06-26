package info.whiter4bbit.oauth.scalatra.example

import org.specs._
import org.specs.matcher._
import org.scalatra.test.specs._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Serialization.{write => jwrite}
import info.whiter4bbit.chttp.oauth.Token
import org.joda.time.DateTime

class RestAPISpec extends EventsAPISpec with JsonSupport {

   addFilter(classOf[MockEventsAPIFilter], "/*") 

   case class beInsertedEvent(event: Event) extends Matcher[Any] {
      def apply(other: => Any) = {	   
        (other match {
          case Event(Some(id), event.name, event.description, event.startDate, event.endDate) => true
          case _ => false
        }, "Object matches inserted event", "Object don't matches inserted event")
      }
   }
 
   "events rest public api" should {
      doBefore {
         cleanCollections
      }
      "create user if all parameters is correct" in {         
         val json = compact(render(Map("login" -> "pasha", 
	                               "password" -> "12345")))				       
         post("/api/user/new", params = ("user", json)) {
	    status must ==(200)
	    val user = parse(body).extract[User]
	    user.login must ==("pasha")
	    user.password must ==("12345")	    
	    user.consumerKey must notBeNull
	    user.consumerSecret must notBeNull
	 }
      }
      "do not create user with login, that already exists" in {
         val json = compact(render(Map("login" -> "pasha", 
	                               "password" -> "54321")))				       
         post("/api/user/new", params = ("user", json)) {} 				       
         post("/api/user/new", params = ("user", json)) {
	    status must ==(400)
	 } 
      }
   }

   "events rest protected api" should {   
      var user: User = null
      doFirst {
         cleanCollections	          
	 val json = compact(render(Map("login" -> "pasha", 	 
	                               "password" -> "54321")))				       
         post("/api/user/new", params = ("user", json)) {
	    status must ==(200)
	    user = parse(body).extract[User]
	 }
      }
      def addEvent(event: Event, token: Token)(f: => Any) = {
         val json = jwrite(event)
       	 oauthPost("/api/events/add", user, Some(token), params = List(("event", json)))(f)
      }
      "present user information" in {
         val token = getOAuthToken(user)
	 val loaded = oauthGet("/api/user/info", user, Some(token)) {
	    status must ==(200)
	    parse(body).extract[User]
	 }
	 loaded must ==(User(user.id, user.login, user.password, user.consumerKey, user.consumerSecret))
      }
      "add event" in {
         val token = getOAuthToken(user)	 
	 val start = new DateTime().withYear(2011).withMonthOfYear(12).withDayOfMonth(12).withTime(12, 0, 0, 0)
	 val end = new DateTime().withYear(2011).withMonthOfYear(12).withDayOfMonth(12).withTime(14, 0, 0, 0)	 
	 val event = Event(None, "event1", "event description", start, end)
	 val stored = addEvent(event, token) {
	    status must ==(200)
	    parse(body).extract[Event] 
	 }
	 stored must beInsertedEvent(event)
      }
      "do not allow to add event with same name twice" in {      
         val token = getOAuthToken(user) 	 
	 val start = new DateTime().withYear(2011).withMonthOfYear(12).withDayOfMonth(12).withTime(12, 0, 0, 0)
	 val end = new DateTime().withYear(2011).withMonthOfYear(12).withDayOfMonth(12).withTime(14, 0, 0, 0)	 
	 val event = Event(None, "event787878", "event description", start, end)
	 addEvent(event, token) { status must ==(200) } 	 
	 addEvent(event, token) { status must ==(400) }
      }
      "show events created by user" in {
         cleanEvents
         val token = getOAuthToken(user)
	 val start = new DateTime().withYear(2011).withMonthOfYear(12).withDayOfMonth(12).withTime(12, 0, 0, 0)
	 val end = new DateTime().withYear(2011).withMonthOfYear(12).withDayOfMonth(12).withTime(14, 0, 0, 0)	 
	 val event1 = Event(None, "The Prodigy Fans meetup", "Meet other The Prodigy fans", start, end)
	 val event2 = Event(None, "Sushi master class", "Chief cook of \"Chinese restaurant\" gives master class", start, end)
	 addEvent(event1, token) { status must ==(200) }	 
	 addEvent(event2, token) { status must ==(200) }
	 val events = oauthGet("/api/events/mine", user, Some(token)) { 
	   status must ==(200)
	   parse(body).extract[List[Event]] 
	 }	 
	 events must haveSize(2)
	 events(0) must beInsertedEvent(event1)
	 events(1) must beInsertedEvent(event2)
      }
      "attend current user to event" in {
         cleanEvents
         val token = getOAuthToken(user)
	 val start = new DateTime().withYear(2011).withMonthOfYear(12).withDayOfMonth(12).withTime(12, 0, 0, 0)	
         val end = new DateTime().withYear(2011).withMonthOfYear(12).withDayOfMonth(12).withTime(18, 0, 0, 0)	
	 val event = Event(None, "Birthday party", "%s's birthday party" format System.getProperty("user.name"), start, end)
	 val added = addEvent(event, token) { 
	   status must ==(200) 
	   parse(body).extract[Event]
	 }
	 val eventId = added.asInstanceOf[Event].id.get
	 val json = compact(render(Map("id" -> eventId)))
	 oauthPost("/api/events/attend", user, Some(token), params = List(("event" -> json))) {
	    status must ==(200)
	 } 
	 oauthGet("/api/events/attendees/" + eventId, user, None) {
	    status must ==(200)	    
	    parse(body).extract[List[String]] must contain(user.id)
	 }
      }
      "find public user info by existing id" in {
         var public: PublicUser = null
         get("/api/users/public/" + user.id) {
	    status must ==(200)
	    public = parse(body).extract[PublicUser]
	 }
	 public.login must ==(user.login)
	 public.id must ==(user.id)
      }
      "return 400 for query user id when id is not correct" in {
         get("/api/users/public/123") {
	    status must ==(400)
	 }
      }
   }
}
