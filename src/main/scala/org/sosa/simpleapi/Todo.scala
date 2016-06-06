package org.sosa

import argonaut.Argonaut._
import argonaut.CodecJson
import org.http4s.argonaut._
import org.http4s.EntityEncoder

import slick.driver.H2Driver.api._

case class Todo(id: Option[Int] = None, title: String, description: String, done: Boolean = false)

class Todos(tag: Tag) extends Table[Todo](tag, "todos") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def title = column[String]("title")
  def description = column[String]("description")
  def done = column[Boolean]("done")
  def * = (id.?, title, description, done) <> (Todo.tupled, Todo.unapply)
}

object Todos {
  val table = TableQuery[Todos]

  def all() = table.result
  def find(idx: Int)  = table.filter(_.id === idx).result.headOption
  def findAllDone() = table.filter(_.done === true).result
  def findAllOpen() = table.filter(_.done === false).result

  def setStatus(idx: Int, status: Boolean) = { val q = for { t <- table if t.id === idx } yield t.done
    q.update(status)
  }

  def setDone(idx: Int) = setStatus(idx, true)
  def setOpen(idx: Int) = setStatus(idx, false)

  def insert(todoF: TodoForm) = table += Todo(None, todoF.title, todoF.description)
  def delete(idx: Int) = table.filter(_.id === idx).delete
}

