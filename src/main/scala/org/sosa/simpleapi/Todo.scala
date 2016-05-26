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
  val todos = TableQuery[Todos]

  def all() = todos.result
  def find(idx: Int)  = todos.filter(_.id === idx).result
  def findAllDone() = todos.filter(_.done === true).result
  def findAllOpen() = todos.filter(_.done === false).result
}
