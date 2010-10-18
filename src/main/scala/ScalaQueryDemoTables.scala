package com.weiglewilczek.demoscalaquery

import org.scalaquery.ql.extended.ExtendedTable

object Student extends ExtendedTable[(Int, String, String)]("student") {
  def matrnr = column[Int]("matrnr", O PrimaryKey, O NotNull)
  def name = column[String]("name", O DBType "varchar(20)")
  def surname = column[String]("surname")
  def * = matrnr ~ name ~ surname
}

object Professor extends ExtendedTable[(Int, String, String)]("professor") {
  def id = column[Int]("id", O AutoInc, O PrimaryKey)
  def title = column[String]("title")
  def surname = column[String]("surname")
  def * = id ~ title ~ surname
}

object Course extends ExtendedTable[(Int, Int, String)]("course") {
  def id = column[Int]("id", O AutoInc, O PrimaryKey)
  def name = column[String]("name")
  def professorId = column[Int]("professorId", O NotNull)
  def professorIdFk = foreignKey("professorIdFk", professorId, Professor)(_.id)
  def * = id ~ professorId ~ name 
}

object StudentAttendsCourse extends ExtendedTable[(Int, Int, Int)]("studentattendscourse") {
  def id = column[Int]("id", O PrimaryKey, O AutoInc)
  def studentMatrnr = column[Int]("studentMatrnr")
  def courseId = column[Int]("courseId")
  def studentMatrnrFk = foreignKey("studentMatrnrFk", studentMatrnr, Student)(_.matrnr)
  def courseIdFk = foreignKey("courseIdFk", courseId, Course)(_.id)
  def * = id ~ studentMatrnr ~ courseId
  def constraint = index("constraint", studentMatrnr ~ courseId, unique = false) 
}
