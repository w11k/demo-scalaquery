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
    DDL.createStatements.foreach(logger info(_))

    logger info ("Filling tables...")
    Person.first ~
    Person.last insertAll (
      ( "John", "Doe" ),
      ( "John", "Doe" ),
      ( "Bobby", "Tables" ),
      ( "Max", "Mustermann" ),
      ( "Hans", "Wurst" ),
      ( "Marcel", "Meier" ),
      ( "Scala", "Johanson")
    )

    Address.personId ~
    Address.street ~
    Address.zipCode ~
    Address.location insertAll (
      (1, "Musterstr. 22", "12345", "Musterhausen"),
      (3, "Musterstr. 21", "12345", "Musterhausen"),
      (2, "Maxstr. 11", "12345", "Musterhausen"),
      (4, "Meyerweg 14", "08765", "Musterdorf"),
      (6, "Schlossplatz 2", "54321", "Vorstadt")
    )

    /* A few basic queries */

    /* SELECT * FROM person; */
    val query1 = for { person <- Person } yield person

    logger info ("Executing: " + query1.selectStatement)
    //query1 foreach { println }

    /* SELECT first FROM person; */
    val query2 = for { person <- Person } yield person.first

    logger info ("Executing: " + query2.selectStatement)
    //query2 foreach { println }

    /* SELECT last FROM person order By last DESC; */
    val query3 = for {
      person <- Person;
      _ <- Query orderBy person.last.desc
    } yield person.last

    logger info ("Executing: " + query3.selectStatement)
    //query3 foreach { println }

    /* SELECT first FROM person group by first; */
    val query4 = for {
      person <- Person;
      _ <- Query groupBy person.first 
    } yield person.first

    logger info ("Executing: " + query4.selectStatement)
    //query4 foreach { println }

    /* SELECT first, last FROM person, address WHERE person.id = address.personId; */
    val query5 = for {
      person <- Person
      address <- Address if person.id is address.personId
    } yield person.first ~ person.last

    logger info ("Executing: " + query5.selectStatement)
    //query5 foreach { println }


    /* SELECT first, location FROM person, address
     * WHERE address.location = "Musterhausen" and person.id = address.personId */
    val query6 = for {
      person <- Person
      address <- Address if (address.location is "Musterhausen") && (person.id is address.personId)
    } yield person.first ~ address.location

    logger info ("Executing: " + query6.selectStatement)
    //query6 foreach { println }

    /* SELECT p.id, a.personId FROM person p, address a */
    val query7 = for {
      person <- Person
      address <- Address
    } yield person.id ~ address.personId

    logger info ("Executing: " + query7.selectStatement)
    //query7 foreach { println }
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
  def personId_fk = foreignKey("personId_fk", personId, Person)(_.id)
  def * = id ~ personId ~ street ~ zipCode ~ location
}