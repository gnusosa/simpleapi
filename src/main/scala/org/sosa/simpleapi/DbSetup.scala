package org.sosa

import slick.driver.H2Driver.api._

object DbSetup {
val migration = DBIO.seq(
  // Create the schema
  // tables using the query interfaces
  Todos.table.schema.create,

  /* Create / Insert */

  // Insert some Todos
  Todos.table += Todo(Some(1), "Do the Laundry", "Before doing the laundry, get quarters.", false),
  Todos.table += Todo(Some(2), "Buy More Coffee from Ritual", "The Colombian one is running out.", false),
  Todos.table += Todo(Some(3), "Buy New Pokemon", "Let's see if the new Pokemon holds up", false),
  Todos.table += Todo(Some(4), "Finish Old Pokemon", "Review the old Pokemon.", true),
  Todos.table += Todo(Some(5), "Buy Kombucha", "You need that healthy drink.", true))
}

