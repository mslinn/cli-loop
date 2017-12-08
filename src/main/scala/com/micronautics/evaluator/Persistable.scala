package com.micronautics.evaluator

import java.io.{File, FileInputStream, FileOutputStream}
import java.nio.file.Path
import com.esotericsoftware.kryo.io.{Input, Output}
import com.twitter.chill.{KryoBase, ScalaKryoInstantiator}
import scala.language.reflectiveCalls
import scala.reflect.ClassTag
import scala.reflect._

/** Pass in a structure, like a map, to be serialized.
  * Objects to be persisted do not need to extend Serializable.
  * Data is stored and retrieved from `~/.cli-loop/variable.data`
  * @see See [[http://javasplitter.blogspot.com/2014/04/two-scala-serialization-examples.html the second example]]
  * @see See [[https://github.com/EsotericSoftware/kryo#quickstart]] */
class Persistable(path: Path) {
  type Closeable = { def close(): Unit }

  def using[A <: Closeable, B](closeable: => A)(f: A => B): B = {
    val closeableRef = closeable // only reference closeable once
    try {
      f(closeableRef)
    } finally {
      try {
        closeableRef.close()
      } catch { case _: Throwable => () }
    }
  }

  val instantiator = new ScalaKryoInstantiator
  instantiator.setRegistrationRequired(false)

  val kryo: KryoBase = instantiator.newKryo

  val file: File = path.resolve("variable.data").toFile
  path.toFile.mkdirs()

  def write(anyRef: AnyRef): Unit =
    using (new Output(new FileOutputStream(file))) { kryo.writeObject(_, anyRef) }

  // See https://stackoverflow.com/a/6200301/553865
  def read[T: ClassTag]: T = {
    if (file.exists) {
      val klass = classTag[T].runtimeClass.asInstanceOf[Class[T]]
      using(new Input(new FileInputStream(file))) { x => kryo.readObject(x, klass) }
    }
    else throw new Exception(s"${ file.getCanonicalPath } does not exist; cannot read data from it")
  }
}
