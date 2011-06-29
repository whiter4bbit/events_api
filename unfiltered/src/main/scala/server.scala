package info.whiter4bbit.events.unf.example 

import unfiltered.oauth._
import info.whiter4bbit.events._
import javax.servlet.Filter

object Jetty extends App {   
   val publicAPI = new Object with PublicAPI with UfServicesImpl
   val protectedAPI = new Object with ProtectedAPI with UfServicesImpl
   val app = new Object with EventsApp with UfServicesImpl with Sessions with UfCollections

   unfiltered.jetty.Http.local(8080)
           .context("/public") { _.filter(publicAPI) }
           .context("/oauth") { _.filter(OAuth(UfStores)) }
	   .filter(app)
	   .context("/protected") { 
	      _.filter(Protection(UfStores))
	      .filter(protectedAPI)
           }.run	   
}

