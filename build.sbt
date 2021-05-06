name := "scanmo"

version := "0.1"

scalaVersion := "2.12.13"

/*https://stackoverflow.com/questions/57433717/which-jar-to-use-for-dynamodb-with-java*/
val AkkaVersion = "2.5.31"
val AkkaHttpVersion = "10.1.11"
libraryDependencies ++= Seq(
  "com.lightbend.akka" %% "akka-stream-alpakka-dynamodb" % "2.0.2",
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.11.1009",
  "org.json4s" %% "json4s-native" % "3.6.11",
  "com.typesafe.play" %% "play-json" % "2.9.2"
)

/*libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.25"
libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-dynamodb" % "1.1.2"
libraryDependencies += "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.11.656"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.25"*/
