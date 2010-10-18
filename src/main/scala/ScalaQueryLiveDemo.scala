package com.weiglewilczek.demoscalaquery

import com.weiglewilczek.slf4s.Logging

import org.scalaquery.ql._
import org.scalaquery.ql.extended.PostgresDriver.Implicit._
// import org.scalaquery.ql.extended.H2Driver.Implicit._
import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession

object ScalaQueryLiveDemo extends Logging {

  val db = Database.forURL("jdbc:postgresql://localhost:5432/sq_demo", "postgres", "postgres", driver = "org.postgresql.Driver")
  def DDL = Student.ddl ++ Professor.ddl ++ Course.ddl ++ StudentAttendsCourse.ddl

  def initDB() {
    /* Database INIT Script for LiveDemo */
    

    db withSession {
      DDL.createStatements foreach { println }
      DDL.create

      /* Filling tables with data. */
      val insertStudent = Student insertAll (
        (2801, "Marcel", "Schmidt"),
        (2802, "Dirk","Meyer"),
        (2803, "Stefan", "Meier")
      )
      insertStudent foreach { x => x.toString }

      Professor.title ~ Professor.surname insertAll (
        ("Dr.", "Wirth"),
        ("Dr.", "Nadobny"),
        ("Prof.", "Tesla"),
        ("Prof.", "Beyer"),
        ("Dr.", "House")
      )
      logger info "%s".format(Professor.insertStatement)

      Course.professorId ~ Course.name insertAll (
        (1, "WV"),
        (2, "DBS"),
        (2, "SWEProg"),
        (3, "MMuKS")
      )
      logger info "%s".format(Course.insertStatement)

      StudentAttendsCourse.studentMatrnr ~ StudentAttendsCourse.courseId insertAll (
        (2801, 1),
        (2801, 2),
        (2803, 2),
        (2802, 2),
        (2803, 3)
      )
      logger info "%s".format(StudentAttendsCourse.insertStatement)
    }
  logger info "%s".format("DB Initalized!")
  }

  def cleanDB() {
    db withSession {
      DDL.drop
    }
  logger info "%s".format("DB Clean!")
  }
}
