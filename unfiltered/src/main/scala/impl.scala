package info.whiter4bbit.events.unf.example

import com.mongodb.casbah.Imports._

trait UfCollections extends UfOAuthCollections {
   val users = MongoConnection()("oauth_unfiltered")("users")
   val sessions = MongoConnection()("oauth_unfiltered")("sessions")
   val requests = MongoConnection()("oauth_unfiltered")("requests")
   val nonces = MongoConnection()("oauth_unfiltered")("nonces")
}

case object UfStores extends Stores {
   val nonces = new Object with Nonces with UfCollections
   val consumers = new Object with Consumers with UfCollections
   val tokens = new Object with Tokens with UfCollections
   val users = new Object with Host with Sessions with UfCollections
}
