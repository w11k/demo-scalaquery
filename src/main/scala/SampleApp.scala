package com.weiglewilczek.demoscalaquery

import com.weiglewilczek.slf4s.Logging

import org.scalaquery.ql._
import org.scalaquery.ql.extended.{ ExtendedTable => Table }
//import org.scalaquery.ql.extended.PostgresDriver.Implicit._
import org.scalaquery.ql.extended.H2Driver.Implicit._
import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession

object SampleApp extends Application with Logging {

  //val db = Database.forURL("jdbc:postgresql://localhost:5432/sq_demo", "postgres", "postgres", driver = "org.postgresql.Driver")
  /* Using H2 only for testing at the moment */
  val db = Database.forURL("jdbc:h2:mem:sq_demo", driver = "org.h2.Driver")
  
  db withSession {
    /* Creating tables */
    val DDL = Person.ddl ++ Address.ddl
    DDL.create
    DDL.createStatements foreach { logger info _ }

    logger info "Filling tables..."
    Person.first ~ Person.last insertAll (
      ("John", "Doe"),
      ("John", "Doe"),
      ("Bobby", "Tables"),
      ("Max", "Mustermann"),
      ("Hans", "Wurst"),
      ("Marcel", "Meier"),
      ("Scala", "Johanson")
    )

    Address.personId ~ Address.street ~ Address.zipCode ~ Address.location insertAll (
      (1, "Musterstr. 22", "12345", "Musterhausen"),
      (3, "Musterstr. 21", "12345", "Musterhausen"),
      (2, "Maxstr. 11", "12345", "Musterhausen"),
      (4, "Meyerweg 14", "08765", "Musterdorf"),
      (6, "Schlossplatz 2", "54321", "Vorstadt")
    )

    /* A few basic queries */

    /* SELECT * FROM person; */
    val query1 = for { person <- Person } yield person
    logger info "query1 : %s".format(query1.selectStatement)
    query1 foreach { logger info "%s".format(_) }

    /* SELECT first FROM person; */
    val query2 = for { person <- Person } yield person.first
    logger info "query2 : %s".format(query2.selectStatement)
    query2 foreach { logger info "%s".format(_) }

    /* SELECT last FROM person order By last DESC; */
    val query3 = for {
      person <- Person; // TODO IDEA needs semicolon!
      _ <- Query orderBy person.last.desc
    } yield person.last
    logger info "query3 : %s".format(query3.selectStatement)
    query3 foreach { logger info "%s".format(_) }

    /* SELECT first FROM person group by first; */
    val query4 = for {
      person <- Person; // TODO IDEA needs semicolon!
      _ <- Query groupBy person.first 
    } yield person.first
    logger info "query4 : %s".format(query4.selectStatement)
    query4 foreach { logger info "%s".format(_) }

    /* SELECT person.first, person.last, address.zipCode FROM person, address WHERE person.id = address.personId; */
    val query5 = for {
      person <- Person
      address <- Address if person.id is address.personId
    } yield person.first ~ person.last ~ address.zipCode
    logger info "query5 : %s".format(query5.selectStatement)
    query5 foreach { logger info "%s".format(_) }

    /* SELECT first, location FROM person, address
     * WHERE address.location = "Musterhausen" and person.id = address.personId */
    val query6 = for {
      person <- Person
      address <- Address if (address.location is "Musterhausen") && (person.id is address.personId)
    } yield person.first ~ address.location
    logger info "query6 : %s".format(query6.selectStatement)
    query6 foreach { logger info "%s".format(_) }

    /* SELECT p.id, a.personId FROM person p, address a */
    val query7 = for {
      person <- Person
      address <- Address
    } yield person.id ~ address.personId
    logger info "query7 : %s".format(query7.selectStatement)
    query7 foreach { logger info "%s".format(_) }

    /* UPDATE person SET first="Robert" WHERE person.first = "Bobby" */
    val query8 = for {
      person <- Person if person.first is "Bobby"
    } yield person.first

    /* UPDATE person SET first = "Robert" WHERE person.first = "Bobby" */
    val query8update = query8.update("Robert")
    logger info "query8update : %s".format(query8.updateStatement)

    /* SELECT id FROM person WHERE person.first = "Scala" and person.last = "johanson"; */
    val query9 = for {
      person <- Person if (person.first is "Scala") && (person.last is "Johanson")
    } yield person.id
    logger info "query9 : %s".format(query9.selectStatement)

    /* SELECT personId FROM address WHERE personId = 7 */
    /* 7 comes from query9.first; since we only have one "Scala Johanson" we know its only one value */
    val query10 = for {
      address <- Address if address.personId is query9.first // TODO improvement needed
    } yield address.location
    logger info "query10 : %s".format(query10.selectStatement)

    /* UPDATE address SET location = "Muhlhausen" WHERE address.personId = 7 */
    val query10update = query10.update("Muhlhausen")
    logger info "query10update : %s".format(query10.updateStatement)

    /* SELECT person FROM person, address WHERE address.zipCode = "12345 and person.id = address.personId */
    val query11person = for {
      person <- Person
      address <- Address if(address.zipCode is "12345") && (person.id is address.personId)
    } yield person
    logger info "query11person : %s".format(query11person.selectStatement)

    /* SELECT address FROM person, address WHERE address.zipCode = "12345" and person.id = address.personId  */
    val query11address = for {
      person <- Person
      address <- Address if(address.zipCode is "12345") && (person.id is address.personId)
    } yield address
    logger info "query11address : %s".format(query11address.selectStatement)

    /* ResultSetMutator allows you to delete, insert and update the current row */
    /* Here we will delete the selected rows from the address and then from person */
    logger info "Before delete"
    query5 foreach { logger info "%s".format(_) }
    query11address.mutate { m => m.delete }
    query11person.mutate { m => m.delete }
    logger info "After delete"
    query5 foreach { logger info "%s".format(_) }
  }
}

object Person extends Table[(Int, String, String)]("person") {
  def id = column[Int]("id", O AutoInc, O PrimaryKey)
  def first = column[String]("first")
  def last = column[String]("last")
  def * = id ~ first ~ last
}

object Address extends Table[(Int, Int, String, String, String)]("address") {
  def id = column[Int]("id", O AutoInc, O PrimaryKey)
  def street = column[String]("street")
  def zipCode = column[String]("zipCode")
  def location = column[String]("location")
  def personId = column[Int]("personId", O NotNull)
  def personIdFk = foreignKey("personIdFk", personId, Person)(_.id)
  def * = id ~ personId ~ street ~ zipCode ~ location
}
