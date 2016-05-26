package org.sosa

import org.http4s._
import org.http4s.server._
import org.http4s.dsl._
import org.http4s.Response

import _root_.argonaut._, Argonaut._
import org.http4s.argonaut._

import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Failure}


object DbSetup {
  val migration = DBIO.seq(
    // Create the schema
    // tables using the query interfaces
    Todos.todos.schema.create,

    /* Create / Insert */

    // Insert some Todos
    Todos.todos += Todo(Some(1), "Do the Laundry", "Before doing the laundry, get quarters.", false),
    Todos.todos += Todo(Some(2), "Buy More Coffee from Ritual", "The Colombian one is running out.", false),
    Todos.todos += Todo(Some(3), "Buy New Pokemon", "Let's see if the new Pokemon holds up", false),
    Todos.todos += Todo(Some(4), "Finish Old Pokemon", "Review the old Pokemon.", true),
      Todos.todos += Todo(Some(5), "Buy Kombucha", "You need that healthy drink.", true))
}


object TodoService {
  implicit def TodoCodecJson: CodecJson[Todo] = casecodec4(Todo.apply, Todo.unapply)("id", "title", "description", "done")
  implicit def TodoEntityEncoder: EntityEncoder[Todo] = jsonEncoderOf[Todo]
  implicit def TodosEntityEncoder: EntityEncoder[List[Todo]] = jsonEncoderOf[List[Todo]]

  val db = Database.forURL("jdbc:h2:mem:todos;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
  val setupFuture = db.run(DbSetup.migration)

  setupFuture onComplete {
    case Success(setup) => println(setup)
    case Failure(t) => println("An error has occured: " + t)
  }


  val httpService = HttpService {
    case GET -> Root / "hello" / name =>
      Ok(jSingleObject("message", jString(s"Hello, ${name}")))

    case GET -> Root / "todos" =>
      val q = db.run(Todos.all)
      val todosList = q.transform(_.toList, Throwable => Throwable)
      Ok(todosList)

    // case req @ POST -> Root / "todos" =>
    //   req.decode[UrlForm] { data =>
    //     val todoForm = for {
    //       title <- data.getFirst("title")
    //       description <- data.getFirst("description")
    //     } yield TodoForm(title, description)
    //   }

    case GET -> Root / "todos" / IntVar(id) =>
      val q = db.run(Todos.find(id))
      val todosList = q.transform(_.toList, Throwable => Throwable)
      Ok(todosList)

    case GET -> Root / "todos" / "done" =>
      val q = db.run(Todos.findAllDone)
      val todosList = q.transform(_.toList, Throwable => Throwable)
      Ok(todosList)

    case GET -> Root / "todos" / "open" =>
      val q = db.run(Todos.findAllOpen)
      val todosList = q.transform(_.toList, Throwable => Throwable)
      Ok(todosList)

  }
}
