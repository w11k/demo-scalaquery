package com.weiglewilczek.demoscalaquery

import com.weiglewilczek.slf4s.Logging

import org.scalaquery.ql._
// import org.scalaquery.ql.extended.PostgresDriver.Implicit._
import org.scalaquery.ql.extended.H2Driver.Implicit._
import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession

object ScalaQueryDemoApp extends Application with Logging {

  // val db = Database.forURL("jdbc:postgresql://localhost:5432/sq_demo", "postgres", "postgres", driver = "org.postgresql.Driver")
  val db = Database.forURL("jdbc:h2:mem:sq_demo", driver = "org.h2.Driver")
  def DDL = Student.ddl ++ Professor.ddl ++ Course.ddl ++ StudentAttendsCourse.ddl

  db withSession {
    DDL.createStatements foreach { logger info _ }
    DDL.create
        

    /* Filling tables with data. */
    Student insertAll (
      (2801, "Marcel", "Schmidt"),
      (2802, "Dirk","Meyer"),
      (2803, "Stefan", "Meier")
    )

    logger info "%s".format(Student.insertStatement)

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

    /* Selecting everything from student. */
    val allStudents = for { student <- Student } yield student
    logger info "%s".format(allStudents.selectStatement)
    allStudents foreach { println }
    
    /* Printing out all student names. */
    logger info "%s".format(Student.map{_.name}.selectStatement)
    Student map { _.name } foreach { println }

    /* Putting all student names into a List[String]. */
    val studentNames = Student map { _.name }
    logger info "%s".format(studentNames.selectStatement)
    val studentNamesList = studentNames.list
    println(studentNamesList)

    /* Selecting matrnr from student. */
    val allMatrnr = for { student <- Student } yield student.matrnr
    logger info "%s".format(allMatrnr.selectStatement)
    allMatrnr foreach { println }

    /* Selecting one specifc student. */
    val oneStudent = for { student <- Student if student.matrnr is 2801 } yield student
    logger info "%s".format(oneStudent.selectStatement)
    oneStudent foreach { println }
    

    /* Same as above. */
    val oneStudent2 = Student where { _.matrnr is 2801 }
    logger info "%s".format(oneStudent2.selectStatement)
    oneStudent2 foreach { println }

    /* Listing all courses where professorId is 2, using the createFinderBy method. */
    val findCourse = Course.createFinderBy(_.professorId)
    logger info "Running: %s".format(findCourse.selectStatement)
    findCourse.list(2) foreach { println }

    /* Selecting surname from student ordered asc by surname. */
    val studentSurname = for {
      student <- Student; // TODO IDEA needs semicolon!
      _ <- Query orderBy student.surname.asc
    } yield student.surname
    logger info "%s".format(studentSurname.selectStatement)
    studentSurname foreach { println }

    /* Selecting professor title and surname and course name that is held by given professor. */
    val professorWithCourse = for {
      professor <- Professor
      course <- Course if professor.id is course.professorId
    } yield professor.title ~ professor.surname ~ course.name
    logger info "%s".format(professorWithCourse.selectStatement)
    professorWithCourse foreach { println }

    /* Selecting professors who aren't giving any courses. */
    val professorsWithNoCourse = Professor where { _.id notIn Course.map { _.professorId } }
    logger info "%s".format(professorsWithNoCourse.selectStatement)
    professorsWithNoCourse foreach { println }

    /* Selecting professors and students where students listen to the professors course. */
    val allStudentsProfessors = for {
      student <- Student
      professor <- Professor
      course <- Course
      sac <- StudentAttendsCourse if (sac.studentMatrnr is student.matrnr) &&
        (sac.courseId is course.id) &&
        (course.professorId is professor.id)
    } yield professor.surname ~ student.surname
    logger info "%s".format(allStudentsProfessors.selectStatement)
    allStudentsProfessors foreach { println }

    /* Joins on professors and courses. */
    /* Inner Join. */
    val professorWithCourseinnerJoin = for {
      Join(p, c) <- Professor innerJoin Course on (_.id is _.professorId)
    } yield p.title ~ p.surname ~ c.name
        logger info "%s".format(professorWithCourseinnerJoin.selectStatement)
    professorWithCourseinnerJoin foreach { println }

    /* Left outer join. */
    val professorWithCourseleftJoin = for {
      Join(p, c) <- Professor leftJoin Course on (_.id is _.professorId)
    } yield p.title.? ~ p.surname.? ~ c.name.?
    logger info "%s".format(professorWithCourseleftJoin.selectStatement)
    professorWithCourseleftJoin foreach { println }

    /* Right outer join. */
    val professorWithCourserightJoin = for {
      Join(p, c) <- Professor rightJoin Course on (_.id is _.professorId)
    } yield p.title.? ~ p.surname.? ~ c.name.?
    logger info "%s".format(professorWithCourserightJoin.selectStatement)
    professorWithCourserightJoin foreach { println }

    /* Full outer joins are not supported yet by H2 Database. */
    /*
    val professorWithStudentouterJoin = for {
      Join(p, c) <- Professor outerJoin Course on (_.id is _.professorId)
    } yield p.surname.? ~ c.name.?
    logger info "%s".format(professorWithStudentouterJoin.selectStatement)
    professorWithStudentouterJoin foreach { println }
    */

    /* Selecting surnames from students and professors */
    val studentSur = for { student <- Student } yield student.surname
    val professorSur = for { professor <- Professor } yield professor.surname

    /* Combining all surnames as a union sorted by surname. */
    val unionSurname = for {
      surname <- professorSur union studentSur; // TODO IDEA needs semicolon!
      _ <- Query orderBy surname
    } yield surname
    logger info "%s".format(unionSurname.selectStatement)
    unionSurname foreach { println }

    /* Updating student.surname where matrnr is 2801. */
    val updateSurname = for { student <- Student if student.matrnr is 2801 } yield student.surname
    updateSurname update ("Mueller")
    logger info "%s".format(updateSurname.updateStatement)
    studentSurname foreach { println }

    /* Updating professor title and surname at the same time. */
    val updateProfessor = for {
      professor <- Professor if professor.surname is "Wirth" 
    } yield professor.title ~ professor.surname
    updateProfessor update ("Prof.","Schmidt")
    logger info "%s".format(updateProfessor.updateStatement)
    professorWithCourse foreach { println }

    /* Deleting a student requires to first delete the entry where the student listens to a course. */
    /* Selecting the student which shall be deleted and selecting the course that the student attended. */
    val deleteStudent = for { student <- Student if student.matrnr is 2803 } yield student
    val deleteStudentCourse = for { course <- StudentAttendsCourse if course.studentMatrnr is 2803 } yield course

    /* Deleting the rows first which had a foreignKey from a student. */
    deleteStudentCourse mutate { m => m.delete }
    logger info "%s".format(deleteStudentCourse.deleteStatement)

    /* Deleting the student */
    deleteStudent mutate { m => m.delete }
    logger info "%s".format(deleteStudent.deleteStatement)
    
    allStudents foreach { println }

    /* Updating a row only if student with matrnr 2801 exists. */
    val updateStudent = for { student <- Student if student.matrnr is 2801 } yield student
    updateStudent mutate { m => m.row_=((m.row._1, "Hans", "Braun")) }
    // TODO Acquire a update statement from example above?

    allStudents foreach { println }

    /* Using collection methods thanks to the Query Monad. */
    Student filter { _.matrnr is 2801 } foreach { println }

    DDL.drop
    
  }
}
