package io.github.mostafamohajeri

import bb.expstyla.exp.GenericTerm
import infrastructure.ExecutionContext
import io.github.mostafamohajeri.normativeservices.EflintBeliefBase

object NormUtils {

  def perform(term: GenericTerm)(implicit e: ExecutionContext) = {
    e.beliefBase.query(term)
  }

  def amend(term: GenericTerm)(implicit e: ExecutionContext) = {
    e.beliefBase.asInstanceOf[EflintBeliefBase].amend(term)
  }
}
