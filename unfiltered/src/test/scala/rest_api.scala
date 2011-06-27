package info.whiter4bbit.events.unf.example

import org.specs._
import dispatch._
import info.whiter4bbit.events._
import info.whiter4bbit.events.json._
import com.mongodb.casbah.Imports._
import net.liftweb.json._
import net.liftweb.json.Serialization.write
import net.liftweb.json.JsonDSL._
import org.apache.http.{HttpResponse}

trait TestDBCollections extends UserMongoCollection with EventMongoCollection {
  val users = MongoConnection()("test_oauth")("users")
  val events = MongoConnection()("test_oauth")("events")
}

trait MockServices extends Services {
  val userService = new Object with UserService with TestDBCollections   
  val eventService = new Object with EventService with TestDBCollections
}

class PublicAPISpecs extends Specification with unfiltered.spec.jetty.Served with JsonSupport {
  val collections = new Object with TestDBCollections
  def setup = _.filter(new Object with PublicAPI with MockServices)    
  
  type PutResponse = ((Int, org.apache.http.HttpResponse, Option[org.apache.http.HttpEntity], () => String) => Any)

  val h = new Http

  "public events api" should {
     doBefore {
        collections.events.drop
	collections.users.drop
     }     
     def addUser(login: String, password: String, f: PutResponse) = {
        val credentials = Map("login" -> login, "password" -> password)
        h(host / "api" / "user" / "new" <<< compact(render(credentials)) as_str f) 
     }
     "create user with valid data" in {
        addUser("pasha", "123", { case (code, _, _, _) => code must_==200 })
     }
     "don't allow to create user with login, that already exists" in {
        addUser("pasha", "123", { case (code, _, _, _) => code must_==200 })
	try {
	   addUser("pasha", "123", { case (code, _, _, _) => code must_==400 })
	} catch {
	   case StatusCode(code, _) => code must_==400
	}
     }
  }
}
