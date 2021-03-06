import sbt._
import Keys._
import com.github.siasia.WebPlugin._

object Dependencies { 
   val scalatraVersion = "2.0.0-SNAPSHOT"

   val scalatra = "org.scalatra" %% "scalatra" % scalatraVersion   
   val scalatraSpecs = "org.scalatra" %% "scalatra-specs" % scalatraVersion
   val scalaz = "org.scalaz" %% "scalaz-core" % "6.0.1"
   val casbah = "com.mongodb.casbah" % "casbah_2.9.0-1" % "2.1.5.0"      
   val scalatest = "org.scalatra" %% "scalatra-scalatest" % scalatraVersion % "test"
   val servletApi = "org.mortbay.jetty" % "servlet-api" % "2.5-20081211" % "provided"
   val slf4jBinding = "ch.qos.logback" % "logback-classic" % "0.9.25" % "runtime"
   val jetty = "org.mortbay.jetty" % "jetty" % "6.1.22" % "jetty"
   val specs = "org.specs" % "specs" % "1.4.3" % "test"
   val liftJsonScalaz = "net.liftweb" %% "lift-json-scalaz" % "2.4-SNAPSHOT"
   val liftJson = "net.liftweb" %% "lift-json" % "2.4-SNAPSHOT"
   val liftJsonExt = "net.liftweb" %% "lift-json-ext" % "2.4-SNAPSHOT"
   val paranamer = "com.thoughtworks.paranamer" % "paranamer" % "2.3"

   val unfilteredFilter = "net.databinder" %% "unfiltered-filter" % "0.3.4"  
   val unfilteredJetty = "net.databinder" %% "unfiltered-jetty" % "0.3.4"
   val unfilteredJson = "net.databinder" %% "unfiltered-json" % "0.3.4"
   val unfilteredNetty = "net.databinder" %% "unfiltered-netty" % "0.3.4"
   val unfilteredOAuth = "net.databinder" %% "unfiltered-oauth" % "0.3.4"
   val unfilteredSpec = "net.databinder" %% "unfiltered-spec" % "0.3.4" //% "test"
   val dispatchOauth = "net.databinder" %% "dispatch-oauth" % "0.7.8"

   val commonsLang = "commons-lang" % "commons-lang" % "2.3"
   val oauthCore = "info.whiter4bbit" %% "oauth-core" % "1.0"
   val oauthMongoDB = "info.whiter4bbit" %% "oauth-mongodb" % "1.0"
   val oauthScalatra = "info.whiter4bbit" %% "oauth-scalatra" % "1.0"
   val chttp = "info.whiter4bbit" %% "chttp" % "1.0"
}

object Resolvers {
   lazy val snapshots = "Scala Tools Snapshots" at "http://scala-tools.org/repo-snapshots/"
   lazy val sonatype = "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
   lazy val fuse = "FuseSource Snapshot Repository" at "http://repo.fusesource.com/nexus/content/repositories/snapshots"   
   lazy val home = Resolver.file("home repo", file("/home/whiter4bbit/documents/whiter4bbit.github.com/maven/")) 

   val allResolvers = Seq(home, snapshots, sonatype, fuse)   
}

object EventsAPI extends Build {
   import Dependencies._
   import Resolvers._  

   val buildOrganization = "info.whiter4bbit"   
   val buildVersion = "1.0"
   val buildScalaVersion = "2.9.0-1"
  
   val buildSettings = Defaults.defaultSettings ++ Seq (
     organization := buildOrganization,
     version      := buildVersion,
     scalaVersion := buildScalaVersion
   )
   
   val scalatraSettings = Seq(
     resolvers := allResolvers,
     libraryDependencies := Seq(scalatra, scalatraSpecs, servletApi, scalatest, slf4jBinding, liftJsonScalaz,
       liftJson, specs, paranamer, oauthScalatra, chttp, jetty, liftJsonExt, casbah, scalaz, oauthCore, oauthMongoDB)
   )

   val unfilteredSettings = Seq (
     resolvers := allResolvers,
     libraryDependencies := Seq(paranamer, unfilteredNetty, unfilteredFilter, unfilteredJetty, unfilteredOAuth, unfilteredJson, liftJsonScalaz, liftJson, liftJsonExt, unfilteredSpec, slf4jBinding, dispatchOauth)     
   )

   val coreSettings = Seq (
     resolvers := allResolvers,
     libraryDependencies := Seq(casbah, liftJson, liftJsonScalaz, liftJsonExt, scalaz, oauthCore, oauthMongoDB, commonsLang)
   )

   lazy val scalatraImpl: Project = Project(   
      "events-scalatra", 
      file("scalatra"), 
      settings = buildSettings ++ webSettings ++ scalatraSettings ++ Seq(
        jettyPort := 8081
      )
   ) dependsOn (eventsAPI)

   lazy val unfilteredImpl = Project(
      "events-unfiltered",
      file("unfiltered"),
      settings = buildSettings ++ unfilteredSettings
   ) dependsOn (eventsAPI)

   lazy val eventsAPI: Project = Project(
      "events-api",
      file("api"),
      settings = buildSettings ++ coreSettings
   ) 
}

