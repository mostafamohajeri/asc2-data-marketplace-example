package io.github.mostafamohajeri
package normativeservices

import bb.expstyla.exp.GenericTerm
import infrastructure.{AkkaMessageSource, ExecutionContext}

object Environment {

  var environmentActor : AkkaMessageSource = null
  var comsLogger : PlantUMLCommunicationLogger = null

  def print_to_file(caseId: String): Unit = {
      comsLogger.writeToFile(caseId,"","")
  }


  def logEvent(content:GenericTerm)(implicit executionContext: ExecutionContext): Unit = {
    comsLogger.logEvent(executionContext.name,content.getStringValue)
  }

  def logActors(actorNames:Seq[String]): Unit = {
    comsLogger.logActor(actorNames)
  }




}
