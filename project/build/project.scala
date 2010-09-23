/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
import sbt._

class DemoScalaQueryProject(info: ProjectInfo) extends DefaultProject(info) {

  // ===================================================================================================================
  // Dependencies
  // ===================================================================================================================

  // Module configurations

  // Compile
  val scalaQuery = "org.scalaquery" %% "scalaquery" % "0.9.0" withSources
  val slf4s = "com.weiglewilczek.slf4s" %% "slf4s" % "1.0.1" withSources

  val h2 = "com.h2database" % "h2" % "1.2.140"
  val postgresql = "postgresql" % "postgresql" % "8.4-701.jdbc4"
  
  // Provided
  val slf4jJdk14 = "org.slf4j" % "slf4j-jdk14" % "1.6.1" % "provided" intransitive

  // Test
  val mockito = "org.mockito" % "mockito-all" % "1.8.4" % "test" withSources
  val specs = "org.scala-tools.testing" %% "specs" % "1.6.5" % "test" withSources
}
