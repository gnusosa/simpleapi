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
import scalaz.concurrent.Task
import scalaz.syntax.either._
import scalaz._

import scala.concurrent.ExecutionContext
import scala.util.{Success, Failure}
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object TodoService {
  implicit def TodoCodecJson: CodecJson[Todo] = casecodec4(Todo.apply, Todo.unapply)("id", "title", "description", "done")
  implicit def TodoEntityEncoder: EntityEncoder[Todo] = jsonEncoderOf[Todo]
  implicit def TodosEntityEncoder: EntityEncoder[List[Todo]] = jsonEncoderOf[List[Todo]]
  implicit def TodoFormCodecJson: CodecJson[TodoForm] = casecodec2(TodoForm.apply, TodoForm.unapply)("title", "description")
  implicit def TodoFormDecoderJson: DecodeJson[TodoForm] = jdecode2L(TodoForm.apply)("title", "description")
  implicit def TodoFormEntityEncoder: EntityEncoder[TodoForm] = jsonEncoderOf[TodoForm]
  implicit def TodoFormEntityDecoder: EntityDecoder[TodoForm] = jsonOf[TodoForm]


  val db = Database.forURL("jdbc:h2:mem:todos;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
  val setupFuture = db.run(DbSetup.migration)

  setupFuture onComplete {
    case Success(setup) => println(setup)
    case Failure(t) => println("An error has occured: " + t)
  }

  val httpService = HttpService {
    case GET -> Root / "todos" =>
      task(Todos.all) flatMap {
        case todos => Ok(todos.toList)
      }

    case req @ POST -> Root / "todos" =>
      req.as[TodoForm] flatMap {
        todo => task(Todos.insert(todo)) flatMap {
          case x => Created()
        }
      }

    case GET -> Root / "todos" / IntVar(id) =>
      task(Todos.find(id)) flatMap {
        case Some(todo) => Ok(todo)
        case _ => NotFound()
      }

    case req@DELETE -> Root / "todos" / IntVar(id) =>
      task(Todos.find(id)) flatMap {
        case Some(todo) => task(Todos.delete(todo.id.get)) flatMap {_ => NoContent()}
        case _ => NotFound()
      }

    case GET -> Root / "todos" / "done" =>
      task(Todos.findAllDone) flatMap {
        case todos => Ok(todos.toList)
      }

    case PUT -> Root / "todos" / "done" / IntVar(id)=>
      task(Todos.find(id)) flatMap {
        case Some(todo) => task(Todos.setDone(todo.id.get)) flatMap {_ => Ok()}
        case _ => NotFound()
      }

    case GET -> Root / "todos" / "open" =>
      task(Todos.findAllOpen) flatMap {
        case todos => Ok(todos.toList)
      }

    case PUT -> Root / "todos" / "open" / IntVar(id)=>
      task(Todos.find(id)) flatMap {
        case Some(todo) => task(Todos.setOpen(todo.id.get)) flatMap {_ => Ok()}
        case _ => NotFound()
      }

  }

  protected def task[R](action: DBIO[R]): Task[R] = {
    val poolSize = 40
    implicit val executionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(poolSize))
    Task.async { t =>
      db.run(action) onComplete {
        case Success(x) => t(\/-(x))
        case Failure(x) => t(-\/(x))
      }
    }
  }

}
