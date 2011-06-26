package info.whiter4bbit.events.unfiltered.example 

import unfiltered.request._
import unfiltered.response._

object EventsAPI extends App {
  val hello = unfiltered.filter.Planify {
    case POST(Path("/api/user/new")) => {
        ResponseString("Hello, world")
    }
  } 
  unfiltered.jetty.Http.local(8080).filter(hello).run
}
