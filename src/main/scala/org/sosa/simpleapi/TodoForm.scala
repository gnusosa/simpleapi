package org.sosa

import argonaut.Argonaut._
import argonaut.CodecJson
import org.http4s.argonaut._
import org.http4s.EntityEncoder

case class TodoForm(title: String, description: String)

object TodoForm {
  implicit def TodoFormCodecJson: CodecJson[TodoForm] = casecodec2(TodoForm.apply, TodoForm.unapply)("title", "description")
}
