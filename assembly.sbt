// Settings for sbt assembly

test in assembly := {}

mainClass in assembly := Some("com.micronautics.Main")

assemblyMergeStrategy in assembly := {
 case PathList("META-INF", _ @ _*) => MergeStrategy.discard
 case _ => MergeStrategy.first
}
