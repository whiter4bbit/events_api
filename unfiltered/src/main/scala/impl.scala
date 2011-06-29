package info.whiter4bbit.events.unf.example

import com.mongodb.casbah.Imports._
import info.whiter4bbit.events.{UserMongoCollection, EventMongoCollection, Services, UserService, EventService}

trait UfCollections extends UfOAuthCollections with UserMongoCollection with EventMongoCollection {
   val users = MongoConnection()("oauth_unfiltered")("users")
   val sessions = MongoConnection()("oauth_unfiltered")("sessions")
   val requests = MongoConnection()("oauth_unfiltered")("requests")
   val nonces = MongoConnection()("oauth_unfiltered")("nonces")
   val events = MongoConnection()("oauth_unfiltered")("events")
}

case object UfStores extends Stores {
   val nonces = new Object with Nonces with UfCollections
   val consumers = new Object with Consumers with UfCollections
   val tokens = new Object with Tokens with UfCollections
   val users = new Object with Host with Sessions with UfCollections
}

trait UfServicesImpl extends Services {
   val userService: UserService = new Object with UserService with UfCollections 
   val eventService: EventService = new Object with EventService with UfCollections 
}
